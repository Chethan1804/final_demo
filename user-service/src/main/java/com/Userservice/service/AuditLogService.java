package com.Userservice.service;

import com.Userservice.dto.AuditLogRequestDTO;
import com.Userservice.entity.AuditLog;
import com.Userservice.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLog createAuditLog(AuditLogRequestDTO dto) {
        AuditLog log = new AuditLog();
        log.setUserId(dto.getUserId());
        log.setAction(dto.getAction());
        log.setEntityType(dto.getEntityType());
        log.setEntityId(dto.getEntityId());
        log.setDetails(dto.getDetails());
        return auditLogRepository.save(log);
    }

    public List<AuditLog> getAuditLogsByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }
}
