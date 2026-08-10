package com.erpilot.app.ragschema;

import com.erpilot.app.common.dto.TableMetadata;
import org.springframework.stereotype.Component;

@Component
public class SchemaDescriptionBuilder {

    public String build(TableMetadata table) {
        StringBuilder sb = new StringBuilder();
        sb.append("Table ").append(table.getTableName()).append(" contient les colonnes : ");

        table.getColumns().forEach(col -> {
            sb.append(col.getName()).append(" (").append(col.getNormalizedType()).append(")");
            if (col.isPrimaryKey()) {
                sb.append(" [clé primaire]");
            }
            sb.append(", ");
        });

        if (!table.getForeignKeys().isEmpty()) {
            sb.append(". Relations : ").append(String.join(", ", table.getForeignKeys()));
        }

        return sb.toString();
    }
}
