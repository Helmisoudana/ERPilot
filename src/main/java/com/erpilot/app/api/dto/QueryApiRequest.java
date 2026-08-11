package com.erpilot.app.api.dto;

import lombok.Data;

@Data
public class QueryApiRequest {
    private String question;
    private String role = "user";
}