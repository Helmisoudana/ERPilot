package com.erpilot.app.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class QueryResult {
    private List<String> columnsNames;
    private List<Map<String,Object>> rows;
    private int rowCount;
    private long executionTimeMs;
}
