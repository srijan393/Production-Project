package com.socialhub.socialhub;

import com.socialhub.socialhub.dto.CreateCommentRequest;
import com.socialhub.socialhub.model.Comment;
import com.socialhub.socialhub.model.Post;
import com.socialhub.socialhub.repository.CommentRepository;
import com.socialhub.socialhub.repository.PostRepository;
import com.socialhub.socialhub.service.CommentService;
import com.socialhub.socialhub.service.OpenAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CommentServiceTest {

    private CommentRepository commentRepository;
    private PostRepository postRepository;
    private OpenAiService openAiService;
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentRepository = mock(CommentRepository.class);
        postRepository = mock(PostRepository.class);
        openAiService = mock(OpenAiService.class);

        commentService = new CommentService(
                commentRepository,
                postRepository,
                openAiService
        );
    }

    @Test
    void addCommentSuccess() {
        Post post = new Post();
        post.setId(10L);
        post.setTitle("What is React?");
        post.setBody("Please explain React basics.");

        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("React is a JavaScript library for building user interfaces.");

        Comment savedComment = mock(Comment.class);
        when(savedComment.getId()).thenReturn(201L);
        when(savedComment.getContent()).thenReturn("React is a JavaScript library for building user interfaces.");
        when(savedComment.getAuthorUsername()).thenReturn("john");
        when(savedComment.getPostId()).thenReturn(10L);

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        doNothing().when(openAiService).moderateText(any(String.class), any(String.class), any(String.class));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
        when(commentRepository.findByPostIdOrderByCreatedAtAsc(10L)).thenReturn(List.of(savedComment));
        when(openAiService.chooseBestAnswer(any(String.class), any(List.class))).thenReturn(1);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comment result = commentService.addComment(10L, request, "john");

        assertNotNull(result);
        assertEquals("john", result.getAuthorUsername());
        assertEquals("React is a JavaScript library for building user interfaces.", result.getContent());

        verify(openAiService).moderateText("React is a JavaScript library for building user interfaces.", "john", "COMMENT");
        verify(commentRepository).save(any(Comment.class));
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void addCommentFailsWhenUserMissing() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("React is useful.");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> commentService.addComment(10L, request, "")
        );

        assertTrue(exception.getReason().contains("Please login first"));
    }

    @Test
    void addCommentFailsWhenContentTooShort() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Hi");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> commentService.addComment(10L, request, "john")
        );

        assertTrue(exception.getReason().contains("at least 3 characters"));
    }

    @Test
    void addCommentFailsWhenPostMissing() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("React is used to build UI.");

        doNothing().when(openAiService).moderateText(any(String.class), any(String.class), any(String.class));
        when(postRepository.findById(10L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> commentService.addComment(10L, request, "john")
        );

        assertTrue(exception.getReason().contains("Post not found"));
    }
}