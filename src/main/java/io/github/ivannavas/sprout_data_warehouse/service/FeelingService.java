package io.github.ivannavas.sprout_data_warehouse.service;

import io.github.ivannavas.sprout_data_warehouse.entity.DatedFeeling;
import io.github.ivannavas.sprout_data_warehouse.entity.Feeling;

import java.time.LocalDate;
import java.util.List;

public interface FeelingService {
    Feeling saveFeeling(Feeling feeling);

    List<Feeling> getAllFeelings();

    DatedFeeling saveDatedFeeling(DatedFeeling datedFeeling);

    /**
     * The feeling records for one day. Scoped by date rather than fetched whole because this table
     * gains rows every day ingested: over a long backfill the full list grows without bound, while
     * what a day's extraction needs stays the handful of rows sharing its date.
     */
    List<DatedFeeling> getDatedFeelingsByDate(LocalDate date);
}
