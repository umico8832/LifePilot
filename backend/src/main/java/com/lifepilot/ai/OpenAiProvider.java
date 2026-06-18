package com.lifepilot.ai;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifepilot.ai.dto.ShoppingDraftResponse;
import com.lifepilot.ai.dto.TodoDraftResponse;
import com.lifepilot.ai.dto.TransactionDraftResponse;

/**
 * OpenAI-compatible AI provider that calls a Chat Completions API
 * to parse natural language into structured drafts.
 *
 * <p>Each parse method sends a system prompt describing the expected JSON
 * structure together with the user's natural language text. The response
 * is extracted from {@code choices[0].message.content} and deserialized
 * into the corresponding draft record.</p>
 *
 * <p>Simple retry logic (configurable {@code retryMaxAttempts}) is applied.
 * All exceptions are caught; on failure the method returns {@code null}
 * so that the caller (AiService) can provide a fallback message.</p>
 */
public class OpenAiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiProvider.class);

    private final RestClient restClient;
    private final String model;
    private final double temperature;
    private final int maxTokens;
    private final int retryMaxAttempts;
    private final ObjectMapper objectMapper;

    public OpenAiProvider(RestClient restClient, String model,
                          double temperature, int maxTokens,
                          int retryMaxAttempts, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.model = model;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.retryMaxAttempts = retryMaxAttempts;
        this.objectMapper = objectMapper;
    }

    // ---- AiProvider implementation ----

    @Override
    public TransactionDraftResponse parseTransaction(String text) {
        String systemPrompt = """
                你是一个记账助手。用户会用自然语言描述一笔收入或支出，你需要解析为 JSON 格式。
                
                必须严格返回如下 JSON 结构，不要包含任何多余文本：
                {
                  "type": "expense" 或 "income",
                  "amount": 数字（大于0），
                  "currency": "CNY",
                  "occurredAt": "ISO 8601 日期时间字符串"（解析不出来就填 null），
                  "merchant": "商户名称"（解析不出来就填 null），
                  "categoryName": "分类名称"（解析不出来就填 null），
                  "note": "备注",
                  "needsReview": true 或 false（不确定时填 true），
                  "rawInput": 原始输入文本,
                  "validationMessage": "提示信息"（没有则填 null）
                }
                """;
        return callChatCompletion(systemPrompt, text, TransactionDraftResponse.class);
    }

    @Override
    public ShoppingDraftResponse parseShoppingList(String text) {
        String systemPrompt = """
                你是一个购物清单助手。用户会用自然语言描述要购买的物品，你需要解析为 JSON 格式。
                
                必须严格返回如下 JSON 结构，不要包含任何多余文本：
                {
                  "listName": "清单名称"（根据内容推断一个合适的名称），
                  "estimatedBudget": null,
                  "items": [
                    {
                      "name": "物品名称",
                      "quantity": 数字,
                      "unit": "单位"（解析不出来就填 null），
                      "estimatedPrice": null
                    }
                  ],
                  "needsReview": true 或 false（不确定时填 true），
                  "rawInput": 原始输入文本,
                  "validationMessage": "提示信息"（没有则填 null）
                }
                """;
        return callChatCompletion(systemPrompt, text, ShoppingDraftResponse.class);
    }

    @Override
    public TodoDraftResponse parseTodo(String text) {
        String systemPrompt = """
                你是一个待办事项助手。用户会用自然语言描述待办事项，你需要解析为 JSON 格式。
                
                必须严格返回如下 JSON 结构，不要包含任何多余文本：
                {
                  "items": [
                    {
                      "title": "待办标题",
                      "description": "描述"（解析不出来就填 null），
                      "priority": "urgent"、"high"、"medium"、"low"（不确定就填 null），
                      "dueAt": "ISO 8601 日期时间字符串"（没有就填 null）
                    }
                  ],
                  "needsReview": true 或 false（不确定时填 true），
                  "rawInput": 原始输入文本,
                  "validationMessage": "提示信息"（没有则填 null）
                }
                """;
        return callChatCompletion(systemPrompt, text, TodoDraftResponse.class);
    }

    // ---- Internal helpers ----

    /**
     * Call the Chat Completions API and deserialize the response content.
     * Returns {@code null} on any failure (after retries).
     */
    private <T> T callChatCompletion(String systemPrompt, String userContent, Class<T> responseType) {
        int inputLen = userContent != null ? userContent.length() : 0;
        log.info("OpenAI request: model={}, inputLength={}", model, inputLen);

        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", temperature,
                "max_tokens", maxTokens,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userContent != null ? userContent : "")
                )
        );

        Exception lastException = null;
        for (int attempt = 1; attempt <= retryMaxAttempts; attempt++) {
            try {
                String responseBody = restClient.post()
                        .uri("/chat/completions")
                        .body(body)
                        .retrieve()
                        .body(String.class);

                String content = extractContent(responseBody);
                if (content == null) {
                    log.warn("OpenAI response had no content on attempt {}/{}", attempt, retryMaxAttempts);
                    continue;
                }

                // Strip markdown code fences if present
                content = stripCodeFences(content);

                T result = objectMapper.readValue(content, responseType);
                log.info("OpenAI parse succeeded on attempt {}", attempt);
                return result;

            } catch (JsonProcessingException e) {
                log.warn("OpenAI response JSON parse failed on attempt {}/{}: {}",
                        attempt, retryMaxAttempts, e.getMessage());
                lastException = e;
            } catch (Exception e) {
                log.warn("OpenAI request failed on attempt {}/{}: {}",
                        attempt, retryMaxAttempts, e.getMessage());
                lastException = e;
            }
        }

        log.error("OpenAI request failed after {} attempts", retryMaxAttempts, lastException);
        return null;
    }

    /**
     * Extract choices[0].message.content from the raw Chat Completions JSON response.
     */
    @SuppressWarnings("unchecked")
    private String extractContent(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }
        try {
            Map<String, Object> root = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) root.get("choices");
            if (choices == null || choices.isEmpty()) {
                return null;
            }
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message == null) {
                return null;
            }
            Object content = message.get("content");
            return content != null ? content.toString() : null;
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse OpenAI response envelope: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Strip markdown code fences (```json ... ``` or ``` ... ```) from content.
     */
    private static String stripCodeFences(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            // Remove opening fence (possibly with language tag like ```json)
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            // Remove closing fence
            int lastFence = trimmed.lastIndexOf("```");
            if (lastFence >= 0) {
                trimmed = trimmed.substring(0, lastFence);
            }
        }
        return trimmed.trim();
    }
}