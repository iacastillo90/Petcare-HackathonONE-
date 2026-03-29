package com.Petcare.Petcare.Services;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.Petcare.Petcare.DTOs.Payment.PaymentMethodResponse;
import com.Petcare.Petcare.DTOs.Payment.PaymentMethodSummary;

/**
 * Async service wrapper for PaymentMethod operations.
 * 
 * All methods automatically propagate SecurityContext thanks to
 * DelegatingSecurityContextAsyncTaskExecutor configured in AsyncConfig.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentMethodAsyncService {

    private final PaymentMethodService paymentMethodService;

    /**
     * Get all payment methods by account asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<List<PaymentMethodSummary>> getAllByAccountAsync(Long accountId) {
        log.debug("Executing getAllByAccountAsync({}) in background", accountId);
        try {
            List<PaymentMethodSummary> methods = paymentMethodService.getAllByAccount(accountId);
            return CompletableFuture.completedFuture(methods);
        } catch (Exception e) {
            log.error("Error in getAllByAccountAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get payment method by ID asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<Optional<PaymentMethodResponse>> getByIdAsync(Long id) {
        log.debug("Executing getByIdAsync({}) in background", id);
        try {
            Optional<PaymentMethodResponse> method = paymentMethodService.getById(id);
            return CompletableFuture.completedFuture(method);
        } catch (Exception e) {
            log.error("Error in getByIdAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}
