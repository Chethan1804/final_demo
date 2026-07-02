package com.notification_service.adapter;

import com.notification_service.dto.EmailRequestDTO;
import com.notification_service.template.EmailTemplateBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;

import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
    "notification.default-sender=test@example.com",
    "notification.retry.max-attempts=3",
    "notification.retry.backoff-delay=100"
})
public class BrevoEmailAdapterRetryTest {

    @Autowired
    private BrevoEmailAdapter brevoEmailAdapter;

    @MockBean
    private JavaMailSender mailSender;

    @MockBean
    private EmailTemplateBuilder templateBuilder;

    @Test
    void testSendEmail_RetriesOnFailure_ThenRecovers() {
        EmailRequestDTO request = new EmailRequestDTO();
        request.setTo("retry@example.com");
        request.setSubject("Retry Test");
        request.setTemplateName("test");
        request.setVariables(new HashMap<>());

        // Simulate a network failure every time
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Simulated SMTP failure"));

        // Call the method; since @Retryable is proxying it, it will catch the exception, retry 3 times, and then call @Recover
        brevoEmailAdapter.sendEmail(request);

        // Verify that createMimeMessage was called exactly 3 times (due to maxAttempts=3)
        verify(mailSender, times(3)).createMimeMessage();
        
        // At this point, the @Recover method would have been executed, suppressing the exception from bubbling up.
    }
}
