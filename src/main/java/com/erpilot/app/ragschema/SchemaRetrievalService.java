package com.erpilot.app.ragschema;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Arrays;

@Service
public class SchemaRetrievalService {

    private final EmbeddingModel embeddingModel;
    private final SchemaChunkRepository repository;

    public SchemaRetrievalService(EmbeddingModel embeddingModel, SchemaChunkRepository repository) {
        this.embeddingModel = embeddingModel;
        this.repository = repository;
    }

    public List<SchemaChunk> findRelevantTables(String userQuestion, int topK) {
        float[] vector = embeddingModel.embed(userQuestion);
        String vectorAsString = Arrays.toString(vector); // format attendu par pgvector
        return repository.findSimilar(vectorAsString, topK);
    }
}