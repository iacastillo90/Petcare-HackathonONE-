package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to perform an operation on an inactive account.
 */
@Slf4j
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InactiveAccountException extends RuntimeException {
    
    public InactiveAccountException(Long id) {
        super("Cuenta inactiva: " + id);
        log.warn("Inactive account with ID: {}", id);
    }
}
