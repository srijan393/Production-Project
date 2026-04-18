package com.socialhub.socialhub.service;

import com.socialhub.socialhub.dto.CreatePostRequest;
import com.socialhub.socialhub.model.Post;
import com.socialhub.socialhub.repository.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final OpenAiService openAiService;

    public PostService(PostRepository postRepository, OpenAiService openAiService) {
        this.postRepository = postRepository;
        this.openAiService = openAiService;
    }

    public List<Post> getPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public Post getPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    public Post createPost(CreatePostRequest request, String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login first");
        }

        if (request.getTitle() == null || request.getTitle().trim().length() < 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title must be at least 5 characters");
        }

        if (request.getBody() == null || request.getBody().trim().length() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question details must be at least 10 characters");
        }

        try {
            openAiService.moderateText(request.getTitle() + "\n" + request.getBody());
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI moderation failed");
        }

        Post post = new Post();
        post.setTitle(request.getTitle().trim());
        post.setBody(request.getBody().trim());
        post.setAuthorUsername(username);

        return postRepository.save(post);
    }

    public Post pinBestAnswer(Long postId, Long commentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        post.setBestCommentId(commentId);
        return postRepository.save(post);
    }

    public Post save(Post post) {
        return postRepository.save(post);
    }
}