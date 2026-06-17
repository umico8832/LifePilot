package com.lifepilot.space;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.lifepilot.space.dto.AddMemberRequest;
import com.lifepilot.space.dto.CreateSpaceRequest;
import com.lifepilot.space.dto.MemberResponse;
import com.lifepilot.space.dto.SpaceResponse;
import com.lifepilot.space.dto.UpdateMemberRoleRequest;
import com.lifepilot.space.dto.UpdateSpaceRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/spaces")
public class HouseholdController {

    private final HouseholdService householdService;

    public HouseholdController(HouseholdService householdService) {
        this.householdService = householdService;
    }

    @GetMapping
    public ApiResponse<List<SpaceResponse>> listSpaces(
            @AuthenticationPrincipal CurrentUserPrincipal principal) {
        requireAuth(principal);
        return ApiResponse.ok(householdService.listSpaces(principal.id()));
    }

    @PostMapping
    public ApiResponse<SpaceResponse> createSpace(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @Valid @RequestBody CreateSpaceRequest request) {
        requireAuth(principal);
        return ApiResponse.ok(householdService.createSpace(principal.id(), request));
    }

    @GetMapping("/{spaceId}")
    public ApiResponse<SpaceResponse> getSpace(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId) {
        requireAuth(principal);
        return ApiResponse.ok(householdService.getSpace(principal.id(), spaceId));
    }

    @PatchMapping("/{spaceId}")
    public ApiResponse<SpaceResponse> updateSpace(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @Valid @RequestBody UpdateSpaceRequest request) {
        requireAuth(principal);
        return ApiResponse.ok(householdService.updateSpace(principal.id(), spaceId, request));
    }

    @GetMapping("/{spaceId}/members")
    public ApiResponse<List<MemberResponse>> listMembers(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId) {
        requireAuth(principal);
        return ApiResponse.ok(householdService.listMembers(principal.id(), spaceId));
    }

    @PostMapping("/{spaceId}/members")
    public ApiResponse<MemberResponse> addMember(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @Valid @RequestBody AddMemberRequest request) {
        requireAuth(principal);
        return ApiResponse.ok(householdService.addMember(principal.id(), spaceId, request));
    }

    @PatchMapping("/{spaceId}/members/{memberId}")
    public ApiResponse<MemberResponse> updateMemberRole(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateMemberRoleRequest request) {
        requireAuth(principal);
        return ApiResponse.ok(householdService.updateMemberRole(principal.id(), spaceId, memberId, request.role()));
    }

    private void requireAuth(CurrentUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Authentication required");
        }
    }
}