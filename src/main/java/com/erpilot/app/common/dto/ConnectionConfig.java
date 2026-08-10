package com.erpilot.app.common.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionConfig {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String dialectName;

    @Builder.Default
    private Integer maxPoolSize=5;
    @Builder.Default
    private Integer connectionTimeoutMs=5000;


}
