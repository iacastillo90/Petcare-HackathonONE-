package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a booking is not found.
 */
@Slf4j
@ResponseStatus(HttpStatus.NOT_FOUND)
public class BookingNotFoundException extends RuntimeException {
    
    public BookingNotFoundException(Long id) {
        super("Reserva no encontrada con ID: " + id);
        log.warn("Booking not found with ID: {}", id);
    }
}
