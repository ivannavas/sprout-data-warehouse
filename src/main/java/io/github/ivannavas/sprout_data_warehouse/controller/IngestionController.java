package io.github.ivannavas.sprout_data_warehouse.controller;

import io.github.ivannavas.sprout_data_warehouse.ingestor.DailyTranscriptionIngestor;
import io.github.ivannavas.sprout_data_warehouse.ingestor.IngestionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingestion")
@RequiredArgsConstructor
public class IngestionController {

    private final DailyTranscriptionIngestor ingestor;

    @GetMapping("/status")
    public IngestionStatus status() {
        return ingestor.status();
    }

    @PostMapping("/stop")
    public ResponseEntity<IngestionStatus> stop() {
        boolean stopping = ingestor.requestStop();
        return ResponseEntity.status(stopping ? HttpStatus.ACCEPTED : HttpStatus.CONFLICT)
                .body(ingestor.status());
    }
}
