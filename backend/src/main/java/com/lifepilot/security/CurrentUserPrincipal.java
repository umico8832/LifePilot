package com.lifepilot.security;

public record CurrentUserPrincipal(
        Long id,
        String email,
        String displayName
) {
}

