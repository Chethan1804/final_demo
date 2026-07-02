package com.resume_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import com.resume_service.dto.PagedResponse;
import com.resume_service.dto.ResumeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.resume_service.entity.Resume;
import com.resume_service.repository.ResumeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
public class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @InjectMocks
    private ResumeService resumeService;

    private ResumeDto.CreateRequest requestDTO;
    private Resume resume;

    @BeforeEach
    void setup() {
        requestDTO = new ResumeDto.CreateRequest();
        requestDTO.setTitle("Software Engineer");
        requestDTO.setSummary("Summary");
        requestDTO.setExperience("Experience");

        resume = new Resume();
        resume.setId(1L);
        resume.setUserId(1L);
        resume.setTitle("Software Engineer");
        resume.setSummary("Summary");
        resume.setExperience("Experience");
        resume.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateResume() {
        when(resumeRepository.save(any(Resume.class))).thenReturn(resume);

        ResumeDto.Response response = resumeService.createResume(1L, requestDTO);

        assertNotNull(response);
        assertEquals("Software Engineer", response.getTitle());

        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void testGetUserResumes() {
        Page<Resume> page = new PageImpl<>(Arrays.asList(resume));
        when(resumeRepository.findByUserId(eq(1L), any(PageRequest.class)))
                .thenReturn(page);

        PagedResponse<ResumeDto.Response> response = resumeService.getUserResumes(1L, PageRequest.of(0, 10));
        assertEquals(1, response.getContent().size());
    }

    @Test
    void testGetResumeById() {
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(resume));

        ResumeDto.Response response = resumeService.getResumeById(1L, 1L);

        assertEquals("Software Engineer", response.getTitle());
    }
}