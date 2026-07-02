package com.AI_service.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PdfServiceTest {

    private PdfService pdfService = new PdfService();

    @Test
    public void testGeneratePdf_Success() {
        byte[] pdfBytes = pdfService.generatePdf("This is a mock resume content.");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
}
