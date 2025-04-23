package com.ocs.auth.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class UserAlreadyExistsException extends AuthException {
    public UserAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT, "USER_ALREADY_EXISTS");
    }

    public UserAlreadyExistsException(String message, Map<String, String> details) {
        super(message, HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", details);
    }
} 