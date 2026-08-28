package com.creatorverse.content.service;

import com.creatorverse.content.dto.ContentCreateRequest;
import com.creatorverse.content.dto.ContentResponse;
import com.creatorverse.content.dto.ContentSummaryResponse;
import com.creatorverse.content.dto.ContentUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContentService {
    
    ContentResponse createContent(ContentCreateRequest request, MultipartFile file, boolean publishNow);
    
    ContentResponse updateContent(Long contentId, ContentUpdateRequest request);
    
    ContentResponse publishContent(Long contentId);
    
    void deleteContent(Long contentId);
    
    ContentResponse getContent(Long contentId);
    
    List<ContentSummaryResponse> getMyContent();
    
    List<ContentSummaryResponse> getMyDrafts();
    
    List<ContentSummaryResponse> getMyPublished();
    
    org.springframework.data.domain.Page<ContentResponse> getPublicFeed(int page, int size);
    
    org.springframework.data.domain.Page<ContentResponse> getPublicContentByCreatorId(Long creatorId, int page, int size);
}
