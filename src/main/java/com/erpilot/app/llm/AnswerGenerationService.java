package com.erpilot.app.llm;

import com.erpilot.app.common.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;


@Service
public class AnswerGenerationService {

    private static final Logger log = LoggerFactory.getLogger(AnswerGenerationService.class);
    private static final int MAX_ROWS_FOR_PROMPT = 50;

    private final ChatClient chatClient;
    private final PromptBuilder promptBuilder;

    public AnswerGenerationService(ChatClient.Builder chatClientBuilder, PromptBuilder promptBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.promptBuilder = promptBuilder;
    }

    public String generateAnswer(String question, QueryResult queryResult) {
        if (queryResult.getRowCount() == 0) {
            return "Aucun résultat trouvé pour cette question.";
        }

        String systemPrompt = promptBuilder.buildAnswerSystemPrompt();
        String userPrompt = promptBuilder.buildAnswerUserPrompt(question, queryResult, MAX_ROWS_FOR_PROMPT);

        log.debug("Génération de la réponse en langage naturel pour : {}", question);

        String answer = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        return answer.trim();
    }
}