package com.creatorverse.creator.service;

import com.creatorverse.common.exception.DuplicateResourceException;
import com.creatorverse.common.exception.ResourceNotFoundException;
import com.creatorverse.creator.dto.CreatorProfileCreateRequest;
import com.creatorverse.creator.dto.CreatorProfileResponse;
import com.creatorverse.creator.dto.CreatorProfileUpdateRequest;
import com.creatorverse.creator.entity.CreatorProfile;
import com.creatorverse.creator.repository.CreatorProfileRepository;
import com.creatorverse.user.entity.Role;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorverse.social.repository.FollowRepository;
import com.creatorverse.auth.security.SecurityUtils;

@Service
public class CreatorProfileService {

    private final CreatorProfileRepository creatorProfileRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public CreatorProfileService(CreatorProfileRepository creatorProfileRepository, 
                                 UserRepository userRepository,
                                 FollowRepository followRepository) {
        this.creatorProfileRepository = creatorProfileRepository;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    @Transactional
    public CreatorProfileResponse createProfile(Long userId, CreatorProfileCreateRequest request) {
        if (creatorProfileRepository.existsByUserId(userId)) {
            throw new DuplicateResourceException("CreatorProfile already exists for user: " + userId);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Note: For Phase 1, we allow profile creation without enforcing ROLE_CREATOR here yet,
        // but we could enforce it if desired. We will keep it simple.
        if (user.getRole() != Role.CREATOR && user.getRole() != Role.ADMIN) {
             throw new IllegalArgumentException("User must have CREATOR role to create a creator profile");
        }

        CreatorProfile profile = new CreatorProfile();
        profile.setUser(user);
        profile.setNiche(request.getNiche());
        profile.setBio(request.getBio());
        profile.setFollowerCount(0); // Default
        profile.setEngagementRate(0.0); // Default

        profile = creatorProfileRepository.save(profile);
        return mapToResponse(profile, false); // Creator obviously doesn't follow themselves on creation
    }

    @Transactional(readOnly = true)
    public CreatorProfileResponse getProfileByUserId(Long userId) {
        CreatorProfile profile = creatorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("CreatorProfile not found for user: " + userId));
        
        boolean isFollowed = false;
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (currentUsername != null) {
            User currentUser = userRepository.findByUsername(currentUsername).orElse(null);
            if (currentUser != null) {
                isFollowed = followRepository.existsByFollowerAndFollowing(currentUser, profile.getUser());
            }
        }
        
        return mapToResponse(profile, isFollowed);
    }

    @Transactional
    public CreatorProfileResponse updateProfile(Long userId, CreatorProfileUpdateRequest request) {
        CreatorProfile profile = creatorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("CreatorProfile not found for user: " + userId));

        profile.setNiche(request.getNiche());
        profile.setBio(request.getBio());

        profile = creatorProfileRepository.save(profile);
        return mapToResponse(profile, false); // Generally editing own profile, so false is safe
    }

    private CreatorProfileResponse mapToResponse(CreatorProfile profile, boolean isFollowedByCurrentUser) {
        CreatorProfileResponse response = new CreatorProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUser().getId());
        response.setNiche(profile.getNiche());
        response.setBio(profile.getBio());
        response.setFollowerCount(profile.getFollowerCount());
        response.setEngagementRate(profile.getEngagementRate());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());
        response.setIsFollowedByCurrentUser(isFollowedByCurrentUser);
        return response;
    }
}
