package com.Petcare.Petcare.Services;

import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.Petcare.Petcare.DTOs.PlatformFee.CreatePlatformFeeRequest;
import com.Petcare.Petcare.DTOs.PlatformFee.PlatformFeeResponse;
import com.Petcare.Petcare.Models.Booking.Booking;

/**
 * Async service wrapper for PlatformFee operations.
 * 
 * All methods automatically propagate SecurityContext thanks to
 * DelegatingSecurityContextAsyncTaskExecutor configured in AsyncConfig.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformFeeAsyncService {

    private final PlatformFeeService platformFeeService;

    /**
     * Calculate and create fee asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<PlatformFeeResponse> calculateAndCreateFeeAsync(CreatePlatformFeeRequest request) {
        log.debug("Executing calculateAndCreateFeeAsync in background");
        try {
            PlatformFeeResponse fee = platformFeeService.calculateAndCreateFee(request);
            return CompletableFuture.completedFuture(fee);
        } catch (Exception e) {
            log.error("Error in calculateAndCreateFeeAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Calculate and create platform fee for booking asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> recalculatePlatformFeeAsync(Booking booking) {
        log.debug("Executing recalculatePlatformFeeAsync({}) in background", booking.getId());
        try {
            platformFeeService.recalculatePlatformFee(booking);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error in recalculatePlatformFeeAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}
