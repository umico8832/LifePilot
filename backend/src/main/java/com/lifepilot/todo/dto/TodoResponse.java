package com.lifepilot.todo.dto;

import java.time.LocalDateTime;

import com.lifepilot.todo.TodoTask;

public record TodoResponse(
        Long id,
        Long householdId,
        String title,
        String description,
        String status,
        String priority,
        LocalDateTime dueAt,
        String repeatRule,
        Long assignedTo,
        boolean overdue,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TodoResponse from(TodoTask task) {
        boolean isOverdue = task.getDueAt() != null
                && "pending".equals(task.getStatus())
                && task.getDueAt().isBefore(LocalDateTime.now());

        return new TodoResponse(
                task.getId(),
                task.getHouseholdId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueAt(),
                task.getRepeatRule(),
                task.getAssignedTo(),
                isOverdue,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}