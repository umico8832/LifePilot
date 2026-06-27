package com.lifepilot.ai;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.ai.dto.AiCallLogResponse;

@Service
public class AiCallLogService {

    private final AiCallLogMapper aiCallLogMapper;
    private final ObjectMapper objectMapper;

    public AiCallLogService(AiCallLogMapper aiCallLogMapper, ObjectMapper objectMapper) {
        this.aiCallLogMapper = aiCallLogMapper;
        this.objectMapper = objectMapper;
    }

    public void recordSuccess(Long userId, Long spaceId, String provider, String scenario,
                              String promptHash, Map<String, Object> requestSummary,
                              Map<String, Object> responseSummary, long durationMs) {
        insertLog(userId, spaceId, provider, scenario, promptHash,
                requestSummary, responseSummary, "success", durationMs, null);
    }

    public void recordFailure(Long userId, Long spaceId, String provider, String scenario,
                              String promptHash, Map<String, Object> requestSummary,
                              RuntimeException exception, long durationMs) {
        insertLog(userId, spaceId, provider, scenario, promptHash,
                requestSummary, null, "failed", durationMs, exception.getMessage());
    }

    public String hashPrompt(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public List<AiCallLogResponse> listLogs(Long spaceId, String scenario, String status, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        LambdaQueryWrapper<AiCallLog> wrapper = new LambdaQueryWrapper<AiCallLog>()
                .eq(AiCallLog::getHouseholdId, spaceId)
                .orderByDesc(AiCallLog::getCreatedAt)
                .last("LIMIT " + safeLimit);
        if (scenario != null && !scenario.isBlank()) {
            wrapper.eq(AiCallLog::getScenario, scenario);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(AiCallLog::getStatus, status);
        }
        return aiCallLogMapper.selectList(wrapper).stream()
                .map(this::toResponse)
                .toList();
    }

    private void insertLog(Long userId, Long spaceId, String provider, String scenario,
                           String promptHash, Map<String, Object> requestSummary,
                           Map<String, Object> responseSummary, String status,
                           long durationMs, String errorMessage) {
        AiCallLog log = new AiCallLog();
        log.setUserId(userId);
        log.setHouseholdId(spaceId);
        log.setProvider(provider);
        log.setScenario(scenario);
        log.setPromptHash(promptHash);
        log.setRequestJson(toJson(requestSummary));
        log.setResponseJson(toJson(responseSummary));
        log.setStatus(status);
        log.setDurationMs(durationMs);
        log.setErrorMessage(truncate(errorMessage, 500));
        aiCallLogMapper.insert(log);
    }

    private String toJson(Map<String, Object> value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{\"serializationError\":true}";
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private AiCallLogResponse toResponse(AiCallLog log) {
        return new AiCallLogResponse(
                log.getId(),
                log.getUserId(),
                log.getHouseholdId(),
                log.getProvider(),
                log.getScenario(),
                log.getPromptHash(),
                log.getRequestJson(),
                log.getResponseJson(),
                log.getStatus(),
                log.getDurationMs(),
                log.getErrorMessage(),
                log.getCreatedAt()
        );
    }
}
