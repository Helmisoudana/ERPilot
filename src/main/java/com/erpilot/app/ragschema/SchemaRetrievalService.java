package com.erpilot.app.ragschema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchemaRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(SchemaRetrievalService.class);
    private static final int DEFAULT_TOP_K = 5;

    private final EmbeddingModel embeddingModel;
    private final SchemaChunkRepository repository;

    public SchemaRetrievalService(EmbeddingModel embeddingModel, SchemaChunkRepository repository) {
        this.embeddingModel = embeddingModel;
        this.repository = repository;
    }

    public List<SchemaChunk> findRelevantTables(String userQuestion, int topK) {
        float[] vector = embeddingModel.embed(userQuestion);

        // Même conversion que SchemaIndexingService — un seul point de vérité (VectorFormat)
        String queryEmbedding = VectorFormat.toPgVectorString(vector);

        List<SchemaChunk> results = repository.findSimilar(queryEmbedding, topK);
        log.debug("Question '{}' -> {} table(s) pertinente(s) trouvée(s)", userQuestion, results.size());

        return results;
    }

    public List<SchemaChunk> findRelevantTables(String userQuestion) {
        return findRelevantTables(userQuestion, DEFAULT_TOP_K);
    }
}
