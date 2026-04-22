package com.socialhub.socialhub.service;

import com.socialhub.socialhub.dto.CreatePostRequest;
import com.socialhub.socialhub.model.Follow;
import com.socialhub.socialhub.model.Post;
import com.socialhub.socialhub.model.User;
import com.socialhub.socialhub.repository.FollowRepository;
import com.socialhub.socialhub.repository.PostRepository;
import com.socialhub.socialhub.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final OpenAiService openAiService;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public PostService(
            PostRepository postRepository,
            OpenAiService openAiService,
            UserRepository userRepository,
            FollowRepository followRepository
    ) {
        this.postRepository = postRepository;
        this.openAiService = openAiService;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    public List<Post> getPosts(String username) {
        List<Post> allPosts = new ArrayList<>(postRepository.findAllByOrderByCreatedAtDesc());

        if (username == null || username.isBlank()) {
            Collections.shuffle(allPosts);
            return allPosts;
        }

        User currentUser = userRepository.findByUsernameIgnoreCase(username).orElse(null);
        if (currentUser == null) {
            Collections.shuffle(allPosts);
            return allPosts;
        }

        List<Follow> following = followRepository.findByFollowerId(currentUser.getId());

        Set<String> followedUsernames = following.stream()
                .map(f -> userRepository.findById(f.getFollowingId()).orElse(null))
                .filter(u -> u != null)
                .map(User::getUsername)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        List<Post> followedPosts = new ArrayList<>();
        List<Post> otherPosts = new ArrayList<>();

        for (Post post : allPosts) {
            String author = post.getAuthorUsername() == null ? "" : post.getAuthorUsername().toLowerCase();

            if (followedUsernames.contains(author)) {
                followedPosts.add(post);
            } else {
                otherPosts.add(post);
            }
        }

        Collections.shuffle(otherPosts);

        List<Post> finalFeed = new ArrayList<>();
        finalFeed.addAll(followedPosts);
        finalFeed.addAll(otherPosts);

        return finalFeed;
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
            openAiService.moderateText(
                    request.getTitle() + "\n" + request.getBody(),
                    username,
                    "POST"
            );
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