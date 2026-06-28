package com.lifepilot.space;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

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

    @Autowired
    private HouseholdInvitationMapper invitationMapper;

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
    void adminCanCreateListAndRevokeInvitation() throws Exception {
        UserRegistration admin = registerUser("invite_admin", "Invite Admin");
        addMember(admin.email(), "admin");

        MvcResult createResult = mockMvc.perform(post("/api/spaces/" + spaceId + "/invitations")
                        .header("Authorization", "Bearer " + admin.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "targetEmail": "guest@example.com", "role": "viewer", "expiresInDays": 3 }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetEmail").value("guest@example.com"))
                .andExpect(jsonPath("$.data.role").value("viewer"))
                .andExpect(jsonPath("$.data.status").value("pending"))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long invitationId = createJson.at("/data/id").asLong();

        mockMvc.perform(get("/api/spaces/" + spaceId + "/invitations")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(invitationId))
                .andExpect(jsonPath("$.data[0].token").doesNotExist());

        mockMvc.perform(delete("/api/spaces/" + spaceId + "/invitations/" + invitationId)
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/spaces/" + spaceId + "/invitations")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("revoked"));
    }

    @Test
    void ordinaryMemberAndNonMemberCannotCreateInvitation() throws Exception {
        UserRegistration member = registerUser("invite_member", "Invite Member");
        UserRegistration outsider = registerUser("invite_outsider", "Invite Outsider");
        addMember(member.email(), "member");

        mockMvc.perform(post("/api/spaces/" + spaceId + "/invitations")
                        .header("Authorization", "Bearer " + member.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"role\": \"member\" }"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        mockMvc.perform(post("/api/spaces/" + spaceId + "/invitations")
                        .header("Authorization", "Bearer " + outsider.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"role\": \"member\" }"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void acceptInvitationAddsMemberAndRejectsDuplicateAccept() throws Exception {
        UserRegistration guest = registerUser("invite_guest", "Invite Guest");
        InvitationCreated invitation = createInvitation(guest.email(), "member");

        mockMvc.perform(post("/api/spaces/invitations/accept")
                        .header("Authorization", "Bearer " + guest.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "token": "%s" }
                                """.formatted(invitation.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("accepted"))
                .andExpect(jsonPath("$.data.acceptedBy").isNumber());

        mockMvc.perform(get("/api/spaces/" + spaceId + "/members")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[1].email").value(guest.email()))
                .andExpect(jsonPath("$.data[1].role").value("member"));

        mockMvc.perform(post("/api/spaces/invitations/accept")
                        .header("Authorization", "Bearer " + guest.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "token": "%s" }
                                """.formatted(invitation.token())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    void acceptInvitationRejectsTargetEmailMismatch() throws Exception {
        UserRegistration guest = registerUser("invite_right", "Invite Right");
        UserRegistration other = registerUser("invite_wrong", "Invite Wrong");
        InvitationCreated invitation = createInvitation(guest.email(), "viewer");

        mockMvc.perform(post("/api/spaces/invitations/accept")
                        .header("Authorization", "Bearer " + other.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "token": "%s" }
                                """.formatted(invitation.token())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void acceptInvitationRejectsExpiredAndRevokedInvitations() throws Exception {
        UserRegistration expiredGuest = registerUser("invite_expired", "Invite Expired");
        InvitationCreated expired = createInvitation(expiredGuest.email(), "member");
        HouseholdInvitation expiredEntity = invitationMapper.selectById(expired.id());
        expiredEntity.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        invitationMapper.updateById(expiredEntity);

        mockMvc.perform(post("/api/spaces/invitations/accept")
                        .header("Authorization", "Bearer " + expiredGuest.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "token": "%s" }
                                """.formatted(expired.token())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));

        UserRegistration revokedGuest = registerUser("invite_revoked", "Invite Revoked");
        InvitationCreated revoked = createInvitation(revokedGuest.email(), "member");
        mockMvc.perform(delete("/api/spaces/" + spaceId + "/invitations/" + revoked.id())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/spaces/invitations/accept")
                        .header("Authorization", "Bearer " + revokedGuest.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "token": "%s" }
                                """.formatted(revoked.token())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    void createInvitationRejectsInvalidRole() throws Exception {
        mockMvc.perform(post("/api/spaces/" + spaceId + "/invitations")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"role\": \"owner\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void adminCanUpdateRoleAndRemoveMember() throws Exception {
        UserRegistration admin = registerUser("admin", "Admin");
        UserRegistration member = registerUser("managed", "Managed Member");
        addMember(admin.email(), "admin");
        long memberId = addMember(member.email(), "member");

        mockMvc.perform(patch("/api/spaces/" + spaceId + "/members/" + memberId)
                        .header("Authorization", "Bearer " + admin.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"role\": \"viewer\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("viewer"));

        mockMvc.perform(delete("/api/spaces/" + spaceId + "/members/" + memberId)
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/spaces/" + spaceId + "/members")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void ordinaryMemberCannotUpdateOrRemoveMembers() throws Exception {
        UserRegistration member = registerUser("ordinary", "Ordinary Member");
        long memberId = addMember(member.email(), "member");

        mockMvc.perform(patch("/api/spaces/" + spaceId + "/members/" + memberId)
                        .header("Authorization", "Bearer " + member.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"role\": \"viewer\" }"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        mockMvc.perform(delete("/api/spaces/" + spaceId + "/members/" + memberId)
                        .header("Authorization", "Bearer " + member.token()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void nonMemberCannotManageMembers() throws Exception {
        UserRegistration outsider = registerUser("outsider_manage", "Outsider");
        long ownerMemberId = getFirstMemberId();

        mockMvc.perform(delete("/api/spaces/" + spaceId + "/members/" + ownerMemberId)
                        .header("Authorization", "Bearer " + outsider.token()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void cannotRemoveOrDemoteLastManager() throws Exception {
        long ownerMemberId = getFirstMemberId();

        mockMvc.perform(patch("/api/spaces/" + spaceId + "/members/" + ownerMemberId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"role\": \"member\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));

        mockMvc.perform(delete("/api/spaces/" + spaceId + "/members/" + ownerMemberId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    void spacesRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/spaces"))
                .andExpect(status().isForbidden());
    }

    private UserRegistration registerUser(String prefix, String displayName) throws Exception {
        String email = "%s_%d@example.com".formatted(prefix, System.nanoTime());
        String body = """
                {
                  "email": "%s",
                  "password": "strong-pass-123",
                  "displayName": "%s"
                }
                """.formatted(email, displayName);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return new UserRegistration(
                json.at("/data/accessToken").asText(),
                json.at("/data/user/email").asText()
        );
    }

    private long addMember(String email, String role) throws Exception {
        String addBody = """
                { "email": "%s", "role": "%s" }
                """.formatted(email, role);

        MvcResult result = mockMvc.perform(post("/api/spaces/" + spaceId + "/members")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.at("/data/id").asLong();
    }

    private long getFirstMemberId() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/spaces/" + spaceId + "/members")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.at("/data/0/id").asLong();
    }

    private InvitationCreated createInvitation(String targetEmail, String role) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/spaces/" + spaceId + "/invitations")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "targetEmail": "%s", "role": "%s" }
                                """.formatted(targetEmail, role)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return new InvitationCreated(
                json.at("/data/id").asLong(),
                json.at("/data/token").asText()
        );
    }

    private record UserRegistration(String token, String email) {}

    private record InvitationCreated(long id, String token) {}
}
