package com.lifepilot.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 32) String type,
        @Size(max = 50) String icon,
        @Size(max = 20) String color
) {
}