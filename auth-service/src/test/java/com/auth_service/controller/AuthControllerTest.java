package com.auth_service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.auth_service.client.NotificationClient;
import com.auth_service.client.UserClient;
import com.auth_service.dto.*;
import com.auth_service.security.JwtUtil;
import com.auth_service.service.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private UserClient userClient;
    
    @Mock
    private NotificationClient notificationClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private AuthController authController;

    private UserRequestResponseDTO userRequestResponseDTO;
    private AuthRequestDTO authRequestDTO;
    private VerifyOtpRequestDTO verifyOtpRequestDTO;

    @BeforeEach
    public void setup() {
        userRequestResponseDTO = new UserRequestResponseDTO();
        userRequestResponseDTO.setId(1L);
        userRequestResponseDTO.setName("Test User");
        userRequestResponseDTO.setEmail("test@example.com");
        userRequestResponseDTO.setPassword("password123");
        userRequestResponseDTO.setRole("USER");

        authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setEmail("test@example.com");
        authRequestDTO.setPassword("password123");

        verifyOtpRequestDTO = new VerifyOtpRequestDTO();
        verifyOtpRequestDTO.setEmail("test@example.com");
        verifyOtpRequestDTO.setPassword("password123");
        verifyOtpRequestDTO.setOtp("123456");
    }

    @Test
    public void testRegister_Success() {
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("encodedPassword");
        // UserClient returns ApiResponse wrapped ResponseEntity
        when(userClient.createUser(any(UserRequestResponseDTO.class)))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Success", userRequestResponseDTO)));

        ResponseEntity<ApiResponse<AuthResponseDTO>> response = authController.register(userRequestResponseDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User registered successfully!", response.getBody().getData().getMessage());
    }

    @Test
    public void testRegister_Failure() {
        when(passwordEncoder.encode(any(CharSequence.class))).thenThrow(new RuntimeException("Encoding error"));

        ResponseEntity<ApiResponse<AuthResponseDTO>> response = authController.register(userRequestResponseDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Registration failed", response.getBody().getMessage());
    }

    @Test
    public void testLogin_Success_SendsOtp() {
        when(userClient.getUserByEmail(any(String.class)))
            .thenReturn(ResponseEntity.ok(ApiResponse.success("Success", userRequestResponseDTO)));
        when(passwordEncoder.matches(any(CharSequence.class), any(String.class))).thenReturn(true);
        // OTP service generates OTP
        // void method, so nothing to return

        ResponseEntity<ApiResponse<AuthResponseDTO>> response = authController.login(authRequestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("OTP sent to your email. Please verify to complete login.", response.getBody().getData().getMessage());
        assertEquals(true, response.getBody().getData().getRequiresOtp());
    }

    @Test
    public void testLogin_InvalidCredentials() {
        when(userClient.getUserByEmail(any(String.class)))
            .thenReturn(ResponseEntity.ok(ApiResponse.success("Success", userRequestResponseDTO)));
        when(passwordEncoder.matches(any(CharSequence.class), any(String.class))).thenReturn(false);

        ResponseEntity<ApiResponse<AuthResponseDTO>> response = authController.login(authRequestDTO);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Login failed", response.getBody().getMessage());
    }

    @Test
    public void testLogin_UserNotFound() {
        when(userClient.getUserByEmail(any(String.class))).thenThrow(new RuntimeException("Service down"));

        ResponseEntity<ApiResponse<AuthResponseDTO>> response = authController.login(authRequestDTO);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not found or service unavailable", response.getBody().getErrors().get("error"));
    }

    @Test
    public void testVerifyOtp_Success() {
        when(userClient.getUserByEmail(any(String.class)))
            .thenReturn(ResponseEntity.ok(ApiResponse.success("Success", userRequestResponseDTO)));
        when(passwordEncoder.matches(any(CharSequence.class), any(String.class))).thenReturn(true);
        when(otpService.validateOtp(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(any(Long.class), any(String.class), any(String.class))).thenReturn("dummy.jwt.token");
        when(jwtUtil.generateRefreshToken(any(Long.class), any(String.class), any(String.class))).thenReturn("dummy.refresh.token");

        ResponseEntity<ApiResponse<AuthResponseDTO>> response = authController.verifyOtp(verifyOtpRequestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Login successful!", response.getBody().getData().getMessage());
        assertEquals("dummy.jwt.token", response.getBody().getData().getToken());
    }

    @Test
    public void testVerifyOtp_InvalidOtp() {
        when(userClient.getUserByEmail(any(String.class)))
            .thenReturn(ResponseEntity.ok(ApiResponse.success("Success", userRequestResponseDTO)));
        when(passwordEncoder.matches(any(CharSequence.class), any(String.class))).thenReturn(true);
        when(otpService.validateOtp(anyString(), anyString())).thenReturn(false);

        ResponseEntity<ApiResponse<AuthResponseDTO>> response = authController.verifyOtp(verifyOtpRequestDTO);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid or expired OTP", response.getBody().getErrors().get("error"));
    }
}
