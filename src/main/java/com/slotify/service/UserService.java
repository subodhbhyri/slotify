package com.slotify.service;

import com.slotify.dto.request.CreateUserRequest;
import com.slotify.dto.response.UserResponse;
import com.slotify.exception.SlotifyException;
import com.slotify.model.User;
import com.slotify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new SlotifyException("User with email " + request.getEmail() + " already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .priority(request.getPriority())
                .build();

        User saved = userRepository.save(user);
        log.info("Created user with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new SlotifyException("User not found with id: " + id));
        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .priority(user.getPriority())
                .createdAt(user.getCreatedAt())
                .build();
    }
}