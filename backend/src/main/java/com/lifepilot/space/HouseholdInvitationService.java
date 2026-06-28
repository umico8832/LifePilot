package com.lifepilot.space;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.common.BusinessException;
import com.lifepilot.space.dto.CreateInvitationRequest;
import com.lifepilot.space.dto.InvitationResponse;
import com.lifepilot.user.UserAccount;
import com.lifepilot.user.UserMapper;

@Service
public class HouseholdInvitationService {

    private static final Set<String> VALID_ROLES = Set.of("admin", "member", "viewer");
    private static final int DEFAULT_EXPIRES_IN_DAYS = 7;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final HouseholdInvitationMapper invitationMapper;
    private final HouseholdMemberMapper memberMapper;
    private final HouseholdService householdService;
    private final UserMapper userMapper;

    public HouseholdInvitationService(HouseholdInvitationMapper invitationMapper,
                                      HouseholdMemberMapper memberMapper,
                                      HouseholdService householdService,
                                      UserMapper userMapper) {
        this.invitationMapper = invitationMapper;
        this.memberMapper = memberMapper;
        this.householdService = householdService;
        this.userMapper = userMapper;
    }

    @Transactional
    public InvitationResponse createInvitation(Long userId, Long spaceId, CreateInvitationRequest request) {
        householdService.requireSpaceRole(userId, spaceId, "owner", "admin");

        String token = generateToken();
        LocalDateTime now = LocalDateTime.now();
        HouseholdInvitation invitation = new HouseholdInvitation();
        invitation.setHouseholdId(spaceId);
        invitation.setInvitedBy(userId);
        invitation.setTargetEmail(normalizeEmail(request.targetEmail()));
        invitation.setRole(normalizeInvitationRole(request.role()));
        invitation.setTokenHash(hashToken(token));
        invitation.setStatus("pending");
        invitation.setExpiresAt(now.plusDays(request.expiresInDays() == null
                ? DEFAULT_EXPIRES_IN_DAYS
                : request.expiresInDays()));
        invitation.setCreatedAt(now);
        invitation.setUpdatedAt(now);
        invitationMapper.insert(invitation);

        return InvitationResponse.from(invitation, token);
    }

    public List<InvitationResponse> listInvitations(Long userId, Long spaceId) {
        householdService.requireSpaceRole(userId, spaceId, "owner", "admin");

        return invitationMapper.selectList(
                new LambdaQueryWrapper<HouseholdInvitation>()
                        .eq(HouseholdInvitation::getHouseholdId, spaceId)
                        .orderByDesc(HouseholdInvitation::getCreatedAt)
        ).stream()
                .map(invitation -> InvitationResponse.from(invitation, null))
                .collect(Collectors.toList());
    }

    @Transactional
    public void revokeInvitation(Long userId, Long spaceId, Long invitationId) {
        householdService.requireSpaceRole(userId, spaceId, "owner", "admin");

        HouseholdInvitation invitation = invitationMapper.selectById(invitationId);
        if (invitation == null || !spaceId.equals(invitation.getHouseholdId())) {
            throw new BusinessException("NOT_FOUND", "Invitation not found");
        }
        if (!"pending".equals(invitation.getStatus())) {
            throw new BusinessException("BUSINESS_ERROR", "Only pending invitations can be revoked");
        }

        invitation.setStatus("revoked");
        invitation.setUpdatedAt(LocalDateTime.now());
        invitationMapper.updateById(invitation);
    }

    @Transactional
    public InvitationResponse acceptInvitation(Long userId, String token) {
        HouseholdInvitation invitation = invitationMapper.selectOne(
                new LambdaQueryWrapper<HouseholdInvitation>()
                        .eq(HouseholdInvitation::getTokenHash, hashToken(token.trim()))
        );
        if (invitation == null) {
            throw new BusinessException("NOT_FOUND", "Invitation not found");
        }

        LocalDateTime now = LocalDateTime.now();
        if (!"pending".equals(invitation.getStatus())) {
            throw new BusinessException("BUSINESS_ERROR", "Invitation is no longer pending");
        }
        if (invitation.getExpiresAt().isBefore(now)) {
            throw new BusinessException("BUSINESS_ERROR", "Invitation has expired");
        }

        UserAccount user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("NOT_FOUND", "User not found");
        }
        String targetEmail = invitation.getTargetEmail();
        if (targetEmail != null && !targetEmail.equalsIgnoreCase(user.getEmail())) {
            throw new BusinessException("FORBIDDEN", "Invitation target email does not match current user");
        }

        HouseholdMember existing = memberMapper.selectOne(
                new LambdaQueryWrapper<HouseholdMember>()
                        .eq(HouseholdMember::getHouseholdId, invitation.getHouseholdId())
                        .eq(HouseholdMember::getUserId, userId)
        );
        if (existing != null && "active".equals(existing.getStatus())) {
            throw new BusinessException("CONFLICT", "User is already a member of this space");
        }
        if (existing != null) {
            existing.setRole(invitation.getRole());
            existing.setStatus("active");
            existing.setUpdatedAt(now);
            memberMapper.updateById(existing);
        } else {
            HouseholdMember member = new HouseholdMember();
            member.setHouseholdId(invitation.getHouseholdId());
            member.setUserId(userId);
            member.setRole(invitation.getRole());
            member.setStatus("active");
            member.setCreatedAt(now);
            member.setUpdatedAt(now);
            memberMapper.insert(member);
        }

        invitation.setStatus("accepted");
        invitation.setAcceptedAt(now);
        invitation.setAcceptedBy(userId);
        invitation.setUpdatedAt(now);
        invitationMapper.updateById(invitation);

        return InvitationResponse.from(invitation, null);
    }

    private String normalizeInvitationRole(String role) {
        String normalized = role == null || role.isBlank() ? "member" : role.trim();
        if (!VALID_ROLES.contains(normalized)) {
            throw new BusinessException("VALIDATION_ERROR", "Invalid invitation role");
        }
        return normalized;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
