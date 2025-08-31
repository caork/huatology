package com.digitaltwin.backend.aspect;

import com.digitaltwin.backend.annotation.RateLimited;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Component
public class RateLimitingAspect {

    private final Map<String, Cache<String, AtomicInteger>> rateLimitCaches = new ConcurrentHashMap<>();

    @Autowired
    private HttpServletRequest request;

    @Around("@annotation(rateLimited)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        String key = generateKey(rateLimited.keyStrategy());

        if (key == null) {
            // If we can't generate a key, proceed without rate limiting
            return joinPoint.proceed();
        }

        String cacheKey = rateLimited.keyStrategy() + ":" + key;
        Cache<String, AtomicInteger> cache = getOrCreateCache(rateLimited.timeWindow());

        AtomicInteger requestCount = cache.get(cacheKey, k -> new AtomicInteger(0));

        if (requestCount.incrementAndGet() > rateLimited.value()) {
            // Rate limit exceeded
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Rate limit exceeded. Try again later.");
            errorResponse.put("retryAfter", rateLimited.timeWindow());

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", String.valueOf(rateLimited.timeWindow()))
                    .body(errorResponse);
        }

        return joinPoint.proceed();
    }

    private String generateKey(RateLimited.KeyStrategy keyStrategy) {
        switch (keyStrategy) {
            case USER:
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    return authentication.getName();
                }
                return getClientIP(); // Fallback to IP if not authenticated

            case IP:
                return getClientIP();

            case GLOBAL:
                return "global";

            default:
                return "default";
        }
    }

    private String getClientIP() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attrs.getRequest();

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }

    private Cache<String, AtomicInteger> getOrCreateCache(int timeWindowSeconds) {
        return rateLimitCaches.computeIfAbsent(
                String.valueOf(timeWindowSeconds),
                k -> Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofSeconds(timeWindowSeconds))
                        .build()
        );
    }
}