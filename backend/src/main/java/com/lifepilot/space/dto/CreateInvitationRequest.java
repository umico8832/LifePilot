package com.lifepilot.space.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record CreateInvitationRequest(
        @Email(message = "targetEmail must be a valid email")
        @Size(max = 255, message = "targetEmail must be at most 255 characters")
        String targetEmail,

        String role,

        @Min(value = 1, message = "expiresInDays must be at least 1")
        @Max(value = 30, message = "expiresInDays must be at most 30")
        Integer expiresInDays
) {
}
