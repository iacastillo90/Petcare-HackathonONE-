package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a coupon is not found.
 */
@Slf4j
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CouponNotFoundException extends RuntimeException {
    
    public CouponNotFoundException(String code) {
        super("Cupón no encontrado");
        log.warn("Coupon not found with code: {}", code);
    }
}
