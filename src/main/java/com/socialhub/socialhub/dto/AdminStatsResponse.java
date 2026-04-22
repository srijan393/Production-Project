package com.socialhub.socialhub.dto;

public class AdminStatsResponse {

    private long usersCount;
    private long postsCount;
    private long commentsCount;
    private long followsCount;
    private long flaggedCount;

    public AdminStatsResponse(long usersCount, long postsCount, long commentsCount, long followsCount, long flaggedCount) {
        this.usersCount = usersCount;
        this.postsCount = postsCount;
        this.commentsCount = commentsCount;
        this.followsCount = followsCount;
        this.flaggedCount = flaggedCount;
    }

    public long getUsersCount() {
        return usersCount;
    }

    public long getPostsCount() {
        return postsCount;
    }

    public long getCommentsCount() {
        return commentsCount;
    }

    public long getFollowsCount() {
        return followsCount;
    }

    public long getFlaggedCount() {
        return flaggedCount;
    }
}