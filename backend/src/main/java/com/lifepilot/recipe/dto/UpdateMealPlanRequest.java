package com.lifepilot.recipe.dto;

import java.time.LocalDate;

public record UpdateMealPlanRequest(
        Long recipeId,
        LocalDate plannedDate,
        String mealType,
        String note
) {
}