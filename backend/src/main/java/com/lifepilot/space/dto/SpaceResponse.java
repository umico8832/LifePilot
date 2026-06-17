package com.lifepilot.space.dto;

import java.time.LocalDateTime;

import com.lifepilot.space.Household;

public record SpaceResponse(
        Long id,
        String name,
        String type,
        Long ownerUserId,
        String memberRole,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SpaceResponse from(Household household, String memberRole) {
        return new SpaceResponse(
                household.getId(),
                household.getName(),
                household.getType(),
                household.getOwnerUserId(),
                memberRole,
                household.getCreatedAt(),
                household.getUpdatedAt()
        );
    }
}