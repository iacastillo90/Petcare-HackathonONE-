package com.Petcare.Petcare.DTOs.Payment;

import jakarta.validation.constraints.*;
import java.io.Serializable;

/**
 * DTO para crear o actualizar un método de pago.
 * Contiene los campos necesarios para la creación/actualización desde el cliente.
 * No incluye campos generados por el servidor como id, createdAt o updatedAt.
 */
public class PaymentMethodDTO implements Serializable {

    @NotNull(message = "El accountId es obligatorio")
    private Long accountId;

    @NotBlank(message = "El tipo de tarjeta es obligatorio")
    @Size(max = 50, message = "El tipo de tarjeta no puede exceder 50 caracteres")
    private String cardType;

    @NotBlank(message = "Los últimos 4 dígitos son obligatorios")
    @Size(min = 4, max = 4, message = "Deben ser exactamente 4 dígitos")
    @Pattern(regexp = "^\\d{4}$", message = "Los últimos dígitos deben ser 4 números")
    private String lastFourDigits;

    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "La fecha de expiración debe tener formato MM/YY")
    private String expiryDate;

    @NotBlank(message = "El token de la pasarela es obligatorio")
    @Size(max = 255, message = "El token no puede exceder 255 caracteres")
    private String gatewayToken;

    /** Indica si se crea como método por defecto. Por defecto false. */
    private boolean isDefault = false;

    public PaymentMethodDTO() {}

    // Getters y setters
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public String getLastFourDigits() { return lastFourDigits; }
    public void setLastFourDigits(String lastFourDigits) { this.lastFourDigits = lastFourDigits; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getGatewayToken() { return gatewayToken; }
    public void setGatewayToken(String gatewayToken) { this.gatewayToken = gatewayToken; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
}
