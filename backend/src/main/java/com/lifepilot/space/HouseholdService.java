package com.lifepilot.space;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.common.BusinessException;
import com.lifepilot.space.dto.AddMemberRequest;
import com.lifepilot.space.dto.CreateSpaceRequest;
import com.lifepilot.space.dto.MemberResponse;
import com.lifepilot.space.dto.SpaceResponse;
import com.lifepilot.space.dto.UpdateSpaceRequest;
import com.lifepilot.user.UserAccount;
import com.lifepilot.user.UserMapper;

@Service
public class HouseholdService {

    private static final Set<String> VALID_ROLES = Set.of("owner", "admin", "member", "viewer");
    private static final Set<String> MANAGER_ROLES = Set.of("owner", "admin");

    private final HouseholdMapper householdMapper;
    private final HouseholdMemberMapper memberMapper;
    private final UserMapper userMapper;

    public HouseholdService(HouseholdMapper householdMapper,
                            HouseholdMemberMapper memberMapper,
                            UserMapper userMapper) {
        this.householdMapper = householdMapper;
        this.memberMapper = memberMapper;
        this.userMapper = userMapper;
    }

    /**
     * Create a personal space for a newly registered user.
     */
    @Transactional
    public Household createPersonalSpace(Long userId, String displayName) {
        Household household = new Household();
        household.setName(displayName != null ? displayName + "'s Space" : "My Space");
        household.setType("personal");
        household.setOwnerUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        household.setCreatedAt(now);
        household.setUpdatedAt(now);
        householdMapper.insert(household);

        HouseholdMember member = new HouseholdMember();
        member.setHouseholdId(household.getId());
        member.setUserId(userId);
        member.setRole("owner");
        member.setStatus("active");
        member.setCreatedAt(now);
        member.setUpdatedAt(now);
        memberMapper.insert(member);

        return household;
    }

    /**
     * List all spaces the current user belongs to.
     */
    public List<SpaceResponse> listSpaces(Long userId) {
        List<HouseholdMember> memberships = memberMapper.selectList(
                new LambdaQueryWrapper<HouseholdMember>()
                        .eq(HouseholdMember::getUserId, userId)
                        .eq(HouseholdMember::getStatus, "active")
        );

        return memberships.stream().map(m -> {
            Household household = householdMapper.selectById(m.getHouseholdId());
            if (household == null) {
                return null;
            }
            return SpaceResponse.from(household, m.getRole());
        }).filter(r -> r != null).collect(Collectors.toList());
    }

    /**
     * Get a specific space if the user is a member.
     */
    public SpaceResponse getSpace(Long userId, Long spaceId) {
        HouseholdMember membership = getMembershipOrThrow(userId, spaceId);
        Household household = householdMapper.selectById(spaceId);
        if (household == null) {
            throw new BusinessException("NOT_FOUND", "Space not found");
        }
        return SpaceResponse.from(household, membership.getRole());
    }

    /**
     * Update space name. Only owner or admin can update.
     */
    @Transactional
    public SpaceResponse updateSpace(Long userId, Long spaceId, UpdateSpaceRequest request) {
        HouseholdMember membership = getMembershipOrThrow(userId, spaceId);
        requireRole(membership.getRole(), "owner", "admin");

        Household household = householdMapper.selectById(spaceId);
        if (household == null) {
            throw new BusinessException("NOT_FOUND", "Space not found");
        }

        if (request.name() != null && !request.name().isBlank()) {
            household.setName(request.name());
        }
        household.setUpdatedAt(LocalDateTime.now());
        householdMapper.updateById(household);

        return SpaceResponse.from(household, membership.getRole());
    }

    /**
     * Create a family space. The creator becomes the owner.
     */
    @Transactional
    public SpaceResponse createSpace(Long userId, CreateSpaceRequest request) {
        String spaceType = request.type() != null ? request.type() : "family";

        Household household = new Household();
        household.setName(request.name());
        household.setType(spaceType);
        household.setOwnerUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        household.setCreatedAt(now);
        household.setUpdatedAt(now);
        householdMapper.insert(household);

        HouseholdMember member = new HouseholdMember();
        member.setHouseholdId(household.getId());
        member.setUserId(userId);
        member.setRole("owner");
        member.setStatus("active");
        member.setCreatedAt(now);
        member.setUpdatedAt(now);
        memberMapper.insert(member);

        return SpaceResponse.from(household, "owner");
    }

    /**
     * List members of a space. Caller must be a member.
     */
    public List<MemberResponse> listMembers(Long userId, Long spaceId) {
        getMembershipOrThrow(userId, spaceId);

        List<HouseholdMember> members = memberMapper.selectList(
                new LambdaQueryWrapper<HouseholdMember>()
                        .eq(HouseholdMember::getHouseholdId, spaceId)
                        .eq(HouseholdMember::getStatus, "active")
        );

        return members.stream().map(m -> {
            UserAccount user = userMapper.selectById(m.getUserId());
            String email = user != null ? user.getEmail() : "unknown";
            String displayName = user != null ? user.getDisplayName() : "unknown";
            return MemberResponse.from(m, email, displayName);
        }).collect(Collectors.toList());
    }

