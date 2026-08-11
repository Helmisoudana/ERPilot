package com.erpilot.app.common.exception;


public class SqlValidationException extends RuntimeException {
    public SqlValidationException(String message) {
        super(message);
    }
}