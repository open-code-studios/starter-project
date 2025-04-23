package com.ocs.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import java.util.Map;

@Getter
public class AuthException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;
    private final Map<String, String> details;

    public AuthException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.details = null;
    }

    public AuthException(String message, HttpStatus status, String errorCode, Map<String, String> details) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.details = details;
    }
} 