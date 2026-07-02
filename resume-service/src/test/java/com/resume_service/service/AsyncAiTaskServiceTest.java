package com.resume_service.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.resume_service.client.AiClient;
import com.resume_service.dto.ApiResponse;
import com.resume_service.entity.AiAnalysisJob;
import com.resume_service.repository.AiAnalysisJobRepository;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class AsyncAiTaskServiceTest {

    @Mock
    private AiAnalysisJobRepository aiAnalysisJobRepository;

    @Mock
    private AiClient aiClient;

    @InjectMocks
    private AsyncAiTaskService asyncAiTaskService;

    private AiAnalysisJob job;
    private byte[] dummyBytes = new byte[]{1, 2, 3};

    @BeforeEach
    public void setup() {
        job = new AiAnalysisJob();
        job.setId(10L);
        job.setStatus("PENDING");
    }

    @Test
    public void testProcessResumeAsync_Success() {
        when(aiAnalysisJobRepository.findById(10L)).thenReturn(Optional.of(job));
        
        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success("OK", Map.of("score", 90));
        when(aiClient.extractInsights(any(MultipartFile.class))).thenReturn(ResponseEntity.ok(apiResponse));

        asyncAiTaskService.processResumeAsync(dummyBytes, "test.pdf", "application/pdf", 10L, 1L);

        assertEquals("COMPLETED", job.getStatus());
        verify(aiAnalysisJobRepository).save(job);
    }

    @Test
    public void testProcessResumeAsync_JobNotFound() {
        when(aiAnalysisJobRepository.findById(10L)).thenReturn(Optional.empty());

        try {
            asyncAiTaskService.processResumeAsync(dummyBytes, "test.pdf", "application/pdf", 10L, 1L);
        } catch (RuntimeException e) {
            assertEquals("Job not found", e.getMessage());
        }
    }

    @Test
    public void testProcessResumeAsync_ApiFailure() {
        when(aiAnalysisJobRepository.findById(10L)).thenReturn(Optional.of(job));
        
        when(aiClient.extractInsights(any(MultipartFile.class))).thenReturn(ResponseEntity.status(500).build());

        asyncAiTaskService.processResumeAsync(dummyBytes, "test.pdf", "application/pdf", 10L, 1L);

        assertEquals("FAILED", job.getStatus());
        verify(aiAnalysisJobRepository).save(job);
    }

    @Test
    public void testProcessResumeAsync_Exception() {
        when(aiAnalysisJobRepository.findById(10L)).thenReturn(Optional.of(job));
        
        when(aiClient.extractInsights(any(MultipartFile.class))).thenThrow(new RuntimeException("Network Error"));

        asyncAiTaskService.processResumeAsync(dummyBytes, "test.pdf", "application/pdf", 10L, 1L);

        assertEquals("FAILED", job.getStatus());
        verify(aiAnalysisJobRepository).save(job);
    }
}
