package com.socialhub.socialhub.dto;

public class UserProfileResponse {

    private String fullName;
    private String username;
    private String email;
    private String role;

    public UserProfileResponse() {
    }

    public UserProfileResponse(String fullName, String username, String email, String role) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}