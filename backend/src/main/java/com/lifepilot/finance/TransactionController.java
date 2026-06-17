package com.lifepilot.finance;

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
import com.lifepilot.finance.dto.CreateTransactionRequest;
import com.lifepilot.finance.dto.TransactionResponse;
import com.lifepilot.finance.dto.UpdateTransactionRequest;
import com.lifepilot.security.CurrentUserPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/spaces/{spaceId}/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ApiResponse<TransactionResponse> create(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @Valid @RequestBody CreateTransactionRequest request) {
        requireAuth(principal);
        return ApiResponse.ok(transactionService.create(principal.id(), spaceId, request));
    }

    @GetMapping
    public ApiResponse<List<TransactionResponse>> list(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId) {
        requireAuth(principal);
        return ApiResponse.ok(transactionService.list(principal.id(), spaceId));
    }

    @GetMapping("/{id}")
    public ApiResponse<TransactionResponse> get(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long id) {
        requireAuth(principal);
        return ApiResponse.ok(transactionService.get(principal.id(), spaceId, id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<TransactionResponse> update(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest request) {
        requireAuth(principal);
        return ApiResponse.ok(transactionService.update(principal.id(), spaceId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long id) {
        requireAuth(principal);
        transactionService.delete(principal.id(), spaceId, id);
        return ApiResponse.ok(null);
    }

    private void requireAuth(CurrentUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Authentication required");
        }
    }
}