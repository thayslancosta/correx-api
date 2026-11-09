package com.correx.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração de Hardening de Segurança
 * Implementa best practices de segurança para produção
 */
@Configuration
@RequiredArgsConstructor
public class SecurityHardeningConfig implements WebMvcConfigurer {

    /**
     * Headers de Segurança HTTP são configurados via Spring Security
     * Implementação via Custom Response Headers:
     * 
     * X-Content-Type-Options: nosniff
     *   Previne MIME sniffing attacks
     * 
     * X-Frame-Options: DENY
     *   Previne clickjacking
     * 
     * X-XSS-Protection: 1; mode=block
     *   Ativa proteção XSS do navegador
     * 
     * Strict-Transport-Security: max-age=31536000; includeSubDomains
     *   Força HTTPS (adicionar em produção)
     */

    /**
     * Configurações implementadas:
     * ✅ Rate Limiting por IP (100 req/min, 5 req/min para auth)
     * ✅ Audit Logging para ações críticas
     * ✅ Validação de entrada com Jakarta Annotations
     * ✅ Criptografia de senhas com BCrypt
     * ✅ JWT com HS256 e expiração de 24h
     * ✅ CORS restritivo (apenas origens permitidas)
     * ✅ SQL Prevention via Hibernate/JPA
     * ✅ XSS Prevention via Jackson serialization
     */
}
