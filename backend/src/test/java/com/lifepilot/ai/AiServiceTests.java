package com.lifepilot.ai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lifepilot.ai.dto.ParseShoppingRequest;
import com.lifepilot.ai.dto.ParseTodoRequest;
import com.lifepilot.ai.dto.ParseTransactionRequest;
import com.lifepilot.ai.dto.ShoppingDraftResponse;
import com.lifepilot.ai.dto.TodoDraftResponse;
import com.lifepilot.ai.dto.TransactionDraftResponse;
import com.lifepilot.ai.dto.AiCallLogResponse;
import com.lifepilot.ai.dto.RecipeRecommendationResponse;
import com.lifepilot.common.BusinessException;
import com.lifepilot.finance.TransactionCategoryMapper;
import com.lifepilot.finance.TransactionRecordMapper;
import com.lifepilot.inventory.InventoryItem;
import com.lifepilot.inventory.InventoryItemMapper;
import com.lifepilot.recipe.MealPlan;
import com.lifepilot.recipe.MealPlanMapper;
import com.lifepilot.recipe.Recipe;
import com.lifepilot.recipe.RecipeMapper;
import com.lifepilot.shopping.ShoppingListMapper;
import com.lifepilot.space.HouseholdService;
import com.lifepilot.todo.TodoTaskMapper;

@ExtendWith(MockitoExtension.class)
class AiServiceTests {

    @Mock
    private AiProvider aiProvider;
    @Mock
    private HouseholdService householdService;
    @Mock
    private TransactionRecordMapper transactionRecordMapper;
    @Mock
    private TransactionCategoryMapper transactionCategoryMapper;
    @Mock
    private InventoryItemMapper inventoryItemMapper;
    @Mock
    private ShoppingListMapper shoppingListMapper;
    @Mock
    private TodoTaskMapper todoTaskMapper;
    @Mock
    private RecipeMapper recipeMapper;
    @Mock
    private MealPlanMapper mealPlanMapper;
    @Mock
    private AiCallLogService aiCallLogService;

    @InjectMocks
    private AiService aiService;

    private static final Long USER_ID = 1L;
    private static final Long SPACE_ID = 10L;

    // --- parseTransaction ---

    @Test
    void parseTransaction_returnsProviderDraft() {
        TransactionDraftResponse draft = new TransactionDraftResponse(
                "expense", new BigDecimal("15.50"), "CNY", null, null, null,
                "早餐", false, "早餐15.5", null);
        when(aiProvider.parseTransaction("早餐15.5")).thenReturn(draft);

        TransactionDraftResponse result = aiService.parseTransaction(USER_ID, SPACE_ID,
                new ParseTransactionRequest("早餐15.5"));

        assertEquals("expense", result.type());
        assertEquals(new BigDecimal("15.50"), result.amount());
        assertFalse(result.needsReview());
        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
        verify(aiCallLogService).recordSuccess(eq(USER_ID), eq(SPACE_ID), anyString(),
                eq("parse_transaction"), any(), anyMap(), anyMap(), anyLong());
    }

    @Test
    void parseTransaction_returnsNeedsReviewOnNull() {
        when(aiProvider.parseTransaction("???")).thenReturn(null);

        TransactionDraftResponse result = aiService.parseTransaction(USER_ID, SPACE_ID,
                new ParseTransactionRequest("???"));

        assertTrue(result.needsReview());
        assertNotNull(result.validationMessage());
        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
        verify(aiCallLogService).recordSuccess(eq(USER_ID), eq(SPACE_ID), anyString(),
                eq("parse_transaction"), any(), anyMap(), anyMap(), anyLong());
    }

    @Test
    void parseTransaction_throwsWhenNotMember() {
        doThrow(new BusinessException("FORBIDDEN", "not a member"))
                .when(householdService).requireSpaceMembership(USER_ID, SPACE_ID);

        assertThrows(BusinessException.class, () ->
                aiService.parseTransaction(USER_ID, SPACE_ID, new ParseTransactionRequest("test")));
        verify(aiCallLogService, never()).recordSuccess(any(), any(), any(), any(), any(), anyMap(), anyMap(), anyLong());
        verify(aiCallLogService, never()).recordFailure(any(), any(), any(), any(), any(), anyMap(), any(), anyLong());
    }

    @Test
    void parseTransaction_recordsFailureWhenProviderThrows() {
        RuntimeException error = new RuntimeException("provider unavailable");
        when(aiProvider.parseTransaction("早餐15.5")).thenThrow(error);

        assertThrows(RuntimeException.class, () -> aiService.parseTransaction(USER_ID, SPACE_ID,
                new ParseTransactionRequest("早餐15.5")));

        verify(aiCallLogService).recordFailure(eq(USER_ID), eq(SPACE_ID), anyString(),
                eq("parse_transaction"), any(), anyMap(), eq(error), anyLong());
    }

    // --- parseShoppingList ---

