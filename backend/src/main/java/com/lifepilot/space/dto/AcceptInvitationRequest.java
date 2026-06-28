package com.lifepilot.space.dto;

import jakarta.validation.constraints.NotBlank;

public record AcceptInvitationRequest(
        @NotBlank(message = "token is required")
        String token
) {
}
