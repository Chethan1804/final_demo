package com.api_gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import javax.crypto.SecretKey;
import java.util.Date;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class JwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;          // ← com.api_gateway.util.JwtUtil

    @Mock
    private GatewayFilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    private static final String HEX_SECRET =
            "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    /**
     * Builds a real Claims object using JJWT 0.12.6 API.
     * DefaultClaims() no-arg constructor does NOT exist in 0.12.6 — use this instead.
     */
    private Claims buildClaims(String subject, String role) {
        SecretKey key = Keys.hmacShaKeyFor(decodeHex(HEX_SECRET));
        String token = Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .claim("userId", 1L)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private byte[] decodeHex(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i + 1), 16));
        return data;
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Test
    public void testFilter_AuthRoute_SkipValidation() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/auth/login").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        jwtFilter.filter(exchange, filterChain).block();

        verify(filterChain).filter(exchange);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    public void testFilter_MissingAuthHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/resumes").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtFilter.filter(exchange, filterChain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    public void testFilter_InvalidAuthHeaderFormat() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/resumes")
                .header(HttpHeaders.AUTHORIZATION, "Basic token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtFilter.filter(exchange, filterChain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    public void testFilter_InvalidToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/resumes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid_token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // 0.12.6: validateToken returns Claims — throw exception for invalid
        when(jwtUtil.validateToken("invalid_token"))
                .thenThrow(new RuntimeException("Invalid JWT"));

        jwtFilter.filter(exchange, filterChain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    public void testFilter_ValidToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/resumes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // 0.12.6: validateToken returns Claims directly — NO separate extractClaims call
        Claims claims = buildClaims("test@example.com", "ROLE_USER");
        when(jwtUtil.validateToken("valid_token")).thenReturn(claims);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        jwtFilter.filter(exchange, filterChain).block();

        verify(filterChain).filter(any(ServerWebExchange.class));
    }

    @Test
    public void testFilter_AdminRoute_Forbidden() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/admin/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Claims claims = buildClaims("test@example.com", "ROLE_USER");
        when(jwtUtil.validateToken("valid_token")).thenReturn(claims);

        jwtFilter.filter(exchange, filterChain).block();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    public void testFilter_AdminRoute_Success() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/admin/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Claims claims = buildClaims("admin@example.com", "ROLE_ADMIN");
        when(jwtUtil.validateToken("valid_token")).thenReturn(claims);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        jwtFilter.filter(exchange, filterChain).block();

        verify(filterChain).filter(any(ServerWebExchange.class));
    }
}