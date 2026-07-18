package io.github.ivannavas.sprout_data_warehouse.repository;

import io.github.ivannavas.sprout_data_warehouse.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
