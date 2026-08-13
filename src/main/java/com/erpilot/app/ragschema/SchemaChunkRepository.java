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
        SELECT * FROM (
            SELECT *, embedding <=> CAST(:queryEmbedding AS vector) AS distance
            FROM schema_chunks
        ) t
        WHERE distance < :maxDistance
        ORDER BY distance
        LIMIT :topK
        """, nativeQuery = true)
    List<SchemaChunk> findSimilar(@Param("queryEmbedding") String queryEmbedding,
                                  @Param("topK") int topK,
                                  @Param("maxDistance") double maxDistance);


    @Transactional
    void deleteByTableName(String tableName);
}
