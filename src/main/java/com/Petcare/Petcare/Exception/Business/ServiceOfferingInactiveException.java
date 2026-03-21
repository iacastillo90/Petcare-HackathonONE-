package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to use an inactive service offering.
 */
@Slf4j
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ServiceOfferingInactiveException extends RuntimeException {
    
    public ServiceOfferingInactiveException(Long id) {
        super("Servicio inactivo: " + id);
        log.warn("Service offering inactive with ID: {}", id);
    }
}
