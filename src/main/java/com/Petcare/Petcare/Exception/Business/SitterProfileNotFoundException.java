package com.Petcare.Petcare.Exception.Business;

public class SitterProfileNotFoundException extends RuntimeException {
    public SitterProfileNotFoundException(String message) {
        super(message);
    }
}
