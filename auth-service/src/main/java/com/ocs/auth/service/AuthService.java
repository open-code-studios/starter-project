package com.ocs.auth.service;

import com.ocs.auth.dto.AuthResponse;
import com.ocs.auth.dto.LoginRequest;
import com.ocs.auth.dto.RegisterRequest;
import com.ocs.auth.enums.ResponseKeyEnum;
import com.ocs.auth.exception.UserAlreadyExistsException;
import com.ocs.auth.exception.UserNotFoundException;
import com.ocs.auth.exception.InvalidTokenException;
import com.ocs.auth.model.Role;
import com.ocs.auth.model.User;
import com.ocs.auth.repository.UserRepository;
import com.ocs.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        try {
            validateUserDoesNotExist(request.getUsername(), request.getEmail());

            // Check if trying to create SUPER_ADMIN
            if (request.getRole() == Role.SUPER_ADMIN) {
                throw new IllegalArgumentException("Cannot create SUPER_ADMIN role. Only one SUPER_ADMIN can exist.");
            }

            // Check if trying to create ADMIN
            if (request.getRole() == Role.ADMIN) {
                // Get current user (if any) to check permissions
                try {
                    User currentUser = getCurrentUser();
                    if (currentUser.getRole() != Role.SUPER_ADMIN) {
                        throw new IllegalArgumentException("Only SUPER_ADMIN can create ADMIN users.");
                    }
                } catch (UserNotFoundException e) {
                    // If no user is logged in, they can't create ADMIN
                    throw new IllegalArgumentException("Only SUPER_ADMIN can create ADMIN users.");
                }
            }

            // If no role specified, default to USER
            Role role = request.getRole() != null ? request.getRole() : Role.USER;
            User user = createAndSaveUser(request, role);
            
            return AuthResponse.builder()
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getRole())
                    .build();
        } catch (UserAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            Map<String, String> details = new HashMap<>();
            details.put(ResponseKeyEnum.ERROR.getValue(), e.getMessage());
            throw new UserAlreadyExistsException("Registration failed", details);
        }
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticateUser(request.getUsername(), request.getPassword());
            User user = findUserByUsername(request.getUsername());
            return createAuthResponse(user);
        } catch (BadCredentialsException e) {
            Map<String, String> details = new HashMap<>();
            details.put(ResponseKeyEnum.USERNAME.getValue(), request.getUsername());
            throw new UserNotFoundException("Invalid username or password", details);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            Map<String, String> details = new HashMap<>();
            details.put(ResponseKeyEnum.ERROR.getValue(), e.getMessage());
            throw new UserNotFoundException("Login failed", details);
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        try {
            if (!jwtService.validateToken(refreshToken)) {
                throw new InvalidTokenException("Invalid/Expired refresh-token");
            }

            String username = jwtService.extractUsername(refreshToken);
            User user = findUserByUsername(username);
            
            return createAuthResponse(user);
        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            Map<String, String> details = new HashMap<>();
            details.put(ResponseKeyEnum.ERROR.getValue(), e.getMessage());
            throw new InvalidTokenException("Token refresh failed", details);
        }
    }

    public boolean validateToken(String token) {
        try {
            return jwtService.validateToken(token);
        } catch (Exception e) {
            Map<String, String> details = new HashMap<>();
            details.put(ResponseKeyEnum.ERROR.getValue(), e.getMessage());
            throw new InvalidTokenException("Token validation failed", details);
        }
    }

    public User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new UserNotFoundException("No authenticated user found");
            }
            return findUserByUsername(authentication.getName());
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            Map<String, String> details = new HashMap<>();
            details.put(ResponseKeyEnum.ERROR.getValue(), e.getMessage());
            throw new UserNotFoundException("Failed to get current user", details);
        }
    }

    public boolean hasRole(String role) {
        try {
            User currentUser = getCurrentUser();
            return currentUser.getRole().getRoleName().equals(role);
        } catch (Exception e) {
            Map<String, String> details = new HashMap<>();
            details.put(ResponseKeyEnum.ERROR.getValue(), e.getMessage());
            throw new UserNotFoundException("Failed to check role", details);
        }
    }

    public boolean hasAnyRole(String... roles) {
        try {
            User currentUser = getCurrentUser();
            for (String role : roles) {
                if (currentUser.getRole().getRoleName().equals(role)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Map<String, String> details = new HashMap<>();
            details.put(ResponseKeyEnum.ERROR.getValue(), e.getMessage());
            throw new UserNotFoundException("Failed to check roles", details);
        }
    }

    // Private helper methods
    private void validateUserDoesNotExist(String username, String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            Map<String, String> details = new HashMap<>();
            details.put(ResponseKeyEnum.USERNAME.getValue(), username);
            throw new UserAlreadyExistsException("Username already exists", details);
        }
        if (userRepository.findByEmail(email).isPresent()) {
            Map<String, String> details = new HashMap<>();
            details.put(ResponseKeyEnum.EMAIL.getValue(), email);
            throw new UserAlreadyExistsException("Email already in use", details);
        }
    }

    private User createAndSaveUser(RegisterRequest request, Role role) {
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(role)
                .build();
        return userRepository.save(user);
    }

    private Authentication authenticateUser(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    Map<String, String> details = new HashMap<>();
                    details.put(ResponseKeyEnum.USERNAME.getValue(), username);
                    return new UserNotFoundException("User not found", details);
                });
    }

    private AuthResponse createAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .role(user.getRole())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
} 