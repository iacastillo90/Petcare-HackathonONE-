package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an invoice is not found.
 */
@Slf4j
@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvoiceNotFoundException extends RuntimeException {
    
    public InvoiceNotFoundException(Long id) {
        super("Factura no encontrada con ID: " + id);
        log.warn("Invoice not found with ID: {}", id);
    }
}
