package com.lifepilot.ai.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiCallLogResponse(
        Long id,
        Long userId,
        Long spaceId,
        String provider,
        String scenario,
        String promptHash,
        String requestJson,
        String responseJson,
        String status,
        Long durationMs,
        String errorMessage,
        LocalDateTime createdAt
) {
}
