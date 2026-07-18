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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@Slf4j
public class DailyTranscriptionIngestor {

    private final String dailyTranscriptionsPath;
    private final DataExtractorAgent dataExtractorAgent;
    private final Retriever retriever;

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
        File sourceDir = new File(dailyTranscriptionsPath);

        if (!sourceDir.exists()) {
            return;
        }

        File[] audioDiaryExports = sourceDir.listFiles();

        if (audioDiaryExports == null) return;

        for (File yearDir : audioDiaryExports) {
            if (!yearDir.isDirectory()) {
                continue;
            }

            File[] monthDirs = yearDir.listFiles();

            if (monthDirs == null) continue;

            for (File monthDir : monthDirs) {
                if (!monthDir.isDirectory()) {
                    continue;
                }

                File[] dayDirs = monthDir.listFiles();

                if (dayDirs == null) continue;

                for (File dayDir : dayDirs) {
                    if (!dayDir.isDirectory()) {
                        continue;
                    }

                    File[] files = dayDir.listFiles();

                    if (files == null) continue;

                    for (File file : files) {
                        ingest(file);
                    }
                }
            }
        }
    }

    private void ingest(File file) {
        if (file.isFile() && file.getName().startsWith("fullDay") && file.getName().endsWith(".md")) {
            LocalDate date = LocalDate.parse(
                    file.getName().replace(".md", "").replace("fullday-", ""),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
            );

            String transcription;
            try {
                transcription = Files.readString(file.toPath());
            } catch (IOException e) {
                log.error("Error reading transcription file: {}", file.getAbsolutePath(), e);
                return;
            }

            dataExtractorAgent.execute("daily_transcription_" + date, transcription);
            retriever.index(Document.of("daily_transcription_" + date, transcription, Map.of("date", date.toString())));
        }
    }
}
