package com.SymphonyAI.tasktracker.service;


import com.SymphonyAI.tasktracker.dto.PageResponse;
import com.SymphonyAI.tasktracker.repository.TaskRepository;
import com.SymphonyAI.tasktracker.repository.UserRepository;
import com.SymphonyAI.tasktracker.dto.UserRequest;
import com.SymphonyAI.tasktracker.dto.UserResponse;
import com.SymphonyAI.tasktracker.entity.User;
import com.SymphonyAI.tasktracker.exception.ConflictException;
import com.SymphonyAI.tasktracker.exception.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public UserService(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("User with email '" + request.email() + "' already exists");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(request.role());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAll(Pageable pageable) {
        return PageResponse.from(userRepository.findAll(pageable).map(UserResponse::from));
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return UserResponse.from(findUser(id));
    }

    public UserResponse update(Long id, UserRequest request) {
        User user = findUser(id);
        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new ConflictException("User with email '" + request.email() + "' already exists");
        }
        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(request.role());
        return UserResponse.from(userRepository.save(user));
    }

    public void delete(Long id) {
        User user = findUser(id);
        if (taskRepository.existsByAssigneeId(id)) {
            throw new ConflictException("Cannot delete user " + id + ": user is still assigned to one or more tasks");
        }
        userRepository.delete(user);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
