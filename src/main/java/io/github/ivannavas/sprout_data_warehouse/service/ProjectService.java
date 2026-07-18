package io.github.ivannavas.sprout_data_warehouse.service;

import io.github.ivannavas.sprout_data_warehouse.entity.Project;

import java.util.List;

public interface ProjectService {
    Project saveProject(Project project);

    List<Project> getAllProjects();
}
