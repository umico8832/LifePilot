package com.lifepilot.ai.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Structured shopping list draft returned by AI parse-shopping endpoint.
 * User must confirm before the draft is written to business tables.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ShoppingDraftResponse(
        String listName,
        BigDecimal estimatedBudget,
        List<ShoppingDraftItem> items,
        boolean needsReview,
        String rawInput,
        String validationMessage
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ShoppingDraftItem(
            String name,
            BigDecimal quantity,
            String unit,
            BigDecimal estimatedPrice
    ) {
    }
}