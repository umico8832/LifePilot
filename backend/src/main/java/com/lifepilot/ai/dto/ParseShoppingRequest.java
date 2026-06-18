package com.lifepilot.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParseShoppingRequest(
        @NotBlank(message = "Text is required")
        @Size(max = 500, message = "Text must be at most 500 characters")
        String text
) {
}