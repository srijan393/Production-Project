package com.socialhub.socialhub.dto;

public class UserProfileResponse {

    private String fullName;
    private String username;
    private String email;
    private String role;
    private String bio;
    private String interests;
    private long followersCount;
    private long followingCount;

    public UserProfileResponse() {
    }

    public UserProfileResponse(
            String fullName,
            String username,
            String email,
            String role,
            String bio,
            String interests,
            long followersCount,
            long followingCount
    ) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.role = role;
        this.bio = bio;
        this.interests = interests;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getInterests() {
        return interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    public long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(long followersCount) {
        this.followersCount = followersCount;
    }

    public long getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(long followingCount) {
        this.followingCount = followingCount;
    }
}