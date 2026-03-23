package com.socialhub.socialhub.controller;

import com.socialhub.socialhub.model.User;
import com.socialhub.socialhub.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {

    private final UserRepository userRepository;

    public TestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/test-db")
    public List<User> testDB() {
        return userRepository.findAll();
    }
}
