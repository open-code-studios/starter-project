package com.ocs.auth.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class UserNotFoundException extends AuthException {
    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
    }

    public UserNotFoundException(String message, Map<String, String> details) {
        super(message, HttpStatus.NOT_FOUND, "USER_NOT_FOUND", details);
    }
} 