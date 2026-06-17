package com.lifepilot.ai;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AiControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private long spaceId;

    @BeforeEach
    void setup() throws Exception {
        String body = """
                {
                  "email": "ai_%d@example.com",
                  "password": "strong-pass-123",
                  "displayName": "AiUser"
                }
                """.formatted(System.nanoTime());

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        token = json.at("/data/accessToken").asText();

        // Get the auto-created personal space
        MvcResult listResult = mockMvc.perform(get("/api/spaces")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode listJson = objectMapper.readTree(listResult.getResponse().getContentAsString());
        spaceId = listJson.at("/data/0/id").asLong();
    }

    @Test
    void parseExpenseWithKnownCategory() throws Exception {
        String body = """
                { "text": "午餐花了32元" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-transaction")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("expense"))
                .andExpect(jsonPath("$.data.amount").value(32))
                .andExpect(jsonPath("$.data.currency").value("CNY"))
                .andExpect(jsonPath("$.data.categoryName").value("餐饮"))
                .andExpect(jsonPath("$.data.needsReview").value(false))
                .andExpect(jsonPath("$.data.rawInput").value("午餐花了32元"));
    }

    @Test
    void parseIncomeWithKnownCategory() throws Exception {
        String body = """
                { "text": "收到工资8000元" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-transaction")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("income"))
                .andExpect(jsonPath("$.data.amount").value(8000))
                .andExpect(jsonPath("$.data.categoryName").value("工资"))
                .andExpect(jsonPath("$.data.needsReview").value(false));
    }

    @Test
    void parseAmbiguousInputNeedsReview() throws Exception {
        String body = """
                { "text": "花了50块" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-transaction")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("expense"))
                .andExpect(jsonPath("$.data.amount").value(50))
                .andExpect(jsonPath("$.data.needsReview").value(true))
                .andExpect(jsonPath("$.data.validationMessage").exists());
    }

    @Test
    void parseInputWithNoAmountReturnsDraftWithError() throws Exception {
        String body = """
                { "text": "买了一些东西" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-transaction")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.amount").doesNotExist())
                .andExpect(jsonPath("$.data.needsReview").value(true))
                .andExpect(jsonPath("$.data.validationMessage").value("未识别到金额，请手动输入金额。"));
    }

    @Test
    void parseEmptyTextReturnsValidationError() throws Exception {
        String body = """
                { "text": "" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-transaction")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void parseRequiresAuthentication() throws Exception {
        String body = """
                { "text": "午餐32元" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void parseDecimalAmount() throws Exception {
        String body = """
                { "text": "超市购物120.50元" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-transaction")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("expense"))
                .andExpect(jsonPath("$.data.amount").value(120.50))
                .andExpect(jsonPath("$.data.categoryName").value("食品日用"));
    }

    @Test
    void parseTransportCategory() throws Exception {
        String body = """
                { "text": "打车25块" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-transaction")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("expense"))
                .andExpect(jsonPath("$.data.amount").value(25))
                .andExpect(jsonPath("$.data.categoryName").value("交通"));
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder get(String url) {
        return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(url);
    }
}