package com.erpilot.app.common.exception;


public class SecurityRuleViolationException extends RuntimeException {
    public SecurityRuleViolationException(String message) {
        super(message);
    }
}