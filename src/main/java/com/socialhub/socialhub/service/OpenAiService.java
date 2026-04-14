package com.socialhub.socialhub.service;

import org.springframework.stereotype.Service;

@Service
public class OpenAiService {

    public String moderate(String content) {
        return "OK"; // bypass AI for now
    }
}