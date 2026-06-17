package com.lifepilot.space.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddMemberRequest(
        @NotBlank @Email String email,
        @Size(max = 32) String role
) {
}