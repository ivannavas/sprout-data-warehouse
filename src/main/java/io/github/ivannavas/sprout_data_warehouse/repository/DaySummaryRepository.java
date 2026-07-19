package io.github.ivannavas.sprout_data_warehouse.repository;

import io.github.ivannavas.sprout_data_warehouse.entity.DaySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DaySummaryRepository extends JpaRepository<DaySummary, Long> {
    Optional<DaySummary> findByDate(LocalDate date);

    /** Projects away the summary text: the caller only wants to know which days exist. */
    @Query("select d.date from DaySummary d order by d.date")
    List<LocalDate> findAllDates();
}
