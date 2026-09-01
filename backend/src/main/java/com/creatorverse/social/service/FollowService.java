package com.creatorverse.social.service;

import com.creatorverse.common.exception.DuplicateResourceException;
import com.creatorverse.common.exception.ResourceNotFoundException;
import com.creatorverse.social.dto.FollowResponse;
import com.creatorverse.social.entity.Follow;
import com.creatorverse.social.repository.FollowRepository;
import com.creatorverse.user.dto.UserResponse;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;



@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    public FollowService(FollowRepository followRepository, 
                         UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FollowResponse followUser(String currentUsername, Long targetUserId) {
        User follower = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        User following = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        if (follower.getId().equals(following.getId())) {
            throw new IllegalArgumentException("User cannot follow themselves");
        }

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new DuplicateResourceException("Already following this user");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);

        follow = followRepository.save(follow);
        
        following.setFollowerCount(following.getFollowerCount() + 1);
        follower.setFollowingCount(follower.getFollowingCount() + 1);
        userRepository.save(following);
        userRepository.save(follower);


        return mapToResponse(follow);
    }

    @Transactional
    public void unfollowUser(String currentUsername, Long targetUserId) {
        User follower = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        User following = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        Follow follow = followRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new ResourceNotFoundException("Follow relationship not found"));

        followRepository.delete(follow);
        
        if (following.getFollowerCount() > 0) {
            following.setFollowerCount(following.getFollowerCount() - 1);
        }
        if (follower.getFollowingCount() > 0) {
            follower.setFollowingCount(follower.getFollowingCount() - 1);
        }
        userRepository.save(following);
        userRepository.save(follower);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getFollowers(Long userId) {
        User following = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return followRepository.findByFollowing(following).stream()
                .map(Follow::getFollower)
                .map(this::mapUserToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getFollowing(Long userId) {
        User follower = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return followRepository.findByFollower(follower).stream()
                .map(Follow::getFollowing)
                .map(this::mapUserToResponse)
                .collect(Collectors.toList());
    }

    private FollowResponse mapToResponse(Follow follow) {
        FollowResponse response = new FollowResponse();
        response.setId(follow.getId());
        response.setFollowerId(follow.getFollower().getId());
        response.setFollowingId(follow.getFollowing().getId());
        response.setCreatedAt(follow.getCreatedAt());
        return response;
    }

    private UserResponse mapUserToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setDisplayName(user.getDisplayName());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
