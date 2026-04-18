package com.socialhub.socialhub.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private final String apiKey = System.getenv("OPENAI_API_KEY");
    private final RestTemplate restTemplate = new RestTemplate();

    // 🔴 MODERATION
    public void moderateText(String text) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("AI key missing");
        }

        String prompt = "Check if this content is inappropriate or 18+. Reply ONLY 'ALLOW' or 'BLOCK'.\n\n" + text;

        String result = callOpenAI(prompt);

        if (result.toUpperCase().contains("BLOCK")) {
            throw new RuntimeException("Content is not allowed (AI moderation)");
        }
    }

    // 🟢 BEST ANSWER
    public int chooseBestAnswer(String question, List<String> answers) {

        if (answers == null || answers.isEmpty()) return -1;

        StringBuilder prompt = new StringBuilder();
        prompt.append("Question:\n").append(question).append("\n\nAnswers:\n");

        for (int i = 0; i < answers.size(); i++) {
            prompt.append(i + 1).append(". ").append(answers.get(i)).append("\n");
        }

        prompt.append("\nReturn ONLY the number of the best answer.");

        String result = callOpenAI(prompt.toString());

        try {
            return Integer.parseInt(result.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 1; // fallback
        }
    }

    // 🔧 COMMON CALL
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

        Map choice = (Map) ((List) response.getBody().get("choices")).get(0);
        Map message = (Map) choice.get("message");

        return message.get("content").toString().trim();
    }
}