package com.SymphonyAI.tasktracker.controller;
import com.SymphonyAI.tasktracker.config.PageableFactory;
import com.SymphonyAI.tasktracker.dto.TaskRequest;
import com.SymphonyAI.tasktracker.dto.TaskResponse;
import com.SymphonyAI.tasktracker.dto.PageResponse;
import com.SymphonyAI.tasktracker.entity.TaskStatus;
import com.SymphonyAI.tasktracker.service.TaskService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final PageableFactory pageableFactory;

    public TaskController(TaskService taskService, PageableFactory pageableFactory) {
        this.taskService = taskService;
        this.pageableFactory = pageableFactory;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request));
    }

    @GetMapping
    public PageResponse<TaskResponse> getAll(
            @RequestParam(name = "status", required = false) TaskStatus status,
            @RequestParam(name = "projectId", required = false) Long projectId,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "direction", required = false) String direction) throws BadRequestException {
        return taskService.getAll(status, projectId,
                pageableFactory.create(page, size, sortBy, direction, SortFields.TASK, "id"));
    }

    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable("id") Long id) {
        return taskService.getById(id);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable("id") Long id, @Valid @RequestBody TaskRequest request) {
        return taskService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
