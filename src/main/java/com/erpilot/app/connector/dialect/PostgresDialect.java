package com.erpilot.app.connector.dialect;

import org.springframework.stereotype.Component;

@Component
public class PostgresDialect implements SqlDialect {

    @Override
    public String getName() {
        return "postgresql";
    }

    @Override
    public String applyLimit(String sql, int limit) {
        String trimmed = sql.trim();
        if (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + " LIMIT " + limit;
    }

    @Override
    public String quoteIdentifier(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public String getDriverClassName() {
        return "org.postgresql.Driver";
    }

    @Override
    public String normalizeType(String nativeType) {
        return switch (nativeType.toLowerCase()) {
            case "jsonb", "json" -> "JSON";
            case "numeric", "decimal" -> "DECIMAL";
            case "timestamptz", "timestamp with time zone" -> "TIMESTAMP_WITH_TZ";
            case "timestamp" -> "TIMESTAMP";
            case "int4", "integer" -> "INTEGER";
            case "int8", "bigint" -> "BIGINT";
            case "bool", "boolean" -> "BOOLEAN";
            case "varchar", "text" -> "VARCHAR";
            default -> nativeType.toUpperCase();
        };
    }
}