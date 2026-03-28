package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a pet is not found.
 */
@Slf4j
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PetNotFoundException extends RuntimeException {
    
    public PetNotFoundException(Long id) {
        super("Mascota no encontrada con ID: " + id);
        log.warn("Pet not found with ID: {}", id);
    }
}
