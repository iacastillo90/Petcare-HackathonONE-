package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to use an inactive sitter.
 */
@Slf4j
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SitterInactiveException extends RuntimeException {
    
    public SitterInactiveException(String email) {
        super("Sitter inactivo: " + email);
        log.warn("Sitter inactive with email: {}", email);
    }
}
