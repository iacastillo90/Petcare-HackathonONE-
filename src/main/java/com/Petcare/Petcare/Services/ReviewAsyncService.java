package com.Petcare.Petcare.Services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.Petcare.Petcare.DTOs.Review.ReviewResponse;

/**
 * Async service wrapper for Review operations.
 * 
 * All methods automatically propagate SecurityContext thanks to
 * DelegatingSecurityContextAsyncTaskExecutor configured in AsyncConfig.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewAsyncService {

    private final ReviewService reviewService;

    /**
     * Get reviews by pet ID asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<List<ReviewResponse>> getReviewsByPetIdAsync(Long petId) {
        log.debug("Executing getReviewsByPetIdAsync({}) in background", petId);
        try {
            List<ReviewResponse> reviews = reviewService.getReviewsByPetId(petId);
            return CompletableFuture.completedFuture(reviews);
        } catch (Exception e) {
            log.error("Error in getReviewsByPetIdAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get reviews by user ID asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<List<ReviewResponse>> getReviewsByUserIdAsync(Long userId) {
        log.debug("Executing getReviewsByUserIdAsync({}) in background", userId);
        try {
            List<ReviewResponse> reviews = reviewService.getReviewsByUserId(userId);
            return CompletableFuture.completedFuture(reviews);
        } catch (Exception e) {
            log.error("Error in getReviewsByUserIdAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}
