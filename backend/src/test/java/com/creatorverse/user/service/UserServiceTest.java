package com.creatorverse.user.service;

import com.creatorverse.common.exception.DuplicateResourceException;
import com.creatorverse.user.dto.UserCreateRequest;
import com.creatorverse.user.entity.Role;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import com.creatorverse.social.repository.FollowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import org.mockito.MockedStatic;
import com.creatorverse.auth.security.SecurityUtils;
import java.util.Optional;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_DuplicateUsername() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("existing");
        
        when(userRepository.existsByUsername("existing")).thenReturn(true);
        
        assertThrows(DuplicateResourceException.class, () -> userService.createUser(request));
    }

    @Test
    void createUser_DuplicateEmail() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        
        assertThrows(DuplicateResourceException.class, () -> userService.createUser(request));
    }

    @Test
    void getUser_Authenticated_FollowedUser_ReturnsTrue() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("currentuser");
            
            User target = new User();
            target.setId(2L);
            target.setUsername("targetuser");
            
            User current = new User();
            current.setId(1L);
            current.setUsername("currentuser");
            
            when(userRepository.findById(2L)).thenReturn(Optional.of(target));
            when(userRepository.findByUsername("currentuser")).thenReturn(Optional.of(current));
            when(followRepository.existsByFollowerAndFollowing(current, target)).thenReturn(true);
            
            var response = userService.getUser(2L);
            assertTrue(response.getIsFollowedByCurrentUser());
        }
    }

    @Test
    void getUser_Authenticated_NotFollowedUser_ReturnsFalse() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("currentuser");
            
            User target = new User();
            target.setId(2L);
            target.setUsername("targetuser");
            
            User current = new User();
            current.setId(1L);
            current.setUsername("currentuser");
            
            when(userRepository.findById(2L)).thenReturn(Optional.of(target));
            when(userRepository.findByUsername("currentuser")).thenReturn(Optional.of(current));
            when(followRepository.existsByFollowerAndFollowing(current, target)).thenReturn(false);
            
            var response = userService.getUser(2L);
            assertFalse(response.getIsFollowedByCurrentUser());
        }
    }

    @Test
    void getUser_AnonymousUser_ReturnsFalse() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn(null);
            
            User target = new User();
            target.setId(2L);
            target.setUsername("targetuser");
            
            when(userRepository.findById(2L)).thenReturn(Optional.of(target));
            
            var response = userService.getUser(2L);
            assertFalse(response.getIsFollowedByCurrentUser());
        }
    }

    @Test
    void updateUser_UpdatesAvatarUrl() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        com.creatorverse.user.dto.UserUpdateRequest request = new com.creatorverse.user.dto.UserUpdateRequest();
        request.setAvatarUrl("http://example.com/avatar.png");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        var response = userService.updateUser(1L, request);
        org.junit.jupiter.api.Assertions.assertEquals("http://example.com/avatar.png", response.getAvatarUrl());
        org.junit.jupiter.api.Assertions.assertEquals("http://example.com/avatar.png", user.getAvatarUrl());
    }

    @Test
    void searchUsers_ReturnsCorrectPage() {
        org.springframework.data.domain.Page<User> page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(new User()));
        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        org.springframework.data.domain.Page<com.creatorverse.user.dto.UserResponse> result = userService.searchUsers("test", org.springframework.data.domain.PageRequest.of(0, 10));

        org.junit.jupiter.api.Assertions.assertEquals(1, result.getTotalElements());
    }
}
