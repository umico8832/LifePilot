package com.lifepilot.shopping;

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
class ShoppingControllerTests {

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
                  "email": "shop_%d@example.com",
                  "password": "strong-pass-123",
                  "displayName": "ShopUser"
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
    void createAndListShoppingLists() throws Exception {
        String createBody = """
                {
                  "name": "Weekly Groceries",
                  "estimatedBudget": 200.00
                }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/shopping-lists")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Weekly Groceries"))
                .andExpect(jsonPath("$.data.status").value("active"))
                .andExpect(jsonPath("$.data.estimatedBudget").value(200.00));

        mockMvc.perform(get("/api/spaces/" + spaceId + "/shopping-lists")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Weekly Groceries"));
    }

    @Test
    void getShoppingListWithItems() throws Exception {
        // Create a list
        String listBody = """
                { "name": "Party Supplies" }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/spaces/" + spaceId + "/shopping-lists")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(listBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long listId = createJson.at("/data/id").asLong();

        // Add an item
        String itemBody = """
                {
                  "name": "Chips",
                  "quantity": 3,
                  "unit": "bags",
                  "estimatedPrice": 15.00
                }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/shopping-lists/" + listId + "/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Chips"))
                .andExpect(jsonPath("$.data.quantity").value(3))
                .andExpect(jsonPath("$.data.purchased").value(false));

        // Get list with items
        mockMvc.perform(get("/api/spaces/" + spaceId + "/shopping-lists/" + listId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Party Supplies"))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("Chips"));
    }

    @Test
    void updateAndDeleteShoppingList() throws Exception {
        String createBody = """
                { "name": "Old List" }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/spaces/" + spaceId + "/shopping-lists")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long listId = createJson.at("/data/id").asLong();

        // Update
        String updateBody = """
                { "name": "Updated List", "status": "completed" }
                """;

        mockMvc.perform(patch("/api/spaces/" + spaceId + "/shopping-lists/" + listId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated List"))
                .andExpect(jsonPath("$.data.status").value("completed"));

        // Delete
        mockMvc.perform(delete("/api/spaces/" + spaceId + "/shopping-lists/" + listId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Verify deleted
        mockMvc.perform(get("/api/spaces/" + spaceId + "/shopping-lists")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void updateAndDeleteShoppingItem() throws Exception {
        // Create list
        String listBody = """
                { "name": "Test List" }
                """;

        MvcResult listResult = mockMvc.perform(post("/api/spaces/" + spaceId + "/shopping-lists")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(listBody))
                .andExpect(status().isOk())
                .andReturn();

        long listId = objectMapper.readTree(listResult.getResponse().getContentAsString()).at("/data/id").asLong();

        // Add item
        String itemBody = """
                { "name": "Milk", "quantity": 2, "unit": "liters" }
                """;

        MvcResult itemResult = mockMvc.perform(post("/api/spaces/" + spaceId + "/shopping-lists/" + listId + "/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody))
                .andExpect(status().isOk())
                .andReturn();

        long itemId = objectMapper.readTree(itemResult.getResponse().getContentAsString()).at("/data/id").asLong();

        // Update item (mark as purchased)
        String updateItemBody = """
                { "purchased": true }
                """;

        mockMvc.perform(patch("/api/spaces/" + spaceId + "/shopping-lists/" + listId + "/items/" + itemId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateItemBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.purchased").value(true));

        // Delete item
        mockMvc.perform(delete("/api/spaces/" + spaceId + "/shopping-lists/" + listId + "/items/" + itemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Verify item deleted
        mockMvc.perform(get("/api/spaces/" + spaceId + "/shopping-lists/" + listId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(0));
    }

    @Test
    void nonMemberCannotAccessShoppingList() throws Exception {
        // Register another user
        String outsiderBody = """
                {
                  "email": "shop_out_%d@example.com",
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
                { "name": "Intruder List" }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/shopping-lists")
                        .header("Authorization", "Bearer " + outsiderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void shoppingListsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/spaces/" + spaceId + "/shopping-lists"))
                .andExpect(status().isForbidden());
    }
}