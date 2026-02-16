package com.example.gameforum.auth;

import com.example.gameforum.auth.dto.AuthResponse;
import com.example.gameforum.auth.dto.LoginRequest;
import com.example.gameforum.auth.dto.RegisterRequest;
import com.example.gameforum.user.UserEntity;
import com.example.gameforum.user.UserRepository;
import com.example.gameforum.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthResponse register(RegisterRequest req) {
        if (users.existsByUsername(req.username())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (users.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already taken");
        }

        UserEntity u = UserEntity.builder()
                .username(req.username().trim())
                .email(req.email().trim().toLowerCase())
                .passwordHash(encoder.encode(req.password()))
                .role(UserRole.USER)
                .createdAt(OffsetDateTime.now())
                .build();

        u = users.save(u);

        String token = jwt.generateToken(u.getId(), u.getUsername(), u.getRole());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest req) {
        String login = req.login().trim();

        UserEntity u = users.findByUsername(login)
                .or(() -> users.findByEmail(login.toLowerCase()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwt.generateToken(u.getId(), u.getUsername(), u.getRole());
        return new AuthResponse(token);
    }

    public boolean isUsernameAvailable(String username) {
        if (username == null) {
            return false;
        }

        String normalized = username.trim();
        if (normalized.length() < 3) {
            return false;
        }

        return !users.existsByUsername(normalized);
    }
}
