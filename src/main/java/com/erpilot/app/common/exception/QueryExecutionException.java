package com.erpilot.app.common.exception;

public class QueryExecutionException extends RuntimeException {
    public QueryExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}