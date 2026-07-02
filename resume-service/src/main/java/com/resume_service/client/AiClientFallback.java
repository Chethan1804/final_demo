package com.resume_service.client;

import com.resume_service.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Component
public class AiClientFallback implements AiClient {

    @Override
    public ResponseEntity<ApiResponse<Map<String, Object>>> extractInsights(MultipartFile file) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("AI service is currently unavailable. Please try again later.", null));
    }
}
