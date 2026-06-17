package com.lifepilot.space.dto;

import jakarta.validation.constraints.Size;

public record UpdateSpaceRequest(
        @Size(max = 100) String name
) {
}