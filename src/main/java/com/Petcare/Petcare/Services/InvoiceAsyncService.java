package com.Petcare.Petcare.Services;

import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.Petcare.Petcare.DTOs.Invoice.InvoiceDetailResponse;
import com.Petcare.Petcare.DTOs.Invoice.InvoiceSummaryResponse;
import com.Petcare.Petcare.Models.Booking.Booking;

/**
 * Async service wrapper for Invoice operations.
 * 
 * All methods automatically propagate SecurityContext thanks to
 * DelegatingSecurityContextAsyncTaskExecutor configured in AsyncConfig.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceAsyncService {

    private final InvoiceService invoiceService;

    /**
     * Get invoice by ID asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<InvoiceDetailResponse> getInvoiceByIdAsync(Long invoiceId) {
        log.debug("Executing getInvoiceByIdAsync({}) in background", invoiceId);
        try {
            InvoiceDetailResponse invoice = invoiceService.getInvoiceById(invoiceId);
            return CompletableFuture.completedFuture(invoice);
        } catch (Exception e) {
            log.error("Error in getInvoiceByIdAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get invoices by account ID asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<Page<InvoiceSummaryResponse>> getInvoicesByAccountIdAsync(Long accountId, Pageable pageable) {
        log.debug("Executing getInvoicesByAccountIdAsync({}) in background", accountId);
        try {
            Page<InvoiceSummaryResponse> invoices = invoiceService.getInvoicesByAccountId(accountId, pageable);
            return CompletableFuture.completedFuture(invoices);
        } catch (Exception e) {
            log.error("Error in getInvoicesByAccountIdAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Generate and process invoice for booking asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> generateAndProcessInvoiceForBookingAsync(Booking booking) {
        log.debug("Executing generateAndProcessInvoiceForBookingAsync({}) in background", booking.getId());
        try {
            invoiceService.generateAndProcessInvoiceForBooking(booking);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error in generateAndProcessInvoiceForBookingAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}
