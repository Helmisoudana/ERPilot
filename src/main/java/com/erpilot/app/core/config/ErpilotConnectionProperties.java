package com.erpilot.app.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "erpilot.connection")
public class ErpilotConnectionProperties {
    private String url;
    private String username;
    private String password;
    private String dialectName;
    private Integer maxPoolSize = 5;

    public boolean isConfigured() {
        return url != null && !url.isBlank();
    }
}