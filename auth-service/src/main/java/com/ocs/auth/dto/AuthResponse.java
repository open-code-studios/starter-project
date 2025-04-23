package com.ocs.auth.dto;

import com.ocs.auth.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private String message;
    private String token;        // Will be null in response body (stored in cookie)
    private String refreshToken; // Will be null in response body (stored in cookie)
} 