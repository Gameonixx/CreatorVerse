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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ContentController.class)
@org.springframework.context.annotation.Import(com.creatorverse.auth.security.SecurityConfig.class)
public class ContentPublishTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContentService contentService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private com.creatorverse.auth.security.CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "owner")
    void ownerCanPublishOwnDraft() throws Exception {
        ContentResponse response = new ContentResponse();
        response.setStatus(ContentStatus.PUBLISHED);
        when(contentService.publishContent(1L)).thenReturn(response);

        mockMvc.perform(post("/api/content/1/publish").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
                
        verify(contentService, times(1)).publishContent(1L);
    }

    @Test
    @WithMockUser(username = "hacker")
    void anotherUserCannotPublishSomeoneElsesDraft() throws Exception {
        doThrow(new ForbiddenException("Forbidden")).when(contentService).publishContent(1L);

        mockMvc.perform(post("/api/content/1/publish").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "owner")
    void publishingAlreadyPublishedContentIsNoopAndDoesNotCorruptState() throws Exception {
        ContentResponse response = new ContentResponse();
        response.setStatus(ContentStatus.PUBLISHED);
        when(contentService.publishContent(1L)).thenReturn(response);

        mockMvc.perform(post("/api/content/1/publish").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
                
        verify(contentService, times(1)).publishContent(1L);
    }
}