    /**
     * Add a member to a space by email. Only owner/admin can add.
     */
    @Transactional
    public MemberResponse addMember(Long userId, Long spaceId, AddMemberRequest request) {
        HouseholdMember callerMembership = getMembershipOrThrow(userId, spaceId);
        requireRole(callerMembership.getRole(), "owner", "admin");

        UserAccount targetUser = userMapper.selectOne(
                new LambdaQueryWrapper<UserAccount>()
                        .eq(UserAccount::getEmail, request.email().toLowerCase())
        );
        if (targetUser == null) {
            throw new BusinessException("NOT_FOUND", "User with email " + request.email() + " not found");
        }

        HouseholdMember existing = memberMapper.selectOne(
                new LambdaQueryWrapper<HouseholdMember>()
                        .eq(HouseholdMember::getHouseholdId, spaceId)
                        .eq(HouseholdMember::getUserId, targetUser.getId())
        );
        String role = normalizeRole(request.role());
        if (existing != null && "active".equals(existing.getStatus())) {
            throw new BusinessException("CONFLICT", "User is already a member of this space");
        }
        if (existing != null) {
            existing.setRole(role);
            existing.setStatus("active");
            existing.setUpdatedAt(LocalDateTime.now());
            memberMapper.updateById(existing);
            return MemberResponse.from(existing, targetUser.getEmail(), targetUser.getDisplayName());
        }

        HouseholdMember newMember = new HouseholdMember();
        newMember.setHouseholdId(spaceId);
        newMember.setUserId(targetUser.getId());
        newMember.setRole(role);
        newMember.setStatus("active");
        LocalDateTime now = LocalDateTime.now();
        newMember.setCreatedAt(now);
        newMember.setUpdatedAt(now);
        memberMapper.insert(newMember);

        return MemberResponse.from(newMember, targetUser.getEmail(), targetUser.getDisplayName());
    }

    /**
     * Update a member's role. Only owner/admin can update.
     */
    @Transactional
    public MemberResponse updateMemberRole(Long userId, Long spaceId, Long memberId, String newRole) {
        HouseholdMember callerMembership = getMembershipOrThrow(userId, spaceId);
        requireRole(callerMembership.getRole(), "owner", "admin");
        String role = normalizeRole(newRole);

        HouseholdMember targetMember = getActiveMemberOrThrow(spaceId, memberId);
        ensureManagerWillRemain(spaceId, targetMember, role);

        targetMember.setRole(role);
        targetMember.setUpdatedAt(LocalDateTime.now());
        memberMapper.updateById(targetMember);

        UserAccount user = userMapper.selectById(targetMember.getUserId());
        String email = user != null ? user.getEmail() : "unknown";
        String displayName = user != null ? user.getDisplayName() : "unknown";
        return MemberResponse.from(targetMember, email, displayName);
    }

    /**
     * Remove a member from a space. Only owner/admin can remove, and at least one owner/admin must remain.
     */
    @Transactional
    public void removeMember(Long userId, Long spaceId, Long memberId) {
        HouseholdMember callerMembership = getMembershipOrThrow(userId, spaceId);
        requireRole(callerMembership.getRole(), "owner", "admin");

        HouseholdMember targetMember = getActiveMemberOrThrow(spaceId, memberId);
        ensureManagerWillRemain(spaceId, targetMember, null);

        targetMember.setStatus("removed");
        targetMember.setUpdatedAt(LocalDateTime.now());
        memberMapper.updateById(targetMember);
    }

    /**
     * Verify that a user belongs to a space, throwing FORBIDDEN if not.
     */
    public void requireSpaceMembership(Long userId, Long spaceId) {
        getMembershipOrThrow(userId, spaceId);
    }

    /**
     * Verify that a user has one of the required roles in a space.
     */
    public void requireSpaceRole(Long userId, Long spaceId, String... roles) {
        HouseholdMember membership = getMembershipOrThrow(userId, spaceId);
        requireRole(membership.getRole(), roles);
    }

    private HouseholdMember getMembershipOrThrow(Long userId, Long spaceId) {
        HouseholdMember membership = memberMapper.selectOne(
                new LambdaQueryWrapper<HouseholdMember>()
                        .eq(HouseholdMember::getHouseholdId, spaceId)
                        .eq(HouseholdMember::getUserId, userId)
                        .eq(HouseholdMember::getStatus, "active")
        );
        if (membership == null) {
            throw new BusinessException("FORBIDDEN", "You are not a member of this space");
        }
        return membership;
    }

    private HouseholdMember getActiveMemberOrThrow(Long spaceId, Long memberId) {
        HouseholdMember targetMember = memberMapper.selectById(memberId);
        if (targetMember == null
                || !targetMember.getHouseholdId().equals(spaceId)
                || !"active".equals(targetMember.getStatus())) {
            throw new BusinessException("NOT_FOUND", "Member not found in this space");
        }
        return targetMember;
    }

    private String normalizeRole(String role) {
        String normalized = role == null || role.isBlank() ? "member" : role.trim();
        if (!VALID_ROLES.contains(normalized)) {
            throw new BusinessException("VALIDATION_ERROR", "Invalid member role");
        }
        return normalized;
    }

    private void ensureManagerWillRemain(Long spaceId, HouseholdMember targetMember, String newRole) {
        if (!MANAGER_ROLES.contains(targetMember.getRole())) {
            return;
        }
        if (newRole != null && MANAGER_ROLES.contains(newRole)) {
            return;
        }
        long activeManagers = memberMapper.selectCount(
                new LambdaQueryWrapper<HouseholdMember>()
                        .eq(HouseholdMember::getHouseholdId, spaceId)
                        .eq(HouseholdMember::getStatus, "active")
                        .in(HouseholdMember::getRole, MANAGER_ROLES)
        );
        if (activeManagers <= 1) {
            throw new BusinessException("BUSINESS_ERROR", "At least one owner or admin must remain");
        }
    }

    private void requireRole(String actual, String... allowed) {
        for (String role : allowed) {
            if (role.equals(actual)) {
                return;
            }
        }
        throw new BusinessException("FORBIDDEN", "Insufficient permissions");
    }
}
