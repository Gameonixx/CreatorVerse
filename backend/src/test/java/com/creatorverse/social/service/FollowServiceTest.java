package com.creatorverse.social.service;

import com.creatorverse.common.exception.DuplicateResourceException;
import com.creatorverse.common.exception.ResourceNotFoundException;
import com.creatorverse.social.dto.FollowResponse;
import com.creatorverse.social.entity.Follow;
import com.creatorverse.social.repository.FollowRepository;
import com.creatorverse.user.dto.UserResponse;
import com.creatorverse.user.entity.Role;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FollowService followService;

    private User follower;
    private User following;
    private Follow follow;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        follower = new User("follower", "follower@test.com", "Follower", Role.USER);
        follower.setId(1L);

        following = new User("following", "following@test.com", "Following", Role.CREATOR);
        following.setId(2L);
        following.setFollowerCount(0);

        follow = new Follow();
        follow.setId(10L);
        follow.setFollower(follower);
        follow.setFollowing(following);
    }

    @Test
    void followUser_Success() {
        when(userRepository.findByUsername("follower")).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        when(followRepository.save(any(Follow.class))).thenReturn(follow);

        FollowResponse response = followService.followUser("follower", 2L);

        assertNotNull(response);
        assertEquals(1L, response.getFollowerId());
        assertEquals(2L, response.getFollowingId());
        verify(followRepository).save(any(Follow.class));
        assertEquals(1, following.getFollowerCount());
    }

    @Test
    void followUser_Duplicate() {
        when(userRepository.findByUsername("follower")).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        when(followRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> followService.followUser("follower", 2L));
        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    void followUser_SelfFollow() {
        when(userRepository.findByUsername("follower")).thenReturn(Optional.of(follower));
        when(userRepository.findById(1L)).thenReturn(Optional.of(follower)); // target is same user

        assertThrows(IllegalArgumentException.class, () -> followService.followUser("follower", 1L));
        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    void followUser_TargetNotFound() {
        when(userRepository.findByUsername("follower")).thenReturn(Optional.of(follower));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> followService.followUser("follower", 99L));
    }

    @Test
    void unfollowUser_Success() {
        following.setFollowerCount(1);
        when(userRepository.findByUsername("follower")).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        when(followRepository.findByFollowerAndFollowing(follower, following)).thenReturn(Optional.of(follow));

        followService.unfollowUser("follower", 2L);

        verify(followRepository).delete(follow);
        assertEquals(0, following.getFollowerCount());
    }

    @Test
    void unfollowUser_FollowerCountNeverNegative() {
        following.setFollowerCount(0); // Already 0
        when(userRepository.findByUsername("follower")).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        when(followRepository.findByFollowerAndFollowing(follower, following)).thenReturn(Optional.of(follow));

        followService.unfollowUser("follower", 2L);

        verify(followRepository).delete(follow);
        assertEquals(0, following.getFollowerCount());
    }

    @Test
    void unfollowUser_NotFound() {
        when(userRepository.findByUsername("follower")).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        when(followRepository.findByFollowerAndFollowing(follower, following)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> followService.unfollowUser("follower", 2L));
    }

    @Test
    void getFollowers_Success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        when(followRepository.findByFollowing(following)).thenReturn(List.of(follow));

        List<UserResponse> followers = followService.getFollowers(2L);

        assertEquals(1, followers.size());
        assertEquals("follower", followers.get(0).getUsername());
    }

    @Test
    void getFollowing_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(followRepository.findByFollower(follower)).thenReturn(List.of(follow));

        List<UserResponse> followingList = followService.getFollowing(1L);

        assertEquals(1, followingList.size());
        assertEquals("following", followingList.get(0).getUsername());
    }
}
