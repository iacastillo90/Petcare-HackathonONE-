package com.Petcare.Petcare.DTOs.Sitter;

import com.Petcare.Petcare.Models.SitterProfile;

import java.math.BigDecimal;

/**
 * DTO que representa un resumen del perfil de un cuidador.
 *
 * <p>Diseñado para ser utilizado en listas y vistas previas donde solo
 * se necesita la información más relevante para el cliente, optimizando
 * el rendimiento y manteniendo un contrato de API limpio.</p>
 *
 * @param id El identificador único del perfil.
 * @param sitterName El nombre completo del cuidador (obtenido de la entidad User).
 * @param profileImageUrl La URL de la imagen de perfil.
 * @param hourlyRate La tarifa por hora del cuidador.
 * @param averageRating La calificación promedio.
 * @param isVerified Indica si el perfil está verificado.
 * @param location La ciudad o ubicación principal del cuidador para contexto en la búsqueda.
 */
public record SitterProfileSummary(
        Long id,
        String sitterName,
        String profileImageUrl,
        BigDecimal hourlyRate,
        BigDecimal averageRating,
        boolean isVerified,
        String location // Campo adicional sugerido para la UI
) {
    public static SitterProfileSummary fromEntity(SitterProfile profile) {
        if (profile == null || profile.getUser() == null) {
            return null; // O manejar el error como prefieras
        }
        return new SitterProfileSummary(
                profile.getId(),
                profile.getUser().getFullName(),
                profile.getProfileImageUrl(),
                profile.getHourlyRate(),
                profile.getAverageRating(),
                profile.getUser().isEmailVerified(),
                profile.getUser().getAddress()
        );
    }
}