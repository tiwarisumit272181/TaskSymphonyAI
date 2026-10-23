package com.SymphonyAI.tasktracker.service;


import com.SymphonyAI.tasktracker.config.AppProperties;
import com.SymphonyAI.tasktracker.dto.TaskRequest;
import com.SymphonyAI.tasktracker.dto.TaskResponse;
import com.SymphonyAI.tasktracker.dto.PageResponse;
import com.SymphonyAI.tasktracker.entity.Project;
import com.SymphonyAI.tasktracker.entity.Task;
import com.SymphonyAI.tasktracker.entity.TaskStatus;
import com.SymphonyAI.tasktracker.entity.User;
import com.SymphonyAI.tasktracker.exception.ResourceNotFoundException;
import com.SymphonyAI.tasktracker.repository.ProjectRepository;
import com.SymphonyAI.tasktracker.repository.TaskRepository;
import com.SymphonyAI.tasktracker.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AppProperties appProperties;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository,
                       AppProperties appProperties) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.appProperties = appProperties;
    }

    public TaskResponse create(TaskRequest request) {
        Project project = findProject(request.projectId());

        Task task = new Task();
        task.setTitle(request.title());
        task.setStatus(request.status() != null ? request.status() : TaskStatus.TODO);
        task.setPriority(request.priority() != null ? request.priority() : appProperties.getDefaultPriority());
        task.setProject(project);
        task.setDueDate(request.dueDate());
        if (request.assigneeId() != null) {
            task.setAssignee(findUser(request.assigneeId()));
        }
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getAll(TaskStatus status, Long projectId, Pageable pageable) {
        Page<Task> tasks;
        if (projectId != null && status != null) {
            tasks = taskRepository.findByProjectIdAndStatus(projectId, status, pageable);
        } else if (projectId != null) {
            tasks = taskRepository.findByProjectId(projectId, pageable);
        } else if (status != null) {
            tasks = taskRepository.findByStatus(status, pageable);
        } else {
            tasks = taskRepository.findAll(pageable);
        }
        return PageResponse.from(tasks.map(TaskResponse::from));
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long id) {
        return TaskResponse.from(findTask(id));
    }

    public TaskResponse update(Long id, TaskRequest request) {
        Task task = findTask(id);
        Project project = findProject(request.projectId());

        task.setTitle(request.title());
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        task.setProject(project);
        task.setAssignee(request.assigneeId() != null ? findUser(request.assigneeId()) : null);
        task.setDueDate(request.dueDate());
        return TaskResponse.from(taskRepository.save(task));
    }

    public void delete(Long id) {
        Task task = findTask(id);
        taskRepository.delete(task);
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }

    private Project findProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
