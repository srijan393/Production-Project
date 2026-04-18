package com.socialhub.socialhub.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OpenAiService {

    private final OpenAIClient client;

    public OpenAiService() {
        try {
            this.client = OpenAIOkHttpClient.fromEnv();
        } catch (Exception e) {
            throw new RuntimeException("OPENAI_API_KEY is missing or invalid in Railway");
        }
    }

    public void moderateText(String text) {
        String prompt = """
                Check if this content contains pornographic, sexual, explicit adult, 18+, or clearly unsafe inappropriate content.
                Reply with ONLY one word:
                ALLOW
                or
                BLOCK

                Content:
                """ + text;

        String result = askOpenAI(prompt);

        if (result == null || result.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI moderation returned empty result");
        }

        String cleaned = result.trim().toUpperCase();

        if (cleaned.contains("BLOCK")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content blocked by AI moderation");
        }

        if (!cleaned.contains("ALLOW")) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI moderation returned invalid result: " + result);
        }
    }

    public int chooseBestAnswer(String question, List<String> answers) {
        if (answers == null || answers.isEmpty()) {
            return -1;
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("Question:\n").append(question).append("\n\nAnswers:\n");

        for (int i = 0; i < answers.size(); i++) {
            prompt.append(i + 1).append(". ").append(answers.get(i)).append("\n");
        }

        prompt.append("\nReturn ONLY the number of the best answer.");

        try {
            String result = askOpenAI(prompt.toString());
            String digits = result == null ? "" : result.replaceAll("[^0-9]", "");
            if (digits.isBlank()) {
                return 1;
            }
            return Integer.parseInt(digits);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    private String askOpenAI(String prompt) {
        try {
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model("gpt-4o-mini")
                    .addSystemMessage("You are a strict moderation and ranking assistant. Follow the user's instruction exactly.")
                    .addUserMessage(prompt)
                    .temperature(0.0)
                    .build();

            ChatCompletion completion = client.chat().completions().create(params);

            if (completion.choices().isEmpty()
                    || completion.choices().get(0).message() == null
                    || completion.choices().get(0).message().content().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI returned no content");
            }

            return completion.choices().get(0).message().content().get(0).text().orElse("").trim();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI call failed: " + e.getMessage());
        }
    }
}