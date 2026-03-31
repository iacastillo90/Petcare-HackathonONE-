package com.Petcare.Petcare.Configurations.Security;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;

    private final ConcurrentHashMap<String, RateLimitEntry> ipRateLimitMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RateLimitEntry> userRateLimitMap = new ConcurrentHashMap<>();

    private static final long WINDOW_MILLIS = 60_000;
    private static final long CLEANUP_INTERVAL_MILLIS = 300_000;

    // NOTA: No incluir paths que terminen con "/" - la lógica de isExcluded los agrega automáticamente
    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/api/services",
            "/api/users/login",
            "/api/users/register",
            "/api/users/register-sitter",
            "/api/users/verify",
            "/api/users/email-available",
            "/api/users/health",
            "/verification-success.html",
            "/swagger-ui",
            "/v3/api-docs",
            "/api-docs",
            "/actuator"
    );

    @PostConstruct
    public void init() {
        startCleanupTask();
    }

    private void startCleanupTask() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limit-cleanup");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::cleanupExpiredEntries,
                CLEANUP_INTERVAL_MILLIS, CLEANUP_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
    }

    private void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        long expirationTime = now - (rateLimitConfig.getBlockDurationMinutes() * 60_000L);

        ipRateLimitMap.entrySet().removeIf(entry ->
                entry.getValue().getWindowStart() < expirationTime);
        userRateLimitMap.entrySet().removeIf(entry ->
                entry.getValue().getWindowStart() < expirationTime);

        log.debug("Rate limit cleanup completed. IP entries: {}, User entries: {}",
                ipRateLimitMap.size(), userRateLimitMap.size());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!rateLimitConfig.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (isExcluded(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String userIdentifier = getUserIdentifier();

        int retryAfterSeconds = checkAndProcessRateLimit(clientIp, userIdentifier);

        if (retryAfterSeconds > 0) {
            log.warn("Rate limit exceeded for IP: {} or User: {}", clientIp, userIdentifier);
            response.setStatus(429);
            response.setContentType("application/json");
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.getWriter().write("{\"status\":429,\"message\":\"Too many requests. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private int checkAndProcessRateLimit(String clientIp, String userIdentifier) {
        long blockDurationMillis = rateLimitConfig.getBlockDurationMinutes() * 60_000L;
        int maxRequests = rateLimitConfig.getRequestsPerMinute();

        RateLimitEntry ipEntry = ipRateLimitMap.compute(clientIp, (key, entry) -> {
            if (entry == null) {
                return new RateLimitEntry();
            }
            if (entry.isWindowExpired(WINDOW_MILLIS)) {
                entry.resetWindow();
            }
            if (entry.isBlocked()) {
                return entry;
            }
            if (entry.getRequestCount() >= maxRequests) {
                entry.block(blockDurationMillis);
            } else {
                entry.incrementRequestCount();
            }
            return entry;
        });

        if (ipEntry.isBlocked()) {
            return rateLimitConfig.getBlockDurationMinutes() * 60;
        }

        if (userIdentifier != null) {
            RateLimitEntry userEntry = userRateLimitMap.compute(userIdentifier, (key, entry) -> {
                if (entry == null) {
                    return new RateLimitEntry();
                }
                if (entry.isWindowExpired(WINDOW_MILLIS)) {
                    entry.resetWindow();
                }
                if (entry.isBlocked()) {
                    return entry;
                }
                if (entry.getRequestCount() >= maxRequests) {
                    entry.block(blockDurationMillis);
                } else {
                    entry.incrementRequestCount();
                }
                return entry;
            });

            if (userEntry.isBlocked()) {
                return rateLimitConfig.getBlockDurationMinutes() * 60;
            }
        }

        return 0;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getUserIdentifier() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() != null) {
            return authentication.getPrincipal().toString();
        }
        return null;
    }

    private boolean isExcluded(String path) {
        if (path == null) {
            return true;
        }

        for (String excluded : EXCLUDED_PATHS) {
            if (path.equals(excluded) || path.startsWith(excluded + "/")) {
                return true;
            }
        }
        return false;
    }

    ConcurrentHashMap<String, RateLimitEntry> getIpRateLimitMap() {
        return ipRateLimitMap;
    }

    ConcurrentHashMap<String, RateLimitEntry> getUserRateLimitMap() {
        return userRateLimitMap;
    }
}
