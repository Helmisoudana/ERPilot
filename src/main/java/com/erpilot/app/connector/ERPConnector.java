package com.erpilot.app.connector;

import com.erpilot.app.common.dto.ConnectionConfig;
import com.erpilot.app.common.dto.QueryResult;
import com.erpilot.app.common.dto.TableMetadata;

import java.util.List;

public interface ERPConnector {

    void connect(ConnectionConfig config);

    boolean testConnection();

    List<TableMetadata> introspectSchema();

    QueryResult executeQuery(String sql, int limit);

    String getDialectName();

    void close();
}
