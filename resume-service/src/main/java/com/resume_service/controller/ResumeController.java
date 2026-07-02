package com.resume_service.controller;

import com.resume_service.dto.ApiResponse;
import com.resume_service.dto.PagedResponse;
import com.resume_service.dto.ResumeDto;
import com.resume_service.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/api/resumes")
    public ResponseEntity<ApiResponse<ResumeDto.Response>> createResume(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ResumeDto.CreateRequest request) {
        ResumeDto.Response resume = resumeService.createResume(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resume created", resume));
    }

    @GetMapping("/api/resumes")
    public ResponseEntity<ApiResponse<PagedResponse<ResumeDto.Response>>> getUserResumes(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<ResumeDto.Response> resumes = resumeService.getUserResumes(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Resumes retrieved", resumes));
    }

    @GetMapping("/api/resumes/{id}")
    public ResponseEntity<ApiResponse<ResumeDto.Response>> getResumeById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        ResumeDto.Response resume = resumeService.getResumeById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Resume found", resume));
    }

    @PutMapping("/api/resumes/{id}")
    public ResponseEntity<ApiResponse<ResumeDto.Response>> updateResume(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ResumeDto.UpdateRequest request) {
        ResumeDto.Response resume = resumeService.updateResume(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Resume updated", resume));
    }

    @DeleteMapping("/api/resumes/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteResume(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        resumeService.deleteResume(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Resume deleted", null));
    }

    @GetMapping("/api/admin/resumes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<ResumeDto.Response>>> getAllResumes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<ResumeDto.Response> resumes = resumeService.getAllResumes(pageable);
        return ResponseEntity.ok(ApiResponse.success("All resumes retrieved", resumes));
    }

    @GetMapping("/api/admin/resumes/user/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<ResumeDto.Response>>> getResumesByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<ResumeDto.Response> resumes = resumeService.getUserResumes(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("User resumes retrieved", resumes));
    }
}