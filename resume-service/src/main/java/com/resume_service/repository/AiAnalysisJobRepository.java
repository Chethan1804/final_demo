package com.resume_service.repository;

import com.resume_service.entity.AiAnalysisJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiAnalysisJobRepository extends JpaRepository<AiAnalysisJob, Long> {
    List<AiAnalysisJob> findByUserId(Long userId);
}
