package com.erpilot.app.connector;

import com.erpilot.app.common.dto.ConnectionConfig;
import com.erpilot.app.connector.dialect.SqlDialectFactory;
import org.springframework.stereotype.Component;

@Component
public class ERPConnectorFactory {

    private final SqlDialectFactory dialectFactory;

    public ERPConnectorFactory(SqlDialectFactory dialectFactory) {
        this.dialectFactory = dialectFactory;
    }

    public ERPConnector createAndConnect(ConnectionConfig config) {
        ERPConnector connector = new JdbcErpConnector(dialectFactory);
        connector.connect(config);
        return connector;
    }
}