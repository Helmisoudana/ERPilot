package com.erpilot.app.llm;

import com.erpilot.app.common.dto.QueryResult;
import com.erpilot.app.ragschema.SchemaChunk;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PromptBuilder {

    private static final String UNIVERSAL_SYSTEM_TEMPLATE = """
            Tu es un moteur Text-to-SQL universel ultra-performant. Ton rôle est de traduire une question en langage naturel en une requête SQL exacte, performante et minimale en te basant UNIQUEMENT sur le schéma de base de données fourni.

            ### RÈGLES D'OR DE SIMPLICITÉ & D'EFFICACITÉ :
            1. RÈGLE DE PARSIMONIE (Occam's Razor SQL) : Utilise le MINIMUM de tables nécessaire. N'effectue JAMAIS de jointure (JOIN) si la table principale contient déjà la colonne nécessaire pour le SELECT, le WHERE ou le ORDER BY.
            2. INTERDICTION D'INVENTION : N'invente AUCUNE table, AUCUNE colonne et AUCUNE valeur. Si une information n'est pas dans le schéma, ne l'invente pas.
            3. SÉCURITÉ STRICTE : Génère EXCLUSIVEMENT des requêtes de lecture (SELECT). Les commandes DDL/DML (INSERT, UPDATE, DELETE, DROP, ALTER, TRUNCATE) sont STRICTEMENT INTERDITES.
            4. COMPATIBILITÉ DIALECTE : Génère un SQL valide et optimisé pour le SGBD cible indiqué dans le schéma.

            ### RÈGLES DE FORMATAGE (RÉPONSE BRUTE) :
            - Réponds UNIQUEMENT avec le code SQL brut.
            - AUCUN mot d'explication, aucun texte introductif ou de conclusion.
            - PAS de balises Markdown (NE PAS mettre ```sql ... ```).
            - N'ajoute PAS de point-virgule (;) à la fin de la requête.
            - Si la question est impossible à résoudre avec le schéma fourni, réponds exactement le mot : IMPOSSIBLE

            ### SCHÉMA DE LA BASE DE DONNÉES FOURNI :
            %s
            """;

    public String buildSystemPrompt(List<SchemaChunk> relevantContext) {
        String schemaDescription = relevantContext.stream()
                .map(SchemaChunk::getDescription)
                .collect(Collectors.joining("\n\n"));
        return UNIVERSAL_SYSTEM_TEMPLATE.formatted(schemaDescription);
    }

    public String buildUserPrompt(String userQuestion) {
        return """
                ### ALGORITHME DE RAISONNEMENT INTELLECTUEL (À exécuter mentalement) :
                Étape 1 : Identifie les entités métier principales mentionnées dans la question.
                Étape 2 : Cherche les tables du schéma qui contiennent ces entités.
                Étape 3 : Vérifie si TOUTES les données demandées (colonnes à afficher et colonnes de filtre) résident dans UNE SEULE table. 
                         -> Si OUI : Génère un SELECT simple sans aucun JOIN.
                         -> Si NON : Identifie le chemin de jointure (JOIN) le plus court et direct via les clés étrangères explicites.
                Étape 4 : Applique les filtres WHERE et l'agrégation (GROUP BY) si nécessaire.

                Question de l'utilisateur : %s
                """.formatted(userQuestion);
    }

    public String buildCorrectionPrompt(String failedSql, String errorMessage, String originalQuestion) {
        return """
                La requête SQL suivante a échoué lors de son exécution sur le SGBD :
                
                [SQL en échec]
                %s

                [Message d'erreur du SGBD]
                %s

                [Question initiale de l'utilisateur]
                %s

                ### INSTRUCTIONS DE AUTO-CORRECTION :
                1. Lis attentivement le message d'erreur du SGBD pour diagnostiquer le problème exact (ex: colonne introuvable, erreur de syntaxe, manque dans GROUP BY, type incompatible).
                2. Vérifie la présence de jointures inutiles qui auraient pu provoquer une erreur ou un résultat vide.
                3. Génère la version corrigée de la requête SQL en respectant scrupuleusement le schéma fourni.

                Réponds UNIQUEMENT avec le code SQL corrigé, sans Markdown et sans texte explicatif.
                """.formatted(failedSql, errorMessage, originalQuestion);
    }

    private static final String UNIVERSAL_ANSWER_SYSTEM_TEMPLATE = """
        Tu es un assistant d'analyse de données universel. Ton rôle est d'expliquer les résultats d'une requête SQL de façon fluide, claire et accessible à un utilisateur métier non technique.

        Directives :
        - Rédige uniquement en langage naturel clair (français).
        - Ne mentionne JAMAIS de termes techniques de base de données (pas de mots comme SQL, Query, SELECT, ID, JSON, NULL, etc.).
        - Base ta réponse STRICTEMENT et EXCLUSIVEMENT sur les données fournies. N'invente aucun fait, aucun chiffre.
        - Si le résultat est un chiffre/compteur unique : Donne directement l'information en une phrase synthétique.
        - Si le résultat est un tableau/liste de données : Fais un résumé des points clés ou du total, puis présente les résultats de manière ordonnée et facile à lire.
        - Reste concis et professionnel (2 à 4 phrases suffisent généralement).
        """;

    public String buildAnswerSystemPrompt() {
        return UNIVERSAL_ANSWER_SYSTEM_TEMPLATE;
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
                ? "\n(Note : Données tronquées. " + rows.size() + " lignes au total, seules les "
                  + maxRows + " premières sont affichées ci-dessus.)"
                : "";

        return """
            Question initiale de l'utilisateur : %s

            [Données brutes récupérées]
            Nombre de lignes retournées : %d
            Nom des colonnes : %s

            Extrait des données :
            %s%s

            Rédige la réponse finale en langage naturel à destination de l'utilisateur.
            """.formatted(
                question,
                queryResult.getRowCount(),
                String.join(", ", queryResult.getColumnsNames()),
                dataAsText,
                truncationNote);
    }
}