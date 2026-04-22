package com.socialhub.socialhub;

import com.socialhub.socialhub.dto.CreatePostRequest;
import com.socialhub.socialhub.model.Post;
import com.socialhub.socialhub.model.User;
import com.socialhub.socialhub.repository.FollowRepository;
import com.socialhub.socialhub.repository.PostRepository;
import com.socialhub.socialhub.repository.UserRepository;
import com.socialhub.socialhub.service.OpenAiService;
import com.socialhub.socialhub.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PostServiceTest {

    private PostRepository postRepository;
    private OpenAiService openAiService;
    private UserRepository userRepository;
    private FollowRepository followRepository;
    private PostService postService;

    @BeforeEach
    void setUp() {
        postRepository = mock(PostRepository.class);
        openAiService = mock(OpenAiService.class);
        userRepository = mock(UserRepository.class);
        followRepository = mock(FollowRepository.class);

        postService = new PostService(
                postRepository,
                openAiService,
                userRepository,
                followRepository
        );
    }

    @Test
    void createPostSuccess() {
        User user = new User();
        user.setId(1L);
        user.setUsername("john");

        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("What is React?");
        request.setBody("Please explain React basics in simple terms.");

        when(userRepository.findByUsernameIgnoreCase("john")).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post result = postService.createPost(request, "john");

        assertNotNull(result);
        assertEquals("What is React?", result.getTitle());
        assertEquals("Please explain React basics in simple terms.", result.getBody());
        assertEquals("john", result.getAuthorUsername());

        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createPostFailsWhenTitleTooShort() {
        User user = new User();
        user.setId(1L);
        user.setUsername("john");

        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Hey");
        request.setBody("Please explain React basics in simple terms.");

        when(userRepository.findByUsernameIgnoreCase("john")).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> postService.createPost(request, "john")
        );

        assertNotNull(exception);
    }

    @Test
    void createPostFailsWhenBodyTooShort() {
        User user = new User();
        user.setId(1L);
        user.setUsername("john");

        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("What is React?");
        request.setBody("short");

        when(userRepository.findByUsernameIgnoreCase("john")).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> postService.createPost(request, "john")
        );

        assertNotNull(exception);
    }

    @Test
    void createPostWithoutKnownUserStillReturnsResultInCurrentImplementation() {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("What is React?");
        request.setBody("Please explain React basics in simple terms.");

        when(userRepository.findByUsernameIgnoreCase("john")).thenReturn(Optional.empty());
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post result = postService.createPost(request, "john");

        assertNotNull(result);
        assertEquals("What is React?", result.getTitle());
        assertEquals("Please explain React basics in simple terms.", result.getBody());
    }
}