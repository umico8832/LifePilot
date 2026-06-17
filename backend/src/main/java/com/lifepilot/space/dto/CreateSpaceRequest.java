package com.lifepilot.space.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSpaceRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 32) String type
) {
}