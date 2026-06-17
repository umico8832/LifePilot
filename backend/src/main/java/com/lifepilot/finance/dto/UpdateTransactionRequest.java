package com.lifepilot.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateTransactionRequest(
        @Positive BigDecimal amount,
        @Size(max = 32) String type,
        Long categoryId,
        LocalDateTime occurredAt,
        @Size(max = 200) String merchant,
        @Size(max = 500) String note
) {
}