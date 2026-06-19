package com.lifepilot.ai.dto;

import java.util.List;

/**
 * Response for AI recipe recommendation based on inventory.
 *
 * @param recipes list of recommended recipes sorted by match score (descending)
 */
public record RecipeRecommendationResponse(
        List<RecommendedRecipe> recipes
) {
    /**
     * A single recommended recipe with match details.
     *
     * @param recipeId        recipe ID
     * @param recipeName      recipe name
     * @param matchedIngredients ingredients found in inventory
     * @param missingIngredients ingredients not found in inventory
     * @param matchScore      match score (0.0 ~ 1.0)
     * @param reason          recommendation reason
     */
    public record RecommendedRecipe(
            Long recipeId,
            String recipeName,
            List<String> matchedIngredients,
            List<String> missingIngredients,
            double matchScore,
            String reason
    ) {
    }
}