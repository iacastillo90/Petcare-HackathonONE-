package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to create a pet with a duplicate name in the same account.
 */
@Slf4j
@ResponseStatus(HttpStatus.CONFLICT)
public class PetAlreadyExistsException extends RuntimeException {
    
    public PetAlreadyExistsException(String name) {
        super("Ya existe una mascota con el nombre '" + name + "' en esta cuenta");
        log.warn("Pet already exists with name: {}", name);
    }
}
