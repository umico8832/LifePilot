package com.lifepilot.recipe.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.lifepilot.recipe.MealPlan;

public record MealPlanResponse(
        Long id,
        Long householdId,
        Long recipeId,
        String recipeName,
        LocalDate plannedDate,
        String mealType,
        String note,
        Long createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MealPlanResponse from(MealPlan mealPlan, String recipeName) {
        return new MealPlanResponse(
                mealPlan.getId(),
                mealPlan.getHouseholdId(),
                mealPlan.getRecipeId(),
                recipeName,
                mealPlan.getPlannedDate(),
                mealPlan.getMealType(),
                mealPlan.getNote(),
                mealPlan.getCreatedBy(),
                mealPlan.getCreatedAt(),
                mealPlan.getUpdatedAt()
        );
    }
}