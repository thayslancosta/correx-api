package com.correx.security;

import com.correx.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Filtro de Rate Limiting
 * Bloqueia requisições quando limite de requisições é excedido
 * Retorna 429 Too Many Requests
 */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ip = getClientIp(request);
        String path = request.getRequestURI();

        // Endpoints sensíveis com limite mais rigoroso
        boolean isStrictEndpoint = path.contains("/auth/login") || path.contains("/auth/register");

        Bucket bucket = isStrictEndpoint ? 
                rateLimitConfig.resolveStrictBucket(ip) : 
                rateLimitConfig.resolveBucket(ip);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            long waitForRefill = 60; // esperar 60 segundos por padrão
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.sendError(429, "Too many requests - please retry after " + waitForRefill + " seconds");
        }
    }

    /**
     * Extrai IP real do cliente (considerando proxies)
     */
    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        } else {
            clientIp = clientIp.split(",")[0].trim();
        }
        return clientIp;
    }
}
