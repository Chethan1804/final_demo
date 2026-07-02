package com.resume_service.controller;

import com.resume_service.dto.ApiResponse;
import com.resume_service.entity.AiAnalysisJob;
import com.resume_service.repository.AiAnalysisJobRepository;
import com.resume_service.service.AsyncAiTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/ai/jobs")
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisController {

    private final AiAnalysisJobRepository aiAnalysisJobRepository;
    private final AsyncAiTaskService asyncAiTaskService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Long>> startAiAnalysisJob(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("file") MultipartFile file) {
        
        log.info("Received AI analysis request for user {}", userId);

        try {
            AiAnalysisJob job = new AiAnalysisJob();
            job.setUserId(userId);
            job.setStatus("PENDING");
            job = aiAnalysisJobRepository.save(job);

            asyncAiTaskService.processResumeAsync(
                    file.getBytes(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    job.getId(),
                    userId
            );

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(ApiResponse.success("AI analysis job started", job.getId()));
        } catch (IOException e) {
            log.error("Failed to read file", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to read file", null));
        }
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<AiAnalysisJob>> getJobStatus(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long jobId) {
        
        AiAnalysisJob job = aiAnalysisJobRepository.findById(jobId)
                .orElse(null);

        if (job == null || !job.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Job not found or access denied", null));
        }

        return ResponseEntity.ok(ApiResponse.success("Job status retrieved", job));
    }
}
