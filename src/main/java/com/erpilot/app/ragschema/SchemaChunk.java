package com.erpilot.app.ragschema;
import jakarta.persistence.*;
import lombok.Data;
import com.pgvector.PGvector;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name="schema_chunks")
@Data
public class SchemaChunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tableName;
    private String columnName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition =  "vector(768)")
    @JdbcTypeCode(SqlTypes.OTHER)
    private PGvector embedding;

}
