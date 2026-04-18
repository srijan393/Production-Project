package com.socialhub.socialhub.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private final String apiKey = System.getenv("OPENAI_API_KEY");
    private final RestTemplate restTemplate = new RestTemplate();

    public void moderateText(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OPENAI_API_KEY is missing on Railway");
        }

        try {
            String prompt = """
                    Check if this content contains pornographic, sexual, explicit adult, 18+, or unsafe inappropriate content.
                    Reply with ONLY one word:
                    ALLOW
                    or
                    BLOCK

                    Content:
                    """ + text;

            String result = callOpenAI(prompt);

            if (result == null || result.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI moderation returned empty result");
            }

            String cleaned = result.trim().toUpperCase();

            if (cleaned.contains("BLOCK")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content blocked by AI moderation");
            }

            if (!cleaned.contains("ALLOW")) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI moderation returned invalid result");
            }

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI moderation failed: " + ex.getMessage());
        }
    }

    public int chooseBestAnswer(String question, List<String> answers) {
        if (answers == null || answers.isEmpty()) {
            return -1;
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OPENAI_API_KEY is missing on Railway");
        }

        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("Question:\n").append(question).append("\n\nAnswers:\n");

            for (int i = 0; i < answers.size(); i++) {
                prompt.append(i + 1).append(". ").append(answers.get(i)).append("\n");
            }

            prompt.append("\nReturn ONLY the number of the best answer.");

            String result = callOpenAI(prompt.toString());

            if (result == null || result.isBlank()) {
                return 1;
            }

            String digits = result.replaceAll("[^0-9]", "");
            if (digits.isBlank()) {
                return 1;
            }

            return Integer.parseInt(digits);

        } catch (Exception ex) {
            ex.printStackTrace();
            return 1;
        }
    }

    private String callOpenAI(String prompt) {
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getBody() == null) {
            throw new RuntimeException("OpenAI response body is null");
        }

        Object choicesObj = response.getBody().get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            throw new RuntimeException("OpenAI response has no choices");
        }

        Object choiceObj = choices.get(0);
        if (!(choiceObj instanceof Map<?, ?> choiceMap)) {
            throw new RuntimeException("OpenAI choice format invalid");
        }

        Object messageObj = choiceMap.get("message");
        if (!(messageObj instanceof Map<?, ?> messageMap)) {
            throw new RuntimeException("OpenAI message format invalid");
        }

        Object contentObj = messageMap.get("content");
        if (contentObj == null) {
            throw new RuntimeException("OpenAI content is null");
        }

        return contentObj.toString().trim();
    }
}