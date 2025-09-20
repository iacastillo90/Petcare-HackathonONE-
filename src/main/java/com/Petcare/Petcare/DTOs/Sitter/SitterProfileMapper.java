package com.Petcare.Petcare.DTOs.Sitter;

import com.Petcare.Petcare.Models.SitterProfile;
import com.Petcare.Petcare.Models.User.User;
import org.springframework.stereotype.Component;

/**
 * Componente de Spring responsable de mapear la entidad {@link SitterProfile} a sus DTOs correspondientes.
 *
 * <p>Esta clase centraliza la lógica de conversión entre el modelo de dominio (entidad JPA) y los
 * objetos de transferencia de datos (DTOs) que se exponen en la API. Al seguir el patrón Mapper,
 * se promueve el principio de responsabilidad única, manteniendo los servicios y controladores
 * limpios de lógica de transformación de datos.</p>
 *
 * <p>Al ser un {@code @Component}, puede ser inyectado en cualquier otra clase gestionada por
 * Spring, como los servicios que necesitan realizar estas conversiones.</p>
 *
 * @see SitterProfile
 * @see SitterProfileSummary
 * @author Equipo Petcare
 * @version 1.0
 * @since 1.0
 */
@Component
public class SitterProfileMapper {

    /**
     * Convierte una entidad {@link SitterProfile} a su DTO de resumen, {@link SitterProfileSummary}.
     *
     * <p>Este método toma la entidad completa del perfil y extrae únicamente los campos necesarios
     * para vistas de lista o resultados de búsqueda. Realiza una "denormalización" al aplanar
     * datos del {@link User} anidado (como {@code fullName} y {@code address}) directamente en el DTO
     * para simplificar su consumo en el frontend.</p>
     *
     * <p>Incluye una validación robusta para asegurar que tanto el perfil como el usuario asociado
     * no sean nulos, previniendo errores por datos inconsistentes.</p>
     *
     * @param profile La entidad {@link SitterProfile} a convertir. Se espera que la entidad
     * {@code User} asociada esté cargada (no sea lazy) y no sea nula.
     * @return Una nueva instancia de {@link SitterProfileSummary} con los datos relevantes mapeados.
     * @throws IllegalArgumentException si el perfil o el usuario asociado son nulos, lo que indica
     * un estado de datos inconsistente que debe ser manejado.
     */
    public SitterProfileSummary toSummaryDto(SitterProfile profile) {
        if (profile == null || profile.getUser() == null) {
            // Es crucial manejar casos nulos para evitar NullPointerException.
            // Lanzar una excepción es una estrategia robusta para señalar datos corruptos o inesperados.
            throw new IllegalArgumentException("El perfil del cuidador o su usuario asociado es nulo.");
        }

        User sitterUser = profile.getUser();

        // Extraemos los datos del CUIDADOR y su PERFIL para construir el DTO de resumen.
        return new SitterProfileSummary(
                profile.getId(),
                sitterUser.getFullName(),
                profile.getProfileImageUrl(),
                profile.getHourlyRate(),
                profile.getAverageRating(),
                profile.isVerified(),
                sitterUser.getAddress() // o un campo más específico como user.getCity() si lo tuvieras.
        );
    }
}