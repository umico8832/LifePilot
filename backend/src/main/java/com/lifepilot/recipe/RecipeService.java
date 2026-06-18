package com.lifepilot.recipe;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.common.BusinessException;
import com.lifepilot.recipe.dto.CreateRecipeRequest;
import com.lifepilot.recipe.dto.RecipeResponse;
import com.lifepilot.recipe.dto.UpdateRecipeRequest;
import com.lifepilot.space.HouseholdService;

@Service
public class RecipeService {

    private final RecipeMapper recipeMapper;
    private final HouseholdService householdService;

    public RecipeService(RecipeMapper recipeMapper, HouseholdService householdService) {
        this.recipeMapper = recipeMapper;
        this.householdService = householdService;
    }

    @Transactional
    public RecipeResponse createRecipe(Long userId, Long spaceId, CreateRecipeRequest request) {
        householdService.requireSpaceMembership(userId, spaceId);

        Recipe recipe = new Recipe();
        recipe.setHouseholdId(spaceId);
        recipe.setName(request.name());
        recipe.setDescription(request.description());
        recipe.setIngredientsJson(request.ingredientsJson());
        recipe.setStepsJson(request.stepsJson());
        recipe.setCreatedBy(userId);

        LocalDateTime now = LocalDateTime.now();
        recipe.setCreatedAt(now);
        recipe.setUpdatedAt(now);

        recipeMapper.insert(recipe);
        return RecipeResponse.from(recipe);
    }

    public List<RecipeResponse> listRecipes(Long userId, Long spaceId) {
        householdService.requireSpaceMembership(userId, spaceId);

        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<Recipe>()
                .eq(Recipe::getHouseholdId, spaceId)
                .orderByDesc(Recipe::getCreatedAt);

        List<Recipe> recipes = recipeMapper.selectList(wrapper);
        return recipes.stream().map(RecipeResponse::from).collect(Collectors.toList());
    }

    public RecipeResponse getRecipe(Long userId, Long spaceId, Long recipeId) {
        householdService.requireSpaceMembership(userId, spaceId);

        Recipe recipe = recipeMapper.selectById(recipeId);
        if (recipe == null || !recipe.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Recipe not found");
        }

        return RecipeResponse.from(recipe);
    }

    @Transactional
    public RecipeResponse updateRecipe(Long userId, Long spaceId, Long recipeId, UpdateRecipeRequest request) {
        householdService.requireSpaceMembership(userId, spaceId);

        Recipe recipe = recipeMapper.selectById(recipeId);
        if (recipe == null || !recipe.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Recipe not found");
        }

        if (request.name() != null) recipe.setName(request.name());
        if (request.description() != null) recipe.setDescription(request.description());
        if (request.ingredientsJson() != null) recipe.setIngredientsJson(request.ingredientsJson());
        if (request.stepsJson() != null) recipe.setStepsJson(request.stepsJson());
        recipe.setUpdatedAt(LocalDateTime.now());

        recipeMapper.updateById(recipe);
        return RecipeResponse.from(recipe);
    }

    @Transactional
    public void deleteRecipe(Long userId, Long spaceId, Long recipeId) {
        householdService.requireSpaceMembership(userId, spaceId);

        Recipe recipe = recipeMapper.selectById(recipeId);
        if (recipe == null || !recipe.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Recipe not found");
        }

        recipeMapper.deleteById(recipeId);
    }
}