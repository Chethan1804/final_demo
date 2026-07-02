package com.Userservice.controller;

import com.Userservice.dto.ApiResponse;
import com.Userservice.dto.AuditLogRequestDTO;
import com.Userservice.entity.AuditLog;
import com.Userservice.service.AuditLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PostMapping
    public ResponseEntity<ApiResponse<AuditLog>> createAuditLog(@Valid @RequestBody AuditLogRequestDTO dto) {
        AuditLog created = auditLogService.createAuditLog(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Audit log created", created));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Audit logs fetched", auditLogService.getAuditLogsByUser(userId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAllLogs() {
        return ResponseEntity.ok(ApiResponse.success("All audit logs fetched", auditLogService.getAllAuditLogs()));
    }
}
