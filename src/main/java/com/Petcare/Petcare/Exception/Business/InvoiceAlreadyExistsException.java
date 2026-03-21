package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to create a duplicate invoice for a booking.
 */
@Slf4j
@ResponseStatus(HttpStatus.CONFLICT)
public class InvoiceAlreadyExistsException extends RuntimeException {
    
    public InvoiceAlreadyExistsException(Long bookingId) {
        super("Ya existe una factura para esta reserva con ID: " + bookingId);
        log.warn("Invoice already exists for booking ID: {}", bookingId);
    }
}
