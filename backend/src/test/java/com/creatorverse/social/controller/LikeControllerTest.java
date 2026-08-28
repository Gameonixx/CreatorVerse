package com.creatorverse.social.controller;

import com.creatorverse.common.exception.DuplicateResourceException;
import com.creatorverse.common.exception.ResourceNotFoundException;
import com.creatorverse.social.dto.LikeResponse;
import com.creatorverse.social.service.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LikeController.class)
@AutoConfigureMockMvc(addFilters = false)
class LikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LikeService likeService;

    private LikeResponse likeResponse;

    @BeforeEach
    void setUp() {
        likeResponse = new LikeResponse();
        likeResponse.setId(100L);
        likeResponse.setUserId(1L);
        likeResponse.setContentId(10L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void likeContent_Success() throws Exception {
        when(likeService.likeContent("testuser", 10L)).thenReturn(likeResponse);

        mockMvc.perform(post("/api/social/content/10/like"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.contentId").value(10));
    }

    @Test
    @WithMockUser(username = "testuser")
    void likeContent_Duplicate() throws Exception {
        when(likeService.likeContent("testuser", 10L)).thenThrow(new DuplicateResourceException("Already liked"));

        mockMvc.perform(post("/api/social/content/10/like"))
                .andExpect(status().isConflict());
    }

    @Test
    void likeContent_Unauthenticated() throws Exception {
        // Without @WithMockUser
        mockMvc.perform(post("/api/social/content/10/like"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void unlikeContent_Success() throws Exception {
        doNothing().when(likeService).unlikeContent("testuser", 10L);

        mockMvc.perform(delete("/api/social/content/10/unlike"))
                .andExpect(status().isNoContent());
    }

    @Test
    void unlikeContent_Unauthenticated() throws Exception {
        // Without @WithMockUser
        mockMvc.perform(delete("/api/social/content/10/unlike"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getLikes_Success() throws Exception {
        when(likeService.getLikes(10L)).thenReturn(List.of(likeResponse));

        mockMvc.perform(get("/api/social/content/10/likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100));
    }
}
