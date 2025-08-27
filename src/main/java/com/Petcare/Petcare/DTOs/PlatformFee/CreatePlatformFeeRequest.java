package com.Petcare.Petcare.DTOs.PlatformFee;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * DTO para la petición de cálculo y creación de una tarifa de plataforma.
 */
public class CreatePlatformFeeRequest {

    @NotNull(message = "El ID de la reserva es obligatorio")
    private Long bookingId;

    @NotNull(message = "El porcentaje de la tarifa es obligatorio")
    @Positive(message = "El porcentaje debe ser positivo")
    private BigDecimal feePercentage;

    // ========== GETTERS Y SETTERS ==========


    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public BigDecimal getFeePercentage() {
        return feePercentage;
    }

    public void setFeePercentage(BigDecimal feePercentage) {
        this.feePercentage = feePercentage;
    }
}