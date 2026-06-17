package com.lifepilot.statistics.dto;

import java.math.BigDecimal;
import java.util.List;

public record FinanceMonthlyResponse(
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance,
        List<CategoryBreakdown> categories
) {

    public record CategoryBreakdown(
            Long categoryId,
            String categoryName,
            BigDecimal amount,
            long count
    ) {
    }
}