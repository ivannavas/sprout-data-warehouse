package io.github.ivannavas.sprout_data_warehouse.service.impl;

import io.github.ivannavas.sprout_data_warehouse.entity.DatedFeeling;
import io.github.ivannavas.sprout_data_warehouse.entity.Feeling;
import io.github.ivannavas.sprout_data_warehouse.repository.DatedFeelingRepository;
import io.github.ivannavas.sprout_data_warehouse.repository.FeelingRepository;
import io.github.ivannavas.sprout_data_warehouse.service.FeelingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeelingServiceImpl implements FeelingService {

    private final FeelingRepository feelingRepository;
    private final DatedFeelingRepository datedFeelingRepository;

    @Override
    public Feeling saveFeeling(Feeling feeling) {
        return feelingRepository.save(feeling);
    }

    @Override
    public List<Feeling> getAllFeelings() {
        return feelingRepository.findAll();
    }

    @Override
    public DatedFeeling saveDatedFeeling(DatedFeeling datedFeeling) {
        return datedFeelingRepository.save(datedFeeling);
    }

    @Override
    public List<DatedFeeling> getDatedFeelingsByDate(LocalDate date) {
        return date == null ? List.of() : datedFeelingRepository.findByDate(date);
    }
}
