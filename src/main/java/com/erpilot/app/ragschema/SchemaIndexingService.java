package com.erpilot.app.ragschema;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import com.pgvector.PGvector;
import com.erpilot.app.common.dto.TableMetadata;
import java.util.List;

@Service
public class SchemaIndexingService {

    private final EmbeddingModel embeddingModel;
    private final SchemaChunkRepository repository;

    public SchemaIndexingService(EmbeddingModel embeddingModel, SchemaChunkRepository repository) {
        this.embeddingModel = embeddingModel;
        this.repository = repository;
    }

    public void indexTable(TableMetadata table) {
        String description = buildDescription(table);
        float[] vector = embeddingModel.embed(description);

        SchemaChunk chunk = new SchemaChunk();
        chunk.setTableName(table.getTableName());
        chunk.setDescription(description);
        chunk.setEmbedding(new PGvector(vector));

        repository.save(chunk);
    }

    private String buildDescription(TableMetadata table) {
        StringBuilder sb = new StringBuilder();
        sb.append("Table ").append(table.getTableName()).append(" contient les colonnes : ");
        table.getColumns().forEach(col ->
                sb.append(col.getName()).append(" (").append(col.getNormalizedType()).append("), ")
        );
        return sb.toString();
    }
}