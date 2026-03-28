package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a cancellation reason is required but not provided.
 */
@Slf4j
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CancellationReasonRequiredException extends RuntimeException {
    
    public CancellationReasonRequiredException() {
        super("Razón de cancelación requerida");
        log.warn("Cancellation reason required");
    }
}
