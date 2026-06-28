package com.lifepilot.ai.dto;

import java.util.List;

public record AiCallLogSummaryResponse(
        long totalCount,
        long successCount,
        long failedCount,
        double successRate,
        long averageDurationMs,
        List<ScenarioCount> scenarioCounts,
        List<StatusCount> statusCounts
) {
    public record ScenarioCount(String scenario, long count) {}

    public record StatusCount(String status, long count) {}
}
