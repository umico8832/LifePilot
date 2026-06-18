package com.lifepilot.statistics.dto;

import java.math.BigDecimal;
import java.util.List;

public record FinanceCategoriesResponse(
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        List<CategoryDetail> expenseCategories,
        List<CategoryDetail> incomeCategories
) {

    public record CategoryDetail(
            Long categoryId,
            String categoryName,
            BigDecimal amount,
            long count
    ) {
    }
}