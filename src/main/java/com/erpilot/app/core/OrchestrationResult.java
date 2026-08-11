package com.erpilot.app.core;

import com.erpilot.app.common.dto.QueryResult;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrchestrationResult {
    private String question;
    private String generatedSql;
    private String executedSql;
    private QueryResult queryResult;
    private List<String> tablesUsed;
    private boolean autoCorrected;
    private String dialect;
}