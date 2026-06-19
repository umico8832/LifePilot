package com.lifepilot.recipe;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.common.BusinessException;
import com.lifepilot.recipe.dto.CreateRecipeRequest;
import com.lifepilot.recipe.dto.RecipeResponse;
import com.lifepilot.recipe.dto.UpdateRecipeRequest;
import com.lifepilot.space.HouseholdService;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTests {

    @Mock
    private RecipeMapper recipeMapper;
    @Mock
    private HouseholdService householdService;

    @InjectMocks
    private RecipeService recipeService;

    private static final Long USER_ID = 1L;
    private static final Long SPACE_ID = 10L;
    private static final Long RECIPE_ID = 600L;

    // --- createRecipe ---

    @Test
    void createRecipe_success() {
        CreateRecipeRequest request = new CreateRecipeRequest(
                "番茄炒蛋", "简单家常菜",
                "[{\"name\":\"番茄\",\"amount\":\"2个\"}]",
                "[\"切番茄\",\"打蛋\",\"炒\"]"
        );

        RecipeResponse response = recipeService.createRecipe(USER_ID, SPACE_ID, request);

        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
        verify(recipeMapper).insert((Recipe) any());
        assertNotNull(response);
    }

    @Test
    void createRecipe_nonMember_throwsException() {
        doThrow(new BusinessException("FORBIDDEN", "Not a member"))
                .when(householdService).requireSpaceMembership(USER_ID, SPACE_ID);

        CreateRecipeRequest request = new CreateRecipeRequest("菜", null, null, null);

        assertThrows(BusinessException.class,
                () -> recipeService.createRecipe(USER_ID, SPACE_ID, request));
        verify(recipeMapper, never()).insert((Recipe) any());
    }

    // --- listRecipes ---

    @Test
    void listRecipes_returnsResults() {
        Recipe recipe = new Recipe();
        recipe.setId(RECIPE_ID);
        recipe.setHouseholdId(SPACE_ID);
        recipe.setName("番茄炒蛋");
        recipe.setCreatedAt(LocalDateTime.now());

        when(recipeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(recipe));

        List<RecipeResponse> result = recipeService.listRecipes(USER_ID, SPACE_ID);

        assertEquals(1, result.size());
    }

    @Test
    void listRecipes_empty_returnsEmptyList() {
        when(recipeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        List<RecipeResponse> result = recipeService.listRecipes(USER_ID, SPACE_ID);

        assertTrue(result.isEmpty());
    }

    // --- getRecipe ---

    @Test
    void getRecipe_found() {
        Recipe recipe = new Recipe();
        recipe.setId(RECIPE_ID);
        recipe.setHouseholdId(SPACE_ID);
        recipe.setName("番茄炒蛋");

        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(recipe);

        RecipeResponse result = recipeService.getRecipe(USER_ID, SPACE_ID, RECIPE_ID);

        assertNotNull(result);
    }

    @Test
    void getRecipe_notFound_throwsException() {
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> recipeService.getRecipe(USER_ID, SPACE_ID, RECIPE_ID));
    }

    @Test
    void getRecipe_wrongSpace_throwsException() {
        Recipe recipe = new Recipe();
        recipe.setId(RECIPE_ID);
        recipe.setHouseholdId(999L);

        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(recipe);

        assertThrows(BusinessException.class,
                () -> recipeService.getRecipe(USER_ID, SPACE_ID, RECIPE_ID));
    }

    // --- updateRecipe ---

    @Test
    void updateRecipe_success() {
        Recipe recipe = new Recipe();
        recipe.setId(RECIPE_ID);
        recipe.setHouseholdId(SPACE_ID);
        recipe.setName("旧菜名");

        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(recipe);

        UpdateRecipeRequest request = new UpdateRecipeRequest(
                "新菜名", "新描述", "[{\"name\":\"鸡蛋\"}]", "[\"打蛋\"]"
        );

        RecipeResponse result = recipeService.updateRecipe(USER_ID, SPACE_ID, RECIPE_ID, request);

        assertNotNull(result);
        verify(recipeMapper).updateById((Recipe) any());
    }

    @Test
    void updateRecipe_notFound_throwsException() {
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(null);

        UpdateRecipeRequest request = new UpdateRecipeRequest(null, null, null, null);

        assertThrows(BusinessException.class,
                () -> recipeService.updateRecipe(USER_ID, SPACE_ID, RECIPE_ID, request));
        verify(recipeMapper, never()).updateById((Recipe) any());
    }

    // --- deleteRecipe ---

    @Test
    void deleteRecipe_success() {
        Recipe recipe = new Recipe();
        recipe.setId(RECIPE_ID);
        recipe.setHouseholdId(SPACE_ID);

        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(recipe);

        recipeService.deleteRecipe(USER_ID, SPACE_ID, RECIPE_ID);

        verify(recipeMapper).deleteById((Long) eq(RECIPE_ID));
    }

    @Test
    void deleteRecipe_notFound_throwsException() {
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> recipeService.deleteRecipe(USER_ID, SPACE_ID, RECIPE_ID));
        verify(recipeMapper, never()).deleteById((Long) any());
    }
}