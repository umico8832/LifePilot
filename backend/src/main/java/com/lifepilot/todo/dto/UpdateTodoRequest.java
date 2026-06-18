package com.lifepilot.todo.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Size;

public record UpdateTodoRequest(
        @Size(max = 300) String title,
        String description,
        @Size(max = 20) String status,
        @Size(max = 20) String priority,
        LocalDateTime dueAt,
        @Size(max = 100) String repeatRule,
        Long assignedTo
) {
}