package com.socialhub.socialhub.controller;

import com.socialhub.socialhub.dto.DiscoverUserResponse;
import com.socialhub.socialhub.dto.UserProfileResponse;
import com.socialhub.socialhub.model.Follow;
import com.socialhub.socialhub.model.User;
import com.socialhub.socialhub.repository.FollowRepository;
import com.socialhub.socialhub.repository.PostRepository;
import com.socialhub.socialhub.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@RestController
@CrossOrigin(origins = {
        "http://localhost:3000",
        "https://test1-b157c.web.app",
        "https://test1-b157c.firebaseapp.com"
})
public class UserController {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;

    public UserController(
            UserRepository userRepository,
            FollowRepository followRepository,
            PostRepository postRepository
    ) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.postRepository = postRepository;
    }

    @GetMapping("/users/me")
    public UserProfileResponse getCurrentUser(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);

        return new UserProfileResponse(
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getBio(),
                user.getInterests(),
                followRepository.countByFollowingId(user.getId()),
                followRepository.countByFollowerId(user.getId()),
                postRepository.countByAuthorUsernameIgnoreCase(user.getUsername())
        );
    }

    @PostMapping("/auth/update-profile")
    public UserProfileResponse updateCurrentUser(
            Authentication authentication,
            @RequestBody UserProfileResponse request
    ) {
        User user = getAuthenticatedUser(authentication);

        String fullName = request.getFullName() == null ? "" : request.getFullName().trim();
        String username = request.getUsername() == null ? "" : request.getUsername().trim().toLowerCase();
        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();
        String bio = request.getBio() == null ? "" : request.getBio().trim();
        String interests = request.getInterests() == null ? "" : request.getInterests().trim();

        if (fullName.length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name must be at least 2 characters");
        }

        if (username.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must be at least 3 characters");
        }

        if (email.isBlank() || !email.contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid email");
        }

        userRepository.findByUsernameIgnoreCase(username).ifPresent(existing -> {
            if (!existing.getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already taken");
            }
        });

        userRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            if (!existing.getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use");
            }
        });

        user.setFullName(fullName);
        user.setUsername(username);
        user.setEmail(email);
        user.setBio(bio);
        user.setInterests(interests);

        User saved = userRepository.save(user);

        return new UserProfileResponse(
                saved.getFullName(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getRole(),
                saved.getBio(),
                saved.getInterests(),
                followRepository.countByFollowingId(saved.getId()),
                followRepository.countByFollowerId(saved.getId()),
                postRepository.countByAuthorUsernameIgnoreCase(saved.getUsername())
        );
    }

    @GetMapping("/users/discover")
    public List<DiscoverUserResponse> discoverUsers(Authentication authentication) {
        User currentUser = getAuthenticatedUser(authentication);

        return userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(user -> new DiscoverUserResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getUsername(),
                        user.getBio(),
                        user.getInterests(),
                        followRepository.countByFollowingId(user.getId()),
                        followRepository.countByFollowerId(user.getId()),
                        followRepository.existsByFollowerIdAndFollowingId(currentUser.getId(), user.getId())
                ))
                .toList();
    }

    @GetMapping("/users/me/following")
    public List<DiscoverUserResponse> getMyFollowing(Authentication authentication) {
        User currentUser = getAuthenticatedUser(authentication);

        return followRepository.findByFollowerId(currentUser.getId()).stream()
                .map(follow -> userRepository.findById(follow.getFollowingId())
                        .orElse(null))
                .filter(user -> user != null)
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(user -> new DiscoverUserResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getUsername(),
                        user.getBio(),
                        user.getInterests(),
                        followRepository.countByFollowingId(user.getId()),
                        followRepository.countByFollowerId(user.getId()),
                        true
                ))
                .toList();
    }

    @PostMapping("/users/{userId}/follow")
    public String followUser(@PathVariable Long userId, Authentication authentication) {
        User currentUser = getAuthenticatedUser(authentication);

        if (currentUser.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot follow yourself");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean alreadyFollowing = followRepository.existsByFollowerIdAndFollowingId(currentUser.getId(), targetUser.getId());
        if (alreadyFollowing) {
            return "Already following";
        }

        Follow follow = new Follow();
        follow.setFollowerId(currentUser.getId());
        follow.setFollowingId(targetUser.getId());

        followRepository.save(follow);
        return "Followed successfully";
    }

    @DeleteMapping("/users/{userId}/follow")
    public String unfollowUser(@PathVariable Long userId, Authentication authentication) {
        User currentUser = getAuthenticatedUser(authentication);

        Follow follow = followRepository.findByFollowerIdAndFollowingId(currentUser.getId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Follow relationship not found"));

        followRepository.delete(follow);
        return "Unfollowed successfully";
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        String username = authentication.getName();

        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}