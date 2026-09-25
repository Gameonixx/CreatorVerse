package com.creatorverse.user.service;

import com.creatorverse.common.exception.DuplicateResourceException;
import com.creatorverse.common.exception.ResourceNotFoundException;
import com.creatorverse.user.dto.UserCreateRequest;
import com.creatorverse.user.dto.UserResponse;
import com.creatorverse.user.dto.UserUpdateRequest;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import com.creatorverse.auth.security.SecurityUtils;
import com.creatorverse.social.repository.FollowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.creatorverse.user.repository.UserSpecification;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public UserService(UserRepository userRepository, FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        User user = new User(request.getUsername(), request.getEmail(), request.getDisplayName(), request.getRole());
        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user, checkFollowStatus(user));
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return mapToResponse(user, checkFollowStatus(user));
    }
    
    private boolean checkFollowStatus(User targetUser) {
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (currentUsername != null) {
            User currentUser = userRepository.findByUsername(currentUsername).orElse(null);
            if (currentUser != null && !currentUser.getId().equals(targetUser.getId())) {
                return followRepository.existsByFollowerAndFollowing(currentUser, targetUser);
            }
        }
        return false;
    }
    
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String q, Pageable pageable) {
        Page<User> users = userRepository.findAll(UserSpecification.withSearchQuery(q), pageable);
        return users.map(this::mapToResponse);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponse mapToResponse(User user) {
        return mapToResponse(user, false);
    }

    private UserResponse mapToResponse(User user, boolean isFollowedByCurrentUser) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setDisplayName(user.getDisplayName());
        response.setRole(user.getRole());
        response.setBio(user.getBio());
        response.setFollowerCount(user.getFollowerCount());
        response.setFollowingCount(user.getFollowingCount());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setIsFollowedByCurrentUser(isFollowedByCurrentUser);
        return response;
    }
}
