package com.lifepilot.finance;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lifepilot.common.ApiResponse;
import com.lifepilot.common.BusinessException;
import com.lifepilot.finance.dto.CategoryResponse;
import com.lifepilot.finance.dto.CreateCategoryRequest;
import com.lifepilot.security.CurrentUserPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/spaces/{spaceId}/transaction-categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ApiResponse<CategoryResponse> create(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @Valid @RequestBody CreateCategoryRequest request) {
        requireAuth(principal);
        return ApiResponse.ok(categoryService.create(principal.id(), spaceId, request));
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>> list(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId) {
        requireAuth(principal);
        return ApiResponse.ok(categoryService.list(principal.id(), spaceId));
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long categoryId) {
        requireAuth(principal);
        categoryService.delete(principal.id(), spaceId, categoryId);
        return ApiResponse.ok(null);
    }

    private void requireAuth(CurrentUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Authentication required");
        }
    }
}