package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a coupon has expired.
 */
@Slf4j
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CouponExpiredException extends RuntimeException {
    
    public CouponExpiredException(String code) {
        super("El cupón no es válido o ha expirado");
        log.warn("Coupon expired with code: {}", code);
    }
}
