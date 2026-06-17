package com.lifepilot.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateTransactionRequest(
        @NotNull @Positive BigDecimal amount,
        @Size(max = 32) String type,
        @Size(max = 10) String currency,
        Long categoryId,
        LocalDateTime occurredAt,
        @Size(max = 200) String merchant,
        @Size(max = 500) String note
) {
}