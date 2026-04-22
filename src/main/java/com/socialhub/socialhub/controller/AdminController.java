package com.socialhub.socialhub.controller;

import com.socialhub.socialhub.dto.AdminStatsResponse;
import com.socialhub.socialhub.model.Comment;
import com.socialhub.socialhub.model.ModerationLog;
import com.socialhub.socialhub.model.Post;
import com.socialhub.socialhub.model.User;
import com.socialhub.socialhub.repository.CommentRepository;
import com.socialhub.socialhub.repository.FollowRepository;
import com.socialhub.socialhub.repository.ModerationLogRepository;
import com.socialhub.socialhub.repository.PostRepository;
import com.socialhub.socialhub.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@CrossOrigin(origins = {
        "http://localhost:3000",
        "https://test1-b157c.web.app",
        "https://test1-b157c.firebaseapp.com"
})
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final FollowRepository followRepository;
    private final ModerationLogRepository moderationLogRepository;

    public AdminController(
            UserRepository userRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            FollowRepository followRepository,
            ModerationLogRepository moderationLogRepository
    ) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.followRepository = followRepository;
        this.moderationLogRepository = moderationLogRepository;
    }

    @GetMapping("/stats")
    public AdminStatsResponse getStats(Authentication authentication) {
        requireAdmin(authentication);

        return new AdminStatsResponse(
                userRepository.count(),
                postRepository.count(),
                commentRepository.count(),
                followRepository.count(),
                moderationLogRepository.count()
        );
    }

    @GetMapping("/users")
    public List<User> getUsers(Authentication authentication) {
        requireAdmin(authentication);
        return userRepository.findAll();
    }

    @GetMapping("/posts")
    public List<Post> getPosts(Authentication authentication) {
        requireAdmin(authentication);
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/comments")
    public List<Comment> getComments(Authentication authentication) {
        requireAdmin(authentication);
        return commentRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/flags")
    public List<ModerationLog> getFlags(Authentication authentication) {
        requireAdmin(authentication);
        return moderationLogRepository.findAllByOrderByCreatedAtDesc();
    }

    @DeleteMapping("/users/{userId}")
    public String deleteUser(@PathVariable Long userId, Authentication authentication) {
        requireAdmin(authentication);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        commentRepository.deleteByAuthorUsernameIgnoreCase(user.getUsername());
        postRepository.deleteByAuthorUsernameIgnoreCase(user.getUsername());
        followRepository.deleteByFollowerIdOrFollowingId(user.getId(), user.getId());
        userRepository.delete(user);

        return "User deleted successfully";
    }

    @DeleteMapping("/posts/{postId}")
    public String deletePost(@PathVariable Long postId, Authentication authentication) {
        requireAdmin(authentication);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        commentRepository.deleteByPostId(postId);
        postRepository.delete(post);

        return "Post deleted successfully";
    }

    @DeleteMapping("/comments/{commentId}")
    public String deleteComment(@PathVariable Long commentId, Authentication authentication) {
        requireAdmin(authentication);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        commentRepository.delete(comment);
        return "Comment deleted successfully";
    }

    private void requireAdmin(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login first");
        }

        User user = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() == null || !user.getRole().equalsIgnoreCase("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access only");
        }
    }
}