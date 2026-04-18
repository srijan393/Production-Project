package com.socialhub.socialhub.service;

import com.socialhub.socialhub.dto.AuthResponse;
import com.socialhub.socialhub.dto.ChangePasswordRequest;
import com.socialhub.socialhub.dto.LoginRequest;
import com.socialhub.socialhub.dto.SignupRequest;
import com.socialhub.socialhub.model.User;
import com.socialhub.socialhub.repository.UserRepository;
import com.socialhub.socialhub.security.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse signup(SignupRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }

        validatePassword(request.getPassword());

        String normalizedUsername = request.getUsername().trim().toLowerCase();
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        List<User> existingUsers = userRepository.findAllByUsernameIgnoreCase(normalizedUsername);
        if (!existingUsers.isEmpty()) {
            throw new RuntimeException("Username already exists");
        }

        userRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(existing -> {
            throw new RuntimeException("Email already exists");
        });

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setFullName(request.getFullName().trim());
        user.setRole("USER");
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return new AuthResponse(null, "USER", "Signup successful");
    }

    public AuthResponse login(LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        String normalizedUsername = request.getUsername().trim().toLowerCase();

        List<User> users = userRepository.findAllByUsernameIgnoreCase(normalizedUsername);

        if (users.isEmpty()) {
            throw new RuntimeException("Username not found");
        }

        if (users.size() > 1) {
            throw new RuntimeException("Duplicate users found. Contact support.");
        }

        User user = users.get(0);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Password is incorrect");
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getRole(), "Login successful");
    }

    public void changePassword(Authentication authentication, ChangePasswordRequest request) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Not authenticated");
        }

        String username = authentication.getName().trim().toLowerCase();

        List<User> users = userRepository.findAllByUsernameIgnoreCase(username);

        if (users.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = users.get(0);

        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            throw new RuntimeException("Current password is required");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new RuntimeException("New password is required");
        }

        if (request.getConfirmNewPassword() == null || request.getConfirmNewPassword().isBlank()) {
            throw new RuntimeException("Please confirm your new password");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("New passwords do not match");
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        validatePassword(request.getNewPassword());

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new RuntimeException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new RuntimeException("Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            throw new RuntimeException("Password must contain at least one number");
        }

        if (!password.matches(".*[@$!%*?&._\\-].*")) {
            throw new RuntimeException("Password must contain at least one special character");
        }
    }
}