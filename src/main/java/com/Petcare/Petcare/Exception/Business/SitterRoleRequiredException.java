package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user does not have the required SITTER role.
 */
@Slf4j
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SitterRoleRequiredException extends RuntimeException {
    
    public SitterRoleRequiredException() {
        super("El usuario debe tener rol SITTER");
        log.warn("Sitter role required");
    }
    
    public SitterRoleRequiredException(Long userId) {
        super("El usuario con ID " + userId + " no tiene el rol de cuidador");
        log.warn("Sitter role required for user ID: {}", userId);
    }
}
