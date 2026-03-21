package com.Petcare.Petcare.DTOs.Sitter;

import com.Petcare.Petcare.Models.SitterProfile;
import com.Petcare.Petcare.Models.User.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * DTO para transferir la información completa de un perfil de cuidador (Sitter).
 *
 * @author Equipo Petcare 10
 * @version 2.0
 * @since 1.0
 * @see SitterProfile
 */
@Schema(description = "DTO para transferir la información completa de un perfil de cuidador (Sitter).")
public record SitterProfileDTO(
        @Schema(description = "El identificador único del perfil de cuidador.", example = "1")
        Long id,

        @Schema(description = "El ID del usuario al que está asociado este perfil.", example = "1")
        Long userId,

        @Schema(description = "La biografía profesional del cuidador.", example = "Cuidador experimentado con más de 5 años...")
        String bio,

        @Schema(description = "La tarifa por hora que el cuidador cobra por sus servicios básicos.", example = "25.00")
        BigDecimal hourlyRate,

        @Schema(description = "El radio de servicio en kilómetros que el cuidador está dispuesto a cubrir.", example = "10")
        Integer servicingRadius,

        @Schema(description = "La URL de la imagen de perfil profesional del cuidador.", example = "https://example.com/profile.jpg")
        String profileImageUrl,

        @Schema(description = "Indica si el perfil ha sido verificado por la plataforma Petcare.", example = "true")
        boolean verified,

        @Schema(description = "Indica si el cuidador está actualmente disponible para aceptar nuevas reservas.", example = "true")
        boolean availableForBookings
) {

    /**
     * Crea una instancia de SitterProfileDTO desde una entidad SitterProfile.
     *
     * @param profile la entidad SitterProfile a convertir
     * @return nueva instancia de SitterProfileDTO con datos poblados
     * @throws IllegalArgumentException si profile es null
     */
    public static SitterProfileDTO fromEntity(SitterProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("La entidad SitterProfile no puede ser null");
        }
        return new SitterProfileDTO(
                profile.getId(),
                profile.getUser() != null ? profile.getUser().getId() : null,
                profile.getBio(),
                profile.getHourlyRate(),
                profile.getServicingRadius(),
                profile.getProfileImageUrl(),
                profile.isVerified(),
                profile.isAvailableForBookings()
        );
    }
}
