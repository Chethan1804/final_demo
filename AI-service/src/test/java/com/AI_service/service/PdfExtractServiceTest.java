package com.AI_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class PdfExtractServiceTest {

    @InjectMocks
    private PdfExtractService pdfExtractService;

    @Mock
    private MultipartFile multipartFile;

    @BeforeEach
    public void setup() {
    }

    @Test
    public void testExtractText_EmptyFile() {
        when(multipartFile.isEmpty()).thenReturn(true);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pdfExtractService.extractText(multipartFile);
        });
        assertEquals("File is empty", exception.getMessage());
    }

    @Test
    public void testExtractText_NullFile() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pdfExtractService.extractText(null);
        });
        assertEquals("File is empty", exception.getMessage());
    }

    @Test
    public void testExtractText_IOException() throws IOException {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getInputStream()).thenThrow(new IOException("Stream failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pdfExtractService.extractText(multipartFile);
        });

        assertEquals("Error reading PDF", exception.getMessage());
    }
    
}
