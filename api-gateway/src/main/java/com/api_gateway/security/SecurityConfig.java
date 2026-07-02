package com.api_gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchange -> exchange
                // Auth endpoints — both direct and gateway-prefixed
                .pathMatchers(
                    "/auth/login",
                    "/auth/register",
                    "/auth/verify-otp",
                    "/auth/refresh",
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/auth/verify-otp",
                    "/api/auth/refresh"
                ).permitAll()
                .pathMatchers("/actuator/**").permitAll()
                // JwtFilter handles all other auth downstream
                .anyExchange().permitAll()
            )
            .build();
    }
}