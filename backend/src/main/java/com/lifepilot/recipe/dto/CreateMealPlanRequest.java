package com.lifepilot.recipe.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record CreateMealPlanRequest(
        @NotNull Long recipeId,
        @NotNull LocalDate plannedDate,
        @NotNull String mealType,
        String note
) {
}