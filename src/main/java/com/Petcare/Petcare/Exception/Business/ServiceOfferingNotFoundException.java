package com.Petcare.Petcare.Exception.Business;

public class ServiceOfferingNotFoundException extends RuntimeException {
    public ServiceOfferingNotFoundException(String message) {
        super(message);
    }
}
