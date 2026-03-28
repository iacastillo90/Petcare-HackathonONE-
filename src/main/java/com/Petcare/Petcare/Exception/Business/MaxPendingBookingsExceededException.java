package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when the maximum number of pending bookings is exceeded.
 */
@Slf4j
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MaxPendingBookingsExceededException extends RuntimeException {
    
    public MaxPendingBookingsExceededException() {
        super("Máximo de reservas pendientes excedido");
        log.warn("Maximum pending bookings exceeded");
    }
}
