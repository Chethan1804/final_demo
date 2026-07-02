package com.notification_service.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification_service.dto.EmailRequestDTO;
import com.notification_service.entity.FailedEmail;
import com.notification_service.repository.FailedEmailRepository;
import com.notification_service.template.EmailTemplateBuilder;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BrevoEmailAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailTemplateBuilder templateBuilder;

    @Mock
    private FailedEmailRepository failedEmailRepository;   // ← new dep

    @Mock
    private ObjectMapper objectMapper;                     // ← new dep

    private BrevoEmailAdapter brevoEmailAdapter;

    private EmailRequestDTO request;

    @BeforeEach
    void setUp() {
        // Updated constructor: 5 args now
        brevoEmailAdapter = new BrevoEmailAdapter(
                mailSender,
                templateBuilder,
                failedEmailRepository,
                objectMapper,
                "no-reply@test.com"
        );

        request = new EmailRequestDTO();
        request.setTo("user@example.com");
        request.setSubject("Test Subject");
        request.setTemplateName("test-template");
        request.setVariables(new HashMap<>());
    }

    @Test
    void testSendEmail_SuccessfulDispatch() {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateBuilder.build(anyString(), any())).thenReturn("<html>Test</html>");

        assertDoesNotThrow(() -> brevoEmailAdapter.sendEmail(request));

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void testSendEmail_ThrowsMessagingException() {
        MimeMessage mimeMessage = new MimeMessage((Session) null) {
            @Override
            public void setSubject(String subject) throws MessagingException {
                throw new MessagingException("Simulated exception");
            }
        };
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertThrows(RuntimeException.class, () -> brevoEmailAdapter.sendEmail(request));
    }

    @Test
    void testSendEmail_ThrowsGenericException() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP failed"));

        assertThrows(RuntimeException.class, () -> brevoEmailAdapter.sendEmail(request));
    }

    @Test
    void testRecoverMethod_PersistsFailedEmail() throws Exception {
        Exception exception = new RuntimeException("Simulated exhaustion of retries");

        // Mock objectMapper so it doesn't NPE on writeValueAsString
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"key\":\"value\"}");

        assertDoesNotThrow(() -> brevoEmailAdapter.recover(exception, request));

        // Verify failed email was persisted to DB
        verify(failedEmailRepository, times(1)).save(any(FailedEmail.class));
    }

    @Test
    void testRecoverMethod_HandlesJsonException() throws Exception {
        Exception exception = new RuntimeException("Retry exhausted");

        // objectMapper throws — recover must swallow it, not rethrow
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("JSON error"));

        assertDoesNotThrow(() -> brevoEmailAdapter.recover(exception, request));

        // DB save must NOT be called if JSON failed
        verify(failedEmailRepository, never()).save(any());
    }
}