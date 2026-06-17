package com.lifepilot.space;

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
class HouseholdControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String ownerToken;
    private long spaceId;

    @BeforeEach
    void setup() throws Exception {
        // Register a unique user and get token
        String body = """
                {
                  "email": "spaceowner_%d@example.com",
                  "password": "strong-pass-123",
                  "displayName": "SpaceOwner"
                }
                """.formatted(System.nanoTime());

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        ownerToken = json.at("/data/accessToken").asText();

        // The personal space was auto-created; retrieve it
        MvcResult listResult = mockMvc.perform(get("/api/spaces")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        JsonNode listJson = objectMapper.readTree(listResult.getResponse().getContentAsString());
        spaceId = listJson.at("/data/0/id").asLong();
    }

    @Test
    void listSpacesReturnsPersonalSpace() throws Exception {
        mockMvc.perform(get("/api/spaces")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].type").value("personal"))
                .andExpect(jsonPath("$.data[0].memberRole").value("owner"));
    }

    @Test
    void getSpaceReturnsSpaceDetails() throws Exception {
        mockMvc.perform(get("/api/spaces/" + spaceId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(spaceId))
                .andExpect(jsonPath("$.data.name").value("SpaceOwner's Space"))
                .andExpect(jsonPath("$.data.type").value("personal"));
    }

    @Test
    void updateSpaceChangesName() throws Exception {
        String updateBody = """
                { "name": "New Name" }
                """;

        mockMvc.perform(patch("/api/spaces/" + spaceId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("New Name"));
    }

    @Test
    void createFamilySpace() throws Exception {
        String createBody = """
                { "name": "Smith Family", "type": "family" }
                """;

        mockMvc.perform(post("/api/spaces")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Smith Family"))
                .andExpect(jsonPath("$.data.type").value("family"))
                .andExpect(jsonPath("$.data.memberRole").value("owner"));
    }

    @Test
    void listMembersReturnsOwner() throws Exception {
        mockMvc.perform(get("/api/spaces/" + spaceId + "/members")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].role").value("owner"))
                .andExpect(jsonPath("$.data[0].displayName").value("SpaceOwner"));
    }

    @Test
    void addMemberAndList() throws Exception {
        // Register another user
        String memberBody = """
                {
                  "email": "member_%d@example.com",
                  "password": "strong-pass-123",
                  "displayName": "Member"
                }
                """.formatted(System.nanoTime());

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode regJson = objectMapper.readTree(regResult.getResponse().getContentAsString());
        String memberEmail = regJson.at("/data/user/email").asText();

        // Add member to space
        String addBody = """
                { "email": "%s", "role": "member" }
                """.formatted(memberEmail);

        mockMvc.perform(post("/api/spaces/" + spaceId + "/members")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(memberEmail))
                .andExpect(jsonPath("$.data.role").value("member"));

        // List members should now be 2
        mockMvc.perform(get("/api/spaces/" + spaceId + "/members")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void addDuplicateMemberReturnsConflict() throws Exception {
        // Register another user
        String dupEmail = "dup_%d@example.com".formatted(System.nanoTime());
        String memberBody = """
                {
                  "email": "%s",
                  "password": "strong-pass-123",
                  "displayName": "DupMember"
                }
                """.formatted(dupEmail);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberBody))
                .andExpect(status().isOk());

        String addBody = """
                { "email": "%s" }
                """.formatted(dupEmail);

        // Add once
        mockMvc.perform(post("/api/spaces/" + spaceId + "/members")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addBody))
                .andExpect(status().isOk());

        // Add again => conflict
        mockMvc.perform(post("/api/spaces/" + spaceId + "/members")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void nonMemberCannotAccessSpace() throws Exception {
        // Register another user who is not a member
        String outsiderBody = """
                {
                  "email": "outsider_%d@example.com",
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

        mockMvc.perform(get("/api/spaces/" + spaceId)
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void spacesRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/spaces"))
                .andExpect(status().isForbidden());
    }
}