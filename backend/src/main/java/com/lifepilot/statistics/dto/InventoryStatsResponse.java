package com.lifepilot.statistics.dto;

import java.util.List;

public record InventoryStatsResponse(
        long totalItems,
        long lowStockCount,
        List<CategoryCount> byCategory
) {
    public record CategoryCount(
            String category,
            long count
    ) {
    }
}