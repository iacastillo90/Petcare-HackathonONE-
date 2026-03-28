package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a sitter is not found.
 */
@Slf4j
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SitterNotFoundException extends RuntimeException {
    
    public SitterNotFoundException(String email) {
        super("Sitter no encontrado: " + email);
        log.warn("Sitter not found: {}", email);
    }
    
    public SitterNotFoundException(Long id) {
        super("Sitter no encontrado con ID: " + id);
        log.warn("Sitter not found with ID: {}", id);
    }
}
