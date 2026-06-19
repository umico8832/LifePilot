package com.lifepilot.recipe;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.common.BusinessException;
import com.lifepilot.recipe.dto.CreateMealPlanRequest;
import com.lifepilot.recipe.dto.MealPlanResponse;
import com.lifepilot.recipe.dto.UpdateMealPlanRequest;
import com.lifepilot.space.HouseholdService;

@ExtendWith(MockitoExtension.class)
class MealPlanServiceTests {

    @Mock
    private MealPlanMapper mealPlanMapper;
    @Mock
    private RecipeMapper recipeMapper;
    @Mock
    private HouseholdService householdService;

    @InjectMocks
    private MealPlanService mealPlanService;

    private static final Long USER_ID = 1L;
    private static final Long SPACE_ID = 10L;
    private static final Long RECIPE_ID = 600L;
    private static final Long MEAL_PLAN_ID = 800L;

    private Recipe buildRecipe() {
        Recipe recipe = new Recipe();
        recipe.setId(RECIPE_ID);
        recipe.setHouseholdId(SPACE_ID);
        recipe.setName("番茄炒蛋");
        return recipe;
    }

    private MealPlan buildMealPlan() {
        MealPlan mp = new MealPlan();
        mp.setId(MEAL_PLAN_ID);
        mp.setHouseholdId(SPACE_ID);
        mp.setRecipeId(RECIPE_ID);
        mp.setPlannedDate(LocalDate.now());
        mp.setMealType("lunch");
        mp.setCreatedBy(USER_ID);
        mp.setCreatedAt(LocalDateTime.now());
        mp.setUpdatedAt(LocalDateTime.now());
        return mp;
    }

    // --- createMealPlan ---

    @Test
    void createMealPlan_success() {
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(buildRecipe());

        CreateMealPlanRequest request = new CreateMealPlanRequest(
                RECIPE_ID, LocalDate.now(), "lunch", "test note"
        );

        MealPlanResponse response = mealPlanService.createMealPlan(USER_ID, SPACE_ID, request);

        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
        verify(mealPlanMapper).insert((MealPlan) any());
        assertNotNull(response);
        assertEquals("番茄炒蛋", response.recipeName());
    }

    @Test
    void createMealPlan_nonMember_throwsException() {
        doThrow(new BusinessException("FORBIDDEN", "Not a member"))
                .when(householdService).requireSpaceMembership(USER_ID, SPACE_ID);

        CreateMealPlanRequest request = new CreateMealPlanRequest(
                RECIPE_ID, LocalDate.now(), "lunch", null
        );

        assertThrows(BusinessException.class,
                () -> mealPlanService.createMealPlan(USER_ID, SPACE_ID, request));
        verify(mealPlanMapper, never()).insert((MealPlan) any());
    }

    @Test
    void createMealPlan_recipeNotFound_throwsException() {
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(null);

        CreateMealPlanRequest request = new CreateMealPlanRequest(
                RECIPE_ID, LocalDate.now(), "lunch", null
        );

        assertThrows(BusinessException.class,
                () -> mealPlanService.createMealPlan(USER_ID, SPACE_ID, request));
        verify(mealPlanMapper, never()).insert((MealPlan) any());
    }

    @Test
    void createMealPlan_recipeWrongSpace_throwsException() {
        Recipe recipe = buildRecipe();
        recipe.setHouseholdId(999L);
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(recipe);

        CreateMealPlanRequest request = new CreateMealPlanRequest(
                RECIPE_ID, LocalDate.now(), "lunch", null
        );

        assertThrows(BusinessException.class,
                () -> mealPlanService.createMealPlan(USER_ID, SPACE_ID, request));
    }

    // --- listMealPlans ---

    @Test
    void listMealPlans_returnsResults() {
        MealPlan mp = buildMealPlan();
        when(mealPlanMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(mp));
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(buildRecipe());

        List<MealPlanResponse> result = mealPlanService.listMealPlans(
                USER_ID, SPACE_ID, LocalDate.now().minusDays(7), LocalDate.now().plusDays(7));

        assertEquals(1, result.size());
        assertEquals("番茄炒蛋", result.get(0).recipeName());
    }

    @Test
    void listMealPlans_empty_returnsEmptyList() {
        when(mealPlanMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        List<MealPlanResponse> result = mealPlanService.listMealPlans(
                USER_ID, SPACE_ID, null, null);

        assertTrue(result.isEmpty());
    }

    // --- getMealPlan ---

    @Test
    void getMealPlan_found() {
        MealPlan mp = buildMealPlan();
        when(mealPlanMapper.selectById(MEAL_PLAN_ID)).thenReturn(mp);
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(buildRecipe());

        MealPlanResponse result = mealPlanService.getMealPlan(USER_ID, SPACE_ID, MEAL_PLAN_ID);

        assertNotNull(result);
        assertEquals("番茄炒蛋", result.recipeName());
    }

    @Test
    void getMealPlan_notFound_throwsException() {
        when(mealPlanMapper.selectById(MEAL_PLAN_ID)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> mealPlanService.getMealPlan(USER_ID, SPACE_ID, MEAL_PLAN_ID));
    }

    @Test
    void getMealPlan_wrongSpace_throwsException() {
        MealPlan mp = buildMealPlan();
        mp.setHouseholdId(999L);
        when(mealPlanMapper.selectById(MEAL_PLAN_ID)).thenReturn(mp);

        assertThrows(BusinessException.class,
                () -> mealPlanService.getMealPlan(USER_ID, SPACE_ID, MEAL_PLAN_ID));
    }

    // --- updateMealPlan ---

    @Test
    void updateMealPlan_success() {
        MealPlan mp = buildMealPlan();
        when(mealPlanMapper.selectById(MEAL_PLAN_ID)).thenReturn(mp);
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(buildRecipe());

        UpdateMealPlanRequest request = new UpdateMealPlanRequest(
                null, LocalDate.now().plusDays(1), "dinner", "updated note"
        );

        MealPlanResponse result = mealPlanService.updateMealPlan(USER_ID, SPACE_ID, MEAL_PLAN_ID, request);

        assertNotNull(result);
        verify(mealPlanMapper).updateById((MealPlan) any());
    }

    @Test
    void updateMealPlan_notFound_throwsException() {
        when(mealPlanMapper.selectById(MEAL_PLAN_ID)).thenReturn(null);

        UpdateMealPlanRequest request = new UpdateMealPlanRequest(null, null, null, null);

        assertThrows(BusinessException.class,
                () -> mealPlanService.updateMealPlan(USER_ID, SPACE_ID, MEAL_PLAN_ID, request));
        verify(mealPlanMapper, never()).updateById((MealPlan) any());
    }

    // --- deleteMealPlan ---

    @Test
    void deleteMealPlan_success() {
        MealPlan mp = buildMealPlan();
        when(mealPlanMapper.selectById(MEAL_PLAN_ID)).thenReturn(mp);

        mealPlanService.deleteMealPlan(USER_ID, SPACE_ID, MEAL_PLAN_ID);

        verify(mealPlanMapper).deleteById((Long) eq(MEAL_PLAN_ID));
    }

    @Test
    void deleteMealPlan_notFound_throwsException() {
        when(mealPlanMapper.selectById(MEAL_PLAN_ID)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> mealPlanService.deleteMealPlan(USER_ID, SPACE_ID, MEAL_PLAN_ID));
        verify(mealPlanMapper, never()).deleteById((Long) any());
    }
}