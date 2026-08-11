package com.erpilot.app.core;

import com.erpilot.app.common.dto.ConnectionConfig;
import com.erpilot.app.common.dto.TableMetadata;
import com.erpilot.app.common.exception.ConnectionException;
import com.erpilot.app.connector.ERPConnector;
import com.erpilot.app.connector.ERPConnectorFactory;
import com.erpilot.app.ragschema.SchemaIndexingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Garde en mémoire la connexion ERP active pour ce MVP (une seule connexion à la fois,
 * pas de multi-tenant). Établit la connexion, introspecte le schéma et déclenche
 * son indexation RAG en une seule étape — c'est le point d'entrée de tout le flux.
 */
@Slf4j
@Service
public class ConnectionSessionService {

    private final ERPConnectorFactory connectorFactory;
    private final SchemaIndexingService schemaIndexingService;

    private ERPConnector activeConnector;
    private ConnectionConfig activeConfig;

    public ConnectionSessionService(ERPConnectorFactory connectorFactory,
                                    SchemaIndexingService schemaIndexingService) {
        this.connectorFactory = connectorFactory;
        this.schemaIndexingService = schemaIndexingService;
    }

    public synchronized String connect(ConnectionConfig config) {
        if (activeConnector != null) {
            log.info("Fermeture de la connexion active avant d'en établir une nouvelle");
            activeConnector.close();
        }

        ERPConnector connector = connectorFactory.createAndConnect(config);
        List<TableMetadata> tables = connector.introspectSchema();
        schemaIndexingService.indexSchema(tables);

        this.activeConnector = connector;
        this.activeConfig = config;

        log.info("Connexion établie et schéma indexé ({} tables, dialecte {})",
                tables.size(), connector.getDialectName());

        return connector.getDialectName();
    }

    public synchronized ERPConnector getActiveConnector() {
        if (activeConnector == null) {
            throw new ConnectionException("Aucune connexion active — appelle d'abord /api/connections/connect");
        }
        return activeConnector;
    }

    public synchronized boolean isConnected() {
        return activeConnector != null && activeConnector.testConnection();
    }

    public synchronized String getActiveDialectName() {
        return activeConnector != null ? activeConnector.getDialectName() : null;
    }

    public synchronized void disconnect() {
        if (activeConnector != null) {
            activeConnector.close();
            activeConnector = null;
            activeConfig = null;
            log.info("Connexion fermée");
        }
    }
}