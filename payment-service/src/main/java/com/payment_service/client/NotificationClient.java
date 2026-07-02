package com.payment_service.client;

import com.payment_service.dto.EmailRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/notifications/send")
    void sendEmail(
            @RequestHeader("Authorization") String authorization,
            @RequestBody EmailRequestDTO emailRequest
    );
}
