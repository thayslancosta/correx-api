package com.correx.service;

import com.correx.exception.BusinessException;
import com.correx.exception.ErrorMessages;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MIN_USEFUL_CHARS = 100;
    private static final int PDF_RENDER_DPI = 150;
    private static final String PNG_FORMAT = "PNG";

    public PdfExtractionResult extrairTextoPdfCompleto(MultipartFile arquivo) {
        validarArquivo(arquivo);

        String filename = Objects.requireNonNull(arquivo.getOriginalFilename());
        logger.info("Iniciando extração de PDF. Arquivo: {}, Tamanho: {} bytes", filename, arquivo.getSize());

        byte[] pdfBytes;
        try {
            pdfBytes = arquivo.getBytes();
        } catch (IOException ex) {
            logger.error("Erro ao ler bytes do arquivo PDF: {}", filename, ex);
            throw new BusinessException(ErrorMessages.PDF_TEXT_EXTRACTION_FAILED, ex);
        }

        try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
            int totalPages = doc.getNumberOfPages();
            logger.info("PDF carregado com sucesso. Páginas: {}", totalPages);

            // Tentar extrair texto digital primeiro
            String textoDigital = extrairTextoDigitalDoPdf(doc);

            if (isTextoDigitalUtil(textoDigital)) {
                logger.info("Texto digital extraído com sucesso. Páginas: {}, Caracteres: {}", totalPages, textoDigital.length());
                return new PdfExtractionResult(
                    textoDigital,
                    totalPages,
                    totalPages,
                    false,
                    PdfExtractionResult.ExtractionType.DIGITAL_TEXT
                );
            }

            // PDF escaneado: converter todas as páginas em imagens e fazer OCR
            logger.info("PDF parece ser escaneado (texto digital insuficiente). Preparando para OCR de {} página(s)...", totalPages);
            return extrairTextoViaOcr(doc, totalPages);

        } catch (BusinessException ex) {
            throw ex;
        } catch (IOException ex) {
            logger.error("Erro ao processar arquivo PDF: {}", filename, ex);
            throw new BusinessException(ErrorMessages.PDF_CORRUPT, ex);
        } catch (Exception ex) {
            logger.error("Erro inesperado ao processar PDF: {}", filename, ex);
            throw new BusinessException(ErrorMessages.PDF_TEXT_EXTRACTION_FAILED, ex);
        }
    }

    /**
     * Extrai texto digital de todas as páginas do PDF
     */
    private String extrairTextoDigitalDoPdf(PDDocument doc) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        return stripper.getText(doc);
    }

    /**
     * Verifica se o texto extraído é útil e contém conteúdo real
     * Utiliza heurística mais robusta que apenas contar caracteres
     */
    private boolean isTextoDigitalUtil(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return false;
        }

        String textoLimpo = texto.trim();

        // Verificar se tem pelo menos quantidade mínima de caracteres não-whitespace
        int charactersCount = textoLimpo.replaceAll("\\s+", "").length();
        if (charactersCount < MIN_USEFUL_CHARS) {
            logger.debug("Texto digital insuficiente: {} caracteres (mínimo: {})", charactersCount, MIN_USEFUL_CHARS);
            return false;
        }

        // Verificar presença de palavras com tamanho significativo
        String[] palavras = textoLimpo.split("\\s+");
        long palavrasValidas = java.util.Arrays.stream(palavras)
            .filter(p -> p.length() > 2)  // Palavras com mais de 2 caracteres
            .count();

        if (palavrasValidas < 10) {
            logger.debug("Insuficiente número de palavras válidas: {} (mínimo: 10)", palavrasValidas);
            return false;
        }

        logger.debug("Texto digital considerado útil: {} caracteres, {} palavras", charactersCount, palavrasValidas);
        return true;
    }

    /**
     * Extrai texto de PDF escaneado convertendo cada página em imagem e enviando para OCR
     */
    private PdfExtractionResult extrairTextoViaOcr(PDDocument doc, int totalPages) {
        List<String> textosPorPagina = new ArrayList<>();
        StringBuilder textoCompleto = new StringBuilder();

        logger.info("Iniciando conversão de {} página(s) para imagem e OCR", totalPages);

        try {
            PDFRenderer renderer = new PDFRenderer(doc);

            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                try {
                    logger.debug("Processando página {} de {}", pageIndex + 1, totalPages);

                    // Renderizar página em imagem
                    BufferedImage image = renderer.renderImageWithDPI(pageIndex, PDF_RENDER_DPI);
                    String base64Image = converterImagemParaBase64(image);

                    logger.debug("Página {} convertida para base64. Tamanho: {} bytes", pageIndex + 1, base64Image.length());

                    textosPorPagina.add(base64Image);

                } catch (IOException ex) {
                    logger.error("Erro ao renderizar página {}: {}", pageIndex + 1, ex.getMessage());
                    // Continuar com próximas páginas ao invés de falhar completamente
                }
            }

            if (textosPorPagina.isEmpty()) {
                throw new BusinessException(ErrorMessages.PDF_CORRUPT);
            }

            logger.info("Conversão de imagens completa. {} página(s) prontas para OCR.", textosPorPagina.size());

            // Retornar para que RedacaoService faça o OCR com OpenAiService
            return new PdfExtractionResult(
                textosPorPagina,  // Será usado como lista de base64 para OCR
                totalPages,
                textosPorPagina.size(),
                true,
                PdfExtractionResult.ExtractionType.SCANNED_OCR
            );

        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Erro ao processar OCR do PDF: {}", ex.getMessage(), ex);
            throw new BusinessException(ErrorMessages.PDF_OCR_FAILED, ex);
        }
    }

    /**
     * Converte uma imagem BufferedImage para string Base64
     */
    private String converterImagemParaBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (!ImageIO.write(image, PNG_FORMAT, baos)) {
            throw new IOException("Falha ao escrever imagem PNG");
        }
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    public String extrairTexto(MultipartFile arquivo) {
        validarArquivo(arquivo);

        String filename = Objects.requireNonNull(arquivo.getOriginalFilename()).toLowerCase();
        String extensao = obterExtensao(filename);

        logger.info("Iniciando extração de texto. Arquivo: {}, Tamanho: {} bytes, Extensão: {}", 
            filename, arquivo.getSize(), extensao);

        String texto;
        try {
            InputStream is = arquivo.getInputStream();
            
            if (extensao.equals("txt")) {
                texto = extrairTextoTxt(is);
            } else if (extensao.equals("pdf")) {
                // Para compatibilidade, extrair apenas texto digital de PDF normais
                texto = extrairTextoPdfDigital(arquivo);
            } else if (extensao.equals("docx")) {
                texto = extrairTextoDocx(is);
            } else if (extensao.equals("doc")) {
                texto = extrairTextoDoc(is);
            } else {
                throw new BusinessException(ErrorMessages.INVALID_FILE_FORMAT);
            }
        } catch (IOException ex) {
            logger.error("Erro ao processar arquivo: {}", filename, ex);
            throw new BusinessException(ErrorMessages.PDF_TEXT_EXTRACTION_FAILED, ex);
        }

        if (texto == null || texto.trim().isEmpty()) {
            throw new BusinessException(ErrorMessages.EMPTY_FILE);
        }

        logger.info("Texto extraído com sucesso. Tamanho: {} caracteres", texto.length());
        return texto;
    }

    /**
     * Extrai apenas texto digital de um PDF (para compatibilidade com método antigo)
     */
    private String extrairTextoPdfDigital(MultipartFile arquivo) throws IOException {
        byte[] pdfBytes = arquivo.getBytes();
        try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    public FileMetadata montarMetadados(MultipartFile arquivo) {
        return new FileMetadata(
            arquivo.getOriginalFilename(),
            arquivo.getContentType(),
            arquivo.getSize()
        );
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            throw new BusinessException(ErrorMessages.EMPTY_FILE);
        }

        if (arquivo.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorMessages.FILE_TOO_LARGE);
        }
    }

    private String extrairTextoTxt(InputStream is) throws IOException {
        byte[] bytes = is.readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String extrairTextoDocx(InputStream is) throws IOException {
        try (XWPFDocument document = new XWPFDocument(is)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                sb.append(paragraph.getText()).append("\n");
            }
            return sb.toString();
        }
    }

    private String extrairTextoDoc(InputStream is) throws IOException {
        throw new BusinessException("Formato .doc legado não suportado. Converta para .docx.");
    }

    private String obterExtensao(String filename) {
        int lastIndex = filename.lastIndexOf(".");
        if (lastIndex > 0 && lastIndex < filename.length() - 1) {
            return filename.substring(lastIndex + 1);
        }
        throw new BusinessException("Arquivo sem extensão válida");
    }

    public record FileMetadata(
        String nomeOriginal,
        String tipoArquivo,
        long tamanhoBytes
    ) {}
}
