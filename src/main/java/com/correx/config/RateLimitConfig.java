package com.correx.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Configuration usando Bucket4j
 * Limita requisições por IP/usuário para prevenir abuso
 */
@Configuration
public class RateLimitConfig {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Cria bucket de rate limit para IP específico
     * Limite: 100 requisições por minuto
     */
    public Bucket resolveBucket(String ip) {
        return buckets.computeIfAbsent(ip, k -> createNewBucket());
    }

    /**
     * Cria novo bucket com limite de 100 requests/minuto
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.simple(100, Duration.ofMinutes(1));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * Limites específicos para endpoints sensíveis
     * Login/Register: 5 requisições por minuto
     */
    public Bucket resolveStrictBucket(String ip) {
        Bandwidth limit = Bandwidth.simple(5, Duration.ofMinutes(1));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
}
