package com.lifepilot.recipe;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.common.BusinessException;
import com.lifepilot.recipe.dto.CreateMealPlanRequest;
import com.lifepilot.recipe.dto.MealPlanResponse;
import com.lifepilot.recipe.dto.UpdateMealPlanRequest;
import com.lifepilot.space.HouseholdService;

@Service
public class MealPlanService {

    private final MealPlanMapper mealPlanMapper;
    private final RecipeMapper recipeMapper;
    private final HouseholdService householdService;

    public MealPlanService(MealPlanMapper mealPlanMapper, RecipeMapper recipeMapper, HouseholdService householdService) {
        this.mealPlanMapper = mealPlanMapper;
        this.recipeMapper = recipeMapper;
        this.householdService = householdService;
    }

    @Transactional
    public MealPlanResponse createMealPlan(Long userId, Long spaceId, CreateMealPlanRequest request) {
        householdService.requireSpaceMembership(userId, spaceId);

        Recipe recipe = recipeMapper.selectById(request.recipeId());
        if (recipe == null || !recipe.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Recipe not found");
        }

        MealPlan mealPlan = new MealPlan();
        mealPlan.setHouseholdId(spaceId);
        mealPlan.setRecipeId(request.recipeId());
        mealPlan.setPlannedDate(request.plannedDate());
        mealPlan.setMealType(request.mealType());
        mealPlan.setNote(request.note());
        mealPlan.setCreatedBy(userId);

        LocalDateTime now = LocalDateTime.now();
        mealPlan.setCreatedAt(now);
        mealPlan.setUpdatedAt(now);

        mealPlanMapper.insert(mealPlan);
        return MealPlanResponse.from(mealPlan, recipe.getName());
    }

    public List<MealPlanResponse> listMealPlans(Long userId, Long spaceId, LocalDate startDate, LocalDate endDate) {
        householdService.requireSpaceMembership(userId, spaceId);

        LambdaQueryWrapper<MealPlan> wrapper = new LambdaQueryWrapper<MealPlan>()
                .eq(MealPlan::getHouseholdId, spaceId);

        if (startDate != null) {
            wrapper.ge(MealPlan::getPlannedDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(MealPlan::getPlannedDate, endDate);
        }

        wrapper.orderByAsc(MealPlan::getPlannedDate).orderByAsc(MealPlan::getMealType);

        List<MealPlan> mealPlans = mealPlanMapper.selectList(wrapper);
        return mealPlans.stream().map(mp -> {
            Recipe recipe = recipeMapper.selectById(mp.getRecipeId());
            String recipeName = recipe != null ? recipe.getName() : "Unknown";
            return MealPlanResponse.from(mp, recipeName);
        }).collect(Collectors.toList());
    }

    public MealPlanResponse getMealPlan(Long userId, Long spaceId, Long mealPlanId) {
        householdService.requireSpaceMembership(userId, spaceId);

        MealPlan mealPlan = mealPlanMapper.selectById(mealPlanId);
        if (mealPlan == null || !mealPlan.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Meal plan not found");
        }

        Recipe recipe = recipeMapper.selectById(mealPlan.getRecipeId());
        String recipeName = recipe != null ? recipe.getName() : "Unknown";
        return MealPlanResponse.from(mealPlan, recipeName);
    }

    @Transactional
    public MealPlanResponse updateMealPlan(Long userId, Long spaceId, Long mealPlanId, UpdateMealPlanRequest request) {
        householdService.requireSpaceMembership(userId, spaceId);

        MealPlan mealPlan = mealPlanMapper.selectById(mealPlanId);
        if (mealPlan == null || !mealPlan.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Meal plan not found");
        }

        if (request.recipeId() != null) {
            Recipe recipe = recipeMapper.selectById(request.recipeId());
            if (recipe == null || !recipe.getHouseholdId().equals(spaceId)) {
                throw new BusinessException("NOT_FOUND", "Recipe not found");
            }
            mealPlan.setRecipeId(request.recipeId());
        }
        if (request.plannedDate() != null) {
            mealPlan.setPlannedDate(request.plannedDate());
        }
        if (request.mealType() != null) {
            mealPlan.setMealType(request.mealType());
        }
        if (request.note() != null) {
            mealPlan.setNote(request.note());
        }
        mealPlan.setUpdatedAt(LocalDateTime.now());

        mealPlanMapper.updateById(mealPlan);

        Recipe recipe = recipeMapper.selectById(mealPlan.getRecipeId());
        String recipeName = recipe != null ? recipe.getName() : "Unknown";
        return MealPlanResponse.from(mealPlan, recipeName);
    }

    @Transactional
    public void deleteMealPlan(Long userId, Long spaceId, Long mealPlanId) {
        householdService.requireSpaceMembership(userId, spaceId);

        MealPlan mealPlan = mealPlanMapper.selectById(mealPlanId);
        if (mealPlan == null || !mealPlan.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Meal plan not found");
        }

        mealPlanMapper.deleteById(mealPlanId);
    }
}