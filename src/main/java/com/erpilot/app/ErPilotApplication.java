package com.erpilot.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration.class
})
public class ErPilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErPilotApplication.class, args);
    }

}