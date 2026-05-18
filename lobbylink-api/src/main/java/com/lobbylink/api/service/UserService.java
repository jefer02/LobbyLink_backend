package com.lobbylink.api.service;

import com.lobbylink.api.dto.response.UserResponse;
import com.lobbylink.api.exception.ResourceNotFoundException;
import com.lobbylink.api.model.User;
import com.lobbylink.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for user profile operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /** Returns a password-safe DTO for the given user ID. */
    public UserResponse findByIdAsResponse(String id) {
        User user = findById(id);
        return UserResponse.builder()
                .id(user.getId())
                .gamertag(user.getGamertag())
                .email(user.getEmail())
                .build();
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void deleteById(String id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
        log.info("User deleted: {}", id);
    }
}
