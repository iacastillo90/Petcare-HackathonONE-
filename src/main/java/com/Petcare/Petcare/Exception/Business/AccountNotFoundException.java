package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an account is not found.
 */
@Slf4j
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AccountNotFoundException extends RuntimeException {
    
    public AccountNotFoundException(Long id) {
        super("Cuenta no encontrada con ID: " + id);
        log.warn("Account not found with ID: {}", id);
    }
}
