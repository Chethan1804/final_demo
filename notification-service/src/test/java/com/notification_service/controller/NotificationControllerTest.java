package com.notification_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification_service.dto.EmailRequestDTO;
import com.notification_service.port.EmailPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailPort emailPort;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSendNotification_ValidRequest_ReturnsAccepted() throws Exception {
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", "Test User");

        EmailRequestDTO request = new EmailRequestDTO();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setTemplateName("welcome-template");
        request.setVariables(vars);

        doNothing().when(emailPort).sendEmail(any(EmailRequestDTO.class));

        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Email has been queued for dispatch."));

        verify(emailPort).sendEmail(any(EmailRequestDTO.class));
    }

    @Test
    void testSendNotification_InvalidRequest_ReturnsBadRequest() throws Exception {
        EmailRequestDTO request = new EmailRequestDTO(); // Missing required fields

        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
