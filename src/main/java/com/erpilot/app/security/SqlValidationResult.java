package com.erpilot.app.security;

import lombok.Value;

import java.util.List;

@Value
public class SqlValidationResult {
    boolean valid;
    String sql;
    List<String> tablesUsed;
    String errorMessage;

    public static SqlValidationResult valid(String sql, List<String> tablesUsed) {
        return new SqlValidationResult(true, sql, tablesUsed, null);
    }

    public static SqlValidationResult invalid(String errorMessage) {
        return new SqlValidationResult(false, null, List.of(), errorMessage);
    }
}