package com.notification_service.consumer;

import com.notification_service.config.RabbitMQConfig;
import com.notification_service.dto.EmailRequestDTO;
import com.notification_service.dto.NotificationEvent;
import com.notification_service.port.EmailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final EmailPort emailPort;

    @RabbitListener(queues = RabbitMQConfig.LOGIN_NOTIFICATION_QUEUE)
    public void consumeLoginEvent(NotificationEvent event) {
        log.info("Received login notification event for: {}", event.getTo());
        dispatchEmail(event);
    }

    @RabbitListener(queues = RabbitMQConfig.REGISTER_NOTIFICATION_QUEUE)
    public void consumeRegisterEvent(NotificationEvent event) {
        log.info("Received register notification event for: {}", event.getTo());
        dispatchEmail(event);
    }

    private void dispatchEmail(NotificationEvent event) {
        try {
            EmailRequestDTO request = EmailRequestDTO.builder()
                    .to(event.getTo())
                    .subject(event.getSubject())
                    .templateName(event.getTemplateName())
                    .variables(event.getVariables())
                    .build();
            emailPort.sendEmail(request);
        } catch (Exception e) {
            log.error("Failed to process notification event for {}: {}", event.getTo(), e.getMessage());
        }
    }
}
