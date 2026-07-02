package com.auth_service.controller;

import com.auth_service.client.NotificationClient;
import com.auth_service.client.UserClient;
import com.auth_service.dto.*;
import com.auth_service.security.JwtUtil;
import com.auth_service.service.OtpService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, refresh token")
public class AuthController {

    private final UserClient userClient;
    private final NotificationClient notificationClient;
    private final com.auth_service.service.NotificationEventPublisher notificationEventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    @Value("${notification.internal.token}")
    private String notificationToken;

    // ─────────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────────
    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(@RequestBody @Valid UserRequestResponseDTO dto) {
        log.info("Register attempt for email: {}", dto.getEmail());

        try {
            dto.setPassword(passwordEncoder.encode(dto.getPassword()));

            if (dto.getRole() == null || dto.getRole().isBlank()) {
                dto.setRole("ROLE_USER");
            }

            userClient.createUser(dto);
            log.info("User registered successfully: {}", dto.getEmail());

            // Send welcome email — non-blocking, swallow failure
            try {
                Map<String, Object> emailRequest = Map.of(
                        "to", dto.getEmail(),
                        "subject", "Welcome to Smart Resume Builder!",
                        "templateName", "welcome-email",
                        "variables", Map.of("username", dto.getName())
                );
                notificationClient.sendNotification("Bearer " + notificationToken, emailRequest);
            } catch (Exception ex) {
                log.warn("Welcome email failed (non-critical): {}", ex.getMessage());
            }

            AuthResponseDTO responseDTO = AuthResponseDTO.builder()
                    .message("User registered successfully!")
                    .email(dto.getEmail())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Registration successful", responseDTO));

        } catch (Exception e) {
            log.error("Registration failed for {}: {}", dto.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Registration failed", e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────
    @Operation(summary = "Login and receive JWT tokens")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@RequestBody @Valid AuthRequestDTO dto) {
        log.info("Login attempt for email: {}", dto.getEmail());

        try {
            ResponseEntity<ApiResponse<UserRequestResponseDTO>> response = userClient.getUserByEmail(dto.getEmail());
            UserRequestResponseDTO user = response.getBody().getData();

            if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                log.warn("Invalid credentials for: {}", dto.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Login failed", "Invalid email or password"));
            }

            otpService.generateAndSendOtp(user.getEmail(), user.getName());
            log.info("OTP generated and sent for: {}", dto.getEmail());

            AuthResponseDTO responseDTO = AuthResponseDTO.builder()
                    .message("OTP sent to your email. Please verify to complete login.")
                    .email(user.getEmail())
                    .requiresOtp(true)
                    .build();

            return ResponseEntity.ok(ApiResponse.success("OTP sent", responseDTO));

        } catch (Exception e) {
            log.error("Login error for {}: {}", dto.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Login failed", "User not found or service unavailable"));
        }
    }

    // ─────────────────────────────────────────────
    // VERIFY OTP
    // ─────────────────────────────────────────────
    @Operation(summary = "Verify OTP to complete login")
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> verifyOtp(@RequestBody @Valid VerifyOtpRequestDTO dto) {
        log.info("OTP Verification attempt for email: {}", dto.getEmail());

        try {
            ResponseEntity<ApiResponse<UserRequestResponseDTO>> response = userClient.getUserByEmail(dto.getEmail());
            UserRequestResponseDTO user = response.getBody().getData();

            if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                log.warn("Invalid credentials during OTP verify for: {}", dto.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Verification failed", "Invalid email or password"));
            }

            boolean isOtpValid = otpService.validateOtp(dto.getEmail(), dto.getOtp());
            if (!isOtpValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Verification failed", "Invalid or expired OTP"));
            }

            String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail(), user.getRole());

            log.info("Login successful (OTP verified) for: {}", dto.getEmail());
            notificationEventPublisher.publishLoginEvent(user.getEmail(), user.getName());

            AuthResponseDTO responseDTO = AuthResponseDTO.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .message("Login successful!")
                    .role(user.getRole())
                    .userId(user.getId())
                    .email(user.getEmail())
                    .requiresOtp(false)
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Login successful", responseDTO));

        } catch (Exception e) {
            log.error("Login error for {}: {}", dto.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Login failed", "User not found or service unavailable"));
        }
    }

    // ─────────────────────────────────────────────
    // REFRESH TOKEN
    // ─────────────────────────────────────────────
    @Operation(summary = "Refresh access token using refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> refresh(
            @RequestBody @Valid RefreshTokenRequestDTO request) {

        String refreshToken = request.getRefreshToken();
        log.info("Token refresh requested");

        if (!jwtUtil.validateToken(refreshToken)) {
            log.warn("Invalid or expired refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token refresh failed", "Invalid or expired refresh token"));
        }

        try {
            Claims claims = jwtUtil.extractClaims(refreshToken);

            String tokenType = claims.get("type", String.class);
            if (!"refresh".equals(tokenType)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Token refresh failed", "Provided token is not a refresh token"));
            }

            Long userId = claims.get("userId", Long.class);
            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            String newAccessToken = jwtUtil.generateToken(userId, email, role);
            String newRefreshToken = jwtUtil.generateRefreshToken(userId, email, role);

            log.info("Token refreshed for userId={}", userId);

            AuthResponseDTO responseDTO = AuthResponseDTO.builder()
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .message("Token refreshed successfully")
                    .role(role)
                    .userId(userId)
                    .email(email)
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Token refreshed", responseDTO));

        } catch (Exception e) {
            log.error("Token refresh error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token refresh failed", e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────
    // VALIDATE TOKEN (for internal service calls)
    // ─────────────────────────────────────────────
    @Operation(summary = "Validate JWT token (internal use)")
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validate(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Validation failed", "Missing or malformed Authorization header"));
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Validation failed", "Invalid or expired token"));
        }

        Claims claims = jwtUtil.extractClaims(token);
        Map<String, Object> tokenData = Map.of(
                "userId", claims.get("userId"),
                "email", claims.getSubject(),
                "role", claims.get("role"),
                "valid", true
        );

        return ResponseEntity.ok(ApiResponse.success("Token is valid", tokenData));
    }
}