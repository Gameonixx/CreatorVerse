package com.creatorverse.content.service;

import com.creatorverse.auth.security.SecurityUtils;
import com.creatorverse.common.exception.ForbiddenException;
import com.creatorverse.common.exception.ResourceNotFoundException;
import com.creatorverse.common.exception.UnauthorizedException;
import com.creatorverse.content.dto.ContentCreateRequest;
import com.creatorverse.content.dto.ContentResponse;
import com.creatorverse.content.dto.ContentUpdateRequest;
import com.creatorverse.content.entity.Content;
import com.creatorverse.content.entity.enums.ContentStatus;
import com.creatorverse.content.entity.enums.ContentType;
import com.creatorverse.content.entity.enums.ContentVisibility;
import com.creatorverse.content.mapper.ContentMapper;
import com.creatorverse.content.repository.ContentRepository;
import com.creatorverse.social.repository.ContentLikeRepository;
import com.creatorverse.social.repository.CommentRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContentServiceTest {

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private ContentLikeRepository contentLikeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MediaStorageService mediaStorageService;

    @Mock
    private ContentMapper contentMapper;

    @InjectMocks
    private ContentServiceImpl contentService;

    private User creatorA;
    private User creatorB;
    private User normalUser;
    private User adminUser;
    
    private Content contentA;

    @BeforeEach
    void setUp() {
        creatorA = new User("creatorA", "a@test.com", "Creator A", Role.CREATOR);
        creatorA.setId(1L);
        
        creatorB = new User("creatorB", "b@test.com", "Creator B", Role.CREATOR);
        creatorB.setId(2L);

        normalUser = new User("user", "user@test.com", "User", Role.USER);
        normalUser.setId(3L);
        
        adminUser = new User("admin", "admin@test.com", "Admin", Role.ADMIN);
        adminUser.setId(4L);
        
        contentA = new Content();
        contentA.setId(10L);
        contentA.setCreator(creatorA);
        contentA.setTitle("Creator A Title");
        contentA.setStatus(ContentStatus.DRAFT);
        contentA.setVisibility(ContentVisibility.PUBLIC);
    }

    // ================= LIFECYCLE TESTS =================

    @Test
    void createContent_Success_Draft() {
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("creatorA");
            when(userRepository.findByUsername("creatorA")).thenReturn(Optional.of(creatorA));

            MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());
            
            Map<String, Object> uploadResult = new HashMap<>();
            uploadResult.put("secure_url", "http://example.com/test.jpg");
            uploadResult.put("format", "jpg");
            uploadResult.put("bytes", 1024L);
            
            when(mediaStorageService.uploadFile(any())).thenReturn(uploadResult);
            
            ContentCreateRequest request = new ContentCreateRequest();
            request.setTitle("My Image");
            request.setContentType(ContentType.IMAGE);

            Content savedContent = new Content();
            savedContent.setId(1L);
            savedContent.setStatus(ContentStatus.DRAFT);
            when(contentRepository.save(any())).thenReturn(savedContent);

            ContentResponse expectedResponse = new ContentResponse();
            expectedResponse.setId(1L);
            expectedResponse.setStatus(ContentStatus.DRAFT);
            when(contentMapper.toResponse(any(Content.class))).thenReturn(expectedResponse);

            ContentResponse response = contentService.createContent(request, file, false);

            assertNotNull(response);
            assertEquals(ContentStatus.DRAFT, response.getStatus());
            verify(contentRepository, times(1)).save(any());
        }
    }

    @Test
    void publishContent_Success_TransitionsDraftToPublished() {
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("creatorA");
            when(contentRepository.findById(10L)).thenReturn(Optional.of(contentA));
            when(contentRepository.save(any())).thenReturn(contentA);
            
            ContentResponse expected = new ContentResponse();
            expected.setStatus(ContentStatus.PUBLISHED);
            when(contentMapper.toResponse(any(Content.class))).thenReturn(expected);

            ContentResponse response = contentService.publishContent(10L);
            
            assertEquals(ContentStatus.PUBLISHED, contentA.getStatus());
            assertNotNull(contentA.getPublishedAt());
            assertEquals(ContentStatus.PUBLISHED, response.getStatus());
            verify(contentRepository, times(1)).save(contentA);
        }
    }
    
    @Test
    void publishContent_AlreadyPublished_DoesNotUpdatePublishedAt() {
        contentA.setStatus(ContentStatus.PUBLISHED);
        LocalDateTime originalTime = LocalDateTime.now().minusDays(1);
        contentA.setPublishedAt(originalTime);
        
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("creatorA");
            when(contentRepository.findById(10L)).thenReturn(Optional.of(contentA));
            
            ContentResponse expected = new ContentResponse();
            expected.setStatus(ContentStatus.PUBLISHED);
            when(contentMapper.toResponse(any(Content.class))).thenReturn(expected);

            contentService.publishContent(10L);
            
            assertEquals(ContentStatus.PUBLISHED, contentA.getStatus());
            assertEquals(originalTime, contentA.getPublishedAt());
            verify(contentRepository, never()).save(any()); // Shouldn't save if already published
        }
    }

    @Test
    void publishContent_NonExistent_ThrowsNotFound() {
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            when(contentRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> contentService.publishContent(99L));
        }
    }

    @Test
    void updateContent_Success_ChangesAllowedMetadata() {
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("creatorA");
            when(contentRepository.findById(10L)).thenReturn(Optional.of(contentA));
            when(contentRepository.save(any())).thenReturn(contentA);
            
            ContentUpdateRequest request = new ContentUpdateRequest();
            request.setTitle("New Title");
            request.setCaption("New Caption");
            request.setVisibility(ContentVisibility.PRIVATE);
            
            contentService.updateContent(10L, request);
            
            assertEquals("New Title", contentA.getTitle());
            assertEquals("New Caption", contentA.getCaption());
            assertEquals(ContentVisibility.PRIVATE, contentA.getVisibility());
            verify(contentRepository, times(1)).save(contentA);
        }
    }

    @Test
    void deleteContent_Success() {
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("creatorA");
            when(contentRepository.findById(10L)).thenReturn(Optional.of(contentA));
            
            contentService.deleteContent(10L);
            
            verify(contentRepository, times(1)).delete(contentA);
        }
    }
    
    // ================= HORIZONTAL OWNERSHIP TESTS =================
    
    @Test
    void updateContent_CreatorBCannotUpdateCreatorAContent() {
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            // Context is Creator B, but content belongs to Creator A
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("creatorB");
            securityUtils.when(() -> SecurityUtils.hasRole("ADMIN")).thenReturn(false);
            when(contentRepository.findById(10L)).thenReturn(Optional.of(contentA));
            
            ContentUpdateRequest request = new ContentUpdateRequest();
            request.setTitle("Hacked Title");
            
            assertThrows(ForbiddenException.class, () -> contentService.updateContent(10L, request));
            verify(contentRepository, never()).save(any());
        }
    }
    
    @Test
    void deleteContent_CreatorBCannotDeleteCreatorAContent() {
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("creatorB");
            securityUtils.when(() -> SecurityUtils.hasRole("ADMIN")).thenReturn(false);
            when(contentRepository.findById(10L)).thenReturn(Optional.of(contentA));
            
            assertThrows(ForbiddenException.class, () -> contentService.deleteContent(10L));
            verify(contentRepository, never()).delete(any());
        }
    }

    @Test
    void publishContent_CreatorBCannotPublishCreatorAContent() {
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("creatorB");
            securityUtils.when(() -> SecurityUtils.hasRole("ADMIN")).thenReturn(false);
            when(contentRepository.findById(10L)).thenReturn(Optional.of(contentA));
            
            assertThrows(ForbiddenException.class, () -> contentService.publishContent(10L));
        }
    }
    
    @Test
    void updateContent_AdminCanOverrideAndUpdateCreatorAContent() {
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            // Context is Admin
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("admin");
            securityUtils.when(() -> SecurityUtils.hasRole("ADMIN")).thenReturn(true);
            when(contentRepository.findById(10L)).thenReturn(Optional.of(contentA));
            when(contentRepository.save(any())).thenReturn(contentA);
            
            ContentUpdateRequest request = new ContentUpdateRequest();
            request.setTitle("Admin Edited Title");
            
            contentService.updateContent(10L, request);
            
            assertEquals("Admin Edited Title", contentA.getTitle());
            verify(contentRepository, times(1)).save(contentA);
        }
    }

    // ================= ROLE AUTHORIZATION TESTS (Create) =================
    
    @Test
    void createContent_Fails_ForUserRole() {
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("user");
            when(userRepository.findByUsername("user")).thenReturn(Optional.of(normalUser));

            MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());
            ContentCreateRequest request = new ContentCreateRequest();

            assertThrows(ForbiddenException.class, () -> contentService.createContent(request, file, false));
        }
    }
    
    @Test
    void createContent_Fails_ForBrandRole() {
        User brandUser = new User("brand", "brand@test.com", "Brand", Role.BRAND);
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("brand");
            when(userRepository.findByUsername("brand")).thenReturn(Optional.of(brandUser));

            MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());
            ContentCreateRequest request = new ContentCreateRequest();

            assertThrows(ForbiddenException.class, () -> contentService.createContent(request, file, false));
        }
    }
    
    @Test
    void createContent_Success_ForAdminRole() {
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("admin");
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

            MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());
            
            Map<String, Object> uploadResult = new HashMap<>();
            uploadResult.put("secure_url", "http://example.com/admin.jpg");
            uploadResult.put("format", "jpg");
            when(mediaStorageService.uploadFile(any())).thenReturn(uploadResult);
            
            ContentCreateRequest request = new ContentCreateRequest();
            request.setTitle("Admin Image");
            request.setContentType(ContentType.IMAGE);

            Content savedContent = new Content();
            savedContent.setId(2L);
            when(contentRepository.save(any())).thenReturn(savedContent);
            when(contentMapper.toResponse(any(Content.class))).thenReturn(new ContentResponse());

            ContentResponse response = contentService.createContent(request, file, false);

            assertNotNull(response);
            verify(contentRepository, times(1)).save(any());
        }
    }

    // ================= PUBLIC FEED TESTS =================

    @Test
    void getPublicFeed_CallsRepositoryWithCorrectFiltersAndPagination() {
        org.springframework.data.domain.Page<Content> emptyPage = org.springframework.data.domain.Page.empty();
        when(contentRepository.findByStatusAndVisibility(
                eq(ContentStatus.PUBLISHED),
                eq(ContentVisibility.PUBLIC),
                any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(emptyPage);

        contentService.getPublicFeed(1, 20);

        verify(contentRepository, times(1)).findByStatusAndVisibility(
                eq(ContentStatus.PUBLISHED),
                eq(ContentVisibility.PUBLIC),
                argThat(pageable -> pageable.getPageNumber() == 1 && pageable.getPageSize() == 20)
        );
    }
}
