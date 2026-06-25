package com.lifepilot.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.lifepilot.ai.dto.ShoppingDraftResponse;
import com.lifepilot.inventory.InventoryItem;
import com.lifepilot.recipe.MealPlan;
import com.lifepilot.recipe.Recipe;

class MockAiProviderTest {

    private final MockAiProvider provider = new MockAiProvider();

    @Test
    void draftShoppingListFromMealPlan_returnsMissingIngredientsOnly() {
        MealPlan plan = new MealPlan();
        plan.setRecipeId(1L);
        plan.setPlannedDate(LocalDate.of(2026, 6, 22));
        plan.setMealType("lunch");

        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("番茄炒蛋");
        recipe.setIngredientsJson("""
                [
                  {"name":"鸡蛋","quantity":"2个"},
                  {"name":"番茄","quantity":"3个"}
                ]
                """);

        InventoryItem eggs = new InventoryItem();
        eggs.setName("鸡蛋");
        eggs.setQuantity(new BigDecimal("4"));
        eggs.setUnit("个");

        InventoryItem tomatoes = new InventoryItem();
        tomatoes.setName("番茄");
        tomatoes.setQuantity(new BigDecimal("1"));
        tomatoes.setUnit("个");

        ShoppingDraftResponse result = provider.draftShoppingListFromMealPlan(
                List.of(plan), List.of(recipe), List.of(eggs, tomatoes));

        assertThat(result.listName()).isEqualTo("饮食计划采购清单");
        assertThat(result.needsReview()).isTrue();
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).name()).isEqualTo("番茄");
        assertThat(result.items().get(0).quantity()).isEqualByComparingTo(new BigDecimal("2"));
        assertThat(result.items().get(0).unit()).isEqualTo("个");
        assertThat(result.rawInput()).contains("番茄炒蛋");
    }

    @Test
    void draftShoppingListFromMealPlan_returnsReviewMessageWhenNoPlans() {
        ShoppingDraftResponse result = provider.draftShoppingListFromMealPlan(
                List.of(), List.of(), List.of());

        assertThat(result.items()).isEmpty();
        assertThat(result.needsReview()).isTrue();
        assertThat(result.validationMessage()).contains("没有饮食计划");
    }

    @Test
    void draftShoppingListFromMealPlan_requiresReviewWhenRecipeHasNoIngredients() {
        MealPlan plan = new MealPlan();
        plan.setRecipeId(1L);
        plan.setPlannedDate(LocalDate.of(2026, 6, 22));
        plan.setMealType("dinner");

        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("空菜谱");
        recipe.setIngredientsJson("[]");

        ShoppingDraftResponse result = provider.draftShoppingListFromMealPlan(
                List.of(plan), List.of(recipe), List.of());

        assertThat(result.items()).isEmpty();
        assertThat(result.needsReview()).isTrue();
        assertThat(result.validationMessage()).contains("缺少食材信息");
    }
}
