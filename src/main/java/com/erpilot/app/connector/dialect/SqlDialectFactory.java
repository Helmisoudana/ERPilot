package com.erpilot.app.connector.dialect;

import com.erpilot.app.common.exception.UnsupportedDialectException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SqlDialectFactory {

    private final Map<String, SqlDialect> dialectsByName;

    public SqlDialectFactory(List<SqlDialect> dialectBeans) {
        this.dialectsByName = dialectBeans.stream()
                .collect(Collectors.toMap(SqlDialect::getName, d -> d));
    }

    public SqlDialect getDialect(String name) {
        SqlDialect dialect = dialectsByName.get(name.toLowerCase());
        if (dialect == null) {
            throw new UnsupportedDialectException(
                    "Dialecte non supporté: '" + name + "'. Disponibles: " + dialectsByName.keySet());
        }
        return dialect;
    }

    public SqlDialect detectFromProductName(String jdbcProductName) {
        String normalized = jdbcProductName.toLowerCase();
        if (normalized.contains("postgresql")) return getDialect("postgresql");
        if (normalized.contains("oracle")) return getDialect("oracle");
        if (normalized.contains("microsoft sql server")) return getDialect("sqlserver");
        if (normalized.contains("mysql")) return getDialect("mysql");
        throw new UnsupportedDialectException(
                "SGBD non reconnu automatiquement: '" + jdbcProductName + "'. " +
                        "Précise dialectName explicitement dans ConnectionConfig.");
    }

    public boolean isSupported(String name) {
        return dialectsByName.containsKey(name.toLowerCase());
    }
}