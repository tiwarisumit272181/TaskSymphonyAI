package com.SymphonyAI.tasktracker.dto;
import com.SymphonyAI.tasktracker.entity.Priority;
import com.SymphonyAI.tasktracker.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TaskRequest(
        @NotBlank(message = "Title is required") String title,
        TaskStatus status,
        Priority priority,
        @NotNull(message = "projectId is required") Long projectId,
        Long assigneeId,
        LocalDate dueDate) {
}
