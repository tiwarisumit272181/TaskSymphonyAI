package com.SymphonyAI.tasktracker.dto;

import jakarta.validation.constraints.NotBlank;

public record ProjectRequest(
        @NotBlank(message = "Name is required") String name,
        String description) {
}
