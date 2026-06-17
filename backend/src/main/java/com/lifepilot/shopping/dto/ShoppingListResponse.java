package com.lifepilot.shopping.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.lifepilot.shopping.ShoppingList;

public record ShoppingListResponse(
        Long id,
        Long householdId,
        String name,
        String status,
        BigDecimal estimatedBudget,
        Long createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ShoppingItemResponse> items
) {
    public static ShoppingListResponse from(ShoppingList list) {
        return new ShoppingListResponse(
                list.getId(),
                list.getHouseholdId(),
                list.getName(),
                list.getStatus(),
                list.getEstimatedBudget(),
                list.getCreatedBy(),
                list.getCreatedAt(),
                list.getUpdatedAt(),
                List.of()
        );
    }

    public static ShoppingListResponse from(ShoppingList list, List<ShoppingItemResponse> items) {
        return new ShoppingListResponse(
                list.getId(),
                list.getHouseholdId(),
                list.getName(),
                list.getStatus(),
                list.getEstimatedBudget(),
                list.getCreatedBy(),
                list.getCreatedAt(),
                list.getUpdatedAt(),
                items
        );
    }
}