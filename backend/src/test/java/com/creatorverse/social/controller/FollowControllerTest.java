package com.creatorverse.social.controller;

import com.creatorverse.common.exception.DuplicateResourceException;
import com.creatorverse.common.exception.ResourceNotFoundException;
import com.creatorverse.social.dto.FollowResponse;
import com.creatorverse.social.service.FollowService;
import com.creatorverse.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FollowController.class)
@AutoConfigureMockMvc(addFilters = false)
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FollowService followService;

    private FollowResponse followResponse;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        followResponse = new FollowResponse();
        followResponse.setId(1L);
        followResponse.setFollowerId(1L);
        followResponse.setFollowingId(2L);

        userResponse = new UserResponse();
        userResponse.setId(2L);
        userResponse.setUsername("following_user");
    }

    @Test
    @WithMockUser(username = "follower")
    void followUser_Success() throws Exception {
        when(followService.followUser("follower", 2L)).thenReturn(followResponse);

        mockMvc.perform(post("/api/social/follow/2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.followerId").value(1))
                .andExpect(jsonPath("$.followingId").value(2));
    }

    @Test
    @WithMockUser(username = "follower")
    void followUser_Duplicate() throws Exception {
        when(followService.followUser("follower", 2L)).thenThrow(new DuplicateResourceException("Already following"));

        mockMvc.perform(post("/api/social/follow/2"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "follower")
    void followUser_SelfFollow() throws Exception {
        when(followService.followUser("follower", 1L)).thenThrow(new IllegalArgumentException("User cannot follow themselves"));

        mockMvc.perform(post("/api/social/follow/1"))
                .andExpect(status().isInternalServerError()); // Or 400 if mapped
    }

    @Test
    @WithMockUser(username = "follower")
    void unfollowUser_Success() throws Exception {
        doNothing().when(followService).unfollowUser("follower", 2L);

        mockMvc.perform(delete("/api/social/unfollow/2"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "follower")
    void unfollowUser_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Not found")).when(followService).unfollowUser("follower", 2L);

        mockMvc.perform(delete("/api/social/unfollow/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFollowers_Success() throws Exception {
        when(followService.getFollowers(2L)).thenReturn(List.of(userResponse));

        mockMvc.perform(get("/api/social/followers/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].username").value("following_user"));
    }

    @Test
    void getFollowing_Success() throws Exception {
        when(followService.getFollowing(1L)).thenReturn(List.of(userResponse));

        mockMvc.perform(get("/api/social/following/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }
}
