package com.creatorverse.social.service;

import com.creatorverse.common.exception.DuplicateResourceException;
import com.creatorverse.common.exception.ResourceNotFoundException;
import com.creatorverse.content.entity.Content;
import com.creatorverse.content.repository.ContentRepository;
import com.creatorverse.social.dto.LikeResponse;
import com.creatorverse.social.entity.ContentLike;
import com.creatorverse.social.repository.ContentLikeRepository;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LikeService {

    private final ContentLikeRepository contentLikeRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;

    public LikeService(ContentLikeRepository contentLikeRepository, UserRepository userRepository, ContentRepository contentRepository) {
        this.contentLikeRepository = contentLikeRepository;
        this.userRepository = userRepository;
        this.contentRepository = contentRepository;
    }

    @Transactional
    public LikeResponse likeContent(String currentUsername, Long contentId) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));

        if (contentLikeRepository.existsByUserAndContent(user, content)) {
            throw new DuplicateResourceException("User already liked this content");
        }

        ContentLike contentLike = new ContentLike();
        contentLike.setUser(user);
        contentLike.setContent(content);

        contentLike = contentLikeRepository.save(contentLike);
        return mapToResponse(contentLike);
    }

    @Transactional
    public void unlikeContent(String currentUsername, Long contentId) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));

        ContentLike contentLike = contentLikeRepository.findByUserAndContent(user, content)
                .orElseThrow(() -> new ResourceNotFoundException("Like not found"));

        contentLikeRepository.delete(contentLike);
    }

    @Transactional(readOnly = true)
    public List<LikeResponse> getLikes(Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));

        return contentLikeRepository.findByContent(content).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private LikeResponse mapToResponse(ContentLike contentLike) {
        LikeResponse response = new LikeResponse();
        response.setId(contentLike.getId());
        response.setUserId(contentLike.getUser().getId());
        response.setContentId(contentLike.getContent().getId());
        response.setCreatedAt(contentLike.getCreatedAt());
        return response;
    }
}
