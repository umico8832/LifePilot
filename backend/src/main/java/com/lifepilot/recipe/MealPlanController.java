package com.lifepilot.recipe;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifepilot.common.ApiResponse;
import com.lifepilot.common.BusinessException;
import com.lifepilot.recipe.dto.CreateMealPlanRequest;
import com.lifepilot.recipe.dto.MealPlanResponse;
import com.lifepilot.recipe.dto.UpdateMealPlanRequest;
import com.lifepilot.security.CurrentUserPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/spaces/{spaceId}/meal-plans")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    public MealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    @PostMapping
    public ApiResponse<MealPlanResponse> createMealPlan(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @Valid @RequestBody CreateMealPlanRequest request) {
        requireAuth(principal);
        return ApiResponse.ok(mealPlanService.createMealPlan(principal.id(), spaceId, request));
    }

    @GetMapping
    public ApiResponse<List<MealPlanResponse>> listMealPlans(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        requireAuth(principal);
        return ApiResponse.ok(mealPlanService.listMealPlans(principal.id(), spaceId, startDate, endDate));
    }

    @GetMapping("/{id}")
    public ApiResponse<MealPlanResponse> getMealPlan(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long id) {
        requireAuth(principal);
        return ApiResponse.ok(mealPlanService.getMealPlan(principal.id(), spaceId, id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<MealPlanResponse> updateMealPlan(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateMealPlanRequest request) {
        requireAuth(principal);
        return ApiResponse.ok(mealPlanService.updateMealPlan(principal.id(), spaceId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMealPlan(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long id) {
        requireAuth(principal);
        mealPlanService.deleteMealPlan(principal.id(), spaceId, id);
        return ApiResponse.ok(null);
    }

    private void requireAuth(CurrentUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Authentication required");
        }
    }
}