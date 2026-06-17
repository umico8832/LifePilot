package com.lifepilot.statistics.dto;

import java.math.BigDecimal;

public record OverviewResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance,
        long transactionCount,
        long inventoryItemCount,
        long shoppingListCount,
        long inventoryAlertCount
) {
}