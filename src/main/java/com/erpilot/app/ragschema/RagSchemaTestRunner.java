package com.erpilot.app.ragschema;

import com.erpilot.app.common.dto.ColumnMetadata;
import com.erpilot.app.common.dto.TableMetadata;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Runner de test manuel — à supprimer ou désactiver une fois le module validé.
 * @Profile("test-rag") permet de ne l'activer que si tu le demandes explicitement,
 * pour ne pas réindexer à chaque démarrage normal de l'app.
 */
@Component
@Profile("test-rag")
public class RagSchemaTestRunner implements CommandLineRunner {

    private final SchemaIndexingService indexingService;
    private final SchemaRetrievalService retrievalService;

    public RagSchemaTestRunner(SchemaIndexingService indexingService,
                               SchemaRetrievalService retrievalService) {
        this.indexingService = indexingService;
        this.retrievalService = retrievalService;
    }

    @Override
    public void run(String... args) {
        System.out.println("=== TEST RAG SCHEMA ===");

        // 1. On fabrique une fausse TableMetadata (pas besoin du connecteur ici)
        TableMetadata table = new TableMetadata();
        table.setTableName("stock_produits");
        table.setColumns(List.of(
                new ColumnMetadata("id", "int4", "INTEGER", false, true),
                new ColumnMetadata("nom_produit", "varchar", "VARCHAR", false, false),
                new ColumnMetadata("quantite", "int4", "INTEGER", true, false)
        ));

        // 2. Indexation
        indexingService.indexTable(table);
        System.out.println("Table indexée : " + table.getTableName());

        // 3. Recherche par similarité
        List<SchemaChunk> results = retrievalService.findRelevantTables(
                "combien de produits sont en stock ?", 3);

        System.out.println("Résultats trouvés : " + results.size());
        results.forEach(r ->
                System.out.println(" - " + r.getTableName() + " : " + r.getDescription()));
    }
}