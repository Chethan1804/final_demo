package com.resume_service.service;

import com.resume_service.client.NotificationClient;
import com.resume_service.client.UserClient;
import com.resume_service.dto.ApiResponse;
import com.resume_service.dto.UserRequestResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
@Service
public class AsyncNotificationTaskService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncNotificationTaskService.class);
    private final NotificationClient notificationClient;
    private final UserClient userClient;

    public AsyncNotificationTaskService(NotificationClient notificationClient, UserClient userClient) {
        this.notificationClient = notificationClient;
        this.userClient = userClient;
    }

    @Async
    public void sendUpdateNotificationAsync(Long userId, int version) {
        try {
            ApiResponse<UserRequestResponseDTO> response = userClient.getUserById(userId);
            UserRequestResponseDTO user = response.getData();
            if (user == null) {
                logger.warn("Could not fetch user id={} for notification", userId);
                return;
            }
            Map<String, Object> emailRequest = Map.of(
                    "to", user.getEmail(),
                    "subject", "Your Resume was Updated!",
                    "templateName", "resume-updated",
                    "variables", Map.of("username", user.getName(), "version", version)
            );

            logger.info("Sending async notification to {}", emailRequest.get("to"));
            notificationClient.sendNotification(emailRequest);
            logger.info("Async notification sent successfully");
        } catch (Exception ex) {
            logger.warn("Failed to send async notification: {}", ex.getMessage());
        }
    }
}
