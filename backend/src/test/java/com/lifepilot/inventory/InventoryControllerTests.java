package com.lifepilot.inventory;

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
class InventoryControllerTests {

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
                  "email": "inv_%d@example.com",
                  "password": "strong-pass-123",
                  "displayName": "InvUser"
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
    void createAndListInventoryItems() throws Exception {
        String createBody = """
                {
                  "name": "Rice",
                  "category": "Food",
                  "quantity": 10,
                  "unit": "kg",
                  "location": "Pantry"
                }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/inventory-items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Rice"))
                .andExpect(jsonPath("$.data.category").value("Food"))
                .andExpect(jsonPath("$.data.quantity").value(10))
                .andExpect(jsonPath("$.data.unit").value("kg"))
                .andExpect(jsonPath("$.data.location").value("Pantry"));

        mockMvc.perform(get("/api/spaces/" + spaceId + "/inventory-items")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Rice"));
    }

    @Test
    void getInventoryItemById() throws Exception {
        String createBody = """
                {
                  "name": "Milk",
                  "category": "Dairy",
                  "quantity": 2,
                  "unit": "liters"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/spaces/" + spaceId + "/inventory-items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long itemId = createJson.at("/data/id").asLong();

        mockMvc.perform(get("/api/spaces/" + spaceId + "/inventory-items/" + itemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Milk"))
                .andExpect(jsonPath("$.data.category").value("Dairy"));
    }

    @Test
    void updateAndDeleteInventoryItem() throws Exception {
        String createBody = """
                { "name": "Old Item", "quantity": 5 }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/spaces/" + spaceId + "/inventory-items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long itemId = createJson.at("/data/id").asLong();

        // Update
        String updateBody = """
                { "name": "Updated Item", "quantity": 10, "unit": "pcs" }
                """;

        mockMvc.perform(patch("/api/spaces/" + spaceId + "/inventory-items/" + itemId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Item"))
                .andExpect(jsonPath("$.data.quantity").value(10))
                .andExpect(jsonPath("$.data.unit").value("pcs"));

        // Delete
        mockMvc.perform(delete("/api/spaces/" + spaceId + "/inventory-items/" + itemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Verify deleted
        mockMvc.perform(get("/api/spaces/" + spaceId + "/inventory-items")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void lowStockAlerts() throws Exception {
        // Create an item with low stock threshold higher than quantity
        String createBody = """
                {
                  "name": "Eggs",
                  "quantity": 3,
                  "lowStockThreshold": 10
                }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/inventory-items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lowStock").value(true));

        // Create an item that is not low stock
        String createBody2 = """
                {
                  "name": "Sugar",
                  "quantity": 20,
                  "lowStockThreshold": 5
                }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/inventory-items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lowStock").value(false));

        // Check alerts endpoint returns only low stock items
        mockMvc.perform(get("/api/spaces/" + spaceId + "/inventory-items/alerts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Eggs"));
    }

    @Test
    void nonMemberCannotAccessInventory() throws Exception {
        // Register another user
        String outsiderBody = """
                {
                  "email": "inv_out_%d@example.com",
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
                { "name": "Intruder Item" }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/inventory-items")
                        .header("Authorization", "Bearer " + outsiderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void inventoryRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/spaces/" + spaceId + "/inventory-items"))
                .andExpect(status().isForbidden());
    }
}