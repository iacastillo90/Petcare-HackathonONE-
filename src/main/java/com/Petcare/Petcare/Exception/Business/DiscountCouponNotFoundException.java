package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a discount coupon is not found.
 */
@Slf4j
@ResponseStatus(HttpStatus.NOT_FOUND)
public class DiscountCouponNotFoundException extends RuntimeException {
    
    public DiscountCouponNotFoundException(String code) {
        super("Cupón no encontrado con código: " + code);
        log.warn("Discount coupon not found with code: {}", code);
    }
}
