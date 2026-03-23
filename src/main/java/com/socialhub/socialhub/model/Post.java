package com.socialhub.socialhub.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length = 160)
    private String title;

    @Column(nullable=false, length = 5000)
    private String body;

    @Column(nullable=false)
    private String authorUsername;

    private Instant createdAt = Instant.now();

    // Best answer (comment id). Null = unsolved
    private Long bestCommentId;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Long getBestCommentId() { return bestCommentId; }
    public void setBestCommentId(Long bestCommentId) { this.bestCommentId = bestCommentId; }
}