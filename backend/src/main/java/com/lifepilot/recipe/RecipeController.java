package com.lifepilot.recipe;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lifepilot.common.ApiResponse;
import com.lifepilot.common.BusinessException;
import com.lifepilot.recipe.dto.CreateRecipeRequest;
import com.lifepilot.recipe.dto.RecipeResponse;
import com.lifepilot.recipe.dto.UpdateRecipeRequest;
import com.lifepilot.security.CurrentUserPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/spaces/{spaceId}/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping
    public ApiResponse<RecipeResponse> createRecipe(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @Valid @RequestBody CreateRecipeRequest request) {
        requireAuth(principal);
        return ApiResponse.ok(recipeService.createRecipe(principal.id(), spaceId, request));
    }

    @GetMapping
    public ApiResponse<List<RecipeResponse>> listRecipes(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId) {
        requireAuth(principal);
        return ApiResponse.ok(recipeService.listRecipes(principal.id(), spaceId));
    }

    @GetMapping("/{id}")
    public ApiResponse<RecipeResponse> getRecipe(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long id) {
        requireAuth(principal);
        return ApiResponse.ok(recipeService.getRecipe(principal.id(), spaceId, id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<RecipeResponse> updateRecipe(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecipeRequest request) {
        requireAuth(principal);
        return ApiResponse.ok(recipeService.updateRecipe(principal.id(), spaceId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRecipe(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long id) {
        requireAuth(principal);
        recipeService.deleteRecipe(principal.id(), spaceId, id);
        return ApiResponse.ok(null);
    }

    private void requireAuth(CurrentUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Authentication required");
        }
    }
}