package com.lifepilot.shopping.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Size;

public record UpdateShoppingItemRequest(
        @Size(max = 200) String name,
        BigDecimal quantity,
        @Size(max = 50) String unit,
        BigDecimal estimatedPrice,
        Boolean purchased
) {
}