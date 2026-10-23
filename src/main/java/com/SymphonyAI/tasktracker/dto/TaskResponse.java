package com.SymphonyAI.tasktracker.dto;

import com.SymphonyAI.tasktracker.entity.Priority;
import com.SymphonyAI.tasktracker.entity.Task;
import com.SymphonyAI.tasktracker.entity.TaskStatus;

import java.time.LocalDate;

public record TaskResponse(Long id, String title, TaskStatus status, Priority priority,
                           Long projectId, Long assigneeId, LocalDate dueDate) {

    public static TaskResponse from(Task t) {
        return new TaskResponse(
                t.getId(),
                t.getTitle(),
                t.getStatus(),
                t.getPriority(),
                t.getProject().getId(),
                t.getAssignee() != null ? t.getAssignee().getId() : null,
                t.getDueDate());
    }
}
