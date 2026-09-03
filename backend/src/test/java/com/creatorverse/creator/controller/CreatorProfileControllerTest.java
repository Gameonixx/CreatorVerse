package com.creatorverse.creator.controller;

import com.creatorverse.auth.security.JwtUtil;
import com.creatorverse.common.exception.ResourceNotFoundException;
import com.creatorverse.creator.dto.CreatorProfileResponse;
import com.creatorverse.creator.service.CreatorProfileService;
import com.creatorverse.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CreatorProfileController.class)
@EnableMethodSecurity
@org.springframework.context.annotation.Import(com.creatorverse.auth.security.SecurityConfig.class)
public class CreatorProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreatorProfileService creatorProfileService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private com.creatorverse.auth.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void getPublicProfile_Unauthenticated_ReturnsOk() throws Exception {
        CreatorProfileResponse mockResponse = new CreatorProfileResponse();
        mockResponse.setId(1L);
        mockResponse.setUserId(2L);
        mockResponse.setNiche("Gaming");

        when(creatorProfileService.getProfileByUserId(2L)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/creators/profile/public/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.niche").value("Gaming"));
    }

    @Test
    void getPublicProfile_UnknownCreator_ReturnsNotFound() throws Exception {
        when(creatorProfileService.getProfileByUserId(999L)).thenThrow(new ResourceNotFoundException("CreatorProfile not found"));

        mockMvc.perform(get("/api/creators/profile/public/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPrivateProfile_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/creators/profile/2"))
                .andExpect(status().isForbidden()); // Without token, filter chain rejects
    }
}
