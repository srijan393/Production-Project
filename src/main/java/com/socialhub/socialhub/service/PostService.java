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

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> getPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
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

        Post post = new Post();
        post.setTitle(request.getTitle().trim());
        post.setBody(request.getBody().trim());
        post.setAuthorUsername(username);

        return postRepository.save(post);
    }

    public Post getPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    public Post save(Post post) {
        return postRepository.save(post);
    }
}