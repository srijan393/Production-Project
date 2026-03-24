package com.socialhub.socialhub.controller;

import com.socialhub.socialhub.dto.CreateCommentRequest;
import com.socialhub.socialhub.dto.CreatePostRequest;
import com.socialhub.socialhub.model.Comment;
import com.socialhub.socialhub.model.Post;
import com.socialhub.socialhub.service.CommentService;
import com.socialhub.socialhub.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    public PostController(PostService postService, CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    @GetMapping("/posts")
    public List<Post> getPosts() {
        return postService.getPosts();
    }

    @PostMapping("/posts")
    public Post createPost(@RequestBody CreatePostRequest request, Authentication auth) {
        String username = auth != null ? auth.getName() : null;
        return postService.createPost(request, username);
    }

    @GetMapping("/posts/{id}")
    public Post getPost(@PathVariable Long id) {
        return postService.getPost(id);
    }

    @GetMapping("/posts/{id}/comments")
    public List<Comment> getComments(@PathVariable Long id) {
        return commentService.getComments(id);
    }

    @PostMapping("/posts/{id}/comments")
    public Comment addComment(@PathVariable Long id,
                              @RequestBody CreateCommentRequest request,
                              Authentication auth) {
        String username = auth != null ? auth.getName() : null;
        postService.getPost(id);
        return commentService.addComment(id, request, username);
    }

    @PostMapping("/posts/{postId}/best-answer/{commentId}")
    public Post pinBestAnswer(@PathVariable Long postId,
                              @PathVariable Long commentId,
                              Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        Post post = postService.getPost(postId);

        if (!auth.getName().equals(post.getAuthorUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only author can pin best answer");
        }

        Comment comment = commentService.getComment(commentId);

        if (!comment.getPostId().equals(postId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment not on this post");
        }

        post.setBestCommentId(commentId);
        return postService.save(post);
    }
}
