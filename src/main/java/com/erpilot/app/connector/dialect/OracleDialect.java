package com.erpilot.app.connector.dialect;

// import org.springframework.stereotype.Component;

// @Component
public class OracleDialect implements SqlDialect {

    @Override
    public String getName() {
        return "oracle";
    }

    @Override
    public String applyLimit(String sql, int limit) {
        String trimmed = sql.trim();
        if (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + " FETCH FIRST " + limit + " ROWS ONLY";
    }

    @Override
    public String quoteIdentifier(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public String getDriverClassName() {
        return "oracle.jdbc.OracleDriver";
    }

    @Override
    public String normalizeType(String nativeType) {
        return switch (nativeType.toUpperCase()) {
            case "NUMBER" -> "DECIMAL";
            case "VARCHAR2", "NVARCHAR2" -> "VARCHAR";
            case "CLOB" -> "TEXT";
            case "DATE" -> "TIMESTAMP";
            default -> nativeType.toUpperCase();
        };
    }

    @Override
    public String getValidationQuery() {
        return "SELECT 1 FROM DUAL";
    }
}