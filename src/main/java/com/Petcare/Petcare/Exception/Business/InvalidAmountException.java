package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an amount is invalid.
 */
@Slf4j
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidAmountException extends RuntimeException {
    
    public InvalidAmountException(String amount) {
        super("Monto inválido: " + amount);
        log.warn("Invalid amount: {}", amount);
    }
}
