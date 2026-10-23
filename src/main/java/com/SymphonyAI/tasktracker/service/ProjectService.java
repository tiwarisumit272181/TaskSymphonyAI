package com.SymphonyAI.tasktracker.service;

import com.SymphonyAI.tasktracker.dto.PageResponse;
import com.SymphonyAI.tasktracker.dto.ProjectRequest;
import com.SymphonyAI.tasktracker.dto.ProjectResponse;
import com.SymphonyAI.tasktracker.dto.TaskResponse;
import com.SymphonyAI.tasktracker.entity.Project;
import com.SymphonyAI.tasktracker.exception.ConflictException;
import com.SymphonyAI.tasktracker.exception.ResourceNotFoundException;
import com.SymphonyAI.tasktracker.repository.ProjectRepository;
import com.SymphonyAI.tasktracker.repository.TaskRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public ProjectService(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    public ProjectResponse create(ProjectRequest request) {
        if (projectRepository.existsByName(request.name())) {
            throw new ConflictException("Project with name '" + request.name() + "' already exists");
        }
        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> getAll(Pageable pageable) {
        return PageResponse.from(projectRepository.findAll(pageable).map(ProjectResponse::from));
    }

    @Transactional(readOnly = true)
    public ProjectResponse getById(Long id) {
        return ProjectResponse.from(findProject(id));
    }

    public ProjectResponse update(Long id, ProjectRequest request) {
        Project project = findProject(id);
        if (!project.getName().equals(request.name()) && projectRepository.existsByName(request.name())) {
            throw new ConflictException("Project with name '" + request.name() + "' already exists");
        }
        project.setName(request.name());
        project.setDescription(request.description());
        return ProjectResponse.from(projectRepository.save(project));
    }

    public void delete(Long id) {
        Project project = findProject(id);
        projectRepository.delete(project);
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getTasksForProject(Long id, Pageable pageable) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project not found: " + id);
        }
        return PageResponse.from(taskRepository.findByProjectId(id, pageable).map(TaskResponse::from));
    }

    private Project findProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }
}
