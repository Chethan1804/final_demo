package com.AI_service.controller;

import com.AI_service.dto.AiRequestDTO;
import com.AI_service.dto.AiResponseDTO;
import com.AI_service.dto.ApiResponse;
import com.AI_service.entity.AiLog;
import com.AI_service.repository.AiLogRepository;
import com.AI_service.service.AiService;
import com.AI_service.service.GeminiService;
import com.AI_service.service.PdfExtractService;
import com.AI_service.service.PdfService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "AI Resume Analysis", description = "Gemini-powered resume improvement, analysis, and PDF generation")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private final GeminiService geminiService;
    private final PdfExtractService pdfExtractService;
    private final PdfService pdfService;
    private final AiService aiService;
    private final AiLogRepository aiLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * POST /api/ai/generate
     * Send a prompt about your resume → get AI improvement suggestions.
     * Requires PREMIUM role (enforced at gateway).
     */
    @PostMapping("/generate")
    @Operation(
        summary = "Generate AI resume suggestions",
        description = "Send a prompt (e.g. 'Improve my skills section') and get AI-powered advice. " +
                      "Requires PREMIUM_USER or ADMIN role."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "AI response generated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "Gemini API error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Premium required")
    })
    public ResponseEntity<ApiResponse<AiResponseDTO>> generate(
            @Valid @RequestBody AiRequestDTO requestDTO,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        log.info("AI generate: userId={}, role={}", userId, userRole);

        try {
            AiResponseDTO response = aiService.generateAiResponse(requestDTO, userId);
            return ResponseEntity.ok(ApiResponse.success("AI response generated", response));
        } catch (RuntimeException e) {
            log.error("AI generate failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(ApiResponse.error("AI service error", e.getMessage()));
        }
    }

    /**
     * POST /api/ai/extract
     * Upload a PDF resume → get JSON analysis (score, strengths, weaknesses, suggestions).
     * Requires PREMIUM role (enforced at gateway).
     */
    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Analyze PDF resume",
        description = "Upload a PDF resume and receive structured AI analysis including " +
                      "score, strengths, weaknesses, and improvement suggestions."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Analysis complete"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Cannot read PDF"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "Gemini API error")
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> extractInsights(
            @RequestParam("file") @NotNull(message = "PDF file is required") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        log.info("AI extract: file={}, userId={}", file.getOriginalFilename(), userId);

        // Extract text from PDF
        String extractedText;
        try {
            extractedText = pdfExtractService.extractText(file);
        } catch (Exception ex) {
            log.error("PDF extraction failed for {}: {}", file.getOriginalFilename(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ApiResponse.error("Failed to extract text from PDF", ex.getMessage()));
        }

        // Call Gemini for analysis
        String aiJsonString;
        try {
            aiJsonString = geminiService.analyzeResume(extractedText);
        } catch (Exception ex) {
            log.error("Gemini analysis failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(ApiResponse.error("Gemini API call failed", ex.getMessage()));
        }

        // Parse JSON response
        Map<String, Object> insights;
        try {
            insights = objectMapper.readValue(aiJsonString, new TypeReference<>() {});
        } catch (Exception parseEx) {
            log.error("Failed to parse Gemini JSON output: {}", aiJsonString);
            // Return raw output as-is if JSON parse fails
            return ResponseEntity.ok(ApiResponse.success("Insights generated (raw)",
                    Map.of("raw", aiJsonString)));
        }

        // Audit log
        aiLogRepository.save(AiLog.builder()
                .userId(userId)
                .actionType("EXTRACT")
                .promptOrFilename(file.getOriginalFilename())
                .resultSummary("score=" + insights.getOrDefault("score", "N/A"))
                .build());

        return ResponseEntity.ok(ApiResponse.success("Insights generated successfully", insights));
    }

    /**
     * POST /api/ai/generate-pdf
     * Upload a PDF resume → get an improved PDF back.
     * Requires PREMIUM role (enforced at gateway).
     */
    @PostMapping(value = "/generate-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Improve and regenerate resume as PDF",
        description = "Upload your existing PDF resume. AI will rewrite and improve it, " +
                      "then return a new professional PDF."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Improved PDF returned"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Cannot read PDF"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "Gemini or PDF generation error")
    })
    public ResponseEntity<byte[]> generatePdf(
            @RequestParam("file") @NotNull(message = "PDF file is required") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        log.info("AI generate-pdf: file={}, userId={}", file.getOriginalFilename(), userId);

        String extractedText = pdfExtractService.extractText(file);
        String improvedText = geminiService.improveResume(extractedText);
        byte[] pdfBytes = pdfService.generatePdf(improvedText);

        // Audit log
        aiLogRepository.save(AiLog.builder()
                .userId(userId)
                .actionType("GENERATE_PDF")
                .promptOrFilename(file.getOriginalFilename())
                .resultSummary("Generated improved PDF, " + pdfBytes.length + " bytes")
                .build());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=improved_resume.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}