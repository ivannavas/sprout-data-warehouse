package io.github.ivannavas.sprout_data_warehouse.repository;

import io.github.ivannavas.sprout_data_warehouse.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
