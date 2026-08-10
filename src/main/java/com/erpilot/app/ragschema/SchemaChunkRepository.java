package com.erpilot.app.ragschema;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SchemaChunkRepository extends JpaRepository<SchemaChunk, Long> {

    @Query(value = """
            SELECT * FROM schema_chunks
            ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
            LIMIT :topK
            """, nativeQuery = true)
    List<SchemaChunk> findSimilar(@Param("queryEmbedding") String queryEmbedding,
                                   @Param("topK") int topK);


    @Transactional
    void deleteByTableName(String tableName);
}
