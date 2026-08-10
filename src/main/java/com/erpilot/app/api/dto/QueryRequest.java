package com.erpilot.app.api.dto;

import com.erpilot.app.common.dto.ConnectionConfig;
import lombok.Data;

@Data
public class QueryRequest {
    private ConnectionConfig connection;
    private String sql;
    private int limit = 100;
}