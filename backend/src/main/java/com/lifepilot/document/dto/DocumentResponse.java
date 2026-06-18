package com.lifepilot.document.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.lifepilot.document.DocumentRecord;

public record DocumentResponse(
        Long id,
        Long householdId,
        String title,
        String type,
        String issuer,
        LocalDate documentDate,
        LocalDate expireAt,
        boolean expiringSoon,
        String storageLocation,
        String metadataJson,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DocumentResponse from(DocumentRecord doc) {
        boolean soon = doc.getExpireAt() != null
                && doc.getExpireAt().isBefore(LocalDate.now().plusDays(30))
                && doc.getExpireAt().isAfter(LocalDate.now().minusDays(1));

        return new DocumentResponse(
                doc.getId(),
                doc.getHouseholdId(),
                doc.getTitle(),
                doc.getType(),
                doc.getIssuer(),
                doc.getDocumentDate(),
                doc.getExpireAt(),
                soon,
                doc.getStorageLocation(),
                doc.getMetadataJson(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }
}