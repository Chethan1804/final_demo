package com.resume_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume_service.client.AiClient;
import com.resume_service.dto.ApiResponse;
import com.resume_service.entity.AiAnalysisJob;
import com.resume_service.repository.AiAnalysisJobRepository;
import com.resume_service.util.CustomMultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AsyncAiTaskService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncAiTaskService.class);

    private final AiAnalysisJobRepository aiAnalysisJobRepository;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    public AsyncAiTaskService(AiAnalysisJobRepository aiAnalysisJobRepository, AiClient aiClient) {
        this.aiAnalysisJobRepository = aiAnalysisJobRepository;
        this.aiClient = aiClient;
        this.objectMapper = new ObjectMapper();
    }

    @Async
    public void processResumeAsync(byte[] fileBytes, String filename, String contentType, Long jobId, Long userId) {
        logger.info("Starting async AI processing for job {}", jobId);
        
        AiAnalysisJob job = aiAnalysisJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        try {
            CustomMultipartFile multipartFile = new CustomMultipartFile(fileBytes, filename, contentType);
            
            ResponseEntity<ApiResponse<Map<String, Object>>> response = aiClient.extractInsights(multipartFile);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> insights = response.getBody().getData();
                job.setResultJson(objectMapper.writeValueAsString(insights));
                job.setStatus("COMPLETED");
                logger.info("AI processing completed successfully for job {}", jobId);
            } else {
                job.setStatus("FAILED");
                job.setResultJson("{\"error\": \"AI service returned failure status\"}");
                logger.error("AI processing failed for job {}", jobId);
            }
        } catch (Exception e) {
            logger.error("Error during async AI processing", e);
            job.setStatus("FAILED");
            job.setResultJson("{\"error\": \"" + e.getMessage() + "\"}");
        } finally {
            aiAnalysisJobRepository.save(job);
        }
    }
}
