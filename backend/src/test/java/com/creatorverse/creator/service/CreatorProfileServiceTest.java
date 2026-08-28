package com.creatorverse.creator.service;

import com.creatorverse.auth.security.SecurityUtils;
import com.creatorverse.common.exception.ResourceNotFoundException;
import com.creatorverse.creator.dto.CreatorProfileResponse;
import com.creatorverse.creator.entity.CreatorProfile;
import com.creatorverse.creator.repository.CreatorProfileRepository;
import com.creatorverse.social.repository.FollowRepository;
import com.creatorverse.user.entity.Role;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreatorProfileServiceTest {

    @Mock
    private CreatorProfileRepository creatorProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private CreatorProfileService creatorProfileService;

    private User creatorUser;
    private User normalUser;
    private CreatorProfile profile;

    @BeforeEach
    void setUp() {
        creatorUser = new User("creator", "creator@test.com", "Creator", Role.CREATOR);
        creatorUser.setId(2L);

        normalUser = new User("user", "user@test.com", "User", Role.USER);
        normalUser.setId(3L);

        profile = new CreatorProfile();
        profile.setId(10L);
        profile.setUser(creatorUser);
        profile.setFollowerCount(100);
    }

    @Test
    void getProfileByUserId_AuthenticatedFollower() {
        when(creatorProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(normalUser));
        when(followRepository.existsByFollowerAndFollowing(normalUser, creatorUser)).thenReturn(true);

        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("user");
            
            CreatorProfileResponse response = creatorProfileService.getProfileByUserId(2L);
            
            assertTrue(response.getIsFollowedByCurrentUser());
        }
    }

    @Test
    void getProfileByUserId_AuthenticatedNonFollower() {
        when(creatorProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(normalUser));
        when(followRepository.existsByFollowerAndFollowing(normalUser, creatorUser)).thenReturn(false);

        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("user");
            
            CreatorProfileResponse response = creatorProfileService.getProfileByUserId(2L);
            
            assertFalse(response.getIsFollowedByCurrentUser());
        }
    }

    @Test
    void getProfileByUserId_Unauthenticated() {
        when(creatorProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));

        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn(null);
            
            CreatorProfileResponse response = creatorProfileService.getProfileByUserId(2L);
            
            assertFalse(response.getIsFollowedByCurrentUser());
        }
    }
}
