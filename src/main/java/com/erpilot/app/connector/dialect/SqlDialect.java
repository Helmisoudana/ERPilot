package com.erpilot.app.connector.dialect;

public interface SqlDialect {
    String getName();

    String applyLimit(String sql , int limit);

    String quoteIdentifier(String identifier);

    String getDriverClassName();

    String normalizeType(String nativeType);

    default String getValidationQuery() {
        return "SELECT 1";
    }

}