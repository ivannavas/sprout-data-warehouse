package io.github.ivannavas.sprout_data_warehouse.ingestor;

import io.github.ivannavas.sprout.annotation.Component;
import io.github.ivannavas.sprout.annotation.Value;
import io.github.ivannavas.sprout.model.Document;
import io.github.ivannavas.sprout.ollama.embedding.OllamaEmbeddingModel;
import io.github.ivannavas.sprout.pgvector.PgVectorStore;
import io.github.ivannavas.sprout.rag.Retriever;
import io.github.ivannavas.sprout_data_warehouse.agent.DataExtractorAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Walks the audio-diary export ({@code <root>/<year>/<month>/<day>/}) and feeds each day's transcription
 * to the agent.
 *
 * <p>Ingesting a day leaves a {@value #REMOVAL_MARKER} file behind in its folder. The day is not deleted
 * there and then: the <em>next</em> run finds the marker, skips the ingestion and deletes the folder,
 * pruning the folders above it as they are left empty. The two passes are deliberate — a day is only
 * destroyed after a later run has seen it safely ingested.
 *
 * <p>The root is never deleted: it is the fixed drop point the exports land in, so it has to survive an
 * empty tree. Everything below it belongs to an export and goes once nothing is left in it.
 */
@Component
@Slf4j
public class DailyTranscriptionIngestor {

    /** Written into a day's folder once it has been ingested; its presence means "delete me next pass". */
    static final String REMOVAL_MARKER = "marked-for-removal";

    private static final String TRANSCRIPTION_PREFIX = "fullday-";
    private static final String TRANSCRIPTION_SUFFIX = ".md";
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String dailyTranscriptionsPath;
    private final DataExtractorAgent dataExtractorAgent;
    private final Retriever retriever;

    // One run at a time: the cron, the startup event and a manual trigger all land here.
    private final AtomicBoolean running = new AtomicBoolean();
    private volatile boolean stopRequested;
    private volatile String currentDay;
    private volatile Instant startedAt;
    private volatile Instant finishedAt;
    private volatile IngestionStatus.Outcome lastOutcome;

    // Only ever touched by the thread holding the run, so plain fields are enough.
    private int daysIngested;
    private int daysDeleted;
    private int daysFailed;

    public DailyTranscriptionIngestor(@Value("${daily.transcriptions.path}") String dailyTranscriptionsPath,
                                      DataExtractorAgent dataExtractorAgent,
                                      OllamaEmbeddingModel ollamaEmbeddingModel,
                                      PgVectorStore pgVectorStore) {
        this.dailyTranscriptionsPath = dailyTranscriptionsPath;
        this.dataExtractorAgent = dataExtractorAgent;
        this.retriever = new Retriever(ollamaEmbeddingModel, pgVectorStore, 100);
    }

    @Scheduled(cron = "0 0 13 * * *")
    @EventListener(ApplicationReadyEvent.class)
    public void scheduledTask() {
        if (!running.compareAndSet(false, true)) {
            log.info("Ingestion already in progress; skipping this trigger");
            return;
        }

        stopRequested = false;
        startedAt = Instant.now();
        finishedAt = null;
        lastOutcome = null;
        daysIngested = 0;
        daysDeleted = 0;
        daysFailed = 0;

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

        for (File yearDir : directories(root)) {
            for (File monthDir : directories(yearDir)) {
                for (File dayDir : directories(monthDir)) {
                    if (stopRequested) {
                        return;
                    }
                    currentDay = dayDir.getAbsolutePath();
                    processDay(dayDir, monthDir, yearDir);
                }
            }
        }
    }

    private void processDay(File dayDir, File monthDir, File yearDir) {
        if (new File(dayDir, REMOVAL_MARKER).exists()) {
            // Ingested on an earlier pass: this is the pass that removes it.
            if (deleteRecursively(dayDir)) {
                daysDeleted++;
                log.info("Deleted ingested day {}", dayDir.getName());
                // The root is deliberately absent from the chain: it is never pruned.
                pruneIfEmpty(monthDir, yearDir);
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
