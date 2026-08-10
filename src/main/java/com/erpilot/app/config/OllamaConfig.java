package com.erpilot.app.config;

import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaConfig {

    @Bean
    public OllamaEmbeddingModel ollamaEmbeddingModel() {
        // 1. Liaison directe vers ton instance Ollama locale
        OllamaApi ollamaApi = new OllamaApi("http://localhost:11434");

        // 2. Définition du modèle à utiliser
        OllamaOptions options = OllamaOptions.create()
                .withModel("nomic-embed-text");

        // 3. On passe directement l'API et les options au constructeur
        return new OllamaEmbeddingModel(ollamaApi, options);
    }
}