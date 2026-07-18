package io.github.ivannavas.sprout_data_warehouse.repository;

import io.github.ivannavas.sprout_data_warehouse.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}
