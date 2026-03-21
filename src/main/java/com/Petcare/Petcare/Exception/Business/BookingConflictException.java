package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is a scheduling conflict for a booking.
 */
@Slf4j
@ResponseStatus(HttpStatus.CONFLICT)
public class BookingConflictException extends RuntimeException {
    
    public BookingConflictException(String message) {
        super(message);
        log.warn("Booking conflict: {}", message);
    }
}
