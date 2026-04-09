package com.socialhub.socialhub.service;

import com.socialhub.socialhub.dto.CreateCommentRequest;
import com.socialhub.socialhub.model.Comment;
import com.socialhub.socialhub.model.Post;
import com.socialhub.socialhub.repository.CommentRepository;
import com.socialhub.socialhub.repository.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final OpenAiService openAiService;

    public CommentService(CommentRepository commentRepository,
                          PostRepository postRepository,
                          OpenAiService openAiService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.openAiService = openAiService;
    }

    public List<Comment> getComments(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    public Comment addComment(Long postId, CreateCommentRequest request, String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        if (request.getContent() == null || request.getContent().trim().length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Answer must be at least 3 characters");
        }

        openAiService.moderateText(request.getContent());

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setContent(request.getContent().trim());
        comment.setAuthorUsername(username);

        Comment savedComment = commentRepository.save(comment);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        List<String> answerTexts = comments.stream()
                .map(Comment::getContent)
                .toList();

        int bestIndex = openAiService.chooseBestAnswer(post.getTitle() + "\n" + post.getBody(), answerTexts);

        if (bestIndex > 0 && bestIndex <= comments.size()) {
            post.setBestCommentId(comments.get(bestIndex - 1).getId());
            postRepository.save(post);
        }

        return savedComment;
    }

    public Comment getComment(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    }
}