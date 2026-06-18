package com.lifepilot.recipe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRecipeRequest(
        @NotBlank @Size(max = 200) String name,
        String description,
        String ingredientsJson,
        String stepsJson
) {
}