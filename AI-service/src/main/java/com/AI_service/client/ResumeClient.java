package com.AI_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "resume-service")
public interface ResumeClient {

    @GetMapping("/api/resumes/user/{userId}/latest")
    Object getLatestResumeByUserId(@PathVariable("userId") Long userId);
}
