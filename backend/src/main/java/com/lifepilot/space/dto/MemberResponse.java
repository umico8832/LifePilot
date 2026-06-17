package com.lifepilot.space.dto;

import java.time.LocalDateTime;

import com.lifepilot.space.HouseholdMember;

public record MemberResponse(
        Long id,
        Long householdId,
        Long userId,
        String email,
        String displayName,
        String role,
        String status,
        LocalDateTime createdAt
) {
    public static MemberResponse from(HouseholdMember member, String email, String displayName) {
        return new MemberResponse(
                member.getId(),
                member.getHouseholdId(),
                member.getUserId(),
                email,
                displayName,
                member.getRole(),
                member.getStatus(),
                member.getCreatedAt()
        );
    }
}