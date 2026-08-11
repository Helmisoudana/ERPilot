package com.erpilot.app.core.config;

import com.erpilot.app.common.dto.ConnectionConfig;
import com.erpilot.app.core.ConnectionSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Si une configuration de connexion est fournie via application.properties / variables
 * d'environnement, on se connecte automatiquement au démarrage. Sinon, l'agent démarre
 * "à vide" et attend un appel explicite à POST /api/connections/connect — c'est ce qui
 * permet de le brancher sur n'importe quel ERP sans redéployer.
 */
@Slf4j
@Component
public class StartupConnectionRunner implements ApplicationRunner {

    private final ErpilotConnectionProperties properties;
    private final ConnectionSessionService connectionSessionService;

    public StartupConnectionRunner(ErpilotConnectionProperties properties,
                                   ConnectionSessionService connectionSessionService) {
        this.properties = properties;
        this.connectionSessionService = connectionSessionService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isConfigured()) {
            log.info("Aucune connexion par défaut configurée (erpilot.connection.url absent) — " +
                    "en attente d'un appel à POST /api/connections/connect");
            return;
        }

        ConnectionConfig config = ConnectionConfig.builder()
                .url(properties.getUrl())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .dialectName(properties.getDialectName())
                .maxPoolSize(properties.getMaxPoolSize())
                .build();

        try {
            String dialect = connectionSessionService.connect(config);
            log.info("Connexion automatique établie au démarrage (dialecte: {})", dialect);
        } catch (Exception e) {
            log.error("Échec de la connexion automatique au démarrage : {}. " +
                    "L'agent reste utilisable via POST /api/connections/connect.", e.getMessage());
        }
    }
}