package com.lifepilot.space;

import java.time.LocalDateTime;
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
        if (existing != null) {
            throw new BusinessException("CONFLICT", "User is already a member of this space");
        }

        String role = request.role() != null ? request.role() : "member";
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

        HouseholdMember targetMember = memberMapper.selectById(memberId);
        if (targetMember == null || !targetMember.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Member not found in this space");
        }

        targetMember.setRole(newRole);
        targetMember.setUpdatedAt(LocalDateTime.now());
        memberMapper.updateById(targetMember);

        UserAccount user = userMapper.selectById(targetMember.getUserId());
        String email = user != null ? user.getEmail() : "unknown";
        String displayName = user != null ? user.getDisplayName() : "unknown";
        return MemberResponse.from(targetMember, email, displayName);
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

    private void requireRole(String actual, String... allowed) {
        for (String role : allowed) {
            if (role.equals(actual)) {
                return;
            }
        }
        throw new BusinessException("FORBIDDEN", "Insufficient permissions");
    }
}