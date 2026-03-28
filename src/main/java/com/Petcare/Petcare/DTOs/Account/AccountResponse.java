package com.Petcare.Petcare.DTOs.Account;

import com.Petcare.Petcare.Models.Account.Account;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para representar información de cuenta en respuestas de API.
 *
 * @author Equipo Petcare 10
 * @version 1.2
 * @since 1.0
 * @see Account
 */
@Schema(description = "DTO con la información completa de una cuenta de usuario.")
public record AccountResponse(
        @Schema(description = "Identificador único de la cuenta.", example = "1")
        Long id,

        @Schema(description = "Número único de identificación de la cuenta.", example = "ACC-2024-001234")
        String accountNumber,

        @Schema(description = "Nombre descriptivo de la cuenta.", example = "Familia Pérez")
        String accountName,

        @Schema(description = "Saldo actual de la cuenta.", example = "150.00")
        BigDecimal balance,

        @Schema(description = "Código de moneda ISO 4217.", example = "USD")
        String currency,

        @Schema(description = "Indica si la cuenta está activa.", example = "true")
        boolean isActive,

        @Schema(description = "Fecha y hora de creación de la cuenta.", example = "2025-02-10T15:25:00Z")
        LocalDateTime createdAt,

        @Schema(description = "Fecha y hora de la última actualización.", example = "2025-02-10T15:25:00Z")
        LocalDateTime updatedAt
) {

    /**
     * Convierte una entidad del modelo {@link Account} a un DTO {@link AccountResponse}.
     *
     * @param account la entidad Account a convertir.
     * @return nueva instancia de AccountResponse con datos poblados.
     * @throws IllegalArgumentException si el {@code account} proporcionado es nulo.
     */
    public static AccountResponse fromEntity(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("La entidad Account no puede ser null");
        }
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountName(),
                account.getBalance(),
                account.getCurrency(),
                account.isActive(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    /**
     * Verifica si la cuenta puede realizar nuevas reservas.
     *
     * @return true si la cuenta está activa.
     */
    public boolean canMakeBookings() {
        return isActive;
    }

    /**
     * Verifica si la cuenta tiene saldo positivo.
     *
     * @return true si el saldo es mayor a cero.
     */
    public boolean hasPositiveBalance() {
        return balance != null && balance.compareTo(BigDecimal.ZERO) > 0;
    }
}
