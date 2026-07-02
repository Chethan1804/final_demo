package com.resume_service.dto;


import com.resume_service.entity.Resume;
import lombok.*;

import java.time.LocalDateTime;

public class ResumeDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long userId;
        private String title;
        private String summary;
        private String skills;
        private String experience;
        private String education;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(Resume resume) {
            return Response.builder()
                    .id(resume.getId())
                    .userId(resume.getUserId())
                    .title(resume.getTitle())
                    .summary(resume.getSummary())
                    .skills(resume.getSkills())
                    .experience(resume.getExperience())
                    .education(resume.getEducation())
                    .createdAt(resume.getCreatedAt())
                    .updatedAt(resume.getUpdatedAt())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String title;
        private String summary;
        private String skills;
        private String experience;
        private String education;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String title;
        private String summary;
        private String skills;
        private String experience;
        private String education;
    }
}