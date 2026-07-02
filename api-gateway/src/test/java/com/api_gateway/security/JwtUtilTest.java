package com.api_gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String validToken;
    private String invalidToken;

    private static final String HEX_SECRET =
            "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    @BeforeEach
    public void setup() {
        jwtUtil = new JwtUtil(HEX_SECRET);

        // Build key same way JwtUtil does — decode hex to bytes
        SecretKey key = Keys.hmacShaKeyFor(decodeHex(HEX_SECRET));

        // 0.12.6 builder API
        validToken = Jwts.builder()
                .subject("user@test.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60))
                .claim("role", "ROLE_USER")
                .claim("userId", 1L)
                .signWith(key)
                .compact();

        invalidToken = validToken + "tampered";
    }

    private byte[] decodeHex(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                                 + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    @Test
    public void testValidateToken_Success() {
        Claims claims = jwtUtil.validateToken(validToken);
        assertNotNull(claims);
        assertEquals("user@test.com", claims.getSubject());
    }

    @Test
    public void testValidateToken_Failure() {
        assertThrows(Exception.class, () -> jwtUtil.validateToken(invalidToken));
    }

    @Test
    public void testValidateToken_Malformed() {
        assertThrows(Exception.class, () -> jwtUtil.validateToken("not.a.jwt.token"));
    }

    @Test
    public void testExtractEmail_Success() {
        String email = jwtUtil.extractEmail(validToken);
        assertEquals("user@test.com", email);
    }

    @Test
    public void testExtractRole_Success() {
        String role = jwtUtil.extractRole(validToken);
        assertEquals("ROLE_USER", role);
    }

    @Test
    public void testExtractUserId_Success() {
        String userId = jwtUtil.extractUserId(validToken);
        assertNotNull(userId);
        assertEquals("1", userId);
    }
}