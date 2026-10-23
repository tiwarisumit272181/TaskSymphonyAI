package com.SymphonyAI.tasktracker.controller;

import com.SymphonyAI.tasktracker.config.PageableFactory;
import com.SymphonyAI.tasktracker.dto.PageResponse;
import com.SymphonyAI.tasktracker.dto.ProjectRequest;
import com.SymphonyAI.tasktracker.dto.ProjectResponse;
import com.SymphonyAI.tasktracker.dto.TaskResponse;
import com.SymphonyAI.tasktracker.service.ProjectService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final PageableFactory pageableFactory;

    public ProjectController(ProjectService projectService, PageableFactory pageableFactory) {
        this.projectService = projectService;
        this.pageableFactory = pageableFactory;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(request));
    }

    @GetMapping
    public PageResponse<ProjectResponse> getAll(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "direction", required = false) String direction) throws BadRequestException {
        return projectService.getAll(
                pageableFactory.create(page, size, sortBy, direction, SortFields.PROJECT, "id"));
    }

    @GetMapping("/{id}")
    public ProjectResponse getById(@PathVariable("id") Long id) {
        return projectService.getById(id);
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable("id") Long id, @Valid @RequestBody ProjectRequest request) {
        return projectService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tasks")
    public PageResponse<TaskResponse> getTasks(
            @PathVariable("id") Long id,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "direction", required = false) String direction) throws BadRequestException {
        return projectService.getTasksForProject(
                id, pageableFactory.create(page, size, sortBy, direction, SortFields.TASK, "id"));
    }
}
