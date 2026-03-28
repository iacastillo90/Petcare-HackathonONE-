package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a booking state transition is invalid.
 */
@Slf4j
@ResponseStatus(HttpStatus.CONFLICT)
public class BookingStateException extends RuntimeException {
    
    public BookingStateException(String message) {
        super(message);
        log.warn("Booking state exception: {}", message);
    }
}
