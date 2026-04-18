package com.socialhub.socialhub.controller;

import com.socialhub.socialhub.dto.UserProfileResponse;
import com.socialhub.socialhub.model.User;
import com.socialhub.socialhub.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin(origins = {
        "http://localhost:3000",
        "https://test1-b157c.web.app",
        "https://test1-b157c.firebaseapp.com"
})
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users/me")
    public UserProfileResponse getCurrentUser(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);

        return new UserProfileResponse(
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    @PostMapping("/auth/update-profile")
    public UserProfileResponse updateCurrentUser(
            Authentication authentication,
            @RequestBody UserProfileResponse request
    ) {
        User user = getAuthenticatedUser(authentication);

        String fullName = request.getFullName() == null ? "" : request.getFullName().trim();
        String username = request.getUsername() == null ? "" : request.getUsername().trim().toLowerCase();
        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();

        if (fullName.length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name must be at least 2 characters");
        }

        if (username.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must be at least 3 characters");
        }

        if (email.isBlank() || !email.contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid email");
        }

        userRepository.findByUsernameIgnoreCase(username).ifPresent(existing -> {
            if (!existing.getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already taken");
            }
        });

        userRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            if (!existing.getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use");
            }
        });

        user.setFullName(fullName);
        user.setUsername(username);
        user.setEmail(email);

        User saved = userRepository.save(user);

        return new UserProfileResponse(
                saved.getFullName(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getRole()
        );
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        String username = authentication.getName();

        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}