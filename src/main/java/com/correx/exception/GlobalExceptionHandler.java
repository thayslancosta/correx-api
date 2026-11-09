package com.correx.exception;

import com.correx.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        logger.warn("Recurso não encontrado: {}", ex.getMessage());
        var response = new ApiErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "RESOURCE_NOT_FOUND",
            ex.getMessage(),
            LocalDateTime.now(),
            List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex) {
        logger.warn("Erro de negócio: {}", ex.getMessage(), ex);
        var response = new ApiErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "BUSINESS_RULE_VIOLATION",
            ex.getMessage(),
            LocalDateTime.now(),
            List.of()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        logger.warn("Acesso não autorizado: {}", ex.getMessage());
        var response = new ApiErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "UNAUTHORIZED",
            ex.getMessage(),
            LocalDateTime.now(),
            List.of()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleExternalServiceException(ExternalServiceException ex) {
        logger.error("Erro ao chamar serviço externo: {}", ex.getMessage(), ex);
        
        // Determinar mensagem segura baseada no tipo de erro
        String mensagemCliente = determinarMensagemSegura(ex.getMessage());
        
        var response = new ApiErrorResponse(
            HttpStatus.BAD_GATEWAY.value(),
            "EXTERNAL_SERVICE_ERROR",
            mensagemCliente,
            LocalDateTime.now(),
            List.of()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        });

        logger.warn("Erro de validação: {} campos com erro", errors.size());

        var response = new ApiErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_ERROR",
            "Erro na validação dos dados",
            LocalDateTime.now(),
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        logger.error("Erro interno do servidor: {} - Path: {}", ex.getMessage(), request.getRequestURI(), ex);
        
        ApiErrorResponse error = new ApiErrorResponse(
            500,
            "INTERNAL_ERROR",
            "Erro interno do servidor",
            LocalDateTime.now(),
            List.of()
        );
        return ResponseEntity.status(500).body(error);
    }

    /**
     * Determina uma mensagem segura para exibir ao cliente baseada no erro externo
     * Evita expor detalhes internos da API
     */
    private String determinarMensagemSegura(String mensagemOriginal) {
        if (mensagemOriginal == null) {
            return "Erro ao processar requisição com serviço externo";
        }

        if (mensagemOriginal.contains("OCR") || mensagemOriginal.contains("PDF")) {
            if (mensagemOriginal.contains("texto insuficiente")) {
                return "O OCR não conseguiu extrair texto suficiente da redação. Verifique a nitidez do PDF e tente novamente.";
            } else if (mensagemOriginal.contains("Resposta inválida")) {
                return "O serviço de OCR retornou uma resposta inválida. Tente novamente mais tarde.";
            } else
            if (mensagemOriginal.contains("timeout")) {
                return "Processamento de OCR demorou demais. Tente com uma imagem menor.";
            } else if (mensagemOriginal.contains("HTTP 429")) {
                return "Limite de requisições atingido. Aguarde alguns minutos e tente novamente.";
            } else if (mensagemOriginal.contains("HTTP 401")) {
                return "Erro de autenticação com serviço de OCR. Contate o suporte.";
            } else if (mensagemOriginal.contains("HTTP 5")) {
                return "Serviço de OCR indisponível. Tente novamente mais tarde.";
            } else {
                return "Falha ao processar OCR do PDF. Verifique se a imagem é legível e tente novamente.";
            }
        }

        if (mensagemOriginal.contains("API")) {
            return "Erro ao comunicar com serviço externo. Tente novamente mais tarde.";
        }

        return "Erro ao processar requisição. Tente novamente mais tarde.";
    }
}
