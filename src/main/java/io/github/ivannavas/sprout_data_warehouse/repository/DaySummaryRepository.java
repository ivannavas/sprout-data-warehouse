package io.github.ivannavas.sprout_data_warehouse.repository;

import io.github.ivannavas.sprout_data_warehouse.entity.DaySummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DaySummaryRepository extends JpaRepository<DaySummary, Long> {
    Optional<DaySummary> findByDate(LocalDate date);
}
