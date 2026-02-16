package com.hyperativa.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "hyperativa-super-secret-key-for-jwt-token-generation-please-change-in-test");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 86400000);
    }

    @Test
    void testGenerateToken() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", "password");

        String token = jwtTokenProvider.generateToken(authentication);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGenerateTokenFromUsername() {
        String username = "testuser";

        String token = jwtTokenProvider.generateTokenFromUsername(username);

        assertNotNull(token);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    void testGetUsernameFromToken() {
        String username = "testuser";
        String token = jwtTokenProvider.generateTokenFromUsername(username);

        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void testValidateToken() {
        String token = jwtTokenProvider.generateTokenFromUsername("testuser");

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void testValidateInvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void testGetExpirationTime() {
        long expirationTime = jwtTokenProvider.getExpirationTime();

        assertEquals(86400000, expirationTime); // 24 hours in ms
    }
}

