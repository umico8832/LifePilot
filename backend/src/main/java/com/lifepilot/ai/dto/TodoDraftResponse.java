package com.lifepilot.ai.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Structured todo draft returned by AI parse-todo endpoint.
 * User must confirm before the draft is written to business tables.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TodoDraftResponse(
        List<TodoDraftItem> items,
        boolean needsReview,
        String rawInput,
        String validationMessage
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TodoDraftItem(
            String title,
            String description,
            String priority,
            LocalDateTime dueAt
    ) {
    }
}