package com.Petcare.Petcare.Configurations.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Security filter to prevent brute force attacks.
 * Tracks failed login attempts per IP and per email.
 * After max attempts, blocks the account/IP for a configured duration.
 * 
 * This helps prevent:
 * - Brute force attacks on login endpoints
 * - Credential stuffing attacks
 * - Password spraying
 */
@Component
public class AccountLockoutFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AccountLockoutFilter.class);
    
    private final ConcurrentHashMap<String, AttemptRecord> ipAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AttemptRecord> emailAttempts = new ConcurrentHashMap<>();

    @Value("${petcare.security.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${petcare.security.lockout-duration-minutes:15}")
    private int lockoutDurationMinutes;

    @Value("${petcare.security.lockout-enabled:true}")
    private boolean lockoutEnabled;

    private static final String LOGIN_ENDPOINT = "/api/users/login";
    private static final String REGISTER_ENDPOINT = "/api/users/register";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String clientIP = getClientIP(request);
        String requestURI = request.getRequestURI();

        // Only apply to authentication endpoints
        if (isAuthEndpoint(requestURI)) {
            if (!lockoutEnabled) {
                filterChain.doFilter(request, response);
                return;
            }

            // Check if IP is locked
            AttemptRecord ipRecord = ipAttempts.get(clientIP);
            if (isLocked(ipRecord)) {
                log.warn("Blocked request from locked IP: {} to {}", clientIP, requestURI);
                response.setStatus(423); // SC_LOCKED
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"error\":\"Account temporarily locked due to too many failed attempts. Please try again later.\"}"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);

        // After filter chain - check if authentication failed
        // This is handled by LoginFailureHandler
    }

    /**
     * Record a failed login attempt from an IP and/or email
     */
    public void recordFailedAttempt(String ip, String email) {
        if (!lockoutEnabled) return;

        // Record IP attempt
        ipAttempts.compute(ip, (key, record) -> {
            if (record == null || record.isExpired(lockoutDurationMinutes)) {
                return new AttemptRecord(1, System.currentTimeMillis());
            }
            record.increment();
            return record;
        });

        // Record email attempt if provided
        if (email != null && !email.isEmpty()) {
            emailAttempts.compute(email, (key, record) -> {
                if (record == null || record.isExpired(lockoutDurationMinutes)) {
                    return new AttemptRecord(1, System.currentTimeMillis());
                }
                record.increment();
                return record;
            });
        }

        log.warn("Failed login attempt. IP: {}, Email: {}", ip, email);
    }

    /**
     * Clear failed attempts after successful login
     */
    public void clearAttempts(String ip, String email) {
        ipAttempts.remove(ip);
        if (email != null && !email.isEmpty()) {
            emailAttempts.remove(email);
        }
    }

    /**
     * Check if IP or email is currently locked
     */
    public boolean isLocked(String ip, String email) {
        AttemptRecord ipRecord = ipAttempts.get(ip);
        if (isLocked(ipRecord)) {
            return true;
        }
        if (email != null && !email.isEmpty()) {
            AttemptRecord emailRecord = emailAttempts.get(email);
            return isLocked(emailRecord);
        }
        return false;
    }

    private boolean isLocked(AttemptRecord record) {
        if (record == null) return false;
        if (record.isExpired(lockoutDurationMinutes)) {
            return false; // Lockout expired
        }
        return record.getAttempts() >= maxLoginAttempts;
    }

    private boolean isAuthEndpoint(String uri) {
        return uri.endsWith(LOGIN_ENDPOINT) || uri.endsWith(REGISTER_ENDPOINT);
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Inner class to track attempt records
     */
    private static class AttemptRecord {
        private final AtomicInteger attempts;
        private final long firstAttemptTime;

        public AttemptRecord(int initial, long time) {
            this.attempts = new AtomicInteger(initial);
            this.firstAttemptTime = time;
        }

        public int getAttempts() {
            return attempts.get();
        }

        public void increment() {
            attempts.incrementAndGet();
        }

        public boolean isExpired(int lockoutMinutes) {
            long elapsed = System.currentTimeMillis() - firstAttemptTime;
            return elapsed > (lockoutMinutes * 60 * 1000);
        }
    }
}
