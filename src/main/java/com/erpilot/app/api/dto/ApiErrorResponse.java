package com.erpilot.app.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ApiErrorResponse {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String traceId;

    public static ApiErrorResponse of(int status, String error, String message, String traceId) {
        return new ApiErrorResponse(Instant.now(), status, error, message, traceId);
    }}