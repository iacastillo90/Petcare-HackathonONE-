package com.Petcare.Petcare.Models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que registra el uso de un cupón en una reserva (aplicación de descuento).
 */
@Entity
@Table(name = "applied_coupon",
        indexes = {
                @Index(name = "idx_appliedcoupon_account_id", columnList = "account_id"),
                @Index(name = "idx_appliedcoupon_booking_id", columnList = "booking_id"),
                @Index(name = "idx_appliedcoupon_coupon_id", columnList = "coupon_id")
        })
public class AppliedCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false, foreignKey = @ForeignKey(name = "fk_appliedcoupon_discountcoupon"))
    private DiscountCoupon coupon;

    @Column(name = "discount_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal discountAmount;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt = LocalDateTime.now();

    public AppliedCoupon() {}

    // Getters y setters

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

    public DiscountCoupon getCoupon() {
        return coupon;
    }

    public void setCoupon(DiscountCoupon coupon) {
        this.coupon = coupon;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    // Utilidad para acceder al id del cupón sin forzar carga completa
    @Transient
    public Long getCouponId() {
        return coupon != null ? coupon.getId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppliedCoupon)) return false;
        AppliedCoupon that = (AppliedCoupon) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AppliedCoupon{" +
                "id=" + id +
                ", bookingId=" + bookingId +
                ", accountId=" + accountId +
                ", couponId=" + getCouponId() +
                ", discountAmount=" + discountAmount +
                ", appliedAt=" + appliedAt +
                '}';
    }
}
