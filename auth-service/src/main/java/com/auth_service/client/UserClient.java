package com.auth_service.client;

import com.auth_service.dto.ApiResponse;
import com.auth_service.dto.ApiResponse;
import com.auth_service.dto.UserRequestResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service")
public interface UserClient {

    @PostMapping("/api/users")
    ResponseEntity<ApiResponse<UserRequestResponseDTO>> createUser(
            @RequestBody UserRequestResponseDTO dto
    );

    @GetMapping("/api/users/email/{email}")
    ResponseEntity<ApiResponse<UserRequestResponseDTO>> getUserByEmail(
            @PathVariable("email") String email
    );

    @GetMapping("/api/users/{id}")
    ResponseEntity<ApiResponse<UserRequestResponseDTO>> getUserById(
            @PathVariable("id") Long id
    );
}