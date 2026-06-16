package com.lifepilot.auth.dto;

import com.lifepilot.user.dto.CurrentUserResponse;

public record AuthResponse(
        String tokenType,
        String accessToken,
        CurrentUserResponse user
) {
}

