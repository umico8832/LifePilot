package com.lifepilot.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.lifepilot.finance.TransactionRecord;

public record TransactionResponse(
        Long id,
        Long householdId,
        Long userId,
        Long categoryId,
        String type,
        BigDecimal amount,
        String currency,
        LocalDateTime occurredAt,
        String merchant,
        String note,
        String source,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TransactionResponse from(TransactionRecord record) {
        return new TransactionResponse(
                record.getId(),
                record.getHouseholdId(),
                record.getUserId(),
                record.getCategoryId(),
                record.getType(),
                record.getAmount(),
                record.getCurrency(),
                record.getOccurredAt(),
                record.getMerchant(),
                record.getNote(),
                record.getSource(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}