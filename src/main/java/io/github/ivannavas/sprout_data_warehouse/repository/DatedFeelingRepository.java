package io.github.ivannavas.sprout_data_warehouse.repository;

import io.github.ivannavas.sprout_data_warehouse.entity.DatedFeeling;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DatedFeelingRepository extends JpaRepository<DatedFeeling, Long> {

    List<DatedFeeling> findByDate(LocalDate date);
}
