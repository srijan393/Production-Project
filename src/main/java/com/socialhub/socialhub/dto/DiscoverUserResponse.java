package com.socialhub.socialhub.dto;

public class DiscoverUserResponse {

    private Long id;
    private String fullName;
    private String username;
    private String bio;
    private String interests;
    private long followersCount;
    private long followingCount;
    private boolean following;

    public DiscoverUserResponse() {
    }

    public DiscoverUserResponse(
            Long id,
            String fullName,
            String username,
            String bio,
            String interests,
            long followersCount,
            long followingCount,
            boolean following
    ) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.bio = bio;
        this.interests = interests;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
        this.following = following;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }
}