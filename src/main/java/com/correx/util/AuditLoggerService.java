package com.correx.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Serviço de Audit Logging
 * Registra ações importantes para auditoria e conformidade
 */
@Slf4j
@Service
public class AuditLoggerService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Log de ação de autenticação
     */
    public void logLogin(String email, boolean success, String reason) {
        String ip = getClientIp();
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String status = success ? "SUCCESS" : "FAILURE";
        
        log.warn("AUDIT_LOGIN [{}] Email: {}, Status: {}, IP: {}, Reason: {}", 
                timestamp, email, status, ip, reason != null ? reason : "N/A");
    }

    /**
     * Log de ação de registro
     */
    public void logRegister(String email, boolean success, String reason) {
        String ip = getClientIp();
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        
        log.warn("AUDIT_REGISTER [{}] Email: {}, Success: {}, IP: {}, Reason: {}", 
                timestamp, email, success, ip, reason != null ? reason : "N/A");
    }

    /**
     * Log de acesso a recurso
     */
    public void logResourceAccess(String email, String resource, String action) {
        String ip = getClientIp();
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        
        log.info("AUDIT_ACCESS [{}] User: {}, Resource: {}, Action: {}, IP: {}", 
                timestamp, email, resource, action, ip);
    }

    /**
     * Log de alteração de dados
     */
    public void logDataModification(String email, String resource, String action, Long resourceId, String changes) {
        String ip = getClientIp();
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        
        log.warn("AUDIT_MODIFICATION [{}] User: {}, Resource: {}, Action: {}, ID: {}, Changes: {}, IP: {}", 
                timestamp, email, resource, action, resourceId, changes, ip);
    }

    /**
     * Log de tentativa não autorizada
     */
    public void logUnauthorizedAccess(String email, String resource, String denialReason) {
        String ip = getClientIp();
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        
        log.error("AUDIT_UNAUTHORIZED [{}] User: {}, Resource: {}, Reason: {}, IP: {}", 
                timestamp, email, resource, denialReason, ip);
    }

    /**
     * Log de erro de segurança
     */
    public void logSecurityEvent(String eventType, String description) {
        String ip = getClientIp();
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        
        log.error("AUDIT_SECURITY [{}] Event: {}, Description: {}, IP: {}", 
                timestamp, eventType, description, ip);
    }

    /**
     * Extrai IP do cliente atual (considerando proxies)
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String clientIp = request.getHeader("X-Forwarded-For");
                if (clientIp == null || clientIp.isEmpty()) {
                    clientIp = request.getRemoteAddr();
                } else {
                    clientIp = clientIp.split(",")[0].trim();
                }
                return clientIp;
            }
        } catch (Exception e) {
            log.debug("Could not resolve client IP", e);
        }
        return "UNKNOWN";
    }
}
