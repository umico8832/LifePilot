package com.lifepilot.finance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class TransactionControllerTests {

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
                  "email": "finance_%d@example.com",
                  "password": "strong-pass-123",
                  "displayName": "FinanceUser"
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
    void createAndListTransaction() throws Exception {
        String createBody = """
                {
                  "amount": 42.50,
                  "type": "expense",
                  "merchant": "Grocery Store",
                  "note": "Weekly groceries"
                }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(42.50))
                .andExpect(jsonPath("$.data.type").value("expense"))
                .andExpect(jsonPath("$.data.merchant").value("Grocery Store"))
                .andExpect(jsonPath("$.data.source").value("manual"));

        mockMvc.perform(get("/api/spaces/" + spaceId + "/transactions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].note").value("Weekly groceries"));
    }

    @Test
    void getAndUpdateTransaction() throws Exception {
        String createBody = """
                {
                  "amount": 100.00,
                  "merchant": "Restaurant"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/spaces/" + spaceId + "/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long txId = createJson.at("/data/id").asLong();

        mockMvc.perform(get("/api/spaces/" + spaceId + "/transactions/" + txId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.merchant").value("Restaurant"));

        String updateBody = """
                { "amount": 120.00, "note": "Updated amount" }
                """;

        mockMvc.perform(patch("/api/spaces/" + spaceId + "/transactions/" + txId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(120.00))
                .andExpect(jsonPath("$.data.note").value("Updated amount"));
    }

    @Test
    void deleteTransaction() throws Exception {
        String createBody = """
                { "amount": 10.00 }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/spaces/" + spaceId + "/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long txId = createJson.at("/data/id").asLong();

        mockMvc.perform(delete("/api/spaces/" + spaceId + "/transactions/" + txId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/spaces/" + spaceId + "/transactions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void nonMemberCannotCreateTransaction() throws Exception {
        // Register another user
        String outsiderBody = """
                {
                  "email": "finance_out_%d@example.com",
                  "password": "strong-pass-123",
                  "displayName": "Outsider"
                }
                """.formatted(System.nanoTime());

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(outsiderBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode regJson = objectMapper.readTree(regResult.getResponse().getContentAsString());
        String outsiderToken = regJson.at("/data/accessToken").asText();

        String createBody = """
                { "amount": 50.00 }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/transactions")
                        .header("Authorization", "Bearer " + outsiderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void validationRejectsNegativeAmount() throws Exception {
        String createBody = """
                { "amount": -10 }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void transactionsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/spaces/" + spaceId + "/transactions"))
                .andExpect(status().isForbidden());
    }
}