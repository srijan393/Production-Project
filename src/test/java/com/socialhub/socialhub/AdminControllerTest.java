package com.socialhub.socialhub;

import com.socialhub.socialhub.controller.AdminController;
import com.socialhub.socialhub.model.Comment;
import com.socialhub.socialhub.model.Post;
import com.socialhub.socialhub.model.User;
import com.socialhub.socialhub.repository.CommentRepository;
import com.socialhub.socialhub.repository.FollowRepository;
import com.socialhub.socialhub.repository.ModerationLogRepository;
import com.socialhub.socialhub.repository.PostRepository;
import com.socialhub.socialhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class AdminControllerTest {

    private UserRepository userRepository;
    private PostRepository postRepository;
    private CommentRepository commentRepository;
    private FollowRepository followRepository;
    private ModerationLogRepository moderationLogRepository;
    private AdminController adminController;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        postRepository = mock(PostRepository.class);
        commentRepository = mock(CommentRepository.class);
        followRepository = mock(FollowRepository.class);
        moderationLogRepository = mock(ModerationLogRepository.class);
        authentication = mock(Authentication.class);

        adminController = new AdminController(
                userRepository,
                postRepository,
                commentRepository,
                followRepository,
                moderationLogRepository
        );
    }

    private void mockAdminAuth() {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole("ADMIN");

        when(authentication.getName()).thenReturn("admin");
        when(userRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(admin));
    }

    @Test
    void deleteUserSuccess() {
        mockAdminAuth();

        User user = new User();
        user.setId(2L);
        user.setUsername("alice");

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        Object result = adminController.deleteUser(2L, authentication);

        assertNotNull(result);
        verify(userRepository).findById(2L);
        verify(userRepository).delete(user);
    }

    @Test
    void deletePostSuccess() {
        mockAdminAuth();

        Post post = new Post();
        post.setId(101L);
        post.setTitle("What is React?");

        when(postRepository.findById(101L)).thenReturn(Optional.of(post));

        Object result = adminController.deletePost(101L, authentication);

        assertNotNull(result);
        verify(postRepository).findById(101L);
        verify(postRepository).delete(post);
    }

    @Test
    void deleteCommentSuccess() {
        mockAdminAuth();

        Comment comment = mock(Comment.class);

        when(commentRepository.findById(201L)).thenReturn(Optional.of(comment));

        Object result = adminController.deleteComment(201L, authentication);

        assertNotNull(result);
        verify(commentRepository).findById(201L);
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteUserNotFoundThrows() {
        mockAdminAuth();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminController.deleteUser(999L, authentication));

        assertNotNull(exception);
    }

    @Test
    void deletePostNotFoundThrows() {
        mockAdminAuth();

        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminController.deletePost(999L, authentication));

        assertNotNull(exception);
    }

    @Test
    void deleteCommentNotFoundThrows() {
        mockAdminAuth();

        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminController.deleteComment(999L, authentication));

        assertNotNull(exception);
    }
}