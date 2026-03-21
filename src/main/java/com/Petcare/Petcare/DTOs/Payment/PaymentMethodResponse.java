package com.Petcare.Petcare.DTOs.Payment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta con información completa de un método de pago.
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 */
@Schema(description = "DTO para la respuesta con información completa de un método de pago.")
public record PaymentMethodResponse(
        @Schema(description = "Identificador único del método de pago.", example = "1")
        Long id,

        @Schema(description = "Identificador de la cuenta asociada.", example = "1")
        Long accountId,

        @Schema(description = "Tipo de tarjeta.", example = "VISA")
        String cardType,

        @Schema(description = "Últimos 4 dígitos de la tarjeta.", example = "1234")
        String lastFourDigits,

        @Schema(description = "Fecha de expiración (MM/YY).", example = "12/25")
        String expiryDate,

        @Schema(description = "Indica si es el método de pago por defecto.", example = "true")
        boolean isDefault,

        @Schema(description = "Indica si el método de pago ha sido verificado.", example = "true")
        boolean isVerified,

        @Schema(description = "Indica si el método de pago está activo.", example = "true")
        boolean isActive,

        @Schema(description = "Fecha y hora de creación.", example = "2025-02-10T15:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Fecha y hora de la última actualización.", example = "2025-02-10T15:30:00")
        LocalDateTime updatedAt
) {
}
