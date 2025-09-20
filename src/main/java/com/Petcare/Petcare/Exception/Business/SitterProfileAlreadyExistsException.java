package com.Petcare.Petcare.Exception.Business;

public class SitterProfileAlreadyExistsException extends RuntimeException {
    public SitterProfileAlreadyExistsException(String message) {
        super(message);
    }
}
