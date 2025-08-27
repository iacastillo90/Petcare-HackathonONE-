package com.Petcare.Petcare.DTOs.PlatformFee;

import com.Petcare.Petcare.Models.PlatformFee;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para las respuestas de la API que contienen información de PlatformFee.
 */
public class PlatformFeeResponse {

    private Long id;
    private Long bookingId;
    private BigDecimal baseAmount;
    private BigDecimal feePercentage;
    private BigDecimal feeAmount;
    private BigDecimal netAmount;
    private LocalDateTime createdAt;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido por JPA.
     */
    public PlatformFeeResponse() {}

    /**
     * Método de fábrica estático para convertir una entidad a DTO.
     */
    public static PlatformFeeResponse fromEntity(PlatformFee entity) {
        if (entity == null) return null;
        PlatformFeeResponse dto = new PlatformFeeResponse();
        dto.setId(entity.getId());
        dto.setBookingId(entity.getBooking() != null ? entity.getBooking().getId() : null);
        dto.setBaseAmount(entity.getBaseAmount());
        dto.setFeePercentage(entity.getFeePercentage());
        dto.setFeeAmount(entity.getFeeAmount());
        dto.setNetAmount(entity.getNetAmount());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

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

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public BigDecimal getFeePercentage() {
        return feePercentage;
    }

    public void setFeePercentage(BigDecimal feePercentage) {
        this.feePercentage = feePercentage;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}