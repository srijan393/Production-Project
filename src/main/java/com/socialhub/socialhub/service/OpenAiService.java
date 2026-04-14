package com.socialhub.socialhub.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenAiService {

    public void moderateText(String text) {
        // temporary bypass
    }

    public int chooseBestAnswer(String question, List<String> answers) {
        if (answers == null || answers.isEmpty()) {
            return -1;
        }
        return 1;
    }
}