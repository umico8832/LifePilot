package com.lifepilot.user.dto;

import com.lifepilot.user.UserAccount;

public record CurrentUserResponse(
        Long id,
        String email,
        String displayName,
        String avatarUrl
) {
    public static CurrentUserResponse from(UserAccount user) {
        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl()
        );
    }
}

