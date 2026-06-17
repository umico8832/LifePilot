package com.lifepilot.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateInventoryItemRequest(
        @Size(max = 200) String name,
        @Size(max = 100) String category,
        @PositiveOrZero BigDecimal quantity,
        @Size(max = 50) String unit,
        @Size(max = 200) String location,
        LocalDateTime expireAt,
        @PositiveOrZero BigDecimal lowStockThreshold
) {
}