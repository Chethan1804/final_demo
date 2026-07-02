package com.AI_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class GeminiServiceTest {

    @InjectMocks
    private GeminiService geminiService;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(geminiService, "API_KEY", "dummy_key");
        ReflectionTestUtils.setField(geminiService, "webClient", webClient);
    }

    @Test
    public void testImproveResume_Success() {
        String fakeJsonResponse = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": "Improved Resume Text"
                      }
                    ]
                  }
                }
              ]
            }
            """;

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(fakeJsonResponse));

        String result = geminiService.improveResume("Raw Resume");

        assertEquals("Improved Resume Text", result);
    }

    @Test
    public void testAnalyzeResume_Success() {
        String fakeJsonResponse = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": "{\\"strengths\\":\\"Good\\", \\"weaknesses\\":\\"None\\", \\"improvements\\":\\"None\\"}"
                      }
                    ]
                  }
                }
              ]
            }
            """;

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(fakeJsonResponse));

        String result = geminiService.analyzeResume("Raw Resume");

        assertEquals("{\"strengths\":\"Good\", \"weaknesses\":\"None\", \"improvements\":\"None\"}", result);
    }

    @Test
    public void testCallGemini_Exception() {
        when(webClient.post()).thenThrow(new RuntimeException("Connection failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            geminiService.improveResume("Raw Resume");
        });

        assertEquals("Gemini API error", exception.getMessage());
    }
}
