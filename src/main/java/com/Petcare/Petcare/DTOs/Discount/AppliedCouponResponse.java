package com.Petcare.Petcare.DTOs.Discount;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AppliedCouponResponse {

    private Long id;
    private Long bookingId;
    private Long accountId;
    private Long couponId;
    private BigDecimal discountAmount;
    private BigDecimal bookingTotal; // total actualizado de la reserva
    private LocalDateTime appliedAt;

    // ========== GETTERS Y SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getBookingTotal() {
        return bookingTotal;
    }

    public void setBookingTotal(BigDecimal bookingTotal) {
        this.bookingTotal = bookingTotal;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
}