    @Test
    void parseShoppingList_returnsProviderDraft() {
        ShoppingDraftResponse draft = new ShoppingDraftResponse(
                "买菜", null, java.util.List.of(),
                false, "买菜", null);
        when(aiProvider.parseShoppingList("买菜")).thenReturn(draft);

        ShoppingDraftResponse result = aiService.parseShoppingList(USER_ID, SPACE_ID,
                new ParseShoppingRequest("买菜"));

        assertEquals("买菜", result.listName());
        assertFalse(result.needsReview());
        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
        verify(aiCallLogService).recordSuccess(eq(USER_ID), eq(SPACE_ID), anyString(),
                eq("parse_shopping"), any(), anyMap(), anyMap(), anyLong());
    }

    @Test
    void parseShoppingList_returnsNeedsReviewOnNull() {
        when(aiProvider.parseShoppingList("???")).thenReturn(null);

        ShoppingDraftResponse result = aiService.parseShoppingList(USER_ID, SPACE_ID,
                new ParseShoppingRequest("???"));

        assertTrue(result.needsReview());
        assertNotNull(result.validationMessage());
    }

    @Test
    void parseShoppingList_throwsWhenNotMember() {
        doThrow(new BusinessException("FORBIDDEN", "not a member"))
                .when(householdService).requireSpaceMembership(USER_ID, SPACE_ID);

        assertThrows(BusinessException.class, () ->
                aiService.parseShoppingList(USER_ID, SPACE_ID, new ParseShoppingRequest("test")));
    }

    // --- parseTodo ---

    @Test
    void parseTodo_returnsProviderDraft() {
        TodoDraftResponse draft = new TodoDraftResponse(
                java.util.List.of(), false, "买牛奶", null);
        when(aiProvider.parseTodo("买牛奶")).thenReturn(draft);

        TodoDraftResponse result = aiService.parseTodo(USER_ID, SPACE_ID,
                new ParseTodoRequest("买牛奶"));

        assertFalse(result.needsReview());
        assertEquals("买牛奶", result.rawInput());
        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
        verify(aiCallLogService).recordSuccess(eq(USER_ID), eq(SPACE_ID), anyString(),
                eq("parse_todo"), any(), anyMap(), anyMap(), anyLong());
    }

    @Test
    void parseTodo_returnsNeedsReviewOnNull() {
        when(aiProvider.parseTodo("???")).thenReturn(null);

        TodoDraftResponse result = aiService.parseTodo(USER_ID, SPACE_ID,
                new ParseTodoRequest("???"));

        assertTrue(result.needsReview());
        assertNotNull(result.validationMessage());
    }

    @Test
    void parseTodo_throwsWhenNotMember() {
        doThrow(new BusinessException("FORBIDDEN", "not a member"))
                .when(householdService).requireSpaceMembership(USER_ID, SPACE_ID);

        assertThrows(BusinessException.class, () ->
                aiService.parseTodo(USER_ID, SPACE_ID, new ParseTodoRequest("test")));
    }

    // --- recommendRecipes ---

    @Test
    void recommendRecipes_returnsProviderRecommendation() {
        RecipeRecommendationResponse expected = new RecipeRecommendationResponse(
                java.util.List.of());
        when(aiProvider.recommendRecipes(anyList(), anyList())).thenReturn(expected);

        RecipeRecommendationResponse result = aiService.recommendRecipes(USER_ID, SPACE_ID);

        assertNotNull(result);
        assertTrue(result.recipes().isEmpty());
        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
        verify(inventoryItemMapper).selectList(any());
        verify(recipeMapper).selectList(any());
        verify(aiCallLogService).recordSuccess(eq(USER_ID), eq(SPACE_ID), anyString(),
                eq("recommend_recipes"), isNull(), anyMap(), anyMap(), anyLong());
    }

    @Test
    void recommendRecipes_throwsWhenNotMember() {
        doThrow(new BusinessException("FORBIDDEN", "not a member"))
                .when(householdService).requireSpaceMembership(USER_ID, SPACE_ID);

        assertThrows(BusinessException.class, () ->
                aiService.recommendRecipes(USER_ID, SPACE_ID));
    }

    @Test
    void recommendRecipes_passesInventoryAndRecipesToProvider() {
        InventoryItem item = new InventoryItem();
        item.setName("鸡蛋");
        Recipe recipe = new Recipe();
        recipe.setName("番茄炒蛋");
        recipe.setIngredientsJson("[{\"name\":\"鸡蛋\",\"quantity\":\"2个\"},{\"name\":\"番茄\",\"quantity\":\"1个\"}]");

        when(inventoryItemMapper.selectList(any())).thenReturn(java.util.List.of(item));
        when(recipeMapper.selectList(any())).thenReturn(java.util.List.of(recipe));

        RecipeRecommendationResponse response = new RecipeRecommendationResponse(
                java.util.List.of(new RecipeRecommendationResponse.RecommendedRecipe(
                        1L, "番茄炒蛋",
                        java.util.List.of("鸡蛋"), java.util.List.of("番茄"),
                        0.5, "部分食材可从库存中获取")));
        when(aiProvider.recommendRecipes(anyList(), anyList())).thenReturn(response);

        RecipeRecommendationResponse result = aiService.recommendRecipes(USER_ID, SPACE_ID);

        assertEquals(1, result.recipes().size());
        assertEquals("番茄炒蛋", result.recipes().get(0).recipeName());
        verify(aiProvider).recommendRecipes(anyList(), anyList());
    }

