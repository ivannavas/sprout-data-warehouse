package io.github.ivannavas.sprout_data_warehouse.ingestor;

import io.github.ivannavas.sprout.annotation.Component;
import io.github.ivannavas.sprout.annotation.Value;
import io.github.ivannavas.sprout.model.Document;
import io.github.ivannavas.sprout.anthropic.embedding.VoyageEmbeddingModel;
import io.github.ivannavas.sprout.pgvector.PgVectorStore;
import io.github.ivannavas.sprout.rag.Retriever;
import io.github.ivannavas.sprout_data_warehouse.agent.DataExtractorAgent;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


@Component
@Slf4j
public class DailyTranscriptionIngestor {

    static final String REMOVAL_MARKER = "marked-for-removal";

    private static final String TRANSCRIPTION_PREFIX = "fullday-";
    private static final String TRANSCRIPTION_SUFFIX = ".md";
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String dailyTranscriptionsPath;
    private final DataExtractorAgent dataExtractorAgent;
    private final Retriever retriever;
    private final ModelUsageTracker usageTracker;

    private final AtomicBoolean running = new AtomicBoolean();
    private volatile boolean stopRequested;
    private volatile String currentDay;
    private volatile Instant startedAt;
    private volatile Instant finishedAt;
    private volatile IngestionStatus.Outcome lastOutcome;

    private int daysIngested;
    private int daysDeleted;
    private int daysFailed;

    public DailyTranscriptionIngestor(@Value("${daily.transcriptions.path}") String dailyTranscriptionsPath,
                                      DataExtractorAgent dataExtractorAgent,
                                      VoyageEmbeddingModel voyageEmbeddingModel,
                                      PgVectorStore pgVectorStore,
                                      ModelUsageTracker usageTracker) {
        this.dailyTranscriptionsPath = dailyTranscriptionsPath;
        this.dataExtractorAgent = dataExtractorAgent;
        this.usageTracker = usageTracker;
        this.retriever = new Retriever(voyageEmbeddingModel, pgVectorStore, 100);
    }

    /**
     * Claims the run and hands it to a thread of its own: a full pass walks every pending day through the
     * extractor, which is far longer than a request should be held open. Returns false when a run is already
     * in progress, leaving that run untouched.
     */
    public boolean start() {
        if (!running.compareAndSet(false, true)) {
            log.info("Ingestion already in progress; ignoring start request");
            return false;
        }

        stopRequested = false;
        startedAt = Instant.now();
        finishedAt = null;
        lastOutcome = null;
        daysIngested = 0;
        daysDeleted = 0;
        daysFailed = 0;
        usageTracker.reset();

        Thread.ofPlatform().name("daily-transcription-ingestion").start(this::run);
        return true;
    }

    /** Runs the pass already claimed by {@link #start()}, and releases the claim when it ends. */
    private void run() {
        try {
            walk();
            lastOutcome = stopRequested ? IngestionStatus.Outcome.STOPPED : IngestionStatus.Outcome.COMPLETED;
        } catch (RuntimeException e) {
            lastOutcome = IngestionStatus.Outcome.FAILED;
            log.error("Ingestion aborted", e);
        } finally {
            currentDay = null;
            finishedAt = Instant.now();
            running.set(false);
            log.info("Ingestion {}: {} ingested, {} deleted, {} failed",
                    lastOutcome, daysIngested, daysDeleted, daysFailed);
            log.info("Model usage: {}", usageTracker.summary());
        }
    }

    /** Asks the run in progress to stop after the day it is on. Returns false if nothing was running. */
    public boolean requestStop() {
        if (!running.get()) {
            return false;
        }
        stopRequested = true;
        log.info("Stop requested; ingestion will finish the current day and halt");
        return true;
    }

    public IngestionStatus status() {
        return new IngestionStatus(running.get(), stopRequested, currentDay,
                startedAt, finishedAt, daysIngested, daysDeleted, daysFailed, lastOutcome);
    }

    private void walk() {
        File root = new File(dailyTranscriptionsPath);
        if (!root.exists()) {
            log.warn("Transcriptions path does not exist: {}", root.getAbsolutePath());
            return;
        }

        walk(root, root, new ArrayDeque<>());
    }

    /**
     * Descends until it reaches the folders that actually hold transcriptions, instead of counting levels: the
     * export wraps its year/month/day tree in a folder of its own (AudioDiary_2026-04-13), and how many wrappers
     * sit on top is not something the ingestion should have to know.
     *
     * <p>{@code ancestors} collects the folders walked through on the way down, innermost first, so a day that is
     * removed can take with it the folders it just emptied. The root is deliberately kept out: it is never pruned.
     */
    private void walk(File dir, File root, Deque<File> ancestors) {
        if (stopRequested) {
            return;
        }
        if (isDay(dir)) {
            currentDay = dir.getAbsolutePath();
            processDay(dir, ancestors.toArray(File[]::new));
            return;
        }

        boolean prunable = dir != root;
        if (prunable) {
            ancestors.push(dir);
        }
        for (File child : directories(dir)) {
            walk(child, root, ancestors);
            if (stopRequested) {
                break;
            }
        }
        if (prunable) {
            ancestors.pop();
        }
    }

