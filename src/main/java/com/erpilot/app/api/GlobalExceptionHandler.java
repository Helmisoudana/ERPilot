package com.erpilot.app.api;

import com.erpilot.app.api.dto.ApiErrorResponse;
import com.erpilot.app.common.exception.*;
import com.erpilot.app.core.exception.QuestionNotAnswerableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.erpilot.app.common.filter.RequestTraceFilter.TRACE_ID_MDC_KEY;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String GENERIC_MESSAGE =
            "Une erreur inattendue est survenue. L'équipe technique a été notifiée.";

    @ExceptionHandler(ConnectionException.class)
    public ResponseEntity<ApiErrorResponse> handleConnection(ConnectionException e) {
        return buildAndLog(HttpStatus.BAD_GATEWAY, e, e.getMessage(), true);
    }

    @ExceptionHandler(SchemaIntrospectionException.class)
    public ResponseEntity<ApiErrorResponse> handleSchemaIntrospection(SchemaIntrospectionException e) {
        return buildAndLog(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage(), true);
    }

    @ExceptionHandler(UnsupportedDialectException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedDialect(UnsupportedDialectException e) {
        return buildAndLog(HttpStatus.BAD_REQUEST, e, e.getMessage(), false);
    }

    @ExceptionHandler(SqlValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleSqlValidation(SqlValidationException e) {
        return buildAndLog(HttpStatus.BAD_REQUEST, e, e.getMessage(), false);
    }

    @ExceptionHandler(SecurityRuleViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleSecurityViolation(SecurityRuleViolationException e) {
        return buildAndLog(HttpStatus.FORBIDDEN, e, e.getMessage(), false);
    }

    @ExceptionHandler(QueryExecutionException.class)
    public ResponseEntity<ApiErrorResponse> handleQueryExecution(QueryExecutionException e) {
        return buildAndLog(HttpStatus.UNPROCESSABLE_ENTITY, e, e.getMessage(), true);
    }

    @ExceptionHandler(QuestionNotAnswerableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotAnswerable(QuestionNotAnswerableException e) {
        return buildAndLog(HttpStatus.UNPROCESSABLE_ENTITY, e, e.getMessage(), false);
    }

    @ExceptionHandler(LlmGenerationException.class)
    public ResponseEntity<ApiErrorResponse> handleLlmGeneration(LlmGenerationException e) {
        // 503 : le problème vient du fournisseur IA (indisponibilité, quota, timeout...),
        // pas d'une erreur du client. Message générique côté client, détail complet en log.
        return buildAndLog(HttpStatus.SERVICE_UNAVAILABLE, e,
                "Le service IA est temporairement indisponible. Merci de réessayer dans quelques instants.",
                true);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception e) {
        return buildAndLog(HttpStatus.INTERNAL_SERVER_ERROR, e, GENERIC_MESSAGE, true);
    }


    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiErrorResponse> handleThrowable(Throwable e) {
        log.error("Erreur grave (Throwable) non gérée [traceId={}]", currentTraceId(), e);
        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                GENERIC_MESSAGE,
                currentTraceId());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ResponseEntity<ApiErrorResponse> buildAndLog(HttpStatus status, Exception e,
                                                         String clientMessage, boolean severe) {
        String traceId = currentTraceId();

        if (severe || status.is5xxServerError()) {
            log.error("[{}] {} — traceId={}", status.value(), e.getMessage(), traceId, e);
        } else {
            log.warn("[{}] {} — traceId={}", status.value(), e.getMessage(), traceId, e);
        }

        ApiErrorResponse body = ApiErrorResponse.of(
                status.value(), status.getReasonPhrase(), clientMessage, traceId);
        return ResponseEntity.status(status).body(body);
    }

    private String currentTraceId() {
        String traceId = MDC.get(TRACE_ID_MDC_KEY);
        return traceId != null ? traceId : "n/a";
    }
}
