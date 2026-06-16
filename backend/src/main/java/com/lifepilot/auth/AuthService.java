package com.lifepilot.auth;

import org.springframework.stereotype.Service;

import com.lifepilot.auth.dto.AuthResponse;
import com.lifepilot.auth.dto.LoginRequest;
import com.lifepilot.auth.dto.RegisterRequest;
import com.lifepilot.common.BusinessException;
import com.lifepilot.security.JwtService;
import com.lifepilot.user.UserAccount;
import com.lifepilot.user.UserService;
import com.lifepilot.user.dto.CurrentUserResponse;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthService(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        UserAccount user = userService.createUser(request.email(), request.password(), request.displayName());
        return toAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        UserAccount user = userService.findByEmail(request.email())
                .filter(found -> userService.matchesPassword(request.password(), found))
                .orElseThrow(() -> new BusinessException("UNAUTHORIZED", "Invalid email or password"));

        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(UserAccount user) {
        return new AuthResponse(
                "Bearer",
                jwtService.createToken(user),
                CurrentUserResponse.from(user)
        );
    }
}

