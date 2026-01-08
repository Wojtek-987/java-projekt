package com.quiz.quizapp.api.error;

import com.quiz.quizapp.common.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice(basePackages = "com.quiz.quizapp.api")
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(base(HttpStatus.NOT_FOUND, ex.getMessage(), req, List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ApiErrorResponse.FieldViolation> violations = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toViolation)
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(base(HttpStatus.BAD_REQUEST, "Validation failed", req, violations));
    }

    @ExceptionHandler(BindException.class)
    ResponseEntity<ApiErrorResponse> bind(BindException ex, HttpServletRequest req) {
        List<ApiErrorResponse.FieldViolation> violations = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toViolation)
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(base(HttpStatus.BAD_REQUEST, "Validation failed", req, violations));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiErrorResponse> badRequest(IllegalArgumentException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(base(HttpStatus.BAD_REQUEST, safe(ex.getMessage(), "Bad request"), req, List.of()));
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ApiErrorResponse> conflict(IllegalStateException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(base(HttpStatus.CONFLICT, safe(ex.getMessage(), "Conflict"), req, List.of()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> generic(Exception ex, HttpServletRequest req) {
        // Avoid leaking internals
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(base(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req, List.of()));
    }

    private ApiErrorResponse base(HttpStatus status, String message, HttpServletRequest req,
                                  List<ApiErrorResponse.FieldViolation> violations) {
        return new ApiErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                req.getRequestURI(),
                violations
        );
    }

    private ApiErrorResponse.FieldViolation toViolation(FieldError fe) {
        return new ApiErrorResponse.FieldViolation(fe.getField(), fe.getDefaultMessage());
    }

    private String safe(String msg, String fallback) {
        if (msg == null || msg.isBlank()) return fallback;
        return msg.length() > 300 ? msg.substring(0, 297) + "..." : msg;
    }
}
