package com.lifepilot.ai;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifepilot.ai.dto.MonthlyReportResponse;
import com.lifepilot.ai.dto.AiCallLogResponse;
import com.lifepilot.ai.dto.AiCallLogSummaryResponse;
import com.lifepilot.ai.dto.ParseShoppingRequest;
import com.lifepilot.ai.dto.ParseTodoRequest;
import com.lifepilot.ai.dto.ParseTransactionRequest;
import com.lifepilot.ai.dto.RecipeRecommendationResponse;
import com.lifepilot.ai.dto.ShoppingDraftResponse;
import com.lifepilot.ai.dto.TodoDraftResponse;
import com.lifepilot.ai.dto.TransactionDraftResponse;
import com.lifepilot.common.ApiResponse;
import com.lifepilot.common.BusinessException;
import com.lifepilot.security.CurrentUserPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/spaces/{spaceId}/parse-transaction")
    public ApiResponse<TransactionDraftResponse> parseTransaction(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @Valid @RequestBody ParseTransactionRequest request) {
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Authentication required");
        }
        return ApiResponse.ok(aiService.parseTransaction(principal.id(), spaceId, request));
    }

    @PostMapping("/spaces/{spaceId}/parse-shopping")
    public ApiResponse<ShoppingDraftResponse> parseShoppingList(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @Valid @RequestBody ParseShoppingRequest request) {
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Authentication required");
        }
        return ApiResponse.ok(aiService.parseShoppingList(principal.id(), spaceId, request));
    }

    @PostMapping("/spaces/{spaceId}/parse-todo")
    public ApiResponse<TodoDraftResponse> parseTodo(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @Valid @RequestBody ParseTodoRequest request) {
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Authentication required");
        }
        return ApiResponse.ok(aiService.parseTodo(principal.id(), spaceId, request));
    }

    @GetMapping("/spaces/{spaceId}/monthly-report")
    public ApiResponse<MonthlyReportResponse> generateMonthlyReport(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @RequestParam int year,
            @RequestParam int month) {
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Authentication required");
        }
        return ApiResponse.ok(aiService.generateMonthlyReport(principal.id(), spaceId, year, month));
    }

    @GetMapping("/spaces/{spaceId}/recommend-recipes")
    public ApiResponse<RecipeRecommendationResponse> recommendRecipes(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId) {
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Authentication required");
        }
        return ApiResponse.ok(aiService.recommendRecipes(principal.id(), spaceId));
    }

    @GetMapping("/spaces/{spaceId}/meal-plan-shopping-draft")
    public ApiResponse<ShoppingDraftResponse> draftShoppingListFromMealPlan(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Authentication required");
        }
        return ApiResponse.ok(aiService.draftShoppingListFromMealPlan(principal.id(), spaceId, startDate, endDate));
    }

    @GetMapping("/spaces/{spaceId}/call-logs")
    public ApiResponse<List<AiCallLogResponse>> listCallLogs(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @RequestParam(required = false) String scenario,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer limit) {
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Authentication required");
        }
        return ApiResponse.ok(aiService.listCallLogs(principal.id(), spaceId, scenario, status, limit));
    }

    @GetMapping("/spaces/{spaceId}/call-logs/summary")
    public ApiResponse<AiCallLogSummaryResponse> summarizeCallLogs(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @RequestParam(required = false) Integer days) {
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Authentication required");
        }
        return ApiResponse.ok(aiService.summarizeCallLogs(principal.id(), spaceId, days));
    }
}
