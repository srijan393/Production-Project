package com.socialhub.socialhub.controller;

import com.socialhub.socialhub.dto.AuthResponse;
import com.socialhub.socialhub.dto.LoginRequest;
import com.socialhub.socialhub.dto.SignupRequest;
import com.socialhub.socialhub.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {
        "http://localhost:3000",
        "https://test1-b157c.web.app",
        "https://test1-b157c.firebaseapp.com"
})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}