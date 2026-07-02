package com.resume_service.client;

import com.resume_service.dto.ApiResponse;
import com.resume_service.dto.UserRequestResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/users/{id}")
    ApiResponse<UserRequestResponseDTO> getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/users/email/{email}")
    ApiResponse<UserRequestResponseDTO> getUserByEmail(@PathVariable("email") String email);
}
