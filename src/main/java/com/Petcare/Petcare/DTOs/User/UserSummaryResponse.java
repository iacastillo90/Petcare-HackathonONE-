package com.Petcare.Petcare.DTOs.User;

import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * DTO para representar una vista resumida y optimizada de un usuario.
 *
 * <p>Este DTO está diseñado específicamente para listados y vistas donde se
 * requiere información esencial sin la sobrecarga de datos completos. Optimiza
 * el rendimiento al transferir solo los campos más relevantes para identificación
 * y toma de decisiones rápidas.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.3
 * @since 1.0
 * @see User
 * @see UserResponse
 */
@Schema(description = "DTO con información resumida de un usuario, optimizado para listados y vistas generales.")
public record UserSummaryResponse(
        @Schema(description = "Identificador único del usuario.", example = "1")
        Long id,

        @Schema(description = "Nombre completo del usuario, formateado para visualización.", example = "John Doe")
        String fullName,

        @Schema(description = "Dirección de correo electrónico del usuario, usada como identificador principal.", example = "john.doe@example.com")
        String email,

        @Schema(description = "Rol del usuario en el sistema, que define sus permisos base.", example = "CLIENT")
        Role role,

        @Schema(description = "Indica si la cuenta del usuario está activa y puede operar en el sistema.", example = "true")
        boolean isActive,

        @Schema(description = "Indica si el correo electrónico del usuario ha sido verificado.", example = "true")
        boolean emailVerified,

        @Schema(description = "Fecha y hora de creación de la cuenta del usuario.", example = "2025-02-10T15:25:00Z")
        LocalDateTime createdAt
) {

    /**
     * Crea una instancia de UserSummaryResponse desde una entidad User.
     * <p>
     * Este método de fábrica centraliza la lógica de mapeo, asegurando consistencia
     * y extrayendo solo la información esencial para esta vista resumida.
     * </p>
     *
     * @param user la entidad User a convertir. No puede ser nula.
     * @return una nueva instancia de UserSummaryResponse.
     * @throws IllegalArgumentException si el objeto user es nulo.
     */
    public static UserSummaryResponse fromEntity(User user) {
        if (user == null) {
            throw new IllegalArgumentException("La entidad User no puede ser null para la conversión a DTO.");
        }
        return new UserSummaryResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.isEmailVerified(),
                user.getCreatedAt()
        );
    }
}
