package com.erpilot.app.llm;

import com.erpilot.app.ragschema.SchemaChunk;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class PromptBuilder {

    private static final String SYSTEM_TEMPLATE = """
            Tu es un générateur de requêtes SQL PostgreSQL.

            Règles strictes à respecter :
            - Réponds UNIQUEMENT avec la requête SQL, sans aucune explication, sans markdown, sans ```sql
            - Utilise UNIQUEMENT les tables et colonnes listées dans le contexte ci-dessous
            - N'invente JAMAIS un nom de table ou de colonne qui n'apparaît pas dans ce contexte
            - Génère uniquement des requêtes SELECT (jamais INSERT, UPDATE, DELETE, DROP, ALTER)
            - N'ajoute pas de point-virgule final
            - Si la question ne peut pas être répondue avec les tables disponibles, réponds exactement : IMPOSSIBLE

            Schéma disponible (tables pertinentes pour cette question) :
            %s
            """;

    public String buildSystemPrompt(List<SchemaChunk> relevantContext) {
        String schemaDescription = relevantContext.stream()
                .map(SchemaChunk::getDescription)
                .collect(Collectors.joining("\n"));
        return SYSTEM_TEMPLATE.formatted(schemaDescription);
    }

    public String buildUserPrompt(String userQuestion) {
        return "Question : " + userQuestion;
    }


    public String buildCorrectionPrompt(String failedSql, String errorMessage, String originalQuestion) {
        return """
                La requête SQL suivante a échoué :
                %s

                Erreur retournée par la base de données :
                %s

                Question originale de l'utilisateur : %s

                Corrige la requête SQL en tenant compte de cette erreur.
                Réponds UNIQUEMENT avec la requête corrigée, sans explication.
                """.formatted(failedSql, errorMessage, originalQuestion);
    }
}
