package com.Petcare.Petcare.Services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.Petcare.Petcare.Models.AppliedCoupon;
import com.Petcare.Petcare.Models.DiscountCoupon;

/**
 * Async service wrapper for AppliedCoupon operations.
 * 
 * All methods automatically propagate SecurityContext thanks to
 * DelegatingSecurityContextAsyncTaskExecutor configured in AsyncConfig.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppliedCouponAsyncService {

    private final AppliedCouponService appliedCouponService;

    /**
     * Get coupons by account asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<List<AppliedCoupon>> getCouponsByAccountAsync(Long accountId) {
        log.debug("Executing getCouponsByAccountAsync({}) in background", accountId);
        try {
            List<AppliedCoupon> coupons = appliedCouponService.getCouponsByAccount(accountId);
            return CompletableFuture.completedFuture(coupons);
        } catch (Exception e) {
            log.error("Error in getCouponsByAccountAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get coupons by booking asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<List<AppliedCoupon>> getCouponsByBookingAsync(Long bookingId) {
        log.debug("Executing getCouponsByBookingAsync({}) in background", bookingId);
        try {
            List<AppliedCoupon> coupons = appliedCouponService.getCouponsByBooking(bookingId);
            return CompletableFuture.completedFuture(coupons);
        } catch (Exception e) {
            log.error("Error in getCouponsByBookingAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Validate coupon asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<Boolean> validateCouponAsync(DiscountCoupon coupon) {
        log.debug("Executing validateCouponAsync in background");
        try {
            boolean isValid = appliedCouponService.validateCoupon(coupon);
            return CompletableFuture.completedFuture(isValid);
        } catch (Exception e) {
            log.error("Error in validateCouponAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}
