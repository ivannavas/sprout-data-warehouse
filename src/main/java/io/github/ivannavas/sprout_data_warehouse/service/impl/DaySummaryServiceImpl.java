package io.github.ivannavas.sprout_data_warehouse.service.impl;

import io.github.ivannavas.sprout_data_warehouse.entity.DaySummary;
import io.github.ivannavas.sprout_data_warehouse.repository.DaySummaryRepository;
import io.github.ivannavas.sprout_data_warehouse.service.DaySummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DaySummaryServiceImpl implements DaySummaryService {

    private final DaySummaryRepository daySummaryRepository;

    @Override
    public DaySummary saveDaySummary(DaySummary daySummary) {
        if (daySummary.getId() == null && daySummary.getDate() != null) {
            daySummaryRepository.findByDate(daySummary.getDate())
                    .ifPresent(existing -> daySummary.setId(existing.getId()));
        }

        return daySummaryRepository.save(daySummary);
    }

    @Override
    public List<DaySummary> getAllDaySummaries() {
        return daySummaryRepository.findAll();
    }

    @Override
    public DaySummary getDaySummaryByDate(LocalDate date) {
        return date == null ? null : daySummaryRepository.findByDate(date).orElse(null);
    }

    @Override
    public List<LocalDate> getRecordedDates() {
        return daySummaryRepository.findAllDates();
    }
}
