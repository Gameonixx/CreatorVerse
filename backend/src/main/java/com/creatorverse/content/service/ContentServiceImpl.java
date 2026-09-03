package com.creatorverse.content.service;

import com.creatorverse.auth.security.SecurityUtils;
import com.creatorverse.common.exception.ForbiddenException;
import com.creatorverse.common.exception.ResourceNotFoundException;
import com.creatorverse.common.exception.UnauthorizedException;
import com.creatorverse.content.dto.ContentCreateRequest;
import com.creatorverse.content.dto.ContentResponse;
import com.creatorverse.content.dto.ContentSummaryResponse;
import com.creatorverse.content.dto.ContentUpdateRequest;
import com.creatorverse.content.entity.Content;
import com.creatorverse.content.entity.enums.ContentStatus;
import com.creatorverse.content.mapper.ContentMapper;
import com.creatorverse.content.repository.ContentRepository;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.creatorverse.social.repository.ContentLikeRepository;
import com.creatorverse.social.repository.CommentRepository;

@Service
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final MediaStorageService mediaStorageService;
    private final ContentMapper contentMapper;
    private final ContentLikeRepository contentLikeRepository;
    private final CommentRepository commentRepository;

    public ContentServiceImpl(ContentRepository contentRepository, 
                              UserRepository userRepository, 
                              MediaStorageService mediaStorageService, 
                              ContentMapper contentMapper,
                              ContentLikeRepository contentLikeRepository,
                              CommentRepository commentRepository) {
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
        this.mediaStorageService = mediaStorageService;
        this.contentMapper = contentMapper;
        this.contentLikeRepository = contentLikeRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public ContentResponse createContent(ContentCreateRequest request, MultipartFile file, boolean publishNow) {
        User creator = getCurrentUserAsCreator();
        
        // Upload file
        Map<String, Object> uploadResult = mediaStorageService.uploadFile(file);
        String mediaUrl = (String) uploadResult.get("secure_url");
        String format = (String) uploadResult.get("format");
        Long bytes = uploadResult.get("bytes") != null ? ((Number) uploadResult.get("bytes")).longValue() : null;
        Integer duration = uploadResult.get("duration") != null ? ((Number) uploadResult.get("duration")).intValue() : null;

        Content content = new Content();
        content.setCreator(creator);
        content.setTitle(request.getTitle());
        content.setCaption(request.getCaption());
        content.setContentType(request.getContentType());
        content.setVisibility(request.getVisibility());
        
        content.setMediaUrl(mediaUrl);
        content.setMimeType(file.getContentType());
        content.setFileSize(bytes);
        content.setDurationSeconds(duration);
        
        // Attempt to extract a generic thumbnail URL if it's a video from Cloudinary
        if (file.getContentType() != null && file.getContentType().startsWith("video/")) {
            content.setThumbnailUrl(mediaUrl.replace("." + format, ".jpg"));
        } else {
            content.setThumbnailUrl(mediaUrl);
        }

        if (publishNow) {
            content.setStatus(ContentStatus.PUBLISHED);
            content.setPublishedAt(LocalDateTime.now());
        } else {
            content.setStatus(ContentStatus.DRAFT);
        }

        content = contentRepository.save(content);
        return contentMapper.toResponse(content);
    }

    @Override
    @Transactional
    public ContentResponse updateContent(Long contentId, ContentUpdateRequest request) {
        Content content = getMyContentEntity(contentId);
        
        content.setTitle(request.getTitle());
        content.setCaption(request.getCaption());
        if (request.getVisibility() != null) {
            content.setVisibility(request.getVisibility());
        }
        
        return contentMapper.toResponse(contentRepository.save(content));
    }

    @Override
    @Transactional
    public ContentResponse publishContent(Long contentId) {
        Content content = getMyContentEntity(contentId);
        if (content.getStatus() == ContentStatus.DRAFT) {
            content.setStatus(ContentStatus.PUBLISHED);
            content.setPublishedAt(LocalDateTime.now());
            content = contentRepository.save(content);
        }
        return contentMapper.toResponse(content);
    }

    @Override
    @Transactional
    public void deleteContent(Long contentId) {
        Content content = getMyContentEntity(contentId);
        contentRepository.delete(content);
        // Note: We might also want to delete from Cloudinary here, but we don't store the exact public_id separately currently.
        // For Phase 3, deleting the metadata is sufficient, or we can extract public_id from URL.
    }

    @Override
    public ContentResponse getContent(Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));
        // Basic check - only owner or admin can view DRAFT or PRIVATE content
        if (content.getStatus() == ContentStatus.DRAFT || content.getVisibility() == com.creatorverse.content.entity.enums.ContentVisibility.PRIVATE) {
            verifyOwnershipOrAdmin(content);
        }
        
        int likeCount = (int) contentLikeRepository.countByContent(content);
        int commentCount = (int) commentRepository.countByContent(content);
        boolean isLiked = false;
        
        String username = SecurityUtils.getCurrentUsername();
        if (username != null) {
            userRepository.findByUsername(username).ifPresent(user -> {
                if (contentLikeRepository.existsByUserAndContent(user, content)) {
                    // Have to use array hack to modify effectively final variable in lambda, or just use a local var properly
                }
            });
            // Let's do it cleanly
            User currentUser = userRepository.findByUsername(username).orElse(null);
            if (currentUser != null) {
                isLiked = contentLikeRepository.existsByUserAndContent(currentUser, content);
            }
        }
        
        return contentMapper.toResponse(content, likeCount, commentCount, isLiked);
    }

    @Override
    public List<ContentSummaryResponse> getMyContent() {
        User creator = getCurrentUserAsCreator();
        return contentMapper.toSummaryResponseList(contentRepository.findByCreatorId(creator.getId()));
    }

    @Override
    public List<ContentSummaryResponse> getMyDrafts() {
        User creator = getCurrentUserAsCreator();
        return contentMapper.toSummaryResponseList(contentRepository.findByCreatorIdAndStatus(creator.getId(), ContentStatus.DRAFT));
    }

    @Override
    public List<ContentSummaryResponse> getMyPublished() {
        User creator = getCurrentUserAsCreator();
        return contentMapper.toSummaryResponseList(contentRepository.findByCreatorIdAndStatus(creator.getId(), ContentStatus.PUBLISHED));
    }

    @Override
    public org.springframework.data.domain.Page<ContentResponse> getPublicFeed(int page, int size) {
        if (size > 50) {
            size = 50; // Prevent unreasonable page sizes
        }
        if (size < 1) {
            size = 10;
        }
        if (page < 0) {
            page = 0;
        }
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "publishedAt"));
                
        org.springframework.data.domain.Page<Content> contents = contentRepository.findByStatusAndVisibility(
                ContentStatus.PUBLISHED, 
                com.creatorverse.content.entity.enums.ContentVisibility.PUBLIC, 
                pageable);
                
        return mapContentPageWithMetrics(contents);
    }

    @Override
    public org.springframework.data.domain.Page<ContentResponse> getPublicContentByUserId(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        if (size > 50) {
            size = 50; // Prevent unreasonable page sizes
        }
        if (size < 1) {
            size = 10;
        }
        if (page < 0) {
            page = 0;
        }
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "publishedAt"));
                
        org.springframework.data.domain.Page<Content> contents = contentRepository.findByCreatorIdAndStatusAndVisibility(
                userId,
                ContentStatus.PUBLISHED, 
                com.creatorverse.content.entity.enums.ContentVisibility.PUBLIC, 
                pageable);
                
        return mapContentPageWithMetrics(contents);
    }

    private org.springframework.data.domain.Page<ContentResponse> mapContentPageWithMetrics(org.springframework.data.domain.Page<Content> contents) {
        if (contents.isEmpty()) {
            return contents.map(c -> contentMapper.toResponse(c, 0, 0, false));
        }

        List<Content> contentList = contents.getContent();
        
        Map<Long, Integer> likeCounts = contentLikeRepository.countLikesForContents(contentList).stream()
                .collect(java.util.stream.Collectors.toMap(
                        arr -> ((Number) arr[0]).longValue(),
                        arr -> ((Number) arr[1]).intValue()
                ));
                
        Map<Long, Integer> commentCounts = commentRepository.countCommentsForContents(contentList).stream()
                .collect(java.util.stream.Collectors.toMap(
                        arr -> ((Number) arr[0]).longValue(),
                        arr -> ((Number) arr[1]).intValue()
                ));
                
        java.util.Set<Long> likedContentIds = new java.util.HashSet<>();
        String username = SecurityUtils.getCurrentUsername();
        if (username != null) {
            User currentUser = userRepository.findByUsername(username).orElse(null);
            if (currentUser != null) {
                likedContentIds.addAll(contentLikeRepository.findLikedContentIds(contentList, currentUser));
            }
        }
        
        return contents.map(content -> contentMapper.toResponse(
                content,
                likeCounts.getOrDefault(content.getId(), 0),
                commentCounts.getOrDefault(content.getId(), 0),
                likedContentIds.contains(content.getId())
        ));
    }

    private User getCurrentUserAsCreator() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private Content getMyContentEntity(Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));
        verifyOwnershipOrAdmin(content);
        return content;
    }

    private void verifyOwnershipOrAdmin(Content content) {
        if (SecurityUtils.hasRole("ADMIN")) return;
        
        String username = SecurityUtils.getCurrentUsername();
        if (username == null || !content.getCreator().getUsername().equals(username)) {
            throw new ForbiddenException("You do not have permission to access this resource");
        }
    }
}
