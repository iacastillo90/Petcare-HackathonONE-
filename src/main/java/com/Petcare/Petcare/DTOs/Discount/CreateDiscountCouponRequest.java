package com.Petcare.Petcare.DTOs.Discount;

import com.Petcare.Petcare.Models.DiscountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para crear un nuevo cupón de descuento.
 */
public class CreateDiscountCouponRequest {

    private String couponCode;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private LocalDateTime expiryDate;
    private Integer maxUses;
    private boolean active;

    // Getters y setters
    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
