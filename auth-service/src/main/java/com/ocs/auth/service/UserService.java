package com.ocs.auth.service;

import com.ocs.auth.dto.UserResponse;
import com.ocs.auth.dto.UserUpdateRequest;
import com.ocs.auth.model.User;
import com.ocs.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public UserResponse getCurrentUserProfile() {
        User currentUser = authService.getCurrentUser();
        return convertToUserResponse(currentUser);
    }

    public UserResponse updateUserProfile(UserUpdateRequest request) {
        User currentUser = authService.getCurrentUser();
        
        if (request.getFirstName() != null) {
            currentUser.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            currentUser.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            currentUser.setEmail(request.getEmail());
        }
        if (request.getPassword() != null) {
            currentUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        currentUser = userRepository.save(currentUser);
        return convertToUserResponse(currentUser);
    }

    public void deleteCurrentUser() {
        User currentUser = authService.getCurrentUser();
        userRepository.delete(currentUser);
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