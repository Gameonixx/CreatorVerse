package com.creatorverse.content.mapper;

import com.creatorverse.content.dto.ContentResponse;
import com.creatorverse.content.dto.ContentSummaryResponse;
import com.creatorverse.content.entity.Content;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ContentMapper {

    public ContentResponse toResponse(Content content, Integer likeCount, Integer commentCount, Boolean isLikedByCurrentUser) {
        if (content == null) return null;
        ContentResponse response = new ContentResponse();
        response.setId(content.getId());
        response.setCreatorId(content.getCreator().getId());
        response.setCreatorDisplayName(content.getCreator().getDisplayName());
        response.setTitle(content.getTitle());
        response.setCaption(content.getCaption());
        response.setContentType(content.getContentType());
        response.setMediaUrl(content.getMediaUrl());
        response.setThumbnailUrl(content.getThumbnailUrl());
        response.setDurationSeconds(content.getDurationSeconds());
        response.setFileSize(content.getFileSize());
        response.setMimeType(content.getMimeType());
        response.setVisibility(content.getVisibility());
        response.setStatus(content.getStatus());
        response.setCreatedAt(content.getCreatedAt());
        response.setUpdatedAt(content.getUpdatedAt());
        response.setPublishedAt(content.getPublishedAt());
        response.setLikeCount(likeCount != null ? likeCount : 0);
        response.setCommentCount(commentCount != null ? commentCount : 0);
        response.setIsLikedByCurrentUser(isLikedByCurrentUser != null ? isLikedByCurrentUser : false);
        return response;
    }

    public ContentResponse toResponse(Content content) {
        return toResponse(content, 0, 0, false);
    }

    public ContentSummaryResponse toSummaryResponse(Content content) {
        if (content == null) return null;
        ContentSummaryResponse response = new ContentSummaryResponse();
        response.setId(content.getId());
        response.setTitle(content.getTitle());
        response.setContentType(content.getContentType());
        response.setThumbnailUrl(content.getThumbnailUrl());
        response.setStatus(content.getStatus());
        response.setPublishedAt(content.getPublishedAt());
        return response;
    }

    public List<ContentResponse> toResponseList(List<Content> contents) {
        return contents.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ContentSummaryResponse> toSummaryResponseList(List<Content> contents) {
        return contents.stream().map(this::toSummaryResponse).collect(Collectors.toList());
    }
}
