package com.lifepilot.shopping;

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
import com.lifepilot.security.CurrentUserPrincipal;
import com.lifepilot.shopping.dto.CreateShoppingItemRequest;
import com.lifepilot.shopping.dto.CreateShoppingListRequest;
import com.lifepilot.shopping.dto.ShoppingItemResponse;
import com.lifepilot.shopping.dto.ShoppingListResponse;
import com.lifepilot.shopping.dto.UpdateShoppingItemRequest;
import com.lifepilot.shopping.dto.UpdateShoppingListRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/spaces/{spaceId}/shopping-lists")
public class ShoppingController {

    private final ShoppingService shoppingService;

    public ShoppingController(ShoppingService shoppingService) {
        this.shoppingService = shoppingService;
    }

    // ---- Shopping List endpoints ----

    @PostMapping
    public ApiResponse<ShoppingListResponse> createList(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @Valid @RequestBody CreateShoppingListRequest request) {
        requireAuth(principal);
        return ApiResponse.ok(shoppingService.createList(principal.id(), spaceId, request));
    }

    @GetMapping
    public ApiResponse<List<ShoppingListResponse>> listLists(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId) {
        requireAuth(principal);
        return ApiResponse.ok(shoppingService.listLists(principal.id(), spaceId));
    }

    @GetMapping("/{id}")
    public ApiResponse<ShoppingListResponse> getList(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long id) {
        requireAuth(principal);
        return ApiResponse.ok(shoppingService.getList(principal.id(), spaceId, id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<ShoppingListResponse> updateList(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateShoppingListRequest request) {
        requireAuth(principal);
        return ApiResponse.ok(shoppingService.updateList(principal.id(), spaceId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteList(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long id) {
        requireAuth(principal);
        shoppingService.deleteList(principal.id(), spaceId, id);
        return ApiResponse.ok(null);
    }

    // ---- Shopping Item endpoints ----

    @PostMapping("/{listId}/items")
    public ApiResponse<ShoppingItemResponse> addItem(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long listId,
            @Valid @RequestBody CreateShoppingItemRequest request) {
        requireAuth(principal);
        return ApiResponse.ok(shoppingService.addItem(principal.id(), spaceId, listId, request));
    }

    @PatchMapping("/{listId}/items/{itemId}")
    public ApiResponse<ShoppingItemResponse> updateItem(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long listId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateShoppingItemRequest request) {
        requireAuth(principal);
        return ApiResponse.ok(shoppingService.updateItem(principal.id(), spaceId, listId, itemId, request));
    }

    @DeleteMapping("/{listId}/items/{itemId}")
    public ApiResponse<Void> deleteItem(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long listId,
            @PathVariable Long itemId) {
        requireAuth(principal);
        shoppingService.deleteItem(principal.id(), spaceId, listId, itemId);
        return ApiResponse.ok(null);
    }

    private void requireAuth(CurrentUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Authentication required");
        }
    }
}