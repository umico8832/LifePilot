package com.lifepilot.ai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AiCallLogServiceTests {

    @Mock
    private AiCallLogMapper aiCallLogMapper;

    private AiCallLogService aiCallLogService;

    @BeforeEach
    void setUp() {
        aiCallLogService = new AiCallLogService(aiCallLogMapper, new ObjectMapper());
    }

    @Test
    void hashPrompt_returnsSha256WithoutRawInput() {
        String hash = aiCallLogService.hashPrompt("早餐15.5");

        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertNotEquals("早餐15.5", hash);
    }

    @Test
    void recordSuccess_insertsSanitizedSummary() {
        aiCallLogService.recordSuccess(
                1L,
                10L,
                "mock",
                "parse_transaction",
                aiCallLogService.hashPrompt("早餐15.5"),
                Map.of("inputLength", 6),
                Map.of("type", "expense", "needsReview", false),
                12L
        );

        ArgumentCaptor<AiCallLog> captor = ArgumentCaptor.forClass(AiCallLog.class);
        verify(aiCallLogMapper).insert(captor.capture());
        AiCallLog log = captor.getValue();

        assertEquals(1L, log.getUserId());
        assertEquals(10L, log.getHouseholdId());
        assertEquals("mock", log.getProvider());
        assertEquals("parse_transaction", log.getScenario());
        assertEquals("success", log.getStatus());
        assertEquals(12L, log.getDurationMs());
        assertFalse(log.getRequestJson().contains("早餐"));
        assertTrue(log.getRequestJson().contains("inputLength"));
        assertTrue(log.getResponseJson().contains("needsReview"));
    }

    @Test
    void recordFailure_truncatesErrorMessage() {
        RuntimeException exception = new RuntimeException("x".repeat(600));

        aiCallLogService.recordFailure(
                1L,
                10L,
                "mock",
                "parse_todo",
                null,
                Map.of("inputLength", 3),
                exception,
                5L
        );

        ArgumentCaptor<AiCallLog> captor = ArgumentCaptor.forClass(AiCallLog.class);
        verify(aiCallLogMapper).insert(captor.capture());
        AiCallLog log = captor.getValue();

        assertEquals("failed", log.getStatus());
        assertEquals(500, log.getErrorMessage().length());
    }

    @Test
    void listLogs_mapsRecordsToResponses() {
        AiCallLog log = new AiCallLog();
        log.setId(100L);
        log.setUserId(1L);
        log.setHouseholdId(10L);
        log.setProvider("mock");
        log.setScenario("parse_todo");
        log.setStatus("success");
        log.setDurationMs(8L);
        log.setCreatedAt(LocalDateTime.now());

        org.mockito.Mockito.when(aiCallLogMapper.selectList(org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.List.of(log));

        java.util.List<com.lifepilot.ai.dto.AiCallLogResponse> result =
                aiCallLogService.listLogs(10L, "parse_todo", "success", 150);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).id());
        assertEquals("parse_todo", result.get(0).scenario());
    }
}
