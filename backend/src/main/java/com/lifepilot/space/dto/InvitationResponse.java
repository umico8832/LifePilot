package com.lifepilot.space.dto;

import java.time.LocalDateTime;

import com.lifepilot.space.HouseholdInvitation;

public record InvitationResponse(
        Long id,
        Long householdId,
        Long invitedBy,
        String targetEmail,
        String role,
        String status,
        LocalDateTime expiresAt,
        LocalDateTime acceptedAt,
        Long acceptedBy,
        LocalDateTime createdAt,
        String token
) {
    public static InvitationResponse from(HouseholdInvitation invitation, String token) {
        return new InvitationResponse(
                invitation.getId(),
                invitation.getHouseholdId(),
                invitation.getInvitedBy(),
                invitation.getTargetEmail(),
                invitation.getRole(),
                invitation.getStatus(),
                invitation.getExpiresAt(),
                invitation.getAcceptedAt(),
                invitation.getAcceptedBy(),
                invitation.getCreatedAt(),
                token
        );
    }
}
