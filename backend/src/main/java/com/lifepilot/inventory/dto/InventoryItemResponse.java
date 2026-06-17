package com.lifepilot.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.lifepilot.inventory.InventoryItem;

public record InventoryItemResponse(
        Long id,
        Long householdId,
        String name,
        String category,
        BigDecimal quantity,
        String unit,
        String location,
        LocalDateTime expireAt,
        BigDecimal lowStockThreshold,
        boolean lowStock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static InventoryItemResponse from(InventoryItem item) {
        boolean isLowStock = item.getLowStockThreshold() != null
                && item.getQuantity() != null
                && item.getQuantity().compareTo(item.getLowStockThreshold()) <= 0;

        return new InventoryItemResponse(
                item.getId(),
                item.getHouseholdId(),
                item.getName(),
                item.getCategory(),
                item.getQuantity(),
                item.getUnit(),
                item.getLocation(),
                item.getExpireAt(),
                item.getLowStockThreshold(),
                isLowStock,
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}