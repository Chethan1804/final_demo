package com.auth_service.client;

import com.auth_service.dto.ApiResponse;
import com.auth_service.dto.UserRequestResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserClientFallback implements UserClient {

    @Override
    public ResponseEntity<ApiResponse<UserRequestResponseDTO>> createUser(UserRequestResponseDTO dto) {
        log.warn("Fallback triggered for createUser email={}", dto.getEmail());
        return ResponseEntity.internalServerError().body(ApiResponse.error("Service unavailable", "user-service is down"));
    }

    @Override
    public ResponseEntity<ApiResponse<UserRequestResponseDTO>> getUserByEmail(String email) {
        log.warn("Fallback triggered for email={}", email);
        return ResponseEntity.internalServerError().body(ApiResponse.error("Service unavailable", "user-service is down"));
    }

    @Override
    public ResponseEntity<ApiResponse<UserRequestResponseDTO>> getUserById(Long id) {
        log.warn("Fallback triggered for userId={}", id);
        return ResponseEntity.internalServerError().body(ApiResponse.error("Service unavailable", "user-service is down"));
    }
}