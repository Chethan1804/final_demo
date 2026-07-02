package com.auth_service.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    public void setup() {
        // A dummy secret key that is at least 256 bits (32 bytes) long, MUST BE HEX!
        // 64 hex characters = 32 bytes
        String secret = "e2d534b4c7308d27d7596c810b42c4b8b671a4c95f1f9f38f12a14e9f5e3d7a8";
        long expiration = 1000 * 60 * 60; // 1 hour
        long refreshExpiration = 1000 * 60 * 60 * 24 * 7; // 7 days
        jwtUtil = new JwtUtil(secret, expiration, refreshExpiration);
    }

    @Test
    public void testGenerateToken() {
        String email = "test@example.com";
        String token = jwtUtil.generateToken(1L, email, "ADMIN");


        assertNotNull(token);
        // Basic check if token follows standard JWT format
        String[] parts = token.split("\\.");
        assertTrue(parts.length == 3, "Token should have 3 parts");
    }
}
