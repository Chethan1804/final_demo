package com.resume_service.service;


import com.resume_service.dto.PagedResponse;
import com.resume_service.dto.ResumeDto;
import com.resume_service.entity.Resume;
import com.resume_service.exception.AccessDeniedException;
import com.resume_service.exception.ResourceNotFoundException;
import com.resume_service.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;

    @Transactional
    public ResumeDto.Response createResume(Long userId, ResumeDto.CreateRequest request) {
        Resume resume = Resume.builder()
                .userId(userId)
                .title(request.getTitle())
                .summary(request.getSummary())
                .skills(request.getSkills())
                .experience(request.getExperience())
                .education(request.getEducation())
                .build();
        return ResumeDto.Response.from(resumeRepository.save(resume));
    }

    public PagedResponse<ResumeDto.Response> getUserResumes(Long userId, Pageable pageable) {
        Page<ResumeDto.Response> page = resumeRepository.findByUserId(userId, pageable)
                .map(ResumeDto.Response::from);
        return PagedResponse.from(page);
    }

    public ResumeDto.Response getResumeById(Long id, Long requesterId) {
        Resume resume = findAndValidateOwnership(id, requesterId);
        return ResumeDto.Response.from(resume);
    }

    @Transactional
    public ResumeDto.Response updateResume(Long id, Long requesterId, ResumeDto.UpdateRequest request) {
        Resume resume = findAndValidateOwnership(id, requesterId);
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            resume.setTitle(request.getTitle());
        }
        if (request.getSummary() != null) {
            resume.setSummary(request.getSummary());
        }
        if (request.getSkills() != null) {
            resume.setSkills(request.getSkills());
        }
        if (request.getExperience() != null) {
            resume.setExperience(request.getExperience());
        }
        if (request.getEducation() != null) {
            resume.setEducation(request.getEducation());
        }
        return ResumeDto.Response.from(resumeRepository.save(resume));
    }

    @Transactional
    public void deleteResume(Long id, Long requesterId) {
        Resume resume = findAndValidateOwnership(id, requesterId);
        resumeRepository.delete(resume);
    }

    public PagedResponse<ResumeDto.Response> getAllResumes(Pageable pageable) {
        Page<ResumeDto.Response> page = resumeRepository.findAll(pageable)
                .map(ResumeDto.Response::from);
        return PagedResponse.from(page);
    }

    private Resume findAndValidateOwnership(Long id, Long requesterId) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + id));
        if (!resume.getUserId().equals(requesterId)) {
            throw new AccessDeniedException("Access denied: resume does not belong to requesting user");
        }
        return resume;
    }
}