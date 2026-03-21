package com.Petcare.Petcare.DTOs.PlatformFee;

import com.Petcare.Petcare.Models.PlatformFee;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para transferir información de tarifas de plataforma.
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see com.Petcare.Petcare.Models.PlatformFee
 */
@Schema(description = "DTO para transferir información de tarifas de plataforma.")
public record PlatformFeeDTO(
        @Schema(description = "Identificador único de la tarifa.", example = "1")
        Long id,

        @Schema(description = "ID de la reserva asociada.", example = "1")
        Long bookingId,

        @Schema(description = "Monto base sobre el cual se calcula la comisión.", example = "100.00")
        BigDecimal baseAmount,

        @Schema(description = "Porcentaje de la comisión aplicado.", example = "10.00")
        BigDecimal feePercentage,

        @Schema(description = "Monto exacto de la comisión cobrada por la plataforma.", example = "10.00")
        BigDecimal feeAmount,

        @Schema(description = "Monto neto que corresponde al cuidador.", example = "90.00")
        BigDecimal netAmount,

        @Schema(description = "Fecha y hora de creación del registro.", example = "2025-01-15T10:30:00")
        LocalDateTime createdAt
) {

    /**
     * Constructor para crear DTO desde entidad PlatformFee.
     */
    public PlatformFeeDTO(PlatformFee platformFee) {
        this(
                platformFee.getId(),
                platformFee.getBooking() != null ? platformFee.getBooking().getId() : null,
                platformFee.getBaseAmount(),
                platformFee.getFeePercentage(),
                platformFee.getFeeAmount(),
                platformFee.getNetAmount(),
                platformFee.getCreatedAt()
        );
    }

    /**
     * Convierte este DTO a una nueva entidad PlatformFee.
     */
    public PlatformFee toEntity() {
        PlatformFee platformFee = new PlatformFee();
        platformFee.setId(this.id);
        platformFee.setBaseAmount(this.baseAmount);
        platformFee.setFeePercentage(this.feePercentage);
        platformFee.setFeeAmount(this.feeAmount);
        platformFee.setNetAmount(this.netAmount);
        platformFee.setCreatedAt(this.createdAt);
        return platformFee;
    }
}
