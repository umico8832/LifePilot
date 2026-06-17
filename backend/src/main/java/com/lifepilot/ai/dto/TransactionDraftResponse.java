package com.lifepilot.ai.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Structured draft returned by AI parse-transaction endpoint.
 * User must confirm before the draft is written to business tables.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionDraftResponse(
        String type,
        BigDecimal amount,
        String currency,
        LocalDateTime occurredAt,
        String merchant,
        String categoryName,
        String note,
        boolean needsReview,
        String rawInput,
        String validationMessage
) {
}