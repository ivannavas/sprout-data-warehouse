package io.github.ivannavas.sprout_data_warehouse.ingestor;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.Instant;

public record IngestionStatus(
        boolean running,
        boolean stopRequested,
        String currentDay,
        @JsonSerialize(using = ToStringSerializer.class) Instant startedAt,
        @JsonSerialize(using = ToStringSerializer.class) Instant finishedAt,
        int daysIngested,
        int daysDeleted,
        int daysFailed,
        Outcome lastOutcome) {

    public enum Outcome {
        COMPLETED,
        STOPPED,
        FAILED
    }
}
