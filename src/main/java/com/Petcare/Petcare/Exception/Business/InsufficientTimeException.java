package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is insufficient time for a booking.
 */
@Slf4j
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientTimeException extends RuntimeException {
    
    public InsufficientTimeException() {
        super("Tiempo insuficiente para la reserva");
        log.warn("Insufficient time for booking");
    }
}
