package io.github.ivannavas.sprout_data_warehouse.repository;

import io.github.ivannavas.sprout_data_warehouse.entity.DatedActivity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatedActivityRepository extends JpaRepository<DatedActivity, Long> {
}
