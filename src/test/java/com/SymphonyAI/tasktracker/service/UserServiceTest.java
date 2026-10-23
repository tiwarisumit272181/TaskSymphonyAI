package com.SymphonyAI.tasktracker.service;

import com.SymphonyAI.tasktracker.dto.PageResponse;
import com.SymphonyAI.tasktracker.dto.UserRequest;
import com.SymphonyAI.tasktracker.dto.UserResponse;
import com.SymphonyAI.tasktracker.entity.User;
import com.SymphonyAI.tasktracker.exception.ConflictException;
import com.SymphonyAI.tasktracker.exception.ResourceNotFoundException;
import com.SymphonyAI.tasktracker.repository.TaskRepository;
import com.SymphonyAI.tasktracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskRepository taskRepository;
    @InjectMocks
    private UserService userService;

    private User user(Long id, String email) {
        User u = new User();
        u.setId(id);
        u.setName("Name " + id);
        u.setEmail(email);
        u.setRole("DEVELOPER");
        return u;
    }

    @Test
    void create_savesAndReturnsUser() {
        when(userRepository.existsByEmail("a@x.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse response = userService.create(new UserRequest("Alice", "a@x.com", "QA"));

        assertEquals(1L, response.id());
        assertEquals("Alice", response.name());
        assertEquals("a@x.com", response.email());
        assertEquals("QA", response.role());
    }

    @Test
    void create_throwsConflict_whenEmailExists() {
        when(userRepository.existsByEmail("a@x.com")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> userService.create(new UserRequest("Alice", "a@x.com", "QA")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getAll_returnsPagedUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(user(1L, "a@x.com"), user(2L, "b@x.com")), pageable, 2));

        PageResponse<UserResponse> result = userService.getAll(pageable);

        assertEquals(2, result.content().size());
        assertEquals("a@x.com", result.content().get(0).email());
        assertEquals("b@x.com", result.content().get(1).email());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());
    }

    @Test
    void getById_returnsUser_whenFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "a@x.com")));

        UserResponse response = userService.getById(1L);

        assertEquals(1L, response.id());
        assertEquals("a@x.com", response.email());
    }

    @Test
    void getById_throwsNotFound_whenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getById(99L));
    }

    @Test
    void update_changesFields_whenEmailUnchanged() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "a@x.com")));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse response = userService.update(1L, new UserRequest("New Name", "a@x.com", "MANAGER"));

        assertEquals("New Name", response.name());
        assertEquals("MANAGER", response.role());
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void update_throwsConflict_whenNewEmailTaken() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "a@x.com")));
        when(userRepository.existsByEmail("b@x.com")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> userService.update(1L, new UserRequest("N", "b@x.com", "QA")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_throwsNotFound_whenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.update(99L, new UserRequest("N", "n@x.com", "QA")));
    }

    @Test
    void delete_removesUser_whenNoTasksAssigned() {
        User existing = user(1L, "a@x.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.existsByAssigneeId(1L)).thenReturn(false);

        userService.delete(1L);

        verify(userRepository).delete(existing);
    }

    @Test
    void delete_throwsConflict_whenUserHasTasks() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "a@x.com")));
        when(taskRepository.existsByAssigneeId(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.delete(1L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void delete_throwsNotFound_whenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.delete(99L));
        verify(userRepository, never()).delete(any());
    }
}