package com.creatorverse.auth.controller;

import com.creatorverse.auth.dto.AuthResponse;
import com.creatorverse.auth.dto.LoginRequest;
import com.creatorverse.auth.dto.RegisterRequest;
import com.creatorverse.auth.service.AuthService;
import com.creatorverse.auth.service.RefreshTokenService;
import com.creatorverse.user.dto.UserResponse;
import com.creatorverse.user.entity.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import com.creatorverse.auth.security.SecurityConfig;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, com.creatorverse.auth.security.JwtAuthenticationFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private RefreshTokenService refreshTokenService;
    
    @MockBean
    private com.creatorverse.auth.security.JwtUtil jwtUtil;
    
    @MockBean
    private com.creatorverse.auth.security.CustomUserDetailsService userDetailsService;
    
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setDisplayName("New User");
        request.setPassword("password123");
        request.setRole(Role.USER);

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setUsername("newuser");

        Mockito.when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("newuser");
        request.setPassword("password123");

        AuthResponse response = new AuthResponse("access-token-123", "refresh-token-123", 1L);

        Mockito.when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-123"));
    }
}
