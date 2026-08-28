package com.creatorverse.content.dto;

import com.creatorverse.content.entity.enums.ContentStatus;
import com.creatorverse.content.entity.enums.ContentType;

import java.time.LocalDateTime;

public class ContentSummaryResponse {
    private Long id;
    private String title;
    private ContentType contentType;
    private String thumbnailUrl;
    private ContentStatus status;
    private LocalDateTime publishedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public ContentType getContentType() { return contentType; }
    public void setContentType(ContentType contentType) { this.contentType = contentType; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public ContentStatus getStatus() { return status; }
    public void setStatus(ContentStatus status) { this.status = status; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
