package com.lifepilot.statistics.dto;

import java.util.List;

public record ShoppingStatsResponse(
        long totalLists,
        long activeLists,
        long completedLists,
        long totalItems,
        long purchasedItems,
        List<DailyTrend> recent30Days
) {
    public record DailyTrend(
            String date,
            long count
    ) {
    }
}