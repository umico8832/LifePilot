package com.lifepilot.ai;

import com.lifepilot.ai.dto.ShoppingDraftResponse;
import com.lifepilot.ai.dto.TodoDraftResponse;
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

    /**
     * Parse natural language text into a shopping list draft.
     * Returns a draft with needsReview=true when the parsing is ambiguous.
     * Returns null if the input cannot be parsed at all.
     */
    ShoppingDraftResponse parseShoppingList(String text);

    /**
     * Parse natural language text into a todo task draft.
     * Returns a draft with needsReview=true when the parsing is ambiguous.
     * Returns null if the input cannot be parsed at all.
     */
    TodoDraftResponse parseTodo(String text);
}
