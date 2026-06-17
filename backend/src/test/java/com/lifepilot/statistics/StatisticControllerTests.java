package com.lifepilot.statistics;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

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
class StatisticControllerTests {

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
                  "email": "stats_%d@example.com",
                  "password": "strong-pass-123",
                  "displayName": "StatsUser"
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
    void overviewReturnsZeroesForEmptySpace() throws Exception {
        mockMvc.perform(get("/api/spaces/" + spaceId + "/statistics/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalIncome").value(0))
                .andExpect(jsonPath("$.data.totalExpense").value(0))
                .andExpect(jsonPath("$.data.netBalance").value(0))
                .andExpect(jsonPath("$.data.transactionCount").value(0))
                .andExpect(jsonPath("$.data.inventoryItemCount").value(0))
                .andExpect(jsonPath("$.data.shoppingListCount").value(0))
                .andExpect(jsonPath("$.data.inventoryAlertCount").value(0));
    }

    @Test
    void overviewCountsTransactions() throws Exception {
        // Create an expense transaction
        String expenseBody = """
                {
                  "type": "expense",
                  "amount": 50.00,
                  "currency": "CNY",
                  "occurredAt": "2026-06-01T10:00:00"
                }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseBody))
                .andExpect(status().isOk());

        // Create an income transaction
        String incomeBody = """
                {
                  "type": "income",
                  "amount": 100.00,
                  "currency": "CNY",
                  "occurredAt": "2026-06-02T10:00:00"
                }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incomeBody))
                .andExpect(status().isOk());

        // Check overview
        mockMvc.perform(get("/api/spaces/" + spaceId + "/statistics/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalIncome").value(100))
                .andExpect(jsonPath("$.data.totalExpense").value(50))
                .andExpect(jsonPath("$.data.netBalance").value(50))
                .andExpect(jsonPath("$.data.transactionCount").value(2));
    }

    @Test
    void overviewCountsInventoryAndShopping() throws Exception {
        // Create an inventory item
        String inventoryBody = """
                { "name": "Rice", "category": "Food", "quantity": 10, "unit": "kg" }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/inventory-items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inventoryBody))
                .andExpect(status().isOk());

        // Create a shopping list
        String shoppingBody = """
                { "name": "Weekly Shopping" }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/shopping-lists")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(shoppingBody))
                .andExpect(status().isOk());

        // Check overview
        mockMvc.perform(get("/api/spaces/" + spaceId + "/statistics/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inventoryItemCount").value(1))
                .andExpect(jsonPath("$.data.shoppingListCount").value(1));
    }

    @Test
    void overviewDetectsLowStockAlerts() throws Exception {
        // Create a low-stock item (quantity < threshold)
        String lowStockBody = """
                { "name": "Eggs", "quantity": 2, "lowStockThreshold": 10 }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/inventory-items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lowStockBody))
                .andExpect(status().isOk());

        // Create a normal stock item
        String normalStockBody = """
                { "name": "Sugar", "quantity": 50, "lowStockThreshold": 5 }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/inventory-items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(normalStockBody))
                .andExpect(status().isOk());

        // Check overview shows 1 alert
        mockMvc.perform(get("/api/spaces/" + spaceId + "/statistics/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inventoryItemCount").value(2))
                .andExpect(jsonPath("$.data.inventoryAlertCount").value(1));
    }

    @Test
    void financeMonthlyReturnsZerosForEmptyMonth() throws Exception {
        mockMvc.perform(get("/api/spaces/" + spaceId + "/statistics/finance/monthly")
                        .header("Authorization", "Bearer " + token)
                        .param("year", "2026")
                        .param("month", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.year").value(2026))
                .andExpect(jsonPath("$.data.month").value(1))
                .andExpect(jsonPath("$.data.totalIncome").value(0))
                .andExpect(jsonPath("$.data.totalExpense").value(0))
                .andExpect(jsonPath("$.data.netBalance").value(0))
                .andExpect(jsonPath("$.data.categories.length()").value(0));
    }

    @Test
    void financeMonthlyAggregatesByCategory() throws Exception {
        // Create a category
        String categoryBody = """
                { "name": "Food", "type": "expense" }
                """;

        MvcResult catResult = mockMvc.perform(post("/api/spaces/" + spaceId + "/transaction-categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode catJson = objectMapper.readTree(catResult.getResponse().getContentAsString());
        long categoryId = catJson.at("/data/id").asLong();

        // Create expense transactions in the same month
        String expense1 = """
                {
                  "type": "expense",
                  "amount": 30.00,
                  "currency": "CNY",
                  "categoryId": %d,
                  "occurredAt": "2026-03-15T10:00:00"
                }
                """.formatted(categoryId);

        String expense2 = """
                {
                  "type": "expense",
                  "amount": 20.00,
                  "currency": "CNY",
                  "categoryId": %d,
                  "occurredAt": "2026-03-20T10:00:00"
                }
                """.formatted(categoryId);

        String income = """
                {
                  "type": "income",
                  "amount": 500.00,
                  "currency": "CNY",
                  "occurredAt": "2026-03-01T10:00:00"
                }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expense1))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/spaces/" + spaceId + "/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expense2))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/spaces/" + spaceId + "/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(income))
                .andExpect(status().isOk());

        // Check monthly finance
        mockMvc.perform(get("/api/spaces/" + spaceId + "/statistics/finance/monthly")
                        .header("Authorization", "Bearer " + token)
                        .param("year", "2026")
                        .param("month", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalIncome").value(500))
                .andExpect(jsonPath("$.data.totalExpense").value(50))
                .andExpect(jsonPath("$.data.netBalance").value(450))
                .andExpect(jsonPath("$.data.categories.length()").value(1))
                .andExpect(jsonPath("$.data.categories[0].categoryName").value("Food"))
                .andExpect(jsonPath("$.data.categories[0].amount").value(50))
                .andExpect(jsonPath("$.data.categories[0].count").value(2));
    }

    @Test
    void statisticsRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/spaces/" + spaceId + "/statistics/overview"))
                .andExpect(status().isForbidden());
    }

    @Test
    void nonMemberCannotAccessStatistics() throws Exception {
        // Register another user
        String outsiderBody = """
                {
                  "email": "stats_out_%d@example.com",
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

        mockMvc.perform(get("/api/spaces/" + spaceId + "/statistics/overview")
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden());
    }
}