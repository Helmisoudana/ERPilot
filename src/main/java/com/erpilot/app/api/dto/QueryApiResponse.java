package com.erpilot.app.api.dto;

import com.erpilot.app.core.OrchestrationResult;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QueryApiResponse {
    private String question;
    private String sqlGenere;
    private String sqlExecute;
    private boolean corrige;
    private String dialecte;
    private List<String> tablesUtilisees;
    private List<String> colonnes;
    private List<Map<String, Object>> lignes;
    private int nombreLignes;
    private long dureeExecutionMs;

    public static QueryApiResponse from(OrchestrationResult result) {
        QueryApiResponse response = new QueryApiResponse();
        response.setQuestion(result.getQuestion());
        response.setSqlGenere(result.getGeneratedSql());
        response.setSqlExecute(result.getExecutedSql());
        response.setCorrige(result.isAutoCorrected());
        response.setDialecte(result.getDialect());
        response.setTablesUtilisees(result.getTablesUsed());
        response.setColonnes(result.getQueryResult().getColumnsNames());
        response.setLignes(result.getQueryResult().getRows());
        response.setNombreLignes(result.getQueryResult().getRowCount());
        response.setDureeExecutionMs(result.getQueryResult().getExecutionTimeMs());
        return response;
    }
}