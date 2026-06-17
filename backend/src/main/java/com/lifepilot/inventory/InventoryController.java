package com.lifepilot.inventory;

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
import com.lifepilot.inventory.dto.CreateInventoryItemRequest;
import com.lifepilot.inventory.dto.InventoryItemResponse;
import com.lifepilot.inventory.dto.UpdateInventoryItemRequest;
import com.lifepilot.security.CurrentUserPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/spaces/{spaceId}/inventory-items")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ApiResponse<InventoryItemResponse> createItem(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @Valid @RequestBody CreateInventoryItemRequest request) {
        requireAuth(principal);
        return ApiResponse.ok(inventoryService.createItem(principal.id(), spaceId, request));
    }

    @GetMapping
    public ApiResponse<List<InventoryItemResponse>> listItems(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId) {
        requireAuth(principal);
        return ApiResponse.ok(inventoryService.listItems(principal.id(), spaceId));
    }

    @GetMapping("/alerts")
    public ApiResponse<List<InventoryItemResponse>> listAlerts(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId) {
        requireAuth(principal);
        return ApiResponse.ok(inventoryService.listLowStockAlerts(principal.id(), spaceId));
    }

    @GetMapping("/{id}")
    public ApiResponse<InventoryItemResponse> getItem(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long id) {
        requireAuth(principal);
        return ApiResponse.ok(inventoryService.getItem(principal.id(), spaceId, id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<InventoryItemResponse> updateItem(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateInventoryItemRequest request) {
        requireAuth(principal);
        return ApiResponse.ok(inventoryService.updateItem(principal.id(), spaceId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteItem(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long id) {
        requireAuth(principal);
        inventoryService.deleteItem(principal.id(), spaceId, id);
        return ApiResponse.ok(null);
    }

    private void requireAuth(CurrentUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Authentication required");
        }
    }
}