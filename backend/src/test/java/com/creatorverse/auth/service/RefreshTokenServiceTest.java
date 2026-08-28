package com.creatorverse.auth.service;

import com.creatorverse.auth.entity.RefreshToken;
import com.creatorverse.auth.repository.RefreshTokenRepository;
import com.creatorverse.common.exception.UnauthorizedException;
import com.creatorverse.user.entity.Role;
import com.creatorverse.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@test.com", "Test", Role.USER);
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 1000L * 60 * 60); // 1 hour
    }

    @Test
    void createRefreshToken_Success() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArguments()[0]);
        String token = refreshTokenService.createRefreshToken(testUser);
        assertNotNull(token);
        assertFalse(token.isEmpty());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void verifyExpiration_ValidToken_ReturnsToken() {
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        token.setRevoked(false);
        assertEquals(token, refreshTokenService.verifyExpiration(token));
    }

    @Test
    void verifyExpiration_ExpiredToken_ThrowsException() {
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(LocalDateTime.now().minusHours(1));
        assertThrows(UnauthorizedException.class, () -> refreshTokenService.verifyExpiration(token));
        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void verifyExpiration_RevokedToken_ThrowsException() {
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        token.setRevoked(true);
        assertThrows(UnauthorizedException.class, () -> refreshTokenService.verifyExpiration(token));
    }
}
