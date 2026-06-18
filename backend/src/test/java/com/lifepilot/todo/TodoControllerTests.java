package com.lifepilot.todo;

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
class TodoControllerTests {

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
                  "email": "todo_%d@example.com",
                  "password": "strong-pass-123",
                  "displayName": "TodoUser"
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
    void createAndListTodoTasks() throws Exception {
        String createBody = """
                {
                  "title": "Buy groceries",
                  "description": "Milk, eggs, bread",
                  "priority": "high"
                }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/todo-tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Buy groceries"))
                .andExpect(jsonPath("$.data.description").value("Milk, eggs, bread"))
                .andExpect(jsonPath("$.data.status").value("pending"))
                .andExpect(jsonPath("$.data.priority").value("high"));

        mockMvc.perform(get("/api/spaces/" + spaceId + "/todo-tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Buy groceries"));
    }

    @Test
    void getTodoTaskById() throws Exception {
        String createBody = """
                {
                  "title": "Clean kitchen"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/spaces/" + spaceId + "/todo-tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long taskId = createJson.at("/data/id").asLong();

        mockMvc.perform(get("/api/spaces/" + spaceId + "/todo-tasks/" + taskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Clean kitchen"))
                .andExpect(jsonPath("$.data.status").value("pending"));
    }

    @Test
    void updateAndDeleteTodoTask() throws Exception {
        String createBody = """
                { "title": "Old task" }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/spaces/" + spaceId + "/todo-tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long taskId = createJson.at("/data/id").asLong();

        // Update title and status
        String updateBody = """
                { "title": "Updated task", "status": "in_progress", "priority": "urgent" }
                """;

        mockMvc.perform(patch("/api/spaces/" + spaceId + "/todo-tasks/" + taskId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated task"))
                .andExpect(jsonPath("$.data.status").value("in_progress"))
                .andExpect(jsonPath("$.data.priority").value("urgent"));

        // Delete
        mockMvc.perform(delete("/api/spaces/" + spaceId + "/todo-tasks/" + taskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Verify deleted
        mockMvc.perform(get("/api/spaces/" + spaceId + "/todo-tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void listTasksByStatus() throws Exception {
        // Create two tasks
        mockMvc.perform(post("/api/spaces/" + spaceId + "/todo-tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"title\": \"Task A\" }"))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(post("/api/spaces/" + spaceId + "/todo-tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"title\": \"Task B\" }"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        long taskBId = json.at("/data/id").asLong();

        // Complete Task B
        mockMvc.perform(patch("/api/spaces/" + spaceId + "/todo-tasks/" + taskBId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"status\": \"completed\" }"))
                .andExpect(status().isOk());

        // Filter by pending
        mockMvc.perform(get("/api/spaces/" + spaceId + "/todo-tasks?status=pending")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Task A"));

        // Filter by completed
        mockMvc.perform(get("/api/spaces/" + spaceId + "/todo-tasks?status=completed")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Task B"));
    }

    @Test
    void nonMemberCannotAccessTodoTasks() throws Exception {
        // Register another user
        String outsiderBody = """
                {
                  "email": "todo_out_%d@example.com",
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
                { "title": "Intruder Task" }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/todo-tasks")
                        .header("Authorization", "Bearer " + outsiderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void todoRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/spaces/" + spaceId + "/todo-tasks"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createTaskWithDefaults() throws Exception {
        String createBody = """
                { "title": "Simple task" }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/todo-tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("pending"))
                .andExpect(jsonPath("$.data.priority").value("medium"))
                .andExpect(jsonPath("$.data.overdue").value(false));
    }
}