package com.correx.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PdfExtractionResult Unit Tests")
class PdfExtractionResultTest {

    @Test
    @DisplayName("Deve criar resultado com texto digital")
    void testDigitalTextResult() {
        // Arrange & Act
        String textoDigital = "Este é um texto digital extraído do PDF";
        PdfExtractionResult result = new PdfExtractionResult(
            textoDigital,
            5,
            5,
            false,
            PdfExtractionResult.ExtractionType.DIGITAL_TEXT
        );

        // Assert
        assertEquals(textoDigital, result.getTextAsString());
        assertEquals(5, result.totalPages());
        assertEquals(5, result.pagesProcessed());
        assertFalse(result.usedOcr());
        assertEquals(PdfExtractionResult.ExtractionType.DIGITAL_TEXT, result.extractionType());
    }

    @Test
    @DisplayName("Deve criar resultado com OCR de múltiplas páginas")
    void testScannedOcrResult() {
        // Arrange
        List<String> base64Images = Arrays.asList("base64img1", "base64img2", "base64img3");

        // Act
        PdfExtractionResult result = new PdfExtractionResult(
            base64Images,
            3,
            3,
            true,
            PdfExtractionResult.ExtractionType.SCANNED_OCR
        );

        // Assert
        List<String> retrievedImages = result.getImagesAsBase64List();
        assertEquals(base64Images, retrievedImages);
        assertEquals(3, retrievedImages.size());
        assertEquals(3, result.totalPages());
        assertEquals(3, result.pagesProcessed());
        assertTrue(result.usedOcr());
        assertEquals(PdfExtractionResult.ExtractionType.SCANNED_OCR, result.extractionType());
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar getTextAsString em resultado SCANNED_OCR")
    void testExceptionWhenGetTextAsStringOnScannedOcr() {
        // Arrange
        List<String> base64Images = Arrays.asList("img1", "img2");
        PdfExtractionResult result = new PdfExtractionResult(
            base64Images,
            2,
            2,
            true,
            PdfExtractionResult.ExtractionType.SCANNED_OCR
        );

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            result::getTextAsString
        );
        
        assertTrue(exception.getMessage().contains("não é String"));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar getImagesAsBase64List em resultado DIGITAL_TEXT")
    void testExceptionWhenGetImagesAsBase64ListOnDigitalText() {
        // Arrange
        String textoDigital = "Texto extraído";
        PdfExtractionResult result = new PdfExtractionResult(
            textoDigital,
            1,
            1,
            false,
            PdfExtractionResult.ExtractionType.DIGITAL_TEXT
        );

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            result::getImagesAsBase64List
        );
        
        assertTrue(exception.getMessage().contains("não é List"));
    }

    @Test
    @DisplayName("Deve criar resultado para formato não-PDF")
    void testOtherFormatResult() {
        // Arrange & Act
        String textoDocx = "Texto extraído de DOCX";
        PdfExtractionResult result = new PdfExtractionResult(
            textoDocx,
            1,
            1,
            false,
            PdfExtractionResult.ExtractionType.OTHER_FORMAT
        );

        // Assert
        assertEquals(textoDocx, result.getTextAsString());
        assertEquals(1, result.totalPages());
        assertFalse(result.usedOcr());
        assertEquals(PdfExtractionResult.ExtractionType.OTHER_FORMAT, result.extractionType());
    }

    @Test
    @DisplayName("Deve validar páginas não processadas")
    void testPartiallyProcessedPages() {
        // Arrange & Act
        List<String> base64Images = Arrays.asList("img1", "img2");
        PdfExtractionResult result = new PdfExtractionResult(
            base64Images,
            5,  // Total de 5 páginas no PDF
            2,  // Mas apenas 2 foram processadas (ex: 3 falharam)
            true,
            PdfExtractionResult.ExtractionType.SCANNED_OCR
        );

        // Assert
        assertEquals(5, result.totalPages());
        assertEquals(2, result.pagesProcessed());
        assertTrue(result.totalPages() >= result.pagesProcessed());
    }
}
