package com.SymphonyAI.tasktracker.controller;

import com.SymphonyAI.tasktracker.config.PageableFactory;
import com.SymphonyAI.tasktracker.dto.PageResponse;
import com.SymphonyAI.tasktracker.dto.UserRequest;
import com.SymphonyAI.tasktracker.dto.UserResponse;
import com.SymphonyAI.tasktracker.service.UserService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PageableFactory pageableFactory;

    public UserController(UserService userService, PageableFactory pageableFactory) {
        this.userService = userService;
        this.pageableFactory = pageableFactory;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @GetMapping
    public PageResponse<UserResponse> getAll(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "direction", required = false) String direction) throws BadRequestException {
        return userService.getAll(
                pageableFactory.create(page, size, sortBy, direction, SortFields.USER, "id"));
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable("id") Long id) {
        return userService.getById(id);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable("id") Long id, @Valid @RequestBody UserRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
