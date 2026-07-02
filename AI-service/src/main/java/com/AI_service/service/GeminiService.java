package com.AI_service.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Service
public class GeminiService {
    private final WebClient webClient = WebClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String API_KEY;

    private final String URL =
    		"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    public String improveResume(String content) {
        String prompt = """
                Act as a professional resume writer.
                Improve and format this resume professionally.
                Keep it clean and ready for PDF.
                but don't give extra info just give the resume
                Resume:
                %s
                """.formatted(content);
        return callGemini(prompt);
    }

    public String analyzeResume(String content) {
        String prompt = """
                Analyze this resume and return strictly a raw JSON string matching exactly this schema, without any markdown blocks or formatting:
                {
                  "score": 85,
                  "strengths": ["string1", "string2"],
                  "weaknesses": ["string1", "string2"],
                  "suggestions": "A detailed paragraph of suggestions."
                }
                Do not wrap the output in ```json or ``` tags. Just output the raw JSON object.
                Resume content to analyze:
                %s
                """.formatted(content);
        String rawOutput = callGemini(prompt);
        if (rawOutput != null) {
            // Strip markdown fences, leading/trailing whitespace
            rawOutput = rawOutput
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();
            // Find first '{' and last '}' — extract only the JSON object
            int start = rawOutput.indexOf('{');
            int end   = rawOutput.lastIndexOf('}');
            if (start != -1 && end != -1 && end > start) {
                rawOutput = rawOutput.substring(start, end + 1);
            }
        }
        return rawOutput;
    }

    private String callGemini(String prompt) {
        try {
            String safePrompt = prompt.replace("\\", "\\\\")
                                      .replace("\"", "\\\"")
                                      .replace("\n", "\\n")
                                      .replace("\r", "\\r")
                                      .replace("\t", "\\t");
            String request = """
            {
              "contents":[{"parts":[{"text":"%s"}]}]
            }
            """.formatted(safePrompt);

            String response = webClient.post()
                    .uri(URL + API_KEY)
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();

            JsonNode root = mapper.readTree(response);
            return root
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            e.printStackTrace();  // TEMP - shows real error in console
            throw new RuntimeException("Gemini API error or timeout: " + e.getMessage(), e);
        }
    }
}