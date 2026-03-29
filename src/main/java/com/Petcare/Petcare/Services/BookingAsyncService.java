package com.Petcare.Petcare.Services;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.Petcare.Petcare.DTOs.Booking.BookingDetailResponse;
import com.Petcare.Petcare.DTOs.Booking.BookingSummaryResponse;

/**
 * Async service wrapper for Booking operations.
 * 
 * Provides asynchronous versions of booking-related operations
 * for better performance and non-blocking execution.
 */
@Service
public class BookingAsyncService {

    private static final Logger log = LoggerFactory.getLogger(BookingAsyncService.class);

    private final BookingService bookingService;

    public BookingAsyncService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Get booking by ID asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<BookingDetailResponse> getBookingByIdAsync(Long id) {
        log.debug("Executing getBookingByIdAsync({}) in background", id);
        try {
            BookingDetailResponse booking = bookingService.getBookingById(id);
            return CompletableFuture.completedFuture(booking);
        } catch (Exception e) {
            log.error("Error in getBookingByIdAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get bookings by user asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<Page<BookingSummaryResponse>> getBookingsByUserAsync(
            Long userId, String role, String status, Pageable pageable) {
        log.debug("Executing getBookingsByUserAsync({}) in background", userId);
        try {
            Page<BookingSummaryResponse> bookings = bookingService.getBookingsByUser(userId, role, status, pageable);
            return CompletableFuture.completedFuture(bookings);
        } catch (Exception e) {
            log.error("Error in getBookingsByUserAsync: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}
