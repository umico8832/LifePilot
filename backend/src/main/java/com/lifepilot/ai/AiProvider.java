package com.lifepilot.ai;

import com.lifepilot.ai.dto.TransactionDraftResponse;

/**
 * Interface for AI providers that parse natural language into structured data.
 * Implementations must be deterministic for tests and never call real external APIs
 * unless explicitly configured to do so.
 */
public interface AiProvider {

    /**
     * Parse natural language text into a transaction draft.
     * Returns a draft with needsReview=true when the parsing is ambiguous.
     * Returns null if the input cannot be parsed at all.
     */
    TransactionDraftResponse parseTransaction(String text);
}