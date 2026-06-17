package com.lifepilot.shopping.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.lifepilot.shopping.ShoppingItem;

public record ShoppingItemResponse(
        Long id,
        Long shoppingListId,
        String name,
        BigDecimal quantity,
        String unit,
        BigDecimal estimatedPrice,
        Boolean purchased,
        Long inventoryItemId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ShoppingItemResponse from(ShoppingItem item) {
        return new ShoppingItemResponse(
                item.getId(),
                item.getShoppingListId(),
                item.getName(),
                item.getQuantity(),
                item.getUnit(),
                item.getEstimatedPrice(),
                item.getPurchased(),
                item.getInventoryItemId(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}