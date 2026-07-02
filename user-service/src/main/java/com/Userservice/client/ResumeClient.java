package com.Userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "resume-service")
public interface ResumeClient {

    @GetMapping("/api/resumes")
    List<Object> getAllResumes();

    @DeleteMapping("/api/resumes/{id}")
    void deleteResume(@PathVariable("id") Long id);

    @GetMapping("/api/admin/resumes/user/{userId}")
    List<Object> getResumesByUserId(@PathVariable("userId") Long userId,
                                    @RequestHeader("X-User-Id") String adminId);
}
