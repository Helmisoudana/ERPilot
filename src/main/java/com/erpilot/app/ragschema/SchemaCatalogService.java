package com.erpilot.app.ragschema;

import com.erpilot.app.common.dto.TableMetadata;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@Component
public class SchemaCatalogService {

    private final AtomicReference<Set<String>> knownTables =
            new AtomicReference<>(Set.of());

    public void updateCatalog(List<TableMetadata> tables) {
        Set<String> normalized = tables.stream()
                .map(t -> normalize(t.getTableName()))
                .collect(Collectors.toUnmodifiableSet());
        knownTables.set(normalized);
    }

    public boolean isKnownTable(String tableName) {
        return knownTables.get().contains(normalize(tableName));
    }

    public Set<String> getKnownTableNames() {
        return knownTables.get();
    }


    private String normalize(String rawName) {
        String cleaned = rawName.replace("\"", "").trim();
        int dotIndex = cleaned.lastIndexOf('.');
        if (dotIndex >= 0) {
            cleaned = cleaned.substring(dotIndex + 1);
        }
        return cleaned.toLowerCase();
    }
}