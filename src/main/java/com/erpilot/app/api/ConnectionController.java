package com.erpilot.app.api;

import com.erpilot.app.api.dto.ConnectionTestResponse;
import com.erpilot.app.api.dto.QueryRequest;
import com.erpilot.app.common.dto.ConnectionConfig;
import com.erpilot.app.common.dto.QueryResult;
import com.erpilot.app.common.dto.TableMetadata;
import com.erpilot.app.common.exception.ConnectionException;
import com.erpilot.app.connector.ERPConnector;
import com.erpilot.app.connector.ERPConnectorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/connections")
public class ConnectionController {

    private final ERPConnectorFactory connectorFactory;

    public ConnectionController(ERPConnectorFactory connectorFactory) {
        this.connectorFactory = connectorFactory;
    }

    @PostMapping("/test")
    public ResponseEntity<ConnectionTestResponse> testConnection(@RequestBody ConnectionConfig config) {
        ERPConnector connector = null;
        try {
            connector = connectorFactory.createAndConnect(config);
            boolean ok = connector.testConnection();

            return ResponseEntity.ok(new ConnectionTestResponse(
                    ok,
                    connector.getDialectName(),
                    ok ? "Connexion réussie" : "Connexion établie mais requête de test échouée"
            ));

        } catch (ConnectionException e) {
            return ResponseEntity.badRequest().body(new ConnectionTestResponse(
                    false, null, e.getMessage()
            ));
        } finally {
            if (connector != null) connector.close();
        }
    }

    @PostMapping("/schema")
    public ResponseEntity<List<TableMetadata>> getSchema(@RequestBody ConnectionConfig config) {
        ERPConnector connector = connectorFactory.createAndConnect(config);
        try {
            List<TableMetadata> schema = connector.introspectSchema();
            return ResponseEntity.ok(schema);
        } finally {
            connector.close();
        }
    }

    @PostMapping("/query")
    public ResponseEntity<QueryResult> executeQuery(@RequestBody QueryRequest request) {
        ERPConnector connector = connectorFactory.createAndConnect(request.getConnection());
        try {
            QueryResult result = connector.executeQuery(request.getSql(), request.getLimit());
            return ResponseEntity.ok(result);
        } finally {
            connector.close();
        }
    }
}