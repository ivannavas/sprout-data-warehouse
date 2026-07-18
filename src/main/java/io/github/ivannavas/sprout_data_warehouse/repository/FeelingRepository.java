package io.github.ivannavas.sprout_data_warehouse.repository;

import io.github.ivannavas.sprout_data_warehouse.entity.Feeling;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeelingRepository extends JpaRepository<Feeling, Long> {
}
