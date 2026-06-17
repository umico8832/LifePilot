package com.lifepilot.shopping.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Size;

public record UpdateShoppingListRequest(
        @Size(max = 200) String name,
        String status,
        BigDecimal estimatedBudget
) {
}