package com.lifepilot.recipe.dto;

import java.time.LocalDateTime;

import com.lifepilot.recipe.Recipe;

public record RecipeResponse(
        Long id,
        Long householdId,
        String name,
        String description,
        String ingredientsJson,
        String stepsJson,
        Long createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RecipeResponse from(Recipe recipe) {
        return new RecipeResponse(
                recipe.getId(),
                recipe.getHouseholdId(),
                recipe.getName(),
                recipe.getDescription(),
                recipe.getIngredientsJson(),
                recipe.getStepsJson(),
                recipe.getCreatedBy(),
                recipe.getCreatedAt(),
                recipe.getUpdatedAt()
        );
    }
}