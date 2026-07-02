package com.notification_service.controller;

import com.notification_service.dto.EmailRequestDTO;
import com.notification_service.port.EmailPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailPort emailPort;

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody @Valid EmailRequestDTO request) {
        // Because port logic is @Async, this returns a 202 ACCEPTED immediately
        emailPort.sendEmail(request);
        return ResponseEntity.accepted().body("Email has been queued for dispatch.");
    }
}
