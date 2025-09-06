package com.Petcare.Petcare.DTOs.Payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para recibir datos al crear o actualizar un método de pago.
 */
@Data
public class PaymentMethodRequest {

    @NotBlank(message = "El tipo de tarjeta es obligatorio")
    @Size(max = 50, message = "El tipo de tarjeta no puede exceder 50 caracteres")
    private String cardType;

    @NotBlank(message = "Los últimos 4 dígitos son obligatorios")
    @Pattern(regexp = "^\\d{4}$", message = "Los últimos dígitos deben ser 4 números")
    private String lastFourDigits;

    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "La fecha de expiración debe tener formato MM/YY")
    private String expiryDate;

    @NotBlank(message = "El token de la pasarela es obligatorio")
    private String gatewayToken;
}
