package com.lifepilot.shopping.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateShoppingItemRequest(
        @NotBlank @Size(max = 200) String name,
        @Positive BigDecimal quantity,
        @Size(max = 50) String unit,
        @Positive BigDecimal estimatedPrice
) {
}