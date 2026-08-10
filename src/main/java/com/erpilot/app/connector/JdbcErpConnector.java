package com.erpilot.app.connector;

import com.erpilot.app.common.dto.*;
import com.erpilot.app.common.exception.*;
import com.erpilot.app.connector.dialect.SqlDialect;
import com.erpilot.app.connector.dialect.SqlDialectFactory;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.*;

@Slf4j
public class JdbcErpConnector implements ERPConnector {

    private final SqlDialectFactory dialectFactory;
    private Connection connection;
    private SqlDialect dialect;
    private ConnectionConfig config;

    public JdbcErpConnector(SqlDialectFactory dialectFactory) {
        this.dialectFactory = dialectFactory;
    }

    @Override
    public void connect(ConnectionConfig config) {
        this.config = config;
        try {
            // Dialecte explicite si fourni, sinon détection automatique après connexion
            if (config.getDialectName() != null) {
                this.dialect = dialectFactory.getDialect(config.getDialectName());
            }

            if (dialect != null) {
                Class.forName(dialect.getDriverClassName());
            }

            this.connection = DriverManager.getConnection(
                    config.getUrl(), config.getUsername(), config.getPassword());

            if (dialect == null) {
                String productName = connection.getMetaData().getDatabaseProductName();
                this.dialect = dialectFactory.detectFromProductName(productName);
            }

            log.info("Connecté à {} via dialecte {}", config.getUrl(), dialect.getName());

        } catch (ClassNotFoundException e) {
            throw new ConnectionException("Driver JDBC introuvable pour ce dialecte", e);
        } catch (SQLException e) {
            throw new ConnectionException("Échec de connexion à " + config.getUrl(), e);
        }
    }

    @Override
    public boolean testConnection() {
        ensureConnected();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(dialect.getValidationQuery());
            return true;
        } catch (SQLException e) {
            log.warn("Test de connexion échoué: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<TableMetadata> introspectSchema() {
        ensureConnected();
        List<TableMetadata> tables = new ArrayList<>();

        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet tableSet = meta.getTables(null, null, "%", new String[]{"TABLE"});

            while (tableSet.next()) {
                String tableName = tableSet.getString("TABLE_NAME");
                String schemaName = tableSet.getString("TABLE_SCHEM");

                TableMetadata table = new TableMetadata();
                table.setTableName(tableName);
                table.setSchemaName(schemaName);
                table.setColumns(extractColumns(meta, tableName));
                table.setForeignKeys(extractForeignKeys(meta, tableName));

                tables.add(table);
            }
            tableSet.close();

        } catch (SQLException e) {
            throw new SchemaIntrospectionException(
                    "Échec de l'introspection du schéma", e);
        }

        log.info("{} tables découvertes", tables.size());
        return tables;
    }

    private List<ColumnMetadata> extractColumns(DatabaseMetaData meta, String tableName) throws SQLException {
        List<ColumnMetadata> columns = new ArrayList<>();
        Set<String> primaryKeys = extractPrimaryKeys(meta, tableName);

        ResultSet columnSet = meta.getColumns(null, null, tableName, "%");
        while (columnSet.next()) {
            String colName = columnSet.getString("COLUMN_NAME");
            String nativeType = columnSet.getString("TYPE_NAME");
            boolean nullable = columnSet.getInt("NULLABLE") == DatabaseMetaData.columnNullable;

            columns.add(new ColumnMetadata(
                    colName,
                    nativeType,
                    dialect.normalizeType(nativeType),
                    nullable,
                    primaryKeys.contains(colName)
            ));
        }
        columnSet.close();
        return columns;
    }

    private Set<String> extractPrimaryKeys(DatabaseMetaData meta, String tableName) throws SQLException {
        Set<String> pks = new HashSet<>();
        ResultSet pkSet = meta.getPrimaryKeys(null, null, tableName);
        while (pkSet.next()) {
            pks.add(pkSet.getString("COLUMN_NAME"));
        }
        pkSet.close();
        return pks;
    }

    private List<String> extractForeignKeys(DatabaseMetaData meta, String tableName) throws SQLException {
        List<String> fks = new ArrayList<>();
        ResultSet fkSet = meta.getImportedKeys(null, null, tableName);
        while (fkSet.next()) {
            String fkColumn = fkSet.getString("FKCOLUMN_NAME");
            String pkTable = fkSet.getString("PKTABLE_NAME");
            String pkColumn = fkSet.getString("PKCOLUMN_NAME");
            fks.add(fkColumn + " -> " + pkTable + "." + pkColumn);
        }
        fkSet.close();
        return fks;
    }

    @Override
    public QueryResult executeQuery(String sql, int limit) {
        ensureConnected();
        String finalSql = dialect.applyLimit(sql, limit);
        long start = System.currentTimeMillis();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(finalSql)) {

            ResultSetMetaData rsMeta = rs.getMetaData();
            int columnCount = rsMeta.getColumnCount();

            List<String> columnNames = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(rsMeta.getColumnLabel(i));
            }

            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (String col : columnNames) {
                    row.put(col, rs.getObject(col));
                }
                rows.add(row);
            }

            long duration = System.currentTimeMillis() - start;
            return new QueryResult(columnNames, rows, rows.size(), duration);

        } catch (SQLException e) {
            // Le message exact est réutilisé plus tard par la boucle d'auto-correction du LLM
            throw new QueryExecutionException("Échec d'exécution: " + e.getMessage(), e);
        }
    }

    @Override
    public String getDialectName() {
        return dialect != null ? dialect.getName() : "unknown";
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            log.warn("Erreur lors de la fermeture de connexion", e);
        }
    }

    private void ensureConnected() {
        try {
            if (connection == null || connection.isClosed()) {
                throw new ConnectionException("Connecteur non connecté — appeler connect() d'abord");
            }
        } catch (SQLException e) {
            throw new ConnectionException("Impossible de vérifier l'état de la connexion", e);
        }
    }
}