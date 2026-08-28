package com.creatorverse.auth.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // A valid 256-bit key in Base64
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "thisisasecretkeywhichisatleast32byteslongforhs256andnotusedinproduction123!");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 1000L * 60 * 15); // 15 mins
    }

    @Test
    void generateAndValidateToken_Success() {
        String token = jwtUtil.generateToken("testuser", "ROLE_USER");
        assertNotNull(token);

        assertTrue(jwtUtil.isTokenValid(token, "testuser"));
        assertEquals("testuser", jwtUtil.extractUsername(token));
    }

    @Test
    void validateToken_WrongUser_ReturnsFalse() {
        String token = jwtUtil.generateToken("testuser", "ROLE_USER");
        assertFalse(jwtUtil.isTokenValid(token, "otheruser"));
    }

    @Test
    void parseToken_InvalidSignature_ThrowsException() {
        String token = jwtUtil.generateToken("testuser", "ROLE_USER");
        
        JwtUtil otherJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(otherJwtUtil, "secretKey", "anothersecretkeywhichisatleast32byteslongforhs256andnotusedinproduction123!");
        ReflectionTestUtils.setField(otherJwtUtil, "jwtExpiration", 1000L * 60 * 15);
        
        String otherToken = otherJwtUtil.generateToken("testuser", "ROLE_USER");

        assertThrows(SignatureException.class, () -> jwtUtil.extractUsername(otherToken));
    }

    @Test
    void parseToken_Malformed_ThrowsException() {
        assertThrows(MalformedJwtException.class, () -> jwtUtil.extractUsername("not.a.valid.jwt"));
    }

    @Test
    void parseToken_Expired_ThrowsException() throws InterruptedException {
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 1L); // 1 ms expiration
        String token = jwtUtil.generateToken("testuser", "ROLE_USER");
        
        Thread.sleep(10); // Wait for expiration

        assertThrows(ExpiredJwtException.class, () -> jwtUtil.isTokenValid(token, "testuser"));
    }
}
