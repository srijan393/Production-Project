package com.socialhub.socialhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiscoverUserResponse {
    private Long id;
    private String fullName;
    private String username;
    private String bio;
    private String interests;
    private long followersCount;
    private long followingCount;
    private boolean following;
}