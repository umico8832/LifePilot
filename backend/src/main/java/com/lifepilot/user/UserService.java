package com.lifepilot.user;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.common.BusinessException;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserAccount createUser(String email, String password, String displayName) {
        String normalizedEmail = normalizeEmail(email);

        if (findByEmail(normalizedEmail).isPresent()) {
            throw new BusinessException("CONFLICT", "Email is already registered");
        }

        LocalDateTime now = LocalDateTime.now();
        UserAccount user = new UserAccount();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setDisplayName(displayName.trim());
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
        return user;
    }

    public Optional<UserAccount> findByEmail(String email) {
        return Optional.ofNullable(userMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getEmail, normalizeEmail(email))));
    }

    public UserAccount requireById(Long userId) {
        UserAccount user = userMapper.selectById(userId);
        if (user == null || !UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new BusinessException("UNAUTHORIZED", "Invalid user session");
        }
        return user;
    }

    public boolean matchesPassword(String rawPassword, UserAccount user) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

