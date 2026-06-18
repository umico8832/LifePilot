package com.lifepilot.ai.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Structured monthly report draft generated from real statistics data.
 * The AI provider formats aggregated data into a human-readable report.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MonthlyReportResponse(
        int year,
        int month,
        FinanceSummary finance,
        InventorySummary inventory,
        ShoppingSummary shopping,
        TodoSummary todo,
        List<String> highlights,
        List<String> suggestions,
        String reportText
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record FinanceSummary(
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal balance,
            int transactionCount,
            List<CategoryItem> topExpenseCategories
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CategoryItem(
            String name,
            BigDecimal amount,
            int count
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record InventorySummary(
            long totalItems,
            long lowStockCount
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ShoppingSummary(
            long listCount
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TodoSummary(
            long totalCount,
            long pendingCount,
            long completedCount,
            long overdueCount
    ) {
    }
}