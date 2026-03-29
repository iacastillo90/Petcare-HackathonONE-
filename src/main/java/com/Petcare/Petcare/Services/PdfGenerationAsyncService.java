package com.Petcare.Petcare.Services;

import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.Petcare.Petcare.DTOs.Invoice.InvoiceDetailResponse;

/**
 * Async service wrapper for PdfGeneration operations.
 * 
 * All methods automatically propagate SecurityContext thanks to
 * DelegatingSecurityContextAsyncTaskExecutor configured in AsyncConfig.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfGenerationAsyncService {

    private final PdfGenerationService pdfGenerationService;

    /**
     * Generate invoice PDF asynchronously.
     * Useful for generating large PDFs without blocking the main thread.
     */
    @Async("taskExecutor")
    public CompletableFuture<byte[]> generateInvoicePdfAsync(InvoiceDetailResponse invoice) {
        log.debug("Executing generateInvoicePdfAsync({}) in background", invoice.id());
        try {
            byte[] pdf = pdfGenerationService.generateInvoicePdf(invoice);
            return CompletableFuture.completedFuture(pdf);
        } catch (Exception e) {
            log.error("Error in generateInvoicePdfAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}
