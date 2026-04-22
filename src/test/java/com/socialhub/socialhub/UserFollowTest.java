package com.socialhub.socialhub;

import com.socialhub.socialhub.controller.UserController;
import com.socialhub.socialhub.model.Follow;
import com.socialhub.socialhub.model.User;
import com.socialhub.socialhub.repository.FollowRepository;
import com.socialhub.socialhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserFollowTest {

    private UserRepository userRepository;
    private FollowRepository followRepository;
    private UserController userController;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        followRepository = mock(FollowRepository.class);
        userController = new UserController(userRepository, followRepository);
        authentication = mock(Authentication.class);
    }

    @Test
    void followUserSuccess() {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("john");

        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setUsername("alice");
        targetUser.setRole("USER");

        when(authentication.getName()).thenReturn("john");
        when(userRepository.findByUsernameIgnoreCase("john")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(false);

        String result = userController.followUser(2L, authentication);

        assertEquals("Followed successfully", result);

        ArgumentCaptor<Follow> captor = ArgumentCaptor.forClass(Follow.class);
        verify(followRepository).save(captor.capture());

        Follow savedFollow = captor.getValue();
        assertEquals(1L, savedFollow.getFollowerId());
        assertEquals(2L, savedFollow.getFollowingId());
    }

    @Test
    void unfollowUserSuccess() {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("john");

        Follow follow = new Follow();
        follow.setId(100L);
        follow.setFollowerId(1L);
        follow.setFollowingId(2L);

        when(authentication.getName()).thenReturn("john");
        when(userRepository.findByUsernameIgnoreCase("john")).thenReturn(Optional.of(currentUser));
        when(followRepository.findByFollowerIdAndFollowingId(1L, 2L)).thenReturn(Optional.of(follow));

        String result = userController.unfollowUser(2L, authentication);

        assertEquals("Unfollowed successfully", result);
        verify(followRepository).delete(follow);
    }

    @Test
    void cannotFollowYourself() {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("john");

        when(authentication.getName()).thenReturn("john");
        when(userRepository.findByUsernameIgnoreCase("john")).thenReturn(Optional.of(currentUser));

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> userController.followUser(1L, authentication));

        assertTrue(exception.getReason().contains("cannot follow yourself"));
    }

    @Test
    void cannotFollowAdminUser() {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("john");

        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setRole("ADMIN");

        when(authentication.getName()).thenReturn("john");
        when(userRepository.findByUsernameIgnoreCase("john")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> userController.followUser(2L, authentication));

        assertTrue(exception.getReason().contains("Admin users cannot be followed"));
    }

    @Test
    void alreadyFollowingReturnsMessage() {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("john");

        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setUsername("alice");
        targetUser.setRole("USER");

        when(authentication.getName()).thenReturn("john");
        when(userRepository.findByUsernameIgnoreCase("john")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(true);

        String result = userController.followUser(2L, authentication);

        assertEquals("Already following", result);
        verify(followRepository, never()).save(any(Follow.class));
    }
}