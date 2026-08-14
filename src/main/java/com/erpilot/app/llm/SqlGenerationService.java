package com.erpilot.app.llm;

import com.erpilot.app.common.exception.LlmGenerationException;
import com.erpilot.app.ragschema.SchemaChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SqlGenerationService {

    private static final Logger log = LoggerFactory.getLogger(SqlGenerationService.class);

    private final ChatClient chatClient;
    private final PromptBuilder promptBuilder;

    public SqlGenerationService(ChatClient.Builder chatClientBuilder, PromptBuilder promptBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.promptBuilder = promptBuilder;
    }

    public String generateSql(String userQuestion, List<SchemaChunk> relevantContext) {
        String systemPrompt = promptBuilder.buildSystemPrompt(relevantContext);
        String userPrompt = promptBuilder.buildUserPrompt(userQuestion);

        log.debug("Génération SQL pour la question : {}", userQuestion);

        String rawResponse = callLlm(systemPrompt, userPrompt, "génération SQL");

        String sql = cleanSqlResponse(rawResponse);
        log.info("SQL généré : {}", sql);
        return sql;
    }


    public String correctSql(String failedSql, String errorMessage, String originalQuestion,
                             List<SchemaChunk> relevantContext) {
        String systemPrompt = promptBuilder.buildSystemPrompt(relevantContext);
        String correctionPrompt = promptBuilder.buildCorrectionPrompt(failedSql, errorMessage, originalQuestion);

        log.warn("Tentative de correction SQL suite à l'erreur : {}", errorMessage);

        String rawResponse = callLlm(systemPrompt, correctionPrompt, "correction SQL");

        return cleanSqlResponse(rawResponse);
    }


    private String callLlm(String systemPrompt, String userPrompt, String contexteAction) {
        try {
            String content = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            if (content == null || content.isBlank()) {
                throw new LlmGenerationException(
                        "Le modèle IA a renvoyé une réponse vide (" + contexteAction + ")");
            }
            return content;

        } catch (LlmGenerationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Échec de l'appel au LLM pendant : {}", contexteAction, e);
            throw new LlmGenerationException(
                    "Le service IA est indisponible ou a échoué pendant : " + contexteAction, e);
        }
    }


    private String cleanSqlResponse(String raw) {
        return raw
                .replaceAll("(?i)```sql", "")
                .replaceAll("```", "")
                .trim();
    }
}
 