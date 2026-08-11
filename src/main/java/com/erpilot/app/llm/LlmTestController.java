package com.erpilot.app.llm;

import com.erpilot.app.ragschema.SchemaChunk;
import com.erpilot.app.ragschema.SchemaRetrievalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/llm")
public class LlmTestController {

    private final SchemaRetrievalService retrievalService;
    private final SqlGenerationService generationService;

    public LlmTestController(SchemaRetrievalService retrievalService,
                              SqlGenerationService generationService) {
        this.retrievalService = retrievalService;
        this.generationService = generationService;
    }

    @GetMapping("/generate-sql")
    public Map<String, Object> generateSql(@RequestParam String question,
                                            @RequestParam(defaultValue = "5") int topK) {
        List<SchemaChunk> context = retrievalService.findRelevantTables(question, topK);
        String sql = generationService.generateSql(question, context);

        return Map.of(
                "question", question,
                "tablesUtilisees", context.stream().map(SchemaChunk::getTableName).toList(),
                "sqlGenere", sql
        );
    }
}
