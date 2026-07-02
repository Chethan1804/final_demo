package com.AI_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which user triggered this AI action (from X-User-Id gateway header)
    @Column
    private String userId;

    @Column(nullable = false)
    private String actionType;   // GENERATE | EXTRACT | GENERATE_PDF

    @Column(columnDefinition = "TEXT")
    private String promptOrFilename;

    @Column(columnDefinition = "TEXT")
    private String resultSummary;  // first 500 chars of AI response for audit

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}