package com.erpilot.app.ragschema;

import com.erpilot.app.common.dto.TableMetadata; // Ajuste selon ton package réel
import com.erpilot.app.common.dto.ColumnMetadata; // Ajuste selon ton package réel
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rag-test")
public class RagTestController {

    private final SchemaIndexingService indexingService;
    private final SchemaRetrievalService retrievalService;

    public RagTestController(SchemaIndexingService indexingService, SchemaRetrievalService retrievalService) {
        this.indexingService = indexingService;
        this.retrievalService = retrievalService;
    }

    // 1. Endpoint pour simuler l'indexation de deux tables (mockées)
    @PostMapping("/index-mock")
    public String indexMockTables() {
        // Table Users
        TableMetadata users = new TableMetadata();
        users.setTableName("users");

        ColumnMetadata col1 = new ColumnMetadata(); col1.setName("id"); col1.setNormalizedType("SERIAL");
        ColumnMetadata col2 = new ColumnMetadata(); col2.setName("username"); col2.setNormalizedType("VARCHAR");
        users.setColumns(List.of(col1, col2));

        // Table Orders
        TableMetadata orders = new TableMetadata();
        orders.setTableName("orders");

        ColumnMetadata col3 = new ColumnMetadata(); col3.setName("id"); col3.setNormalizedType("SERIAL");
        ColumnMetadata col4 = new ColumnMetadata(); col4.setName("total_amount"); col4.setNormalizedType("DECIMAL");
        orders.setColumns(List.of(col3, col4));

        // Indexation
        indexingService.indexTable(users);
        indexingService.indexTable(orders);

        return "Table 'users' et 'orders' indexées avec succès dans pgvector !";
    }

    // 2. Endpoint pour poser une question et chercher les tables pertinentes
    @GetMapping("/search")
    public List<SchemaChunk> search(@RequestParam String question, @RequestParam(defaultValue = "1") int topK) {
        return retrievalService.findRelevantTables(question, topK);
    }
}