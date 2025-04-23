package com.ocs.auth.service;

import com.ocs.auth.dto.UserResponse;
import com.ocs.auth.exception.UserNotFoundException;
import com.ocs.auth.model.Role;
import com.ocs.auth.model.User;
import com.ocs.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final AuthService authService;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAllByRoleIn(List.of(Role.USER, Role.ADMIN)).stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return convertToUserResponse(user);
    }

    public UserResponse updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        // Only allow updating to USER or ADMIN roles
        if (!role.equals("USER") && !role.equals("ADMIN")) {
            throw new IllegalArgumentException("Invalid role. Only USER and ADMIN roles are allowed.");
        }
        
        user.setRole(role.equals("ADMIN") ? Role.ADMIN : Role.USER);
        user = userRepository.save(user);
        return convertToUserResponse(user);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
} 