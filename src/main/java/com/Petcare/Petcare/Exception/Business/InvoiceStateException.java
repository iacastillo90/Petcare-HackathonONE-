package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an invoice state transition is invalid.
 */
@Slf4j
@ResponseStatus(HttpStatus.CONFLICT)
public class InvoiceStateException extends RuntimeException {
    
    public InvoiceStateException(String message) {
        super(message);
        log.warn("Invoice state exception: {}", message);
    }
}
