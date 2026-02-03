package com.jonathamjtm.gestaoartistas.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Value("${security.rate-limit.capacity}")
    private int rateLimitCapacity; // Valor 10 conforme application.properties

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") ||
                path.startsWith("/actuator") || path.contains("favicon")) {
            chain.doFilter(request, response);
            return;
        }

        String key = resolveKey(httpRequest);
        Bucket bucket = cache.computeIfAbsent(key, this::createNewBucket);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate Limit excedido para o usuário/IP: {}", key);
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\": \"Muitas requisicoes. Limite de 10 por minuto excedido.\"}");
        }
    }

    private String resolveKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return "USER:" + auth.getName();
        }
        return "IP:" + request.getRemoteAddr();
    }

    private Bucket createNewBucket(String key) {
        Bandwidth limit = Bandwidth.classic(rateLimitCapacity,
                Refill.greedy(rateLimitCapacity, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}