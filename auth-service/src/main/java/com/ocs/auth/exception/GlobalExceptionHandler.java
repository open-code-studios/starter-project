package com.ocs.auth.exception;

import com.ocs.auth.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex, WebRequest request) {
        log.error("Auth Exception: {}", ex.getMessage(), ex);
        return buildErrorResponse(ex, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        log.error("Bad Credentials: {}", ex.getMessage(), ex);
        return buildErrorResponse(
            new AuthException("Invalid username or password", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"),
            request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.error("Access Denied: {}", ex.getMessage(), ex);
        return buildErrorResponse(
            new AuthException("Access denied", HttpStatus.FORBIDDEN, "ACCESS_DENIED"),
            request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation Error: {}", ex.getMessage(), ex);
        
        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult().getAllErrors().stream()
                // Sometimes, binding errors are not field-specific (e.g., object-level errors) thats why I use if-else
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return ErrorResponse.ValidationError.builder()
                                .field(fieldError.getField())
                                .message(fieldError.getDefaultMessage())
                                .rejectedValue(fieldError.getRejectedValue() != null ?
                                        fieldError.getRejectedValue().toString() : null)
                                .build();
                    } else {
                        return ErrorResponse.ValidationError.builder()
                                .field("global")
                                .message(error.getDefaultMessage())
                                .rejectedValue(null)
                                .build();
                    }
                })
            .collect(Collectors.toList());

        String path = (request instanceof ServletWebRequest swr) ? swr.getRequest().getRequestURI() : "N/A";

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed")
                .path(path)
                .errorCode("VALIDATION_ERROR")
                .validationErrors(validationErrors)
                .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        log.error("Constraint Violation: {}", ex.getMessage(), ex);
        
        Map<String, String> details = ex.getConstraintViolations().stream()
            .collect(Collectors.toMap(
                violation -> violation.getPropertyPath().toString(),
                violation -> violation.getMessage()
            ));

        return buildErrorResponse(
            new AuthException("Constraint violation", HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION", details),
            request
        );
    }

    @ExceptionHandler(RegistrationFailedException.class)
    public ResponseEntity<ErrorResponse> handleRegistrationFailedException(RegistrationFailedException ex, WebRequest request) {
        log.warn("Registration failed: {}", ex.getMessage(), ex);

        return buildErrorResponse(
                new AuthException(
                        ex.getMessage(),
                        ex.getStatus(), // likely HttpStatus.CONFLICT
                        ex.getErrorCode(),
                        ex.getDetails()
                ),
                request
        );
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                new AuthException("Unexpected runtime error", HttpStatus.INTERNAL_SERVER_ERROR, "UNHANDLED_RUNTIME_EXCEPTION"),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildErrorResponse(
            new AuthException("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR"),
            request
        );
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(AuthException ex, WebRequest request) {
        return ResponseEntity.status(ex.getStatus())
            .body(ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .message(ex.getMessage())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .errorCode(ex.getErrorCode())
                .details(ex.getDetails())
                .build());
    }
} 