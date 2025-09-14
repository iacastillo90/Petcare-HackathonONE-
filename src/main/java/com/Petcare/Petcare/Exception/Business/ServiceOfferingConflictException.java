package com.Petcare.Petcare.Exception.Business;

public class ServiceOfferingConflictException extends RuntimeException {
    public ServiceOfferingConflictException(String message) {
        super(message);
    }
}
