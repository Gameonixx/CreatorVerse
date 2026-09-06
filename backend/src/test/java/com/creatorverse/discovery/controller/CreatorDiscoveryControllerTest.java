package com.creatorverse.discovery.controller;

import com.creatorverse.auth.security.JwtUtil;
import com.creatorverse.discovery.dto.CreatorCardResponse;
import com.creatorverse.discovery.service.CreatorDiscoveryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CreatorDiscoveryController.class)
@EnableMethodSecurity
@org.springframework.context.annotation.Import(com.creatorverse.auth.security.SecurityConfig.class)
public class CreatorDiscoveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreatorDiscoveryService discoveryService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private com.creatorverse.auth.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void discoverCreators_ReturnsCreators() throws Exception {
        CreatorCardResponse response1 = new CreatorCardResponse(2L, "creator1", "Creator One", null, "Bio", 1000, "Fashion", 5.0);
        Page<CreatorCardResponse> page = new PageImpl<>(List.of(response1));

        when(discoveryService.discoverCreators(eq("creator"), eq("Fashion"), eq(500), eq(null), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/discovery/creators")
                        .param("search", "creator")
                        .param("niche", "Fashion")
                        .param("minFollowers", "500")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "followerCount")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("creator1"))
                .andExpect(jsonPath("$.content[0].email").doesNotExist());
    }
}
