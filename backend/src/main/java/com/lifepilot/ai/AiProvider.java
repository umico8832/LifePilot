package com.lifepilot.ai;

import java.util.List;

import com.lifepilot.ai.dto.RecipeRecommendationResponse;
import com.lifepilot.ai.dto.ShoppingDraftResponse;
import com.lifepilot.ai.dto.TodoDraftResponse;
import com.lifepilot.ai.dto.TransactionDraftResponse;
import com.lifepilot.inventory.InventoryItem;
import com.lifepilot.recipe.MealPlan;
import com.lifepilot.recipe.Recipe;

/**
 * Interface for AI providers that parse natural language into structured data.
 * Implementations must be deterministic for tests and never call real external APIs
 * unless explicitly configured to do so.
 */
public interface AiProvider {

    /**
     * Parse natural language text into a transaction draft.
     * Returns a draft with needsReview=true when the parsing is ambiguous.
     * Returns null if the input cannot be parsed at all.
     */
    TransactionDraftResponse parseTransaction(String text);

    /**
     * Parse natural language text into a shopping list draft.
     * Returns a draft with needsReview=true when the parsing is ambiguous.
     * Returns null if the input cannot be parsed at all.
     */
    ShoppingDraftResponse parseShoppingList(String text);

    /**
     * Parse natural language text into a todo task draft.
     * Returns a draft with needsReview=true when the parsing is ambiguous.
     * Returns null if the input cannot be parsed at all.
     */
    TodoDraftResponse parseTodo(String text);

    /**
     * Recommend recipes based on current inventory.
     * Returns a list of recommended recipes sorted by match score (descending).
     * Returns an empty list if no recipes can be recommended.
     */
    RecipeRecommendationResponse recommendRecipes(List<InventoryItem> inventory, List<Recipe> recipes);

    /**
     * Generate a shopping list draft from planned meals and current inventory.
     * Returns a reviewable draft; callers must not write business records without user confirmation.
     */
    ShoppingDraftResponse draftShoppingListFromMealPlan(List<MealPlan> mealPlans, List<Recipe> recipes, List<InventoryItem> inventory);
}
