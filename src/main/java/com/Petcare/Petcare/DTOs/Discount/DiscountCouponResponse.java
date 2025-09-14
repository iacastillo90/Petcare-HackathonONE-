package com.Petcare.Petcare.DTOs.Discount;

import com.Petcare.Petcare.Models.DiscountCoupon;
import com.Petcare.Petcare.Models.DiscountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para devolver la información de un cupón.
 */
public class DiscountCouponResponse {

    private Long id;
    private String couponCode;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private LocalDateTime expiryDate;
    private Integer maxUses;
    private boolean active;
    private Integer usedCount;

    // Método de fábrica para mapear entidad -> DTO
    public static DiscountCouponResponse fromEntity(DiscountCoupon coupon) {
        if (coupon == null) {
            return null;
        }
        DiscountCouponResponse dto = new DiscountCouponResponse();
        dto.setId(coupon.getId());
        dto.setCouponCode(coupon.getCouponCode());
        dto.setDiscountType(coupon.getDiscountType());
        dto.setDiscountValue(coupon.getDiscountValue());
        dto.setExpiryDate(coupon.getExpiryDate());
        dto.setMaxUses(coupon.getMaxUses());
        dto.setActive(coupon.isActive());
        dto.setUsedCount(coupon.getUsedCount());
        return dto;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(Integer usedCount) {
        this.usedCount = usedCount;
    }
}
