package io.github.ivannavas.sprout_data_warehouse.service;

import io.github.ivannavas.sprout_data_warehouse.entity.DaySummary;

import java.util.List;

public interface DaySummaryService {
    DaySummary saveDaySummary(DaySummary daySummary);

    List<DaySummary> getAllDaySummaries();
}
