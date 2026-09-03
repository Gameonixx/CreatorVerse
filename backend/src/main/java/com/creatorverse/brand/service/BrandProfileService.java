package com.creatorverse.brand.service;

import com.creatorverse.brand.dto.BrandProfileCreateRequest;
import com.creatorverse.brand.dto.BrandProfileResponse;
import com.creatorverse.brand.dto.BrandProfileUpdateRequest;
import com.creatorverse.brand.entity.BrandProfile;
import com.creatorverse.brand.repository.BrandProfileRepository;
import com.creatorverse.common.exception.DuplicateResourceException;
import com.creatorverse.common.exception.ResourceNotFoundException;
import com.creatorverse.user.entity.Role;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandProfileService {

    private final BrandProfileRepository brandProfileRepository;
    private final UserRepository userRepository;

    public BrandProfileService(BrandProfileRepository brandProfileRepository, UserRepository userRepository) {
        this.brandProfileRepository = brandProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BrandProfileResponse createProfile(Long userId, BrandProfileCreateRequest request) {
        if (brandProfileRepository.existsByUserId(userId)) {
            throw new DuplicateResourceException("BrandProfile already exists for user: " + userId);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Removed role check allowing standard users to create brand profiles

        BrandProfile profile = new BrandProfile();
        profile.setUser(user);
        profile.setCompanyName(request.getCompanyName());
        profile.setDescription(request.getDescription());
        profile.setIndustry(request.getIndustry());
        profile.setWebsiteUrl(request.getWebsiteUrl());
        profile.setLogoUrl(request.getLogoUrl());

        profile = brandProfileRepository.save(profile);
        return mapToResponse(profile);
    }

    @Transactional(readOnly = true)
    public BrandProfileResponse getProfileByUserId(Long userId) {
        BrandProfile profile = brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("BrandProfile not found for user: " + userId));
        return mapToResponse(profile);
    }

    @Transactional
    public BrandProfileResponse updateProfile(Long userId, BrandProfileUpdateRequest request) {
        BrandProfile profile = brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("BrandProfile not found for user: " + userId));

        profile.setCompanyName(request.getCompanyName());
        profile.setDescription(request.getDescription());
        profile.setIndustry(request.getIndustry());
        profile.setWebsiteUrl(request.getWebsiteUrl());
        profile.setLogoUrl(request.getLogoUrl());

        profile = brandProfileRepository.save(profile);
        return mapToResponse(profile);
    }

    @Transactional
    public void deleteProfile(Long userId) {
        BrandProfile profile = brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("BrandProfile not found for user: " + userId));
        brandProfileRepository.delete(profile);
    }

    private BrandProfileResponse mapToResponse(BrandProfile profile) {
        BrandProfileResponse response = new BrandProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUser().getId());
        response.setCompanyName(profile.getCompanyName());
        response.setDescription(profile.getDescription());
        response.setIndustry(profile.getIndustry());
        response.setWebsiteUrl(profile.getWebsiteUrl());
        response.setLogoUrl(profile.getLogoUrl());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());
        return response;
    }
}
