package com.Petcare.Petcare.DTOs.Sitter;

import com.Petcare.Petcare.Models.SitterProfile;
import com.Petcare.Petcare.Models.User.User;

import java.math.BigDecimal;

/**
 * DTO que representa un resumen optimizado del perfil de un cuidador (Sitter).
 *
 * <p>Este DTO está diseñado para ser ligero y eficiente, ideal para su uso en listados,
 * resultados de búsqueda o cualquier vista previa donde se necesite la información más
 * relevante para el cliente sin la sobrecarga de datos completos. Su objetivo principal
 * es mejorar el rendimiento de la API y simplificar el trabajo del frontend.</p>
 *
 * <p><b>Características Clave:</b></p>
 * <ul>
 * <li><b>Inmutabilidad:</b> Al ser un {@code record}, sus datos son inmutables,
 * lo que garantiza la consistencia de la información una vez creada.</li>
 * <li><b>Datos Denormalizados:</b> Incluye campos como {@code sitterName} y {@code location}
 * directamente, que se obtienen de la entidad {@link User} asociada. Esto evita que el
 * cliente de la API tenga que hacer consultas adicionales o manejar objetos anidados complejos.</li>
 * <li><b>Contrato Explícito:</b> Define claramente la estructura de datos que la API
 * devolverá para las listas de cuidadores.</li>
 * </ul>
 *
 * @param id El identificador único del perfil del cuidador.
 * @param sitterName El nombre completo del cuidador (obtenido de la entidad {@code User}).
 * @param profileImageUrl La URL de la imagen de perfil del cuidador.
 * @param hourlyRate La tarifa base por hora que cobra el cuidador.
 * @param averageRating La calificación promedio del cuidador, calculada a partir de las reseñas.
 * @param isVerified Indica si el perfil del cuidador ha sido verificado por la plataforma.
 * @param location La ciudad o ubicación principal del cuidador, para dar contexto en la búsqueda.
 *
 * @see SitterProfile
 * @see SitterProfileDTO
 * @author Equipo Petcare
 * @version 1.0
 * @since 1.0
 */
public record SitterProfileSummary(
        Long id,
        String sitterName,
        String profileImageUrl,
        BigDecimal hourlyRate,
        BigDecimal averageRating,
        boolean isVerified,
        String location
) {
    /**
     * Método de fábrica estático para convertir una entidad {@link SitterProfile} a este DTO de resumen.
     *
     * <p>Este es el mecanismo preferido para crear instancias de {@code SitterProfileSummary},
     * ya que centraliza la lógica de mapeo y asegura que los datos denormalizados (como el nombre
     * y la dirección del usuario) se extraigan correctamente de la entidad anidada {@code User}.</p>
     *
     * <p>Maneja de forma segura el caso en que el perfil o el usuario asociado sean nulos para
     * prevenir {@code NullPointerException}.</p>
     *
     * @param profile La entidad {@link SitterProfile} de origen, que debe tener su entidad {@code User} asociada cargada.
     * @return Una nueva instancia de {@code SitterProfileSummary} con los datos mapeados,
     * o {@code null} si el perfil de entrada o su usuario asociado son nulos.
     * @apiNote En una implementación más robusta, se podría considerar lanzar una excepción
     * en lugar de devolver {@code null} si los datos de entrada son inconsistentes.
     */
    public static SitterProfileSummary fromEntity(SitterProfile profile) {
        if (profile == null || profile.getUser() == null) {
            // Se retorna null para evitar errores si un perfil está incompleto en la BD.
            // El servicio que llama a este método debe manejar este caso.
            return null;
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