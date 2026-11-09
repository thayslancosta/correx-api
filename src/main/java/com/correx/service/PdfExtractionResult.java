package com.correx.service;

/**
 * Resultado da extração de texto de um arquivo PDF.
 * Encapsula informações sobre como o texto foi extraído e se OCR foi utilizado.
 * 
 * Para DIGITAL_TEXT: extractedText contém o texto extraído
 * Para SCANNED_OCR: extractedText contém List<String> (base64 images) que precisam OCR
 */
public record PdfExtractionResult(
    Object extractedText,  // String para DIGITAL_TEXT, List<String> para SCANNED_OCR
    int totalPages,
    int pagesProcessed,
    boolean usedOcr,
    ExtractionType extractionType
) {
    /**
     * Tipo de extração realizada
     */
    public enum ExtractionType {
        /**
         * Texto digital extraído diretamente do PDF
         */
        DIGITAL_TEXT,
        /**
         * Texto extraído via OCR de PDF escaneado
         */
        SCANNED_OCR,
        /**
         * Texto extraído de arquivo não-PDF (DOC, DOCX, TXT)
         */
        OTHER_FORMAT
    }

    /**
     * Obtém o texto extraído como String (para DIGITAL_TEXT)
     */
    public String getTextAsString() {
        if (extractedText instanceof String) {
            return (String) extractedText;
        }
        throw new IllegalStateException("extractedText não é String. Use getImagesAsBase64List() para SCANNED_OCR");
    }

    /**
     * Obtém as imagens em base64 (para SCANNED_OCR)
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> getImagesAsBase64List() {
        if (extractedText instanceof java.util.List) {
            return (java.util.List<String>) extractedText;
        }
        throw new IllegalStateException("extractedText não é List. Use getTextAsString() para DIGITAL_TEXT");
    }
}
