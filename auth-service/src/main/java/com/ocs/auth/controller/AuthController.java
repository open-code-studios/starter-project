package com.ocs.auth.controller;

import com.ocs.auth.dto.AuthResponse;
import com.ocs.auth.dto.LoginRequest;
import com.ocs.auth.dto.RegisterRequest;
import com.ocs.auth.security.CookieUtils;
import com.ocs.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        authResponse.setMessage("Registration successful. Please login to continue.");
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        
        // Set tokens in cookies
        cookieUtils.addTokenCookie(response, authResponse.getToken());
        cookieUtils.addRefreshTokenCookie(response, authResponse.getRefreshToken());
        
        // Remove tokens from response
        authResponse.setToken(null);
        authResponse.setRefreshToken(null);
        authResponse.setMessage("Login successful");
        
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue(name = "${application.security.jwt.refresh-token.cookie-name}") String refreshToken,
            HttpServletResponse response) {
            AuthResponse authResponse = authService.refreshToken(refreshToken);
        
        // Set new tokens in cookies
        cookieUtils.addTokenCookie(response, authResponse.getToken());
        cookieUtils.addRefreshTokenCookie(response, refreshToken);
        
        // Remove tokens from response
        authResponse.setToken(null);
        authResponse.setRefreshToken(null);
        authResponse.setMessage("Token refreshed successfully");
        
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(
            @CookieValue(name = "${application.security.jwt.cookie-name}") String token) {
        return ResponseEntity.ok(authService.validateToken(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletResponse response) {
        cookieUtils.deleteTokenCookie(response);
        cookieUtils.deleteRefreshTokenCookie(response);
        return ResponseEntity.ok(AuthResponse.builder()
                .message("Logout successful")
                .build());
    }
} 