    @Test
    void recommendRecipes_emptyRecipesWhenNoData() {
        when(inventoryItemMapper.selectList(any())).thenReturn(java.util.List.of());
        when(recipeMapper.selectList(any())).thenReturn(java.util.List.of());

        RecipeRecommendationResponse expected = new RecipeRecommendationResponse(java.util.List.of());
        when(aiProvider.recommendRecipes(anyList(), anyList())).thenReturn(expected);

        RecipeRecommendationResponse result = aiService.recommendRecipes(USER_ID, SPACE_ID);

        assertTrue(result.recipes().isEmpty());
    }

    // --- draftShoppingListFromMealPlan ---

    @Test
    void draftShoppingListFromMealPlan_returnsProviderDraft() {
        MealPlan mealPlan = new MealPlan();
        mealPlan.setRecipeId(20L);
        mealPlan.setPlannedDate(LocalDate.of(2026, 6, 22));
        Recipe recipe = new Recipe();
        recipe.setId(20L);
        recipe.setName("番茄炒蛋");
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setName("鸡蛋");

        ShoppingDraftResponse expected = new ShoppingDraftResponse(
                "饮食计划采购清单",
                null,
                java.util.List.of(new ShoppingDraftResponse.ShoppingDraftItem(
                        "番茄", BigDecimal.ONE, "个", null)),
                true,
                "2026-06-22 lunch 番茄炒蛋",
                "已根据饮食计划和当前库存生成缺口清单，请确认数量和单位后创建。"
        );

        when(mealPlanMapper.selectList(any())).thenReturn(java.util.List.of(mealPlan));
        when(recipeMapper.selectList(any())).thenReturn(java.util.List.of(recipe));
        when(inventoryItemMapper.selectList(any())).thenReturn(java.util.List.of(inventoryItem));
        when(aiProvider.draftShoppingListFromMealPlan(anyList(), anyList(), anyList())).thenReturn(expected);

        ShoppingDraftResponse result = aiService.draftShoppingListFromMealPlan(
                USER_ID, SPACE_ID, LocalDate.of(2026, 6, 22), LocalDate.of(2026, 6, 28));

        assertEquals("饮食计划采购清单", result.listName());
        assertEquals(1, result.items().size());
        assertEquals("番茄", result.items().get(0).name());
        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
        verify(mealPlanMapper).selectList(any());
        verify(recipeMapper).selectList(any());
        verify(inventoryItemMapper).selectList(any());
        verify(aiProvider).draftShoppingListFromMealPlan(anyList(), anyList(), anyList());
        verify(aiCallLogService).recordSuccess(eq(USER_ID), eq(SPACE_ID), anyString(),
                eq("meal_plan_shopping_draft"), isNull(), anyMap(), anyMap(), anyLong());
    }

    @Test
    void draftShoppingListFromMealPlan_skipsRecipeQueryWhenNoMealPlans() {
        ShoppingDraftResponse expected = new ShoppingDraftResponse(
                "饮食计划采购清单", null, java.util.List.of(), true, null,
                "当前日期范围内没有饮食计划，请先安排菜谱。"
        );

        when(mealPlanMapper.selectList(any())).thenReturn(java.util.List.of());
        when(inventoryItemMapper.selectList(any())).thenReturn(java.util.List.of());
        when(aiProvider.draftShoppingListFromMealPlan(anyList(), anyList(), anyList())).thenReturn(expected);

        ShoppingDraftResponse result = aiService.draftShoppingListFromMealPlan(
                USER_ID, SPACE_ID, LocalDate.of(2026, 6, 22), LocalDate.of(2026, 6, 28));

        assertTrue(result.needsReview());
        assertTrue(result.items().isEmpty());
        verify(recipeMapper, never()).selectList(any());
    }

    @Test
    void draftShoppingListFromMealPlan_throwsWhenEndDateBeforeStartDate() {
        assertThrows(BusinessException.class, () -> aiService.draftShoppingListFromMealPlan(
                USER_ID, SPACE_ID, LocalDate.of(2026, 6, 28), LocalDate.of(2026, 6, 22)));
    }

    @Test
    void listCallLogs_requiresMembershipAndDelegatesToLogService() {
        AiCallLogResponse log = new AiCallLogResponse(
                100L, USER_ID, SPACE_ID, "mock", "parse_todo", null,
                "{\"inputLength\":3}", "{\"taskCount\":1}", "success",
                6L, null, LocalDateTime.now());
        when(aiCallLogService.listLogs(SPACE_ID, "parse_todo", "success", 20))
                .thenReturn(java.util.List.of(log));

        java.util.List<AiCallLogResponse> result = aiService.listCallLogs(
                USER_ID, SPACE_ID, "parse_todo", "success", 20);

        assertEquals(1, result.size());
        assertEquals("parse_todo", result.get(0).scenario());
        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
        verify(aiCallLogService).listLogs(SPACE_ID, "parse_todo", "success", 20);
    }
}
