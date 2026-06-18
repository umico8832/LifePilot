package com.lifepilot.recipe;

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
class RecipeControllerTests {

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
                  "email": "recipe_%d@example.com",
                  "password": "strong-pass-123",
                  "displayName": "RecipeUser"
                }
                """.formatted(System.nanoTime());

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        token = json.at("/data/accessToken").asText();

        MvcResult listResult = mockMvc.perform(get("/api/spaces")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode listJson = objectMapper.readTree(listResult.getResponse().getContentAsString());
        spaceId = listJson.at("/data/0/id").asLong();
    }

    @Test
    void createAndListRecipes() throws Exception {
        String createBody = """
                {
                  "name": "Tomato Soup",
                  "description": "A warm tomato soup",
                  "ingredientsJson": "[{\\"name\\":\\"tomato\\",\\"amount\\":\\"3\\"}]",
                  "stepsJson": "[\\"Boil\\",\\"Blend\\"]"
                }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/recipes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Tomato Soup"))
                .andExpect(jsonPath("$.data.description").value("A warm tomato soup"));

        mockMvc.perform(get("/api/spaces/" + spaceId + "/recipes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Tomato Soup"));
    }

    @Test
    void getRecipeById() throws Exception {
        String createBody = """
                {
                  "name": "Pasta",
                  "description": "Simple pasta"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/spaces/" + spaceId + "/recipes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long recipeId = createJson.at("/data/id").asLong();

        mockMvc.perform(get("/api/spaces/" + spaceId + "/recipes/" + recipeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Pasta"))
                .andExpect(jsonPath("$.data.description").value("Simple pasta"));
    }

    @Test
    void updateAndDeleteRecipe() throws Exception {
        String createBody = """
                { "name": "Old Recipe" }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/spaces/" + spaceId + "/recipes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long recipeId = createJson.at("/data/id").asLong();

        String updateBody = """
                { "name": "New Recipe", "description": "Updated description" }
                """;

        mockMvc.perform(patch("/api/spaces/" + spaceId + "/recipes/" + recipeId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("New Recipe"))
                .andExpect(jsonPath("$.data.description").value("Updated description"));

        mockMvc.perform(delete("/api/spaces/" + spaceId + "/recipes/" + recipeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/spaces/" + spaceId + "/recipes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void nonMemberCannotAccessRecipes() throws Exception {
        String outsiderBody = """
                {
                  "email": "recipe_out_%d@example.com",
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
                { "name": "Intruder Recipe" }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/recipes")
                        .header("Authorization", "Bearer " + outsiderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void recipeRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/spaces/" + spaceId + "/recipes"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createRecipeValidation() throws Exception {
        String invalidBody = """
                { "name": "" }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/recipes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRecipeNotFound() throws Exception {
        mockMvc.perform(get("/api/spaces/" + spaceId + "/recipes/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}