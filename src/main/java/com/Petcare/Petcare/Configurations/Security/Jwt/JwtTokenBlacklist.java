package com.Petcare.Petcare.Configurations.Security.Jwt;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * JWT Token Blacklist for implementing token revocation.
 * 
 * This component allows tokens to be invalidated before their natural expiration.
 * Use cases:
 * - User logout
 * - Password change
 * - Account suspension
 * - Token compromise detection
 * 
 * The blacklist is cleaned automatically to prevent memory leaks.
 */
@Component
public class JwtTokenBlacklist {

    private final ConcurrentHashMap<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * Add a token to the blacklist
     * 
     * @param token The JWT token to blacklist
     * @param expirationTime The expiration time of the token
     */
    public void blacklistToken(String token, Instant expirationTime) {
        blacklistedTokens.put(token, expirationTime);
    }

    /**
     * Check if a token is blacklisted
     * 
     * @param token The JWT token to check
     * @return true if the token is blacklisted, false otherwise
     */
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    /**
     * Remove a specific token from the blacklist
     * 
     * @param token The JWT token to remove
     */
    public void removeFromBlacklist(String token) {
        blacklistedTokens.remove(token);
    }

    /**
     * Clear all blacklisted tokens (use with caution)
     */
    public void clearBlacklist() {
        blacklistedTokens.clear();
    }

    /**
     * Get the number of blacklisted tokens
     * 
     * @return Number of tokens in blacklist
     */
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }

    /**
     * Initialize cleanup scheduler
     * Runs every 5 minutes to remove expired tokens from blacklist
     */
    public void initCleanup() {
        cleanupScheduler.scheduleAtFixedRate(() -> {
            Instant now = Instant.now();
            blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
        }, 5, 5, TimeUnit.MINUTES);
    }

    /**
     * Shutdown the cleanup scheduler
     */
    public void shutdown() {
        cleanupScheduler.shutdown();
    }
}
