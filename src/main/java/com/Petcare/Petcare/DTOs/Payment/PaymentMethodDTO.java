package com.Petcare.Petcare.DTOs.Payment;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para crear o actualizar un método de pago.
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 */
@Schema(description = "DTO para crear o actualizar un método de pago.")
public record PaymentMethodDTO(
        @Schema(description = "El ID de la cuenta asociada.", example = "1")
        Long accountId,

        @Schema(description = "El tipo de tarjeta.", example = "VISA")
        String cardType,

        @Schema(description = "Los últimos 4 dígitos de la tarjeta.", example = "1234")
        String lastFourDigits,

        @Schema(description = "La fecha de expiración (MM/YY).", example = "12/25")
        String expiryDate,

        @Schema(description = "El token de la pasarela de pago.", example = "tok_xxxxxxxxxxxxx")
        String gatewayToken,

        @Schema(description = "Indica si se crea como método por defecto.", example = "false")
        boolean isDefault
) {
}
