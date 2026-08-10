package com.erpilot.app.ragschema;

import jakarta.persistence.*;
import lombok.Data;
import com.pgvector.PGvector;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "schema_chunks")
@Data
public class SchemaChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tableName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Type(PGvectorType.class)
    @Column(columnDefinition = "vector(768)")
    private PGvector embedding;
}
