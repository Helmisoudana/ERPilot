package com.erpilot.app.connector.dialect;

// import org.springframework.stereotype.Component;

// @Component
public class SqlServerDialect implements SqlDialect {

    @Override
    public String getName() {
        return "sqlserver";
    }

    @Override
    public String applyLimit(String sql, int limit) {
        return sql.replaceFirst("(?i)SELECT", "SELECT TOP " + limit);
    }

    @Override
    public String quoteIdentifier(String identifier) {
        return "[" + identifier + "]";
    }

    @Override
    public String getDriverClassName() {
        return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    }

    @Override
    public String normalizeType(String nativeType) {
        return switch (nativeType.toLowerCase()) {
            case "nvarchar", "varchar" -> "VARCHAR";
            case "datetime2", "datetime" -> "TIMESTAMP";
            case "bit" -> "BOOLEAN";
            case "int" -> "INTEGER";
            case "bigint" -> "BIGINT";
            default -> nativeType.toUpperCase();
        };
    }
}