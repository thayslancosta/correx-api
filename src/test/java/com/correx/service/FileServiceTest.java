package com.correx.service;

import com.correx.exception.BusinessException;
import com.correx.exception.ErrorMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("FileService Unit Tests")
class FileServiceTest {

    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileService();
    }

    @Test
    @DisplayName("Deve rejeitar arquivo vazio")
    void testEmptyFile() {
        // Arrange
        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> fileService.extrairTexto(emptyFile));
        
        assertEquals(ErrorMessages.EMPTY_FILE, exception.getMessage());
    }

    @Test
    @DisplayName("Deve rejeitar arquivo com tamanho superior a 10MB")
    void testFileTooLarge() {
        // Arrange
        MultipartFile largeFile = mock(MultipartFile.class);
        when(largeFile.isEmpty()).thenReturn(false);
        when(largeFile.getSize()).thenReturn(11 * 1024 * 1024L);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> fileService.extrairTexto(largeFile));
        
        assertEquals(ErrorMessages.FILE_TOO_LARGE, exception.getMessage());
    }

    @Test
    @DisplayName("Deve rejeitar arquivo sem extensão válida")
    void testFileWithoutValidExtension() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("arquivo_sem_extensao");

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> fileService.extrairTexto(file));
        
        assertTrue(exception.getMessage().contains("extensão"));
    }

    @Test
    @DisplayName("Deve rejeitar formato de arquivo não suportado")
    void testUnsupportedFileFormat() throws IOException {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("arquivo.exe");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> fileService.extrairTexto(file));
        
        assertEquals(ErrorMessages.INVALID_FILE_FORMAT, exception.getMessage());
    }

    @Test
    @DisplayName("Deve extrair texto de arquivo TXT com sucesso")
    void testExtractTextFromTxt() throws IOException {
        // Arrange
        String expectedText = "Este é um texto de teste";
        MultipartFile txtFile = mock(MultipartFile.class);
        when(txtFile.isEmpty()).thenReturn(false);
        when(txtFile.getSize()).thenReturn((long) expectedText.getBytes().length);
        when(txtFile.getOriginalFilename()).thenReturn("arquivo.txt");
        when(txtFile.getInputStream()).thenReturn(
            new ByteArrayInputStream(expectedText.getBytes())
        );

        // Act
        String resultado = fileService.extrairTexto(txtFile);

        // Assert
        assertEquals(expectedText, resultado);
    }

    @Test
    @DisplayName("Deve metadados do arquivo com sucesso")
    void testMontarMetadados() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("redacao.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(5000L);

        // Act
        FileService.FileMetadata metadata = fileService.montarMetadados(file);

        // Assert
        assertEquals("redacao.pdf", metadata.nomeOriginal());
        assertEquals("application/pdf", metadata.tipoArquivo());
        assertEquals(5000L, metadata.tamanhoBytes());
    }

    @Test
    @DisplayName("Deve retornar erro para PDF vazio")
    void testEmptyPdfFile() throws IOException {
        // Arrange
        byte[] emptyPdfBytes = new byte[0];
        MultipartFile pdfFile = mock(MultipartFile.class);
        when(pdfFile.isEmpty()).thenReturn(false);
        when(pdfFile.getSize()).thenReturn(100L);
        when(pdfFile.getOriginalFilename()).thenReturn("vazio.pdf");
        when(pdfFile.getBytes()).thenReturn(emptyPdfBytes);

        // Act & Assert
        assertThrows(Exception.class, () -> fileService.extrairTextoPdfCompleto(pdfFile));
    }

    @Test
    @DisplayName("Deve validar metadados do arquivo")
    void testFileMetadataRecord() {
        // Arrange & Act
        FileService.FileMetadata metadata = new FileService.FileMetadata(
            "teste.pdf",
            "application/pdf",
            2048L
        );

        // Assert
        assertNotNull(metadata);
        assertEquals("teste.pdf", metadata.nomeOriginal());
        assertEquals("application/pdf", metadata.tipoArquivo());
        assertEquals(2048L, metadata.tamanhoBytes());
    }
}
