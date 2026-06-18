package com.lifepilot.statistics.dto;

public record TodoStatsResponse(
        long totalCount,
        long pendingCount,
        long inProgressCount,
        long completedCount,
        long cancelledCount,
        long overdueCount
) {
}