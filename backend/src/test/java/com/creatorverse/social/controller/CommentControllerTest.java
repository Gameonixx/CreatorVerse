package com.creatorverse.social.controller;

import com.creatorverse.common.exception.ForbiddenException;
import com.creatorverse.common.exception.ResourceNotFoundException;
import com.creatorverse.social.dto.CommentRequest;
import com.creatorverse.social.dto.CommentResponse;
import com.creatorverse.social.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    private CommentResponse commentResponse;

    @BeforeEach
    void setUp() {
        commentResponse = new CommentResponse();
        commentResponse.setId(100L);
        commentResponse.setUserId(1L);
        commentResponse.setContentId(10L);
        commentResponse.setText("Test comment");
    }

    @Test
    @WithMockUser(username = "testuser")
    void createComment_Success() throws Exception {
        when(commentService.createComment(eq("testuser"), eq(10L), any(CommentRequest.class))).thenReturn(commentResponse);

        mockMvc.perform(post("/api/social/content/10/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"Test comment\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.text").value("Test comment"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createComment_BlankText() throws Exception {
        mockMvc.perform(post("/api/social/content/10/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"\"}"))
                .andExpect(status().isBadRequest()); // Handled by @Valid
    }

    @Test
    void createComment_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/social/content/10/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"Test comment\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getComments_Success() throws Exception {
        when(commentService.getComments(10L)).thenReturn(List.of(commentResponse));

        mockMvc.perform(get("/api/social/content/10/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteComment_Success() throws Exception {
        doNothing().when(commentService).deleteComment("testuser", 100L);

        mockMvc.perform(delete("/api/social/comments/100"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "otheruser")
    void deleteComment_Forbidden() throws Exception {
        doThrow(new ForbiddenException("Forbidden")).when(commentService).deleteComment("otheruser", 100L);

        mockMvc.perform(delete("/api/social/comments/100"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteComment_Unauthenticated() throws Exception {
        mockMvc.perform(delete("/api/social/comments/100"))
                .andExpect(status().isUnauthorized());
    }
}
