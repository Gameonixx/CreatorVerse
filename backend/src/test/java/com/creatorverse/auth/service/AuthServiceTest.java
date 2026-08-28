package com.creatorverse.auth.service;

import com.creatorverse.auth.dto.AuthResponse;
import com.creatorverse.auth.dto.LoginRequest;
import com.creatorverse.auth.dto.RegisterRequest;
import com.creatorverse.auth.entity.RefreshToken;
import com.creatorverse.auth.security.JwtUtil;
import com.creatorverse.common.exception.DuplicateResourceException;
import com.creatorverse.common.exception.UnauthorizedException;
import com.creatorverse.user.dto.UserResponse;
import com.creatorverse.user.entity.Role;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "Test User", Role.USER);
        testUser.setPassword("encodedPassword");
    }

    @Test
    void register_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("testuser");
        req.setEmail("test@example.com");
        req.setPassword("password123");
        req.setRole(Role.USER);

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        
        User savedUser = new User("testuser", "test@example.com", "Test User", Role.USER);
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse res = authService.register(req);

        assertNotNull(res);
        assertEquals("testuser", res.getUsername());
    }

    @Test
    void register_DuplicateUsername_ThrowsException() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("testuser");
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> authService.register(req));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("testuser");
        req.setEmail("test@test.com");
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> authService.register(req));
    }

    @Test
    void register_AdminRole_ThrowsException() {
        RegisterRequest req = new RegisterRequest();
        req.setRole(Role.ADMIN);
        assertThrows(IllegalArgumentException.class, () -> authService.register(req));
    }

    @Test
    void login_Success() {
        LoginRequest req = new LoginRequest();
        req.setUsernameOrEmail("testuser");
        req.setPassword("password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken("testuser", "USER")).thenReturn("jwtToken");
        when(refreshTokenService.createRefreshToken(testUser)).thenReturn("refreshToken");

        AuthResponse res = authService.login(req);

        assertEquals("jwtToken", res.getAccessToken());
        assertEquals("refreshToken", res.getRefreshToken());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void refreshToken_Success() {
        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setUser(testUser);

        when(refreshTokenService.findByToken("oldToken")).thenReturn(Optional.of(tokenEntity));
        when(refreshTokenService.verifyExpiration(tokenEntity)).thenReturn(tokenEntity);
        when(jwtUtil.generateToken("testuser", "USER")).thenReturn("newJwt");
        when(refreshTokenService.createRefreshToken(testUser)).thenReturn("newRefresh");

        AuthResponse res = authService.refreshToken("oldToken");

        assertEquals("newJwt", res.getAccessToken());
        assertEquals("newRefresh", res.getRefreshToken());
        verify(refreshTokenService).revokeToken(tokenEntity); // Verifies rotation!
    }

    @Test
    void refreshToken_InvalidToken_ThrowsException() {
        when(refreshTokenService.findByToken("invalid")).thenReturn(Optional.empty());
        assertThrows(UnauthorizedException.class, () -> authService.refreshToken("invalid"));
    }
}
