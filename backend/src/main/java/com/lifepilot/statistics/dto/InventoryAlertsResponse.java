package com.lifepilot.statistics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InventoryAlertsResponse(
        List<AlertItem> expiringItems,
        List<AlertItem> lowStockItems,
        int totalAlerts
) {
    public record AlertItem(
            Long id,
            String name,
            String category,
            BigDecimal quantity,
            String unit,
            String location,
            LocalDateTime expireAt,
            BigDecimal lowStockThreshold,
            String alertType  // "expiring" or "low_stock"
    ) {}
}