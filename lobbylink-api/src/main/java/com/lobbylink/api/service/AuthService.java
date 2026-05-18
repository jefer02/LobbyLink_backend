package com.lobbylink.api.service;

import com.lobbylink.api.dto.request.LoginRequest;
import com.lobbylink.api.dto.request.RegisterRequest;
import com.lobbylink.api.dto.response.AuthResponse;
import com.lobbylink.api.exception.BadRequestException;
import com.lobbylink.api.exception.ConflictException;
import com.lobbylink.api.model.User;
import com.lobbylink.api.repository.UserRepository;
import com.lobbylink.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Business logic for user registration and login.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user, hashes the password with BCrypt and returns a JWT.
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered: " + request.getEmail());
        }

        var user = User.builder()
                .gamertag(request.getGamertag())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        String token = jwtService.generateToken(user.getEmail());
        return buildAuthResponse(user, token);
    }

    /**
     * Authenticates the user with Spring Security (BCrypt check) and returns a JWT.
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        String token = jwtService.generateToken(user.getEmail());
        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user, token);
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .gamertag(user.getGamertag())
                .email(user.getEmail())
                .build();
    }
}
