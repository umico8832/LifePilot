package com.lifepilot.statistics.dto;

import java.util.List;

public record TodoStatsResponse(
        long totalCount,
        long pendingCount,
        long inProgressCount,
        long completedCount,
        long cancelledCount,
        long overdueCount,
        double completionRate,
        List<DailyTrend> recent30Days
) {
    public record DailyTrend(String date, long count) {}
}
