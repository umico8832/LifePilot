package com.lifepilot.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.lifepilot.common.ApiResponse;
import com.lifepilot.common.BusinessException;
import com.lifepilot.security.CurrentUserPrincipal;
import com.lifepilot.user.dto.CurrentUserResponse;
import com.lifepilot.user.dto.ProfileUpdateRequest;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(@AuthenticationPrincipal CurrentUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Authentication required");
        }

        return ApiResponse.ok(CurrentUserResponse.from(userService.requireById(principal.id())));
    }

    @PutMapping("/me")
    public ApiResponse<CurrentUserResponse> updateProfile(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @Valid @RequestBody ProfileUpdateRequest request) {
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Authentication required");
        }

        return ApiResponse.ok(CurrentUserResponse.from(
                userService.updateProfile(principal.id(), request.getDisplayName(), request.getAvatarUrl())));
    }
}

