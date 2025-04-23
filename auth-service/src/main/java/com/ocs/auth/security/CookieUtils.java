package com.ocs.auth.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    @Value("${application.security.jwt.cookie-name}")
    private String tokenCookieName;

    @Value("${application.security.jwt.refresh-token.cookie-name}")
    private String refreshTokenCookieName;

    @Value("${application.security.jwt.cookie.name}")
    private String cookieName;

    public void addTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(tokenCookieName, token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(24 * 60 * 60); // 24 hours
        response.addCookie(cookie);
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(refreshTokenCookieName, refreshToken);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(cookie);
    }

    public void deleteTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(tokenCookieName, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(refreshTokenCookieName, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
} 