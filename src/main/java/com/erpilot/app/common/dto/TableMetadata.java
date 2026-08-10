package com.erpilot.app.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TableMetadata {
    private String tableName;
    private String schemaName;
    private List<ColumnMetadata> columns = new ArrayList<>();
    private List<String> foreignKeys = new ArrayList<>();
}