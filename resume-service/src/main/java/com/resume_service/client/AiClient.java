package com.resume_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import com.resume_service.dto.ApiResponse;

@FeignClient(name = "ai-service", fallback = AiClientFallback.class)
public interface AiClient {

    @PostMapping(value = "/api/ai/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ApiResponse<Map<String, Object>>> extractInsights(
            @RequestPart("file") MultipartFile file);
}
