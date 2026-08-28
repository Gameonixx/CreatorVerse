package com.creatorverse.content.controller;

import com.creatorverse.auth.security.JwtUtil;
import com.creatorverse.common.exception.ForbiddenException;
import com.creatorverse.content.dto.ContentResponse;
import com.creatorverse.content.entity.enums.ContentStatus;
import com.creatorverse.content.service.ContentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContentController.class)
@EnableMethodSecurity
@org.springframework.context.annotation.Import(com.creatorverse.auth.security.SecurityConfig.class)
public class ContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContentService contentService;
    
    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private com.creatorverse.auth.security.CustomUserDetailsService customUserDetailsService;

    // ================= ROLE AUTHORIZATION TESTS =================

    @Test
    @WithMockUser(username = "creator1", roles = {"CREATOR"})
    void createContent_AsCreator_Allowed() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", "video data".getBytes());
        MockMultipartFile metadata = new MockMultipartFile("metadata", "", "application/json",
                "{\"title\":\"My Video\", \"contentType\":\"VIDEO\"}".getBytes());

        ContentResponse mockResponse = new ContentResponse();
        mockResponse.setId(1L);
        mockResponse.setTitle("My Video");
        mockResponse.setStatus(ContentStatus.DRAFT);
        when(contentService.createContent(any(), any(), eq(false))).thenReturn(mockResponse);

        mockMvc.perform(multipart("/api/content")
                        .file(file)
                        .file(metadata)
                        .param("publishNow", "false")
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("My Video"));
    }

    @Test
    @WithMockUser(username = "admin1", roles = {"ADMIN"})
    void createContent_AsAdmin_Allowed() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", "video data".getBytes());
        MockMultipartFile metadata = new MockMultipartFile("metadata", "", "application/json",
                "{\"title\":\"Admin Video\", \"contentType\":\"VIDEO\"}".getBytes());

        ContentResponse mockResponse = new ContentResponse();
        mockResponse.setId(2L);
        mockResponse.setTitle("Admin Video");
        when(contentService.createContent(any(), any(), eq(false))).thenReturn(mockResponse);

        mockMvc.perform(multipart("/api/content")
                        .file(file)
                        .file(metadata)
                        .param("publishNow", "false")
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void createContent_AsUser_Forbidden() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", "video data".getBytes());
        MockMultipartFile metadata = new MockMultipartFile("metadata", "", "application/json",
                "{\"title\":\"My Video\", \"contentType\":\"VIDEO\"}".getBytes());

        mockMvc.perform(multipart("/api/content")
                        .file(file)
                        .file(metadata)
                        .param("publishNow", "false")
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "brand1", roles = {"BRAND"})
    void createContent_AsBrand_Forbidden() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", "video data".getBytes());
        MockMultipartFile metadata = new MockMultipartFile("metadata", "", "application/json",
                "{\"title\":\"Brand Video\", \"contentType\":\"VIDEO\"}".getBytes());

        mockMvc.perform(multipart("/api/content")
                        .file(file)
                        .file(metadata)
                        .param("publishNow", "false")
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden());
    }
    
    // ================= OWNERSHIP / EXCEPTION MAPPING TESTS =================

    @Test
    @WithMockUser(username = "creatorB", roles = {"CREATOR"})
    void deleteContent_WhenServiceThrowsForbidden_Returns403() throws Exception {
        doThrow(new ForbiddenException("You do not have permission")).when(contentService).deleteContent(10L);

        mockMvc.perform(delete("/api/content/10")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ================= UPLOAD VALIDATION TESTS =================
    
    @Test
    @WithMockUser(username = "creator1", roles = {"CREATOR"})
    void createContent_MissingFile_ReturnsError() throws Exception {
        MockMultipartFile metadata = new MockMultipartFile("metadata", "", "application/json",
                "{\"title\":\"My Video\", \"contentType\":\"VIDEO\"}".getBytes());

        // File part is completely missing
        mockMvc.perform(multipart("/api/content")
                        .file(metadata)
                        .param("publishNow", "false")
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError());
    }
    
    @Test
    @WithMockUser(username = "creator1", roles = {"CREATOR"})
    void createContent_MissingMetadata_ReturnsError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", "video data".getBytes());

        // Metadata part is missing
        mockMvc.perform(multipart("/api/content")
                        .file(file)
                        .param("publishNow", "false")
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError());
    }

    // ================= PUBLIC FEED TESTS =================

    @Test
    void getPublicFeed_Unauthenticated_ReturnsOk() throws Exception {
        org.springframework.data.domain.Page<ContentResponse> emptyPage = org.springframework.data.domain.Page.empty();
        when(contentService.getPublicFeed(0, 10)).thenReturn(emptyPage);

        mockMvc.perform(get("/api/content/feed")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getPublicFeed_PaginationParams_PassedToService() throws Exception {
        org.springframework.data.domain.Page<ContentResponse> emptyPage = org.springframework.data.domain.Page.empty();
        when(contentService.getPublicFeed(2, 5)).thenReturn(emptyPage);

        mockMvc.perform(get("/api/content/feed")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    // ================= PUBLIC CREATOR CONTENT TESTS =================

    @Test
    void getPublicContentByCreator_Unauthenticated_ReturnsOk() throws Exception {
        org.springframework.data.domain.Page<ContentResponse> emptyPage = org.springframework.data.domain.Page.empty();
        when(contentService.getPublicContentByCreatorId(2L, 0, 10)).thenReturn(emptyPage);

        mockMvc.perform(get("/api/content/creator/2"))
                .andExpect(status().isOk());
    }

    @Test
    void getPublicContentByCreator_PaginationParams_PassedToService() throws Exception {
        org.springframework.data.domain.Page<ContentResponse> emptyPage = org.springframework.data.domain.Page.empty();
        when(contentService.getPublicContentByCreatorId(2L, 1, 20)).thenReturn(emptyPage);

        mockMvc.perform(get("/api/content/creator/2")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }
}
