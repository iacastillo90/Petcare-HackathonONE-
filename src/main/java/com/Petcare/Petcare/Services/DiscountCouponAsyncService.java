package com.Petcare.Petcare.Services;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.Petcare.Petcare.Models.DiscountCoupon;

/**
 * Async service wrapper for DiscountCoupon operations.
 * 
 * All methods automatically propagate SecurityContext thanks to
 * DelegatingSecurityContextAsyncTaskExecutor configured in AsyncConfig.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountCouponAsyncService {

    private final DiscountCouponService discountCouponService;

    /**
     * Get all discount coupons asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<List<DiscountCoupon>> getAllDiscountCouponsAsync() {
        log.debug("Executing getAllDiscountCouponsAsync in background");
        try {
            List<DiscountCoupon> coupons = discountCouponService.getAllDiscountCoupons();
            return CompletableFuture.completedFuture(coupons);
        } catch (Exception e) {
            log.error("Error in getAllDiscountCouponsAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get discount coupon by ID asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<Optional<DiscountCoupon>> getDiscountCouponByIdAsync(Long id) {
        log.debug("Executing getDiscountCouponByIdAsync({}) in background", id);
        try {
            Optional<DiscountCoupon> coupon = discountCouponService.getDiscountCouponById(id);
            return CompletableFuture.completedFuture(coupon);
        } catch (Exception e) {
            log.error("Error in getDiscountCouponByIdAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get discount coupon by code asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<Optional<DiscountCoupon>> getDiscountCouponByCodeAsync(String couponCode) {
        log.debug("Executing getDiscountCouponByCodeAsync({}) in background", couponCode);
        try {
            Optional<DiscountCoupon> coupon = discountCouponService.getDiscountCouponByCode(couponCode);
            return CompletableFuture.completedFuture(coupon);
        } catch (Exception e) {
            log.error("Error in getDiscountCouponByCodeAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}
