package com.socialhub.socialhub.repository;

import com.socialhub.socialhub.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
    List<Comment> findAllByOrderByCreatedAtDesc();
    long countByAuthorUsernameIgnoreCase(String authorUsername);
    void deleteByPostId(Long postId);
    void deleteByAuthorUsernameIgnoreCase(String authorUsername);
}