package com.erpilot.app.ragschema;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchemaChunkRepository extends JpaRepository<SchemaChunk, Long> {

    @Query(value = """
        SELECT * FROM schema_chunks
        ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
        LIMIT :topK
        """, nativeQuery = true)
    List<SchemaChunk> findSimilar(String queryEmbedding, int topK);
}