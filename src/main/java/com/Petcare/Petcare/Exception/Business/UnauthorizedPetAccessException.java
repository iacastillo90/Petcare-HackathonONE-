package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user attempts to access a pet they don't have permission for.
 */
@Slf4j
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedPetAccessException extends RuntimeException {
    
    public UnauthorizedPetAccessException(Long petId, Long accountId) {
        super("No tienes acceso a esta mascota con ID: " + petId);
        log.warn("Unauthorized pet access attempt - Pet ID: {}, Account ID: {}", petId, accountId);
    }
    
    public UnauthorizedPetAccessException() {
        super("No tienes acceso a esta mascota");
        log.warn("Unauthorized pet access");
    }
}
