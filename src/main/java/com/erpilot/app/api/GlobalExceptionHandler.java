package com.erpilot.app.api;

import com.erpilot.app.api.dto.ApiErrorResponse;
import com.erpilot.app.common.exception.*;
import com.erpilot.app.core.exception.QuestionNotAnswerableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConnectionException.class)
    public ResponseEntity<ApiErrorResponse> handleConnection(ConnectionException e) {
        return build(HttpStatus.BAD_GATEWAY, e);
    }

    @ExceptionHandler(SchemaIntrospectionException.class)
    public ResponseEntity<ApiErrorResponse> handleSchemaIntrospection(SchemaIntrospectionException e) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    @ExceptionHandler(UnsupportedDialectException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedDialect(UnsupportedDialectException e) {
        return build(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(SqlValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleSqlValidation(SqlValidationException e) {
        return build(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(SecurityRuleViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleSecurityViolation(SecurityRuleViolationException e) {
        return build(HttpStatus.FORBIDDEN, e);
    }

    @ExceptionHandler(QueryExecutionException.class)
    public ResponseEntity<ApiErrorResponse> handleQueryExecution(QueryExecutionException e) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, e);
    }

    @ExceptionHandler(QuestionNotAnswerableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotAnswerable(QuestionNotAnswerableException e) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, e);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception e) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, Exception e) {
        ApiErrorResponse body = ApiErrorResponse.of(status.value(), status.getReasonPhrase(), e.getMessage());
        return ResponseEntity.status(status).body(body);
    }
}