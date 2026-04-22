package com.socialhub.socialhub;

import com.socialhub.socialhub.controller.UserController;
import com.socialhub.socialhub.dto.DiscoverUserResponse;
import com.socialhub.socialhub.dto.UserProfileResponse;
import com.socialhub.socialhub.model.Follow;
import com.socialhub.socialhub.model.User;
import com.socialhub.socialhub.repository.FollowRepository;
import com.socialhub.socialhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    private UserRepository userRepository;
    private FollowRepository followRepository;
    private UserController userController;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        followRepository = mock(FollowRepository.class);
        authentication = mock(Authentication.class);

        userController = new UserController(userRepository, followRepository);
    }

    @Test
    void getCurrentUserSuccess() {
        User user = new User();
        user.setId(1L);
        user.setFullName("John Doe");
        user.setUsername("john");
        user.setEmail("john@test.com");
        user.setRole("USER");
        user.setBio("Java developer");
        user.setInterests("Spring Boot, React");

        when(authentication.getName()).thenReturn("john");
        when(userRepository.findByUsernameIgnoreCase("john")).thenReturn(Optional.of(user));
        when(followRepository.countByFollowingId(1L)).thenReturn(5L);
        when(followRepository.countByFollowerId(1L)).thenReturn(3L);

        UserProfileResponse result = userController.getCurrentUser(authentication);

        assertNotNull(result);
        assertEquals("John Doe", result.getFullName());
        assertEquals("john", result.getUsername());
        assertEquals("john@test.com", result.getEmail());
        assertEquals("USER", result.getRole());
        assertEquals(5L, result.getFollowersCount());
        assertEquals(3L, result.getFollowingCount());
    }

    @Test
    void updateCurrentUserSuccess() {
        User user = new User();
        user.setId(1L);
        user.setUsername("john");

        UserProfileResponse request = new UserProfileResponse();
        request.setFullName("John Updated");
        request.setUsername("johnupdated");
        request.setEmail("johnupdated@test.com");
        request.setBio("Updated bio");
        request.setInterests("Testing, Java");

        when(authentication.getName()).thenReturn("john");
        when(userRepository.findByUsernameIgnoreCase("john")).thenReturn(Optional.of(user));
        when(userRepository.findByUsernameIgnoreCase("johnupdated")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("johnupdated@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(followRepository.countByFollowingId(1L)).thenReturn(4L);
        when(followRepository.countByFollowerId(1L)).thenReturn(2L);

        UserProfileResponse result = userController.updateCurrentUser(authentication, request);

        assertNotNull(result);
        assertEquals("John Updated", result.getFullName());
        assertEquals("johnupdated", result.getUsername());
        assertEquals("johnupdated@test.com", result.getEmail());
        assertEquals("Updated bio", result.getBio());
        assertEquals("Testing, Java", result.getInterests());
    }

    @Test
    void discoverUsersReturnsOnlyNonAdminNonSelfUsers() {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("john");
        currentUser.setCreatedAt(LocalDateTime.now());

        User normalUser = new User();
        normalUser.setId(2L);
        normalUser.setFullName("Alice Johnson");
        normalUser.setUsername("alice");
        normalUser.setRole("USER");
        normalUser.setBio("Developer");
        normalUser.setInterests("React");
        normalUser.setCreatedAt(LocalDateTime.now().minusDays(1));

        User adminUser = new User();
        adminUser.setId(3L);
        adminUser.setFullName("Admin User");
        adminUser.setUsername("admin");
        adminUser.setRole("ADMIN");
        adminUser.setCreatedAt(LocalDateTime.now().minusDays(2));

        when(authentication.getName()).thenReturn("john");
        when(userRepository.findByUsernameIgnoreCase("john")).thenReturn(Optional.of(currentUser));
        when(userRepository.findAll()).thenReturn(List.of(currentUser, normalUser, adminUser));
        when(followRepository.countByFollowingId(2L)).thenReturn(10L);
        when(followRepository.countByFollowerId(2L)).thenReturn(3L);
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(false);

        List<DiscoverUserResponse> result = userController.discoverUsers(authentication);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("alice", result.get(0).getUsername());
    }

    @Test
    void getMyFollowingReturnsFollowingUsers() {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("john");

        Follow follow = new Follow();
        follow.setFollowerId(1L);
        follow.setFollowingId(2L);

        User followedUser = new User();
        followedUser.setId(2L);
        followedUser.setFullName("Alice Johnson");
        followedUser.setUsername("alice");
        followedUser.setBio("Developer");
        followedUser.setInterests("React");
        followedUser.setCreatedAt(LocalDateTime.now());

        when(authentication.getName()).thenReturn("john");
        when(userRepository.findByUsernameIgnoreCase("john")).thenReturn(Optional.of(currentUser));
        when(followRepository.findByFollowerId(1L)).thenReturn(List.of(follow));
        when(userRepository.findById(2L)).thenReturn(Optional.of(followedUser));
        when(followRepository.countByFollowingId(2L)).thenReturn(6L);
        when(followRepository.countByFollowerId(2L)).thenReturn(2L);

        List<DiscoverUserResponse> result = userController.getMyFollowing(authentication);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("alice", result.get(0).getUsername());
        assertTrue(result.get(0).isFollowing());
    }
}