package io.github.ivannavas.sprout_data_warehouse.service.impl;

import io.github.ivannavas.sprout_data_warehouse.entity.Project;
import io.github.ivannavas.sprout_data_warehouse.repository.ProjectRepository;
import io.github.ivannavas.sprout_data_warehouse.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    @Override
    public Project saveProject(Project project) {
        return projectRepository.save(project);
    }

    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }
}
