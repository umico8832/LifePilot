package com.lifepilot.document;

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
class DocumentControllerTests {

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
                  "email": "doc_%d@example.com",
                  "password": "strong-pass-123",
                  "displayName": "DocUser"
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
    void createAndListDocuments() throws Exception {
        String createBody = """
                {
                  "title": "Electricity Bill June",
                  "type": "invoice",
                  "issuer": "Power Company",
                  "documentDate": "2026-06-01",
                  "expireAt": "2026-07-01"
                }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/documents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Electricity Bill June"))
                .andExpect(jsonPath("$.data.type").value("invoice"))
                .andExpect(jsonPath("$.data.issuer").value("Power Company"));

        mockMvc.perform(get("/api/spaces/" + spaceId + "/documents")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Electricity Bill June"));
    }

    @Test
    void getDocumentById() throws Exception {
        String createBody = """
                {
                  "title": "Warranty Card",
                  "type": "warranty",
                  "issuer": "Samsung"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/spaces/" + spaceId + "/documents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long docId = createJson.at("/data/id").asLong();

        mockMvc.perform(get("/api/spaces/" + spaceId + "/documents/" + docId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Warranty Card"))
                .andExpect(jsonPath("$.data.type").value("warranty"));
    }

    @Test
    void updateAndDeleteDocument() throws Exception {
        String createBody = """
                {
                  "title": "Old Receipt",
                  "type": "receipt"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/spaces/" + spaceId + "/documents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long docId = createJson.at("/data/id").asLong();

        // Update title
        String updateBody = """
                { "title": "Updated Receipt", "storageLocation": "Drawer A" }
                """;

        mockMvc.perform(patch("/api/spaces/" + spaceId + "/documents/" + docId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Receipt"))
                .andExpect(jsonPath("$.data.storageLocation").value("Drawer A"));

        // Delete
        mockMvc.perform(delete("/api/spaces/" + spaceId + "/documents/" + docId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Verify deleted
        mockMvc.perform(get("/api/spaces/" + spaceId + "/documents")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void listDocumentsByType() throws Exception {
        // Create two documents with different types
        mockMvc.perform(post("/api/spaces/" + spaceId + "/documents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"title\": \"Invoice A\", \"type\": \"invoice\" }"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/spaces/" + spaceId + "/documents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"title\": \"Receipt B\", \"type\": \"receipt\" }"))
                .andExpect(status().isOk());

        // Filter by invoice
        mockMvc.perform(get("/api/spaces/" + spaceId + "/documents?type=invoice")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Invoice A"));

        // Filter by receipt
        mockMvc.perform(get("/api/spaces/" + spaceId + "/documents?type=receipt")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Receipt B"));
    }

    @Test
    void nonMemberCannotAccessDocuments() throws Exception {
        // Register another user
        String outsiderBody = """
                {
                  "email": "doc_out_%d@example.com",
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
                { "title": "Intruder Doc", "type": "other" }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/documents")
                        .header("Authorization", "Bearer " + outsiderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void documentRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/spaces/" + spaceId + "/documents"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createDocumentWithInvalidType() throws Exception {
        String createBody = """
                {
                  "title": "Bad Doc",
                  "type": "nonexistent_type"
                }
                """;

        mockMvc.perform(post("/api/spaces/" + spaceId + "/documents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_TYPE"));
    }
}