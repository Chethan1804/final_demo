package com.resume_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeResponseDTO {

    private Long id;

    private String title;

    private String fullName;

    private String email;

    private String phone;

    private String summary;

    private String experience;

    private String lastUpdated;

    private String type;
}