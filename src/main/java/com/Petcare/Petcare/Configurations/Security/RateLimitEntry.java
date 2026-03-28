package com.Petcare.Petcare.Configurations.Security;

public class RateLimitEntry {
    private int requestCount;
    private long windowStart;
    private Long blockedUntil;

    public RateLimitEntry() {
        this.requestCount = 1;
        this.windowStart = System.currentTimeMillis();
        this.blockedUntil = null;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public long getWindowStart() {
        return windowStart;
    }

    public Long getBlockedUntil() {
        return blockedUntil;
    }

    public boolean isBlocked() {
        return blockedUntil != null && System.currentTimeMillis() < blockedUntil;
    }

    public void incrementRequestCount() {
        this.requestCount++;
    }

    public void block(long durationMillis) {
        this.blockedUntil = System.currentTimeMillis() + durationMillis;
    }

    public boolean isWindowExpired(long windowMillis) {
        return System.currentTimeMillis() - windowStart > windowMillis;
    }

    public void resetWindow() {
        this.requestCount = 1;
        this.windowStart = System.currentTimeMillis();
        this.blockedUntil = null;
    }
}
