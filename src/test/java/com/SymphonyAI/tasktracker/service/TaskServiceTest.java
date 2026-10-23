package com.SymphonyAI.tasktracker.service;
import com.SymphonyAI.tasktracker.config.AppProperties;
import com.SymphonyAI.tasktracker.dto.TaskRequest;
import com.SymphonyAI.tasktracker.dto.TaskResponse;
import com.SymphonyAI.tasktracker.entity.*;
import com.SymphonyAI.tasktracker.repository.ProjectRepository;
import com.SymphonyAI.tasktracker.repository.TaskRepository;
import com.SymphonyAI.tasktracker.repository.UserRepository;
import com.SymphonyAI.tasktracker.dto.PageResponse;
import com.SymphonyAI.tasktracker.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AppProperties appProperties;
    @InjectMocks
    private TaskService taskService;

    private final Pageable pageable = PageRequest.of(0, 10);

    private Project project(Long id) {
        Project p = new Project();
        p.setId(id);
        p.setName("Project " + id);
        return p;
    }

    private User user(Long id) {
        User u = new User();
        u.setId(id);
        u.setName("User " + id);
        u.setEmail("user" + id + "@x.com");
        return u;
    }

    private Task task(Long id, Project project) {
        Task t = new Task();
        t.setId(id);
        t.setTitle("Task " + id);
        t.setStatus(TaskStatus.TODO);
        t.setPriority(Priority.LOW);
        t.setProject(project);
        return t;
    }

    // ---------- create ----------

    @Test
    void create_usesDefaultPriorityAndStatus_whenNotSupplied() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L)));
        when(appProperties.getDefaultPriority()).thenReturn(Priority.MEDIUM);
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        TaskResponse response = taskService.create(new TaskRequest("Test", null, null, 1L, null, null));

        assertEquals(Priority.MEDIUM, response.priority());
        assertEquals(TaskStatus.TODO, response.status());
        assertEquals(1L, response.projectId());
        assertNull(response.assigneeId());
    }

    @Test
    void create_usesSuppliedValuesAndAssignee() {
        LocalDate due = LocalDate.of(2026, 12, 31);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L)));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L)));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        TaskResponse response = taskService.create(
                new TaskRequest("Test", TaskStatus.IN_PROGRESS, Priority.HIGH, 1L, 2L, due));

        assertEquals(TaskStatus.IN_PROGRESS, response.status());
        assertEquals(Priority.HIGH, response.priority());
        assertEquals(2L, response.assigneeId());
        assertEquals(due, response.dueDate());
        verify(appProperties, never()).getDefaultPriority();
    }

    @Test
    void create_throwsNotFound_whenProjectMissing() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taskService.create(new TaskRequest("T", null, null, 99L, null, null)));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void create_throwsNotFound_whenAssigneeMissing() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L)));
        when(appProperties.getDefaultPriority()).thenReturn(Priority.MEDIUM);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taskService.create(new TaskRequest("T", null, null, 1L, 99L, null)));
        verify(taskRepository, never()).save(any());
    }

    // ---------- getById ----------

    @Test
    void getById_returnsTask_whenFound() {
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task(5L, project(1L))));

        TaskResponse response = taskService.getById(5L);

        assertEquals(5L, response.id());
        assertEquals("Task 5", response.title());
        assertEquals(1L, response.projectId());
    }

    @Test
    void getById_throwsNotFound_whenMissing() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getById(99L));
    }

    // ---------- getAll ----------

    @Test
    void getAll_noFilters_returnsEverything() {
        Project p = project(1L);
        when(taskRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(task(1L, p), task(2L, p)), pageable, 2));

        PageResponse<TaskResponse> result = taskService.getAll(null, null, pageable);

        assertEquals(2, result.content().size());
        assertEquals(2, result.totalElements());
        verify(taskRepository).findAll(pageable);
    }

    @Test
    void getAll_filterByStatusOnly() {
        when(taskRepository.findByStatus(TaskStatus.DONE, pageable))
                .thenReturn(new PageImpl<>(List.of(task(1L, project(1L))), pageable, 1));

        PageResponse<TaskResponse> result = taskService.getAll(TaskStatus.DONE, null, pageable);

        assertEquals(1, result.content().size());
        verify(taskRepository).findByStatus(TaskStatus.DONE, pageable);
    }

    @Test
    void getAll_filterByProjectOnly() {
        when(taskRepository.findByProjectId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(task(1L, project(1L))), pageable, 1));

        PageResponse<TaskResponse> result = taskService.getAll(null, 1L, pageable);

        assertEquals(1, result.content().size());
        verify(taskRepository).findByProjectId(1L, pageable);
    }

    @Test
    void getAll_filterByProjectAndStatus() {
        when(taskRepository.findByProjectIdAndStatus(1L, TaskStatus.TODO, pageable))
                .thenReturn(new PageImpl<>(List.of(task(1L, project(1L))), pageable, 1));

        PageResponse<TaskResponse> result = taskService.getAll(TaskStatus.TODO, 1L, pageable);

        assertEquals(1, result.content().size());
        verify(taskRepository).findByProjectIdAndStatus(1L, TaskStatus.TODO, pageable);
    }

    // ---------- update ----------

    @Test
    void update_changesAllFields() {
        Task existing = task(5L, project(1L));
        LocalDate due = LocalDate.of(2027, 1, 15);
        when(taskRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project(2L)));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user(3L)));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        TaskResponse response = taskService.update(5L,
                new TaskRequest("Updated", TaskStatus.DONE, Priority.HIGH, 2L, 3L, due));

        assertEquals("Updated", response.title());
        assertEquals(TaskStatus.DONE, response.status());
        assertEquals(Priority.HIGH, response.priority());
        assertEquals(2L, response.projectId());
        assertEquals(3L, response.assigneeId());
        assertEquals(due, response.dueDate());
    }

    @Test
    void update_keepsStatusAndPriority_whenNotSupplied_andClearsAssignee() {
        Task existing = task(5L, project(1L));
        existing.setStatus(TaskStatus.IN_PROGRESS);
        existing.setPriority(Priority.HIGH);
        existing.setAssignee(user(3L));
        when(taskRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L)));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        TaskResponse response = taskService.update(5L,
                new TaskRequest("Same", null, null, 1L, null, null));

        assertEquals(TaskStatus.IN_PROGRESS, response.status());
        assertEquals(Priority.HIGH, response.priority());
        assertNull(response.assigneeId());
    }

    @Test
    void update_throwsNotFound_whenTaskMissing() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taskService.update(99L, new TaskRequest("T", null, null, 1L, null, null)));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void update_throwsNotFound_whenProjectMissing() {
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task(5L, project(1L))));
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taskService.update(5L, new TaskRequest("T", null, null, 99L, null, null)));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void update_throwsNotFound_whenAssigneeMissing() {
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task(5L, project(1L))));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L)));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taskService.update(5L, new TaskRequest("T", null, null, 1L, 99L, null)));
        verify(taskRepository, never()).save(any());
    }

    // ---------- delete ----------

    @Test
    void delete_removesTask_whenFound() {
        Task existing = task(5L, project(1L));
        when(taskRepository.findById(5L)).thenReturn(Optional.of(existing));

        taskService.delete(5L);

        verify(taskRepository).delete(existing);
    }

    @Test
    void delete_throwsNotFound_whenMissing() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.delete(99L));
        verify(taskRepository, never()).delete(any());
    }
}