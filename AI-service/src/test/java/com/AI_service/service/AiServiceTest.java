package com.AI_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.AI_service.dto.AiRequestDTO;
import com.AI_service.dto.AiResponseDTO;
import com.AI_service.entity.AiLog;
import com.AI_service.repository.AiLogRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AiServiceTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private AiLogRepository aiLogRepository;

    @Mock
    private com.AI_service.client.NotificationClient notificationClient;

    @InjectMocks
    private AiService aiService;

    private AiRequestDTO requestDTO;

    @BeforeEach
    public void setup() {
        requestDTO = new AiRequestDTO();
        requestDTO.setUserId(1L);
        requestDTO.setPrompt("Make it look professional");
    }

    @Test
    public void testGenerateAiResponse_Success() {
        String fakeAiResult = "Your resume looks great! Consider adding metrics.";
        when(geminiService.improveResume(anyString())).thenReturn(fakeAiResult);
        when(aiLogRepository.save(any(AiLog.class))).thenReturn(new AiLog());

        AiResponseDTO response = aiService.generateAiResponse(requestDTO, "1");

        assertNotNull(response);
        assertEquals(fakeAiResult, response.getResponse());
        verify(geminiService, times(1)).improveResume(anyString());
        verify(aiLogRepository, times(1)).save(any(AiLog.class));
    }

    @Test
    public void testGenerateAiResponse_GeminiFailure() {
        when(geminiService.improveResume(anyString()))
                .thenThrow(new RuntimeException("Gemini API timeout"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                aiService.generateAiResponse(requestDTO, "1"));

        assertTrue(ex.getMessage().contains("AI service temporarily unavailable"));
    }

    @Test
    public void testGenerateAiResponse_NullUserId() {
        String fakeAiResult = "Professional resume advice here.";
        when(geminiService.improveResume(anyString())).thenReturn(fakeAiResult);
        when(aiLogRepository.save(any(AiLog.class))).thenReturn(new AiLog());

        // callerUserId null — should use requestDTO.getUserId() as fallback
        AiResponseDTO response = aiService.generateAiResponse(requestDTO, null);

        assertNotNull(response);
        assertNotNull(response.getResponse());
    }

    @Test
    public void testGenerateAiResponse_NotificationFailureDoesNotPropagate() {
        when(geminiService.improveResume(anyString())).thenReturn("Good resume!");
        when(aiLogRepository.save(any(AiLog.class))).thenReturn(new AiLog());
        doThrow(new RuntimeException("Email service down"))
                .when(notificationClient).sendNotification(any());

        // Notification failure must NOT throw — it's fire-and-forget
        AiResponseDTO response = aiService.generateAiResponse(requestDTO, "1");
        assertNotNull(response);
    }
}