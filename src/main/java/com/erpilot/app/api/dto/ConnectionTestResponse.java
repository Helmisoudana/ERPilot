package com.erpilot.app.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConnectionTestResponse {
    private boolean success;
    private String dialectDetected;
    private String message;
}