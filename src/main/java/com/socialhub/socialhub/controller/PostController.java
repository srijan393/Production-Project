package com.socialhub.socialhub.controller;

import com.socialhub.socialhub.dto.CreateCommentRequest;
import com.socialhub.socialhub.dto.CreatePostRequest;
import com.socialhub.socialhub.model.Comment;
import com.socialhub.socialhub.model.Post;
import com.socialhub.socialhub.repository.CommentRepository;
import com.socialhub.socialhub.repository.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class PostController {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public PostController(PostRepository postRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    // Home feed
    @GetMapping("/posts")
    public List<Post> getPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    // Create post (JWT user)
    @PostMapping("/posts")
    public Post createPost(@RequestBody CreatePostRequest request, Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        if (request.getTitle() == null || request.getTitle().trim().length() < 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title too short");
        }
        if (request.getBody() == null || request.getBody().trim().length() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body too short");
        }

        Post post = new Post();
        post.setTitle(request.getTitle().trim());
        post.setBody(request.getBody().trim());
        post.setAuthorUsername(auth.getName());

        return postRepository.save(post);
    }

    @GetMapping("/posts/{id}")
    public Post getPost(@PathVariable Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    @GetMapping("/posts/{id}/comments")
    public List<Comment> getComments(@PathVariable Long id) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(id);
    }

    @PostMapping("/posts/{id}/comments")
    public Comment addComment(@PathVariable Long id, @RequestBody CreateCommentRequest request, Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (request.getContent() == null || request.getContent().trim().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment too short");
        }

        Comment c = new Comment();
        c.setPostId(id);
        c.setContent(request.getContent().trim());
        c.setAuthorUsername(auth.getName());
        return commentRepository.save(c);
    }

    // Pin best answer (ONLY post author)
    @PostMapping("/posts/{postId}/best-answer/{commentId}")
    public Post pinBestAnswer(@PathVariable Long postId, @PathVariable Long commentId, Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (!auth.getName().equals(post.getAuthorUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only author can pin best answer");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (!comment.getPostId().equals(postId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment not on this post");
        }

        post.setBestCommentId(commentId);
        return postRepository.save(post);
    }
}