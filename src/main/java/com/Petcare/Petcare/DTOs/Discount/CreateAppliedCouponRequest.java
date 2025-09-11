package com.Petcare.Petcare.DTOs.Discount;

public class CreateAppliedCouponRequest {

    private Long bookingId;
    private Long accountId; // necesario para tu servicio
    private String couponCode;

    public CreateAppliedCouponRequest() {}

    public CreateAppliedCouponRequest(Long bookingId, Long accountId, String couponCode) {
        this.bookingId = bookingId;
        this.accountId = accountId;
        this.couponCode = couponCode;
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

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
}
