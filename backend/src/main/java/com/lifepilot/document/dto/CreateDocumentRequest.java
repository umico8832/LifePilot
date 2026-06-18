package com.lifepilot.document.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDocumentRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 50) String type,
        @Size(max = 255) String issuer,
        LocalDate documentDate,
        LocalDate expireAt,
        @Size(max = 255) String storageLocation,
        String metadataJson
) {
}