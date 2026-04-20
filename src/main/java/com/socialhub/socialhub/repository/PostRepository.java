package com.socialhub.socialhub.repository;

import com.socialhub.socialhub.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
    long countByAuthorUsernameIgnoreCase(String authorUsername);
}