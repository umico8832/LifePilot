package com.lifepilot.recipe.dto;

import jakarta.validation.constraints.Size;

public record UpdateRecipeRequest(
        @Size(max = 200) String name,
        String description,
        String ingredientsJson,
        String stepsJson
) {
}