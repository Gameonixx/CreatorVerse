package com.creatorverse.content.controller;

import com.creatorverse.auth.security.JwtUtil;
import com.creatorverse.common.exception.ForbiddenException;
import com.creatorverse.common.exception.ResourceNotFoundException;
import com.creatorverse.content.service.ContentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContentController.class)
@org.springframework.context.annotation.Import(com.creatorverse.auth.security.SecurityConfig.class)
public class ContentDeletionTest {

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
    void ownerCanDeleteOwnDraft() throws Exception {
        doNothing().when(contentService).deleteContent(1L);

        mockMvc.perform(delete("/api/content/1").with(csrf()))
                .andExpect(status().isNoContent());
                
        verify(contentService, times(1)).deleteContent(1L);
    }

    @Test
    @WithMockUser(username = "owner")
    void ownerCanDeleteOwnPublishedContent() throws Exception {
        doNothing().when(contentService).deleteContent(2L);

        mockMvc.perform(delete("/api/content/2").with(csrf()))
                .andExpect(status().isNoContent());
                
        verify(contentService, times(1)).deleteContent(2L);
    }
    @Test
    @WithMockUser(username = "hacker")
    void anotherUserCannotDeleteContent() throws Exception {
        doThrow(new ForbiddenException("Forbidden")).when(contentService).deleteContent(1L);

        mockMvc.perform(delete("/api/content/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousCannotDeleteContent() throws Exception {
        mockMvc.perform(delete("/api/content/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "owner")
    void deletingNonexistentContentReturnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Not found")).when(contentService).deleteContent(99L);

        mockMvc.perform(delete("/api/content/99").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "owner")
    void successfulDeletionRemovesContent() throws Exception {
        doNothing().when(contentService).deleteContent(1L);

        mockMvc.perform(delete("/api/content/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(contentService, times(1)).deleteContent(1L);
    }
}
