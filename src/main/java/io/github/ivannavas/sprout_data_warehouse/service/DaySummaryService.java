package io.github.ivannavas.sprout_data_warehouse.service;

import io.github.ivannavas.sprout_data_warehouse.entity.DaySummary;

import java.time.LocalDate;
import java.util.List;

public interface DaySummaryService {
    DaySummary saveDaySummary(DaySummary daySummary);

    List<DaySummary> getAllDaySummaries();

    /** @return the summary of {@code date}, or null when that day was never ingested. */
    DaySummary getDaySummaryByDate(LocalDate date);

    /** The dates that have a summary, oldest first: which days the diary actually covers. */
    List<LocalDate> getRecordedDates();
}
