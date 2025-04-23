package com.ocs.auth.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class RegistrationFailedException extends AuthException {

    private static final String DEFAULT_ERROR_CODE = "REGISTRATION_FAILED";

    public RegistrationFailedException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, DEFAULT_ERROR_CODE);
    }

    public RegistrationFailedException(String message, Map<String, String> details) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, DEFAULT_ERROR_CODE, details);
    }
}
