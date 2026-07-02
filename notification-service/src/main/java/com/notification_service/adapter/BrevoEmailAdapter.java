package com.notification_service.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification_service.dto.EmailRequestDTO;
import com.notification_service.entity.FailedEmail;
import com.notification_service.port.EmailPort;
import com.notification_service.repository.FailedEmailRepository;
import com.notification_service.template.EmailTemplateBuilder;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BrevoEmailAdapter implements EmailPort {

    private final JavaMailSender mailSender;
    private final EmailTemplateBuilder templateBuilder;
    private final FailedEmailRepository failedEmailRepository;
    private final ObjectMapper objectMapper;
    private final String defaultSender;

    public BrevoEmailAdapter(
            JavaMailSender mailSender,
            EmailTemplateBuilder templateBuilder,
            FailedEmailRepository failedEmailRepository,
            ObjectMapper objectMapper,
            @Value("${notification.default-sender}") String defaultSender) {
        this.mailSender = mailSender;
        this.templateBuilder = templateBuilder;
        this.failedEmailRepository = failedEmailRepository;
        this.objectMapper = objectMapper;
        this.defaultSender = defaultSender;
    }

    @Override
    @Async
    @Retryable(
            value = {Exception.class},
            maxAttemptsExpression = "${notification.retry.max-attempts}",
            backoff = @Backoff(delayExpression = "${notification.retry.backoff-delay}")
    )
    public void sendEmail(EmailRequestDTO request) {
        log.info("Preparing async email to {}", request.getTo());
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(defaultSender);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(
                templateBuilder.build(request.getTemplateName(), request.getVariables()),
                true
            );
            mailSender.send(message);
            log.info("Email dispatched to {}", request.getTo());

        } catch (MessagingException e) {
            log.error("MIME build failed for {}: {}", request.getTo(), e.getMessage());
            throw new RuntimeException("Email dispatch failed", e);
        } catch (Exception e) {
            log.error("SMTP error for {}: {}", request.getTo(), e.getMessage());
            throw new RuntimeException("SMTP interaction failed", e);
        }
    }

    @Recover
    public void recover(Exception e, EmailRequestDTO request) {
        log.error("All retries exhausted for {}. Persisting to failed_emails.", request.getTo());
        try {
            String variablesJson = objectMapper.writeValueAsString(request.getVariables());
            failedEmailRepository.save(FailedEmail.builder()
                    .toEmail(request.getTo())
                    .subject(request.getSubject())
                    .templateName(request.getTemplateName())
                    .variables(variablesJson)
                    .errorMessage(e.getMessage())
                    .build());
        } catch (Exception jsonEx) {
            log.error("Failed to persist failed email record: {}", jsonEx.getMessage());
        }
    }
}