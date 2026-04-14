package com.socialhub.socialhub.controller;

import com.socialhub.socialhub.dto.CreateCommentRequest;
import com.socialhub.socialhub.model.Comment;
import com.socialhub.socialhub.service.CommentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {
        "http://localhost:3000",
        "https://test1-b157c.web.app"
})
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/posts/{postId}/comments")
    public List<Comment> getComments(@PathVariable Long postId) {
        return commentService.getComments(postId);
    }

    @PostMapping("/posts/{postId}/comments")
    public Comment addComment(@PathVariable Long postId,
                              @RequestBody CreateCommentRequest request,
                              Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return commentService.addComment(postId, request, username);
    }
}