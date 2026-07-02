package com.api_gateway.security;

import com.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class JwtFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // Gateway receives /api/auth/... paths — must match with /api prefix
    private static final List<String> PUBLIC_PATHS = List.of(
    	    "/api/auth/login",
    	    "/api/auth/register",
    	    "/api/auth/verify-otp",
    	    "/api/auth/refresh",
    	    "/api/auth/validate",
    	    "/auth/login",
    	    "/auth/register",
    	    "/auth/verify-otp",
    	    "/auth/refresh",
    	    "/auth/validate",
    	    "/api/payments/webhook"
    	);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Public paths — skip JWT check
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or malformed Authorization header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        // util.JwtUtil.validateToken() returns Claims directly — throws on invalid
        Claims claims;
        try {
            claims = jwtUtil.validateToken(token);
        } catch (Exception e) {
            log.warn("Invalid JWT token for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String role   = claims.get("role", String.class);
        String email  = claims.getSubject();
        Long userId   = claims.get("userId", Long.class);

        // ─── RBAC ENFORCEMENT ───

        // /api/admin/** → ROLE_ADMIN only
        if (path.startsWith("/api/admin/") && !"ROLE_ADMIN".equals(role)) {
            log.warn("FORBIDDEN: {} tried {} with role={}", email, path, role);
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

     // /api/ai/extract → ROLE_PREMIUM_USER or ROLE_ADMIN only
        if (path.startsWith("/api/ai/extract") &&
                !("ROLE_PREMIUM_USER".equals(role) || "ROLE_ADMIN".equals(role))) {
            log.warn("FORBIDDEN: {} tried {} with role={}", email, path, role);
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }
     

        // /api/premium/** → ROLE_PREMIUM_USER or ROLE_ADMIN
        if (path.startsWith("/api/premium/") &&
                !("ROLE_PREMIUM_USER".equals(role) || "ROLE_ADMIN".equals(role))) {
            log.warn("FORBIDDEN: {} tried {} with role={}", email, path, role);
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // ─── PROPAGATE USER CONTEXT DOWNSTREAM ───
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r
                        .header("X-User-Role",  role   != null ? role                   : "")
                        .header("X-User-Email", email  != null ? email                  : "")
                        .header("X-User-Id",    userId != null ? String.valueOf(userId) : ""))
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        email, null,
                        role != null ? List.of(new SimpleGrantedAuthority(role)) : List.of()
                );

        return chain.filter(mutatedExchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}