package com.erpilot.app.llm;

import com.erpilot.app.common.dto.QueryResult;
import com.erpilot.app.ragschema.SchemaChunk;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class PromptBuilder {

    private static final String SYSTEM_TEMPLATE = """
            Tu es un expert générateur de requêtes SQL PostgreSQL.

            Règles strictes à respecter :
            - Réponds UNIQUEMENT avec la requête SQL, sans aucune explication, sans markdown, sans ```sql
            - Utilise UNIQUEMENT les tables et colonnes listées dans le contexte ci-dessous
            - N'invente JAMAIS un nom de table ou de colonne qui n'apparaît pas dans ce contexte
            - Génère uniquement des requêtes SELECT (jamais INSERT, UPDATE, DELETE, DROP, ALTER)
            - N'ajoute pas de point-virgule final
            - Si la question ne peut pas être répondue avec les tables disponibles, réponds exactement : IMPOSSIBLE

            Règles PostgreSQL strictes :
            1. GROUP BY : Toutes les colonnes présentes dans le SELECT qui ne sont PAS enveloppées dans une fonction d'agrégation (SUM, COUNT, AVG, MIN, MAX) doivent OBLIGATOIREMENT être présentes dans la clause GROUP BY.
               Exemple : SELECT c.id, c.nom, SUM(o.montant) ... GROUP BY c.id, c.nom
            2. PAS DE SUM(BOOLEAN) : Ne fais JAMAIS SUM(condition).
               Pour compter des conditions, utilise COUNT(*) FILTER (WHERE condition) ou SUM(CASE WHEN condition THEN 1 ELSE 0 END).
            3. TYPES NUMÉRIQUES : N'applique les fonctions SUM() et AVG() qu'à des colonnes strictement numériques.

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

                Instructions de correction :
                1. Analyse attentivement le message d'erreur de PostgreSQL.
                2. Si l'erreur mentionne 'must appear in the GROUP BY clause', ajoute TOUTES les colonnes non agrégées du SELECT dans le GROUP BY.
                3. Si l'erreur mentionne 'function sum(boolean) does not exist', remplace SUM(condition) par COUNT(*) FILTER (WHERE condition) ou un CASE WHEN.
                4. Corrige la requête SQL en respectant le schéma et ces règles.

                Réponds UNIQUEMENT avec la requête SQL corrigée, sans aucune explication ni bloc markdown.
                """.formatted(failedSql, errorMessage, originalQuestion);
    }

    private static final String ANSWER_SYSTEM_TEMPLATE = """
        Tu es un assistant qui explique en français, de façon claire et concise,
        le résultat d'une requête SQL à un utilisateur métier qui ne connaît pas le SQL.

        Règles :
        - Réponds uniquement en langage naturel, sans SQL, sans JSON, sans markdown.
        - Base-toi UNIQUEMENT sur les données fournies, n'invente aucun chiffre.
        - Si une seule valeur est demandée, donne-la directement dans une phrase.
        - Si plusieurs lignes sont retournées, résume les points clés (totaux, tendances,
          valeurs extrêmes) puis, si pertinent, liste brièvement les éléments principaux.
        - Sois synthétique : quelques phrases suffisent, sauf si la liste des résultats
          est elle-même la réponse attendue.
        """;

    public String buildAnswerSystemPrompt() {
        return ANSWER_SYSTEM_TEMPLATE;
    }

    public String buildAnswerUserPrompt(String question, QueryResult queryResult, int maxRows) {
        List<Map<String, Object>> rows = queryResult.getRows();
        List<Map<String, Object>> sample = rows.size() > maxRows ? rows.subList(0, maxRows) : rows;

        String dataAsText = sample.stream()
                .map(row -> row.entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining(", ")))
                .collect(Collectors.joining("\n"));

        String truncationNote = rows.size() > maxRows
                ? "\n(Résultat tronqué : " + rows.size() + " lignes au total, seules les "
                  + maxRows + " premières sont montrées ci-dessus.)"
                : "";

        return """
            Question de l'utilisateur : %s

            Colonnes : %s
            Nombre total de lignes : %d

            Données :
            %s%s

            Rédige la réponse à donner à l'utilisateur.
            """.formatted(
                question,
                String.join(", ", queryResult.getColumnsNames()),
                queryResult.getRowCount(),
                dataAsText,
                truncationNote);
    }
}