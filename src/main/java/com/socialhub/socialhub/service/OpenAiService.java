package com.socialhub.socialhub.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.moderations.ModerationCreateParams;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenAiService {

    private final OpenAIClient client;

    public OpenAiService() {
        String apiKey = System.getenv("OPENAI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("OPENAI_API_KEY is missing");
        }

        this.client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    public void moderateText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        try {
            var result = client.moderations().create(
                    ModerationCreateParams.builder()
                            .model("omni-moderation-latest")
                            .input(text)
                            .build()
            );

            boolean flagged = result.results().get(0).flagged();

            if (flagged) {
                throw new RuntimeException("Inappropriate content detected");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("AI moderation failed: " + e.getMessage());
        }
    }

    public int chooseBestAnswer(String question, List<String> answers) {
        if (answers == null || answers.isEmpty()) {
            return -1;
        }

        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("Question:\n")
                    .append(question == null ? "" : question)
                    .append("\n\nAnswers:\n");

            for (int i = 0; i < answers.size(); i++) {
                prompt.append(i + 1)
                        .append(". ")
                        .append(answers.get(i))
                        .append("\n");
            }

            prompt.append("\nReturn only the number of the best answer. No explanation.");

            Response response = client.responses().create(
                    ResponseCreateParams.builder()
                            .model(ChatModel.GPT_4O_MINI)
                            .input(prompt.toString())
                            .build()
            );

            String output = response.output().stream()
                    .flatMap(item -> item.message().stream())
                    .flatMap(message -> message.content().stream())
                    .flatMap(content -> content.outputText().stream())
                    .map(outputText -> outputText.text())
                    .findFirst()
                    .orElse("1")
                    .trim();

            int choice = Integer.parseInt(output);
            if (choice >= 1 && choice <= answers.size()) {
                return choice;
            }
        } catch (Exception ignored) {
        }

        return 1;
    }
}