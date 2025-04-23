package com.ocs.auth.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class InvalidTokenException extends AuthException {
    public InvalidTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
    }

    public InvalidTokenException(String message, Map<String, String> details) {
        super(message, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", details);
    }
} 