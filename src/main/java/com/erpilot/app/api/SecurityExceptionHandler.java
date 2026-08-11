package com.erpilot.app.api;

import com.erpilot.app.common.exception.SecurityRuleViolationException;
import com.erpilot.app.common.exception.SqlValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(SqlValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(SqlValidationException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "validation_failed", "message", e.getMessage()));
    }

    @ExceptionHandler(SecurityRuleViolationException.class)
    public ResponseEntity<Map<String, String>> handleRuleViolation(SecurityRuleViolationException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "security_rule_violation", "message", e.getMessage()));
    }
}