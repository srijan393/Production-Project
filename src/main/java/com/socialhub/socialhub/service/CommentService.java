package com.socialhub.socialhub.service;

import com.socialhub.socialhub.dto.CreateCommentRequest;
import com.socialhub.socialhub.model.Comment;
import com.socialhub.socialhub.repository.CommentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<Comment> getComments(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    public Comment addComment(Long postId, CreateCommentRequest request, String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        if (request.getContent() == null || request.getContent().trim().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment too short");
        }

        Comment c = new Comment();
        c.setPostId(postId);
        c.setContent(request.getContent().trim());
        c.setAuthorUsername(username);

        return commentRepository.save(c);
    }

    public Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    }
}