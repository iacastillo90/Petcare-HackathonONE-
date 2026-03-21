package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a service offering is not found.
 */
@Slf4j
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ServiceOfferingNotFoundException extends RuntimeException {
    
    public ServiceOfferingNotFoundException(Long id) {
        super("Servicio no encontrado con ID: " + id);
        log.warn("Service offering not found with ID: {}", id);
    }
}
