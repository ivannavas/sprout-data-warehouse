package io.github.ivannavas.sprout_data_warehouse.service;

import io.github.ivannavas.sprout_data_warehouse.entity.DatedFeeling;
import io.github.ivannavas.sprout_data_warehouse.entity.Feeling;

import java.util.List;

public interface FeelingService {
    Feeling saveFeeling(Feeling feeling);

    List<Feeling> getAllFeelings();

    DatedFeeling saveDatedFeeling(DatedFeeling datedFeeling);

    List<DatedFeeling> getAllDatedFeelings();
}
