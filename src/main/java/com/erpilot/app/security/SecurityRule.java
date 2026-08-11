package com.erpilot.app.security;


public interface SecurityRule {
    void apply(SqlValidationResult validation, String role);
}