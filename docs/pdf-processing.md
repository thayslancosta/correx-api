# PDF Processing and OCR

The CorreX API accepts essay text and document uploads. Its PDF processing flow distinguishes between documents containing usable digital text and scanned PDFs that require OCR.

## Processing flow

1. The upload reaches the essay controller and is validated.
2. For PDFs, `FileService` uses Apache PDFBox to extract text across pages.
3. The service checks whether that text contains enough usable content.
4. If the PDF appears to be scanned, its pages are rendered as images at 150 DPI.
5. `RedacaoService` sends the resulting page images to the OpenAI OCR integration.
6. Extracted text can then enter the essay assessment workflow; the teacher retains final review.

For DOC, DOCX and TXT uploads, the file service uses text extraction rather than this PDF OCR path.

## Key implementation files

- `src/main/java/com/correx/service/FileService.java` — PDF inspection, extraction and rendering.
- `src/main/java/com/correx/service/PdfExtractionResult.java` — extraction result types.
- `src/main/java/com/correx/service/RedacaoService.java` — essay workflow and OCR orchestration.
- `src/main/java/com/correx/service/openai/OpenAiService.java` — external AI integration.

## Limitations

- OCR requires a configured external API key and may incur usage costs.
- Accuracy depends on the scan quality, document content and the external model.
- Production deployment, end-to-end performance and full automated-test reliability have not been established for this portfolio version.
- Teachers should verify extracted content and assessment output before relying on results.

This document describes the implementation's architecture, not a benchmark or guarantee of OCR accuracy.
