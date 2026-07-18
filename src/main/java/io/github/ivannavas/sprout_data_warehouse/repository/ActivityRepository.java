package io.github.ivannavas.sprout_data_warehouse.repository;

import io.github.ivannavas.sprout_data_warehouse.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
}
