package com.lifepilot.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.common.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private static final Long USER_ID = 1L;

    // --- updateProfile ---

    @Test
    void updateProfile_updatesDisplayName() {
        UserAccount user = buildUser();
        when(userMapper.selectById(USER_ID)).thenReturn(user);

        UserAccount result = userService.updateProfile(USER_ID, "New Name", null);

        assertEquals("New Name", result.getDisplayName());
        assertNull(result.getAvatarUrl());
        verify(userMapper).updateById(result);
    }

    @Test
    void updateProfile_updatesAvatarUrl() {
        UserAccount user = buildUser();
        when(userMapper.selectById(USER_ID)).thenReturn(user);

        UserAccount result = userService.updateProfile(USER_ID, null, "https://example.com/avatar.jpg");

        assertEquals("Original", result.getDisplayName());
        assertEquals("https://example.com/avatar.jpg", result.getAvatarUrl());
    }

    @Test
    void updateProfile_clearsAvatarUrlWhenBlank() {
        UserAccount user = buildUser();
        user.setAvatarUrl("https://old.jpg");
        when(userMapper.selectById(USER_ID)).thenReturn(user);

        UserAccount result = userService.updateProfile(USER_ID, null, "  ");

        assertNull(result.getAvatarUrl());
    }

    @Test
    void updateProfile_preservesExistingValuesWhenNull() {
        UserAccount user = buildUser();
        user.setAvatarUrl("https://existing.jpg");
        when(userMapper.selectById(USER_ID)).thenReturn(user);

        UserAccount result = userService.updateProfile(USER_ID, null, null);

        assertEquals("Original", result.getDisplayName());
        assertEquals("https://existing.jpg", result.getAvatarUrl());
    }

    @Test
    void updateProfile_throwsWhenUserNotFound() {
        when(userMapper.selectById(USER_ID)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> userService.updateProfile(USER_ID, "Name", null));
    }

    @Test
    void updateProfile_throwsWhenUserInactive() {
        UserAccount user = buildUser();
        user.setStatus("SUSPENDED");
        when(userMapper.selectById(USER_ID)).thenReturn(user);

        assertThrows(BusinessException.class,
                () -> userService.updateProfile(USER_ID, "Name", null));
    }

    @Test
    void updateProfile_trimsDisplayName() {
        UserAccount user = buildUser();
        when(userMapper.selectById(USER_ID)).thenReturn(user);

        UserAccount result = userService.updateProfile(USER_ID, "  Trimmed  ", null);

        assertEquals("Trimmed", result.getDisplayName());
    }

    // --- requireById ---

    @Test
    void requireById_returnsUserWhenActive() {
        UserAccount user = buildUser();
        when(userMapper.selectById(USER_ID)).thenReturn(user);

        UserAccount result = userService.requireById(USER_ID);

        assertEquals(USER_ID, result.getId());
    }

    @Test
    void requireById_throwsWhenUserNotFound() {
        when(userMapper.selectById(USER_ID)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> userService.requireById(USER_ID));
    }

    @Test
    void requireById_throwsWhenUserInactive() {
        UserAccount user = buildUser();
        user.setStatus("SUSPENDED");
        when(userMapper.selectById(USER_ID)).thenReturn(user);

        assertThrows(BusinessException.class,
                () -> userService.requireById(USER_ID));
    }

    // --- findByEmail ---

    @Test
    void findByEmail_findsExistingUser() {
        UserAccount user = buildUser();
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        Optional<UserAccount> result = userService.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void findByEmail_returnsEmptyWhenNotFound() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        Optional<UserAccount> result = userService.findByEmail("missing@example.com");

        assertTrue(result.isEmpty());
    }

    // --- helper ---

    private UserAccount buildUser() {
        UserAccount user = new UserAccount();
        user.setId(USER_ID);
        user.setEmail("test@example.com");
        user.setDisplayName("Original");
        user.setPasswordHash("hashed");
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}