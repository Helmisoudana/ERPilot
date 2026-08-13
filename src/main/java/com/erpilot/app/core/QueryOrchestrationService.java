package com.erpilot.app.core;

import com.erpilot.app.common.dto.QueryResult;
import com.erpilot.app.common.exception.QueryExecutionException;
import com.erpilot.app.connector.ERPConnector;
import com.erpilot.app.core.exception.QuestionNotAnswerableException;
import com.erpilot.app.llm.SqlGenerationService;
import com.erpilot.app.ragschema.SchemaChunk;
import com.erpilot.app.ragschema.SchemaRetrievalService;
import com.erpilot.app.security.SqlSecurityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class QueryOrchestrationService {

    private static final int MAX_CORRECTION_ATTEMPTS = 2;
    private static final int DEFAULT_ROW_LIMIT = 200;
    private static final String IMPOSSIBLE_MARKER = "IMPOSSIBLE";

    private final SchemaRetrievalService schemaRetrievalService;
    private final SqlGenerationService sqlGenerationService;
    private final SqlSecurityService sqlSecurityService;
    private final ConnectionSessionService connectionSessionService;

    public QueryOrchestrationService(SchemaRetrievalService schemaRetrievalService,
                                     SqlGenerationService sqlGenerationService,
                                     SqlSecurityService sqlSecurityService,
                                     ConnectionSessionService connectionSessionService) {
        this.schemaRetrievalService = schemaRetrievalService;
        this.sqlGenerationService = sqlGenerationService;
        this.sqlSecurityService = sqlSecurityService;
        this.connectionSessionService = connectionSessionService;
    }

    public OrchestrationResult processQuestion(String question, String role) {
        ERPConnector connector = connectionSessionService.getActiveConnector();

        List<SchemaChunk> relevantContext = schemaRetrievalService.findRelevantTables(question);
        if (relevantContext.isEmpty()) {
            throw new QuestionNotAnswerableException(
                    "Aucune table pertinente trouvée pour cette question.");
        }

        String generatedSql = sqlGenerationService.generateSql(question, relevantContext);
        if (IMPOSSIBLE_MARKER.equalsIgnoreCase(generatedSql.trim())) {
            throw new QuestionNotAnswerableException(
                    "Le modèle ne peut pas répondre à cette question avec le schéma disponible.");
        }

        return executeWithRetry(question, role, relevantContext, generatedSql, connector, 0);
    }

    private OrchestrationResult executeWithRetry(String question, String role,
                                                 List<SchemaChunk> relevantContext,
                                                 String candidateSql, ERPConnector connector,
                                                 int attempt) {
        String securedSql = sqlSecurityService.secureQuery(candidateSql, role);

        try {
            QueryResult result = connector.executeQuery(securedSql, DEFAULT_ROW_LIMIT);

            return new OrchestrationResult(
                    question,
                    candidateSql,
                    securedSql,
                    result,
                    relevantContext.stream().map(SchemaChunk::getTableName).toList(),
                    attempt > 0,
                    connector.getDialectName()
            );

        } catch (QueryExecutionException e) {
            if (attempt >= MAX_CORRECTION_ATTEMPTS) {
                log.error("Échec définitif après {} tentative(s) de correction", attempt, e);
                throw e;
            }

            log.warn("Exécution échouée (tentative {}), demande de correction au LLM : {}",
                    attempt + 1, e.getMessage());

            String correctedSql = sqlGenerationService.correctSql(
                    candidateSql, e.getMessage(), question, relevantContext);

            return executeWithRetry(question, role, relevantContext, correctedSql, connector, attempt + 1);
        }
    }
}