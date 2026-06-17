package com.lifepilot.finance.dto;

import java.time.LocalDateTime;

import com.lifepilot.finance.TransactionCategory;

public record CategoryResponse(
        Long id,
        Long householdId,
        String name,
        String type,
        String icon,
        String color,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CategoryResponse from(TransactionCategory category) {
        return new CategoryResponse(
                category.getId(),
                category.getHouseholdId(),
                category.getName(),
                category.getType(),
                category.getIcon(),
                category.getColor(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}