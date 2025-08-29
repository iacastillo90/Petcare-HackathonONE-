package com.Petcare.Petcare.DTOs.PlatformFee;

import com.Petcare.Petcare.Models.PlatformFee;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para transferir información de tarifas de plataforma.
 *
 * <p>Este DTO se usa para exponer datos de {@link com.Petcare.Petcare.Models.PlatformFee}
 * sin exponer la estructura interna de las entidades JPA.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 */
public class PlatformFeeDTO {

    private Long id;

    /**
     * ID de la reserva asociada en lugar de la entidad completa.
     */
    @NotNull(message = "El ID de la reserva es obligatorio")
    private Long bookingId;

    /**
     * Monto base sobre el cual se calcula la comisión.
     */
    @NotNull(message = "El monto base no puede ser nulo")
    @DecimalMin(value = "0.0", message = "El monto base debe ser positivo")
    @Digits(integer = 10, fraction = 2, message = "El monto base debe tener máximo 10 dígitos y 2 decimales")
    private BigDecimal baseAmount;

    /**
     * Porcentaje de la comisión aplicado.
     */
    @NotNull(message = "El porcentaje de la tarifa no puede ser nulo")
    @DecimalMin(value = "0.0", message = "El porcentaje debe ser positivo")
    @Digits(integer = 5, fraction = 2, message = "El porcentaje debe tener máximo 5 dígitos y 2 decimales")
    private BigDecimal feePercentage;

    /**
     * Monto exacto de la comisión cobrada por la plataforma.
     */
    @NotNull(message = "El monto de la tarifa no puede ser nulo")
    @DecimalMin(value = "0.0", message = "El monto de la tarifa debe ser positivo")
    @Digits(integer = 10, fraction = 2, message = "El monto de la tarifa debe tener máximo 10 dígitos y 2 decimales")
    private BigDecimal feeAmount;

    /**
     * Monto neto que corresponde al cuidador.
     */
    @NotNull(message = "El monto neto no puede ser nulo")
    @DecimalMin(value = "0.0", message = "El monto neto debe ser positivo")
    @Digits(integer = 10, fraction = 2, message = "El monto neto debe tener máximo 10 dígitos y 2 decimales")
    private BigDecimal netAmount;

    /**
     * Fecha y hora de creación del registro.
     */
    private LocalDateTime createdAt;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido para frameworks de serialización.
     */
    public PlatformFeeDTO() {
    }

    /**
     * Constructor para crear DTO desde entidad PlatformFee.
     */
    public PlatformFeeDTO(PlatformFee platformFee) {
        this.id = platformFee.getId();
        this.bookingId = platformFee.getBooking() != null ? platformFee.getBooking().getId() : null;
        this.baseAmount = platformFee.getBaseAmount();
        this.feePercentage = platformFee.getFeePercentage();
        this.feeAmount = platformFee.getFeeAmount();
        this.netAmount = platformFee.getNetAmount();
        this.createdAt = platformFee.getCreatedAt();
    }

    /**
     * Constructor completo para testing y casos específicos.
     */
    public PlatformFeeDTO(Long id, Long bookingId, BigDecimal baseAmount,
                          BigDecimal feePercentage, BigDecimal feeAmount,
                          BigDecimal netAmount, LocalDateTime createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.baseAmount = baseAmount;
        this.feePercentage = feePercentage;
        this.feeAmount = feeAmount;
        this.netAmount = netAmount;
        this.createdAt = createdAt;
    }

    // ========== GETTERS ==========

    public Long getId() {
        return id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public BigDecimal getFeePercentage() {
        return feePercentage;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    // ========== MÉTODO DE UTILIDAD ==========

    /**
     * Convierte este DTO a una nueva entidad PlatformFee.
     * Útil para operaciones de creación.
     */
    public PlatformFee toEntity() {
        PlatformFee platformFee = new PlatformFee();
        platformFee.setId(this.id);
        // Nota: La entidad Booking debe ser asignada externamente
        platformFee.setBaseAmount(this.baseAmount);
        platformFee.setFeePercentage(this.feePercentage);
        platformFee.setFeeAmount(this.feeAmount);
        platformFee.setNetAmount(this.netAmount);
        platformFee.setCreatedAt(this.createdAt);
        return platformFee;
    }

    @Override
    public String toString() {
        return "PlatformFeeDTO{" +
                "id=" + id +
                ", bookingId=" + bookingId +
                ", feeAmount=" + feeAmount +
                ", netAmount=" + netAmount +
                ", createdAt=" + createdAt +
                '}';
    }
}