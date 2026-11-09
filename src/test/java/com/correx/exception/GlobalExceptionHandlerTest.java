package com.correx.exception;

import com.correx.dto.ApiErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Test
    @DisplayName("Deve tratar ResourceNotFoundException corretamente")
    void testHandleResourceNotFoundException() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Recurso não encontrado");

        // Act
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleResourceNotFound(exception);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().code());
    }

    @Test
    @DisplayName("Deve tratar BusinessException corretamente")
    void testHandleBusinessException() {
        // Arrange
        BusinessException exception = new BusinessException("Erro de negócio");

        // Act
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleBusinessException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("BUSINESS_RULE_VIOLATION", response.getBody().code());
    }

    @Test
    @DisplayName("Deve tratar UnauthorizedException corretamente")
    void testHandleUnauthorizedException() {
        // Arrange
        UnauthorizedException exception = new UnauthorizedException("Acesso não autorizado");

        // Act
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleUnauthorized(exception);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().status());
        assertEquals("UNAUTHORIZED", response.getBody().code());
    }

    @Test
    @DisplayName("Deve tratar ExternalServiceException de OCR corretamente")
    void testHandleExternalServiceExceptionForOcr() {
        // Arrange
        ExternalServiceException exception = new ExternalServiceException(
            ErrorMessages.OPENAI_OCR_ERROR
        );

        // Act
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleExternalServiceException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(502, response.getBody().status());
        assertEquals("EXTERNAL_SERVICE_ERROR", response.getBody().code());
        // Deve conter mensagem segura
        assertNotNull(response.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar ExternalServiceException de timeout de OCR")
    void testHandleExternalServiceExceptionForOcrTimeout() {
        // Arrange
        ExternalServiceException exception = new ExternalServiceException(
            "OCR timeout - aguarde"
        );

        // Act
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleExternalServiceException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        // Deve retornar mensagem segura para cliente
        assertTrue(response.getBody().message().contains("Processamento"));
    }

    @Test
    @DisplayName("Deve tratar ExternalServiceException de HTTP 429")
    void testHandleExternalServiceExceptionFor429() {
        // Arrange
        ExternalServiceException exception = new ExternalServiceException(
            "HTTP 429 - Rate limit exceeded"
        );

        // Act
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleExternalServiceException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("Limite"));
    }

    @Test
    @DisplayName("Deve tratar ExternalServiceException de PDF")
    void testHandleExternalServiceExceptionForPdf() {
        // Arrange
        ExternalServiceException exception = new ExternalServiceException(
            "PDF extraction failed"
        );

        // Act
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleExternalServiceException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("OCR"));
    }

    @Test
    @DisplayName("Deve retornar mensagem segura padrão para erro desconhecido")
    void testHandleExternalServiceExceptionGeneric() {
        // Arrange
        ExternalServiceException exception = new ExternalServiceException(
            "Erro genérico desconhecido"
        );

        // Act
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleExternalServiceException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("serviço externo"));
    }

    @Test
    @DisplayName("Deve tratar Exception genérica como erro interno")
    void testHandleGenericException() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/redacoes/upload");
        
        Exception exception = new Exception("Erro inesperado");

        // Act
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleGeneric(exception, request);

        // Assert
        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("INTERNAL_ERROR", response.getBody().code());
    }

    @Test
    @DisplayName("Deve processar MethodArgumentNotValidException")
    void testHandleValidationException() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(mock(org.springframework.validation.BindingResult.class));
        when(exception.getBindingResult().getFieldErrors()).thenReturn(java.util.List.of());

        // Act
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleValidationException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("VALIDATION_ERROR", response.getBody().code());
    }
}
