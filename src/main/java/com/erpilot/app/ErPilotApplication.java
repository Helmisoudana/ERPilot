package com.erpilot.app;

import com.erpilot.app.core.config.ErpilotConnectionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class ErPilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErPilotApplication.class, args);
    }

}