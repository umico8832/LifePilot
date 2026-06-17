package com.lifepilot.statistics;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifepilot.common.ApiResponse;
import com.lifepilot.common.BusinessException;
import com.lifepilot.security.CurrentUserPrincipal;
import com.lifepilot.statistics.dto.FinanceMonthlyResponse;
import com.lifepilot.statistics.dto.OverviewResponse;

@RestController
@RequestMapping("/api/spaces/{spaceId}/statistics")
public class StatisticController {

    private final StatisticService statisticService;

    public StatisticController(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @GetMapping("/overview")
    public ApiResponse<OverviewResponse> overview(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId) {
        requireAuth(principal);
        return ApiResponse.ok(statisticService.getOverview(principal.id(), spaceId));
    }

    @GetMapping("/finance/monthly")
    public ApiResponse<FinanceMonthlyResponse> financeMonthly(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @RequestParam int year,
            @RequestParam int month) {
        requireAuth(principal);
        return ApiResponse.ok(statisticService.getFinanceMonthly(principal.id(), spaceId, year, month));
    }

    private void requireAuth(CurrentUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Authentication required");
        }
    }
}