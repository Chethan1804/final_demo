package com.AI_service.service;

import com.AI_service.client.NotificationClient;
import com.AI_service.dto.AiRequestDTO;
import com.AI_service.dto.AiResponseDTO;
import com.AI_service.entity.AiLog;
import com.AI_service.repository.AiLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final GeminiService geminiService;
    private final AiLogRepository aiLogRepository;
    private final NotificationClient notificationClient;

    /**
     * Generate AI-powered resume improvement suggestions via Gemini.
     * Called from POST /api/ai/generate with { userId, prompt }.
     */
    public AiResponseDTO generateAiResponse(AiRequestDTO requestDTO, String callerUserId) {

        log.info("AI generate request: userId={}, prompt length={}", 
                 requestDTO.getUserId(), requestDTO.getPrompt().length());

        // Build a rich prompt for resume assistance
        String fullPrompt = buildResumePrompt(requestDTO.getPrompt());

        String aiResult;
        try {
            aiResult = geminiService.improveResume(fullPrompt);
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw new RuntimeException("AI service temporarily unavailable: " + e.getMessage());
        }

        // Persist audit log
        String userId = callerUserId != null ? callerUserId : String.valueOf(requestDTO.getUserId());
        aiLogRepository.save(AiLog.builder()
                .userId(userId)
                .actionType("GENERATE")
                .promptOrFilename(requestDTO.getPrompt())
                .resultSummary(aiResult != null && aiResult.length() > 500
                        ? aiResult.substring(0, 500) : aiResult)
                .build());

        // Fire-and-forget notification (non-blocking)
        sendInsightsReadyNotification(userId, requestDTO.getPrompt());

        return new AiResponseDTO(aiResult);
    }

    private String buildResumePrompt(String userPrompt) {
        return """
                You are an expert resume coach and professional writer.
                
                User request: %s
                
                Provide a clear, actionable, and professional response.
                Focus specifically on resume improvement, career advice, or the specific request above.
                Be concise but thorough. Format your response with clear sections where appropriate.
                """.formatted(userPrompt);
    }

    private void sendInsightsReadyNotification(String userId, String promptPreview) {
        try {
            java.util.Map<String, Object> emailRequest = java.util.Map.of(
                    "to", "user@example.com",  // notification-service resolves actual email
                    "subject", "Your AI Resume Insights Are Ready!",
                    "templateName", "ai-insights-ready",
                    "variables", java.util.Map.of(
                            "username", "User " + userId,
                            "suggestionPreview", promptPreview.length() > 60
                                    ? promptPreview.substring(0, 60) + "..." : promptPreview
                    )
            );
            notificationClient.sendNotification(emailRequest);
        } catch (Exception ex) {
            // Non-blocking — notification failure must never fail the AI response
            log.warn("AI insights notification failed (non-critical): {}", ex.getMessage());
        }
    }
}