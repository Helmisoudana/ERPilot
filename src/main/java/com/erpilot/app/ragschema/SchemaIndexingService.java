package com.erpilot.app.ragschema;

import com.erpilot.app.common.dto.TableMetadata;
import com.pgvector.PGvector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class SchemaIndexingService {

    private static final Logger log = LoggerFactory.getLogger(SchemaIndexingService.class);

    private final EmbeddingModel embeddingModel;
    private final SchemaChunkRepository repository;
    private final SchemaDescriptionBuilder descriptionBuilder;

    public SchemaIndexingService(EmbeddingModel embeddingModel,
                                  SchemaChunkRepository repository,
                                  SchemaDescriptionBuilder descriptionBuilder) {
        this.embeddingModel = embeddingModel;
        this.repository = repository;
        this.descriptionBuilder = descriptionBuilder;
    }

    /**
     * Indexe une table unique. Idempotent : les anciens chunks de cette
     * table sont supprimés avant réinsertion, donc rappeler cette méthode
     * plusieurs fois (ex: à chaque redémarrage) ne crée pas de doublons.
     */
    @Transactional
    public void indexTable(TableMetadata table) {
        repository.deleteByTableName(table.getTableName());

        String description = descriptionBuilder.build(table);
        float[] vector = embeddingModel.embed(description);

        SchemaChunk chunk = new SchemaChunk();
        chunk.setTableName(table.getTableName());
        chunk.setDescription(description);
        chunk.setEmbedding(new PGvector(vector));

        repository.save(chunk);
        log.info("Table indexée : {} ({} colonnes)", table.getTableName(), table.getColumns().size());
    }

    /**
     * Indexe un schéma complet en une fois — c'est cette méthode qui sera
     * appelée depuis le module connector, juste après ERPConnector.introspectSchema().
     */
    public void indexSchema(List<TableMetadata> tables) {
        log.info("Début indexation du schéma : {} tables", tables.size());
        tables.forEach(this::indexTable);
        log.info("Indexation terminée : {} tables indexées", tables.size());
    }
}
