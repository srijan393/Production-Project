package com.socialhub.socialhub.controller;

import com.socialhub.socialhub.dto.CreatePostRequest;
import com.socialhub.socialhub.model.Post;
import com.socialhub.socialhub.service.PostService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {
        "http://localhost:3000",
        "https://test1-b157c.web.app",
        "https://test1-b157c.firebaseapp.com"
})
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/posts")
    public List<Post> getPosts(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return postService.getPosts(username);
    }

    @GetMapping("/posts/{id}")
    public Post getPost(@PathVariable Long id) {
        return postService.getPost(id);
    }

    @PostMapping("/posts")
    public Post createPost(@RequestBody CreatePostRequest request, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return postService.createPost(request, username);
    }

    @PostMapping("/posts/{postId}/best-answer/{commentId}")
    public Post pinBestAnswer(@PathVariable Long postId, @PathVariable Long commentId) {
        return postService.pinBestAnswer(postId, commentId);
    }
}