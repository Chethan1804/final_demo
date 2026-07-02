package com.auth_service.service;

import com.auth_service.config.RabbitMQConfig;
import com.auth_service.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishLoginEvent(String email, String name) {
        try {
            NotificationEvent event = NotificationEvent.builder()
                    .to(email)
                    .subject("New Login Detected — Smart Resume Builder")
                    .templateName("login-alert")
                    .variables(java.util.Map.of("username", name))
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.LOGIN_ROUTING_KEY,
                    event
            );
            log.info("Login event published to RabbitMQ for: {}", email);
        } catch (Exception e) {
            log.warn("Failed to publish login event for {}: {}", email, e.getMessage());
        }
    }

    public void publishRegisterEvent(String email, String name) {
        try {
            NotificationEvent event = NotificationEvent.builder()
                    .to(email)
                    .subject("Welcome to Smart Resume Builder!")
                    .templateName("welcome-email")
                    .variables(java.util.Map.of("username", name))
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.REGISTER_ROUTING_KEY,
                    event
            );
            log.info("Register event published to RabbitMQ for: {}", email);
        } catch (Exception e) {
            log.warn("Failed to publish register event for {}: {}", email, e.getMessage());
        }
    }
}
