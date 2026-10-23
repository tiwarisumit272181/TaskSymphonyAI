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
import org.junit.jupiter.api.Test;
import com.SymphonyAI.tasktracker.entity.Task;
import com.SymphonyAI.tasktracker.entity.TaskStatus;
import com.SymphonyAI.tasktracker.entity.Priority;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TaskRepository taskRepository;
    @InjectMocks
    private ProjectService projectService;

    private Project project(Long id, String name) {
        Project p = new Project();
        p.setId(id);
        p.setName(name);
        p.setDescription("desc");
        return p;
    }

    @Test
    void create_savesAndReturnsProject() {
        when(projectRepository.existsByName("Alpha")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> {
            Project p = i.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProjectResponse response = projectService.create(new ProjectRequest("Alpha", "desc"));

        assertEquals(1L, response.id());
        assertEquals("Alpha", response.name());
        assertEquals("desc", response.description());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void create_throwsConflict_whenNameExists() {
        when(projectRepository.existsByName("Alpha")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> projectService.create(new ProjectRequest("Alpha", null)));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void getAll_returnsPagedProjects() {
        Pageable pageable = PageRequest.of(0, 10);
        when(projectRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(project(1L, "A"), project(2L, "B")), pageable, 2));

        PageResponse<ProjectResponse> result = projectService.getAll(pageable);

        assertEquals(2, result.content().size());
        assertEquals("A", result.content().get(0).name());
        assertEquals("B", result.content().get(1).name());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());
    }

    @Test
    void getById_returnsProject_whenFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L, "A")));

        ProjectResponse response = projectService.getById(1L);

        assertEquals(1L, response.id());
        assertEquals("A", response.name());
    }

    @Test
    void getById_throwsNotFound_whenMissing() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.getById(99L));
    }

    @Test
    void update_changesFields_whenNameUnchanged() {
        Project existing = project(1L, "A");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArgument(0));

        ProjectResponse response = projectService.update(1L, new ProjectRequest("A", "new desc"));

        assertEquals("A", response.name());
        assertEquals("new desc", response.description());
        verify(projectRepository, never()).existsByName(any());
    }

    @Test
    void update_changesName_whenNewNameIsFree() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L, "A")));
        when(projectRepository.existsByName("B")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArgument(0));

        ProjectResponse response = projectService.update(1L, new ProjectRequest("B", "d"));

        assertEquals("B", response.name());
    }

    @Test
    void update_throwsConflict_whenNewNameTaken() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L, "A")));
        when(projectRepository.existsByName("B")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> projectService.update(1L, new ProjectRequest("B", "d")));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void update_throwsNotFound_whenMissing() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.update(99L, new ProjectRequest("X", null)));
    }

    @Test
    void delete_removesProject_whenFound() {
        Project existing = project(1L, "A");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));

        projectService.delete(1L);

        verify(projectRepository).delete(existing);
    }

    @Test
    void delete_throwsNotFound_whenMissing() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.delete(99L));
        verify(projectRepository, never()).delete(any());
    }

    @Test
    void getTasksForProject_returnsPagedTasks_whenProjectExists() {
        Pageable pageable = PageRequest.of(0, 10);
        Project p = project(1L, "A");
        Task t = new Task();
        t.setId(10L);
        t.setTitle("Task 1");
        t.setStatus(TaskStatus.TODO);
        t.setPriority(Priority.LOW);
        t.setProject(p);

        when(projectRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findByProjectId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(t), pageable, 1));

        PageResponse<TaskResponse> result = projectService.getTasksForProject(1L, pageable);

        assertEquals(1, result.content().size());
        assertEquals("Task 1", result.content().getFirst().title());
        assertEquals(1L, result.content().getFirst().projectId());
        assertEquals(1, result.totalElements());
    }

    @Test
    void getTasksForProject_throwsNotFound_whenProjectMissing() {
        Pageable pageable = PageRequest.of(0, 10);
        when(projectRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.getTasksForProject(99L, pageable));
        verify(taskRepository, never()).findByProjectId(any(), any());
    }
}