    /** A folder is a day when it holds transcriptions, or the marker left by the pass that ingested them. */
    private static boolean isDay(File dir) {
        if (new File(dir, REMOVAL_MARKER).exists()) {
            return true;
        }
        for (File file : files(dir)) {
            if (isTranscription(file)) {
                return true;
            }
        }
        return false;
    }

    private void processDay(File dayDir, File... prunableAncestors) {
        if (new File(dayDir, REMOVAL_MARKER).exists()) {
            // Ingested on an earlier pass: this is the pass that removes it.
            if (deleteRecursively(dayDir)) {
                daysDeleted++;
                log.info("Deleted ingested day {}", dayDir.getName());
                pruneIfEmpty(prunableAncestors);
            } else {
                daysFailed++;
                log.error("Could not delete day marked for removal: {}", dayDir.getAbsolutePath());
            }
            return;
        }

        boolean ingestedSomething = false;
        for (File file : files(dayDir)) {
            if (isTranscription(file)) {
                ingestedSomething |= ingest(file);
            }
        }
        if (ingestedSomething) {
            markForRemoval(dayDir);
        }
    }

    /** @return true when the transcription made it into both the warehouse and the index. */
    private boolean ingest(File file) {
        LocalDate date;
        try {
            date = LocalDate.parse(stripAffixes(file.getName()), FILE_DATE);
        } catch (RuntimeException e) {
            daysFailed++;
            log.error("Cannot read a date out of transcription file name: {}", file.getName(), e);
            return false;
        }

        String transcription;
        try {
            transcription = Files.readString(file.toPath());
        } catch (IOException e) {
            daysFailed++;
            log.error("Error reading transcription file: {}", file.getAbsolutePath(), e);
            return false;
        }

        String documentId = "daily_transcription_" + date;
        try {
            dataExtractorAgent.execute(documentId, transcription);
            retriever.index(Document.of(documentId, transcription, Map.of("date", date.toString())));
        } catch (RuntimeException e) {
            daysFailed++;
            log.error("Failed ingesting transcription for {}", date, e);
            return false;
        }

        daysIngested++;
        return true;
    }

    private void markForRemoval(File dayDir) {
        try {
            Files.writeString(new File(dayDir, REMOVAL_MARKER).toPath(),
                    "Ingested at " + Instant.now() + System.lineSeparator());
        } catch (IOException e) {
            // Not fatal: without the marker the day is simply ingested again on the next pass.
            log.error("Could not mark {} for removal; it will be ingested again", dayDir.getAbsolutePath(), e);
        }
    }

    /**
     * Deletes each folder in the chain that the last removal may have emptied, innermost first, stopping
     * at the first one that still holds something. The chain is passed in rather than walked up through
     * {@code getParentFile()} so the pruning cannot reach the root, which must survive an empty tree.
     */
    private void pruneIfEmpty(File... chain) {
        for (File dir : chain) {
            if (!isEmpty(dir)) {
                return;
            }
            if (dir.delete()) {
                log.info("Removed empty folder {}", dir.getAbsolutePath());
            } else {
                log.warn("Could not remove empty folder {}", dir.getAbsolutePath());
                return;
            }
        }
    }

    private static boolean isTranscription(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        // Case-insensitive because the export has not been consistent about the "fullday" prefix.
        return file.isFile() && name.startsWith(TRANSCRIPTION_PREFIX) && name.endsWith(TRANSCRIPTION_SUFFIX);
    }

    private static String stripAffixes(String fileName) {
        return fileName.substring(TRANSCRIPTION_PREFIX.length(), fileName.length() - TRANSCRIPTION_SUFFIX.length());
    }

    private static boolean deleteRecursively(File dir) {
        for (File child : files(dir)) {
            boolean deleted = child.isDirectory() ? deleteRecursively(child) : child.delete();
            if (!deleted) {
                return false;
            }
        }
        return dir.delete();
    }

    private static boolean isEmpty(File dir) {
        return dir.isDirectory() && files(dir).length == 0;
    }

    private static File[] directories(File dir) {
        File[] children = dir.listFiles(File::isDirectory);
        return children == null ? new File[0] : children;
    }

    private static File[] files(File dir) {
        File[] children = dir.listFiles();
        return children == null ? new File[0] : children;
    }
}
