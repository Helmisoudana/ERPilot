package com.erpilot.app.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnMetadata {
    private String name ;
    private String nativeType;
    private String normalizedType;
    private boolean nullable;
    private boolean primaryKey;

}
