package io.github.ivannavas.sprout_data_warehouse.repository;

import io.github.ivannavas.sprout_data_warehouse.entity.DatedFeeling;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatedFeelingRepository extends JpaRepository<DatedFeeling, Long> {
}
