package io.github.ivannavas.sprout_data_warehouse.repository;

import io.github.ivannavas.sprout_data_warehouse.entity.PersonEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonEventRepository extends JpaRepository<PersonEvent, Long> {
}
