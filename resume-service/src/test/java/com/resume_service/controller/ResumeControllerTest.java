package com.resume_service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import com.resume_service.dto.ApiResponse;
import com.resume_service.dto.PagedResponse;
import com.resume_service.dto.ResumeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.resume_service.service.ResumeService;

@ExtendWith(MockitoExtension.class)
public class ResumeControllerTest {

    @Mock
    private ResumeService resumeService;

    @InjectMocks
    private ResumeController resumeController;

    private ResumeDto.CreateRequest requestDTO;
    private ResumeDto.UpdateRequest updateRequestDTO;
    private ResumeDto.Response responseDTO;

    @BeforeEach
    public void setup() {
        requestDTO = new ResumeDto.CreateRequest();
        requestDTO.setTitle("Software Engineer");
        requestDTO.setSummary("Summary");
        requestDTO.setExperience("Experience");

        updateRequestDTO = new ResumeDto.UpdateRequest();
        updateRequestDTO.setTitle("Software Engineer Updated");

        responseDTO = ResumeDto.Response.builder()
                .id(1L)
                .userId(1L)
                .title("Software Engineer")
                .summary("Summary")
                .experience("Experience")
                .build();
    }

    @Test
    public void testCreateResume_Success() {
        when(resumeService.createResume(eq(1L), any(ResumeDto.CreateRequest.class)))
                .thenReturn(responseDTO);

        ResponseEntity<ApiResponse<ResumeDto.Response>> response =
                resumeController.createResume(1L, requestDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertEquals(1L, response.getBody().getData().getId());
    }

    @Test
    public void testUpdateResume_Success() {
        ResumeDto.Response updatedResponse = ResumeDto.Response.builder()
                .id(1L).title("Software Engineer Updated").build();

        when(resumeService.updateResume(eq(1L), eq(1L), any(ResumeDto.UpdateRequest.class)))
                .thenReturn(updatedResponse);

        ResponseEntity<ApiResponse<ResumeDto.Response>> response =
                resumeController.updateResume(1L, 1L, updateRequestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Software Engineer Updated", response.getBody().getData().getTitle());
    }

    @Test
    public void testGetUserResumes_Success() {
        PagedResponse<ResumeDto.Response> pagedResponse = new PagedResponse<>();
        pagedResponse.setContent(Arrays.asList(responseDTO));
        pagedResponse.setTotalElements(1L);

        when(resumeService.getUserResumes(eq(1L), any(Pageable.class)))
                .thenReturn(pagedResponse);

        ResponseEntity<ApiResponse<PagedResponse<ResumeDto.Response>>> response =
                resumeController.getUserResumes(1L, 0, 10, "createdAt", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().getContent().size());
        assertEquals(1L, response.getBody().getData().getContent().get(0).getId());
    }

    @Test
    public void testGetResumeById_Success() {
        when(resumeService.getResumeById(1L, 1L)).thenReturn(responseDTO);

        ResponseEntity<ApiResponse<ResumeDto.Response>> response =
                resumeController.getResumeById(1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Software Engineer", response.getBody().getData().getTitle());
    }

    @Test
    public void testDeleteResume_Success() {
        doNothing().when(resumeService).deleteResume(1L, 1L);

        ResponseEntity<ApiResponse<Void>> response =
                resumeController.deleteResume(1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}