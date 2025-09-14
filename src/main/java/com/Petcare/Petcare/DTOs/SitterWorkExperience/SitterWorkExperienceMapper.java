package com.Petcare.Petcare.DTOs.SitterWorkExperience;

import com.Petcare.Petcare.Models.SitterProfile;
import com.Petcare.Petcare.Models.SitterWorkExperience;

/**
 * Clase de utilidad (utility class) para mapear entre la entidad {@link SitterWorkExperience} y sus DTOs asociados.
 * <p>
 * Esta clase centraliza toda la lógica de conversión de datos, promoviendo el principio de
 * responsabilidad única y manteniendo las capas de servicio y controladores limpias de esta tarea.
 * Al ser una clase de utilidad con métodos estáticos, no está diseñada para ser instanciada.
 *
 * @see SitterWorkExperience
 * @see SitterWorkExperienceRequestDTO
 * @see SitterWorkExperienceResponseDTO
 * @see SitterWorkExperienceSummaryDTO
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 */
public final class SitterWorkExperienceMapper {

    /**
     * Constructor privado para prevenir la instanciación de esta clase de utilidad.
     */
    private SitterWorkExperienceMapper() {
        // Esta clase no debe ser instanciada.
    }

    /**
     * Convierte un DTO de solicitud (`SitterWorkExperienceRequestDTO`) a una entidad {@link SitterWorkExperience}.
     * <p>
     * Este método se utiliza en las operaciones de creación y actualización. Toma los datos crudos
     * de la API y los usa para poblar una nueva entidad JPA, asociándola con su perfil de cuidador padre.
     *
     * @param dto El DTO de origen con los datos proporcionados por el cliente. No debe ser nulo.
     * @param sitterProfile La entidad {@link SitterProfile} padre a la que se vinculará esta experiencia. No debe ser nula.
     * @return Una nueva entidad {@link SitterWorkExperience}, lista para ser persistida. Devuelve {@code null} si el DTO de entrada es nulo.
     */
    public static SitterWorkExperience toEntity(SitterWorkExperienceRequestDTO dto, SitterProfile sitterProfile) {
        if (dto == null) {
            return null;
        }

        SitterWorkExperience entity = new SitterWorkExperience();
        entity.setSitterProfile(sitterProfile); // La relación es crucial
        entity.setCompanyName(dto.getCompanyName());
        entity.setJobTitle(dto.getJobTitle());
        entity.setResponsibilities(dto.getResponsibilities());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        return entity;
    }

    /**
     * Convierte una entidad {@link SitterWorkExperience} a su DTO de respuesta detallado.
     * <p>
     * Este mapeo se utiliza cuando se necesita devolver todos los detalles de un registro de
     * experiencia laboral, incluyendo campos extensos como las responsabilidades.
     *
     * @param entity La entidad {@link SitterWorkExperience} de origen, obtenida de la base de datos.
     * @return Un {@link SitterWorkExperienceResponseDTO} con los datos completos. Devuelve {@code null} si la entidad de entrada es nula.
     */
    public static SitterWorkExperienceResponseDTO toResponseDTO(SitterWorkExperience entity) {
        if (entity == null) {
            return null;
        }

        return new SitterWorkExperienceResponseDTO(
                entity.getId(),
                entity.getCompanyName(),
                entity.getJobTitle(),
                entity.getResponsibilities(),
                entity.getStartDate(),
                entity.getEndDate()
        );
    }

    /**
     * Convierte una entidad {@link SitterWorkExperience} a su DTO de resumen.
     * <p>
     * Este mapeo está optimizado para listados. Excluye campos más pesados como
     * {@code responsibilities} para mantener la carga útil (payload) de la respuesta de la API
     * lo más ligera y eficiente posible.
     *
     * @param entity La entidad {@link SitterWorkExperience} de origen, obtenida de la base de datos.
     * @return Un {@link SitterWorkExperienceSummaryDTO} ligero. Devuelve {@code null} si la entidad de entrada es nula.
     */
    public static SitterWorkExperienceSummaryDTO toSummaryDTO(SitterWorkExperience entity) {
        if (entity == null) {
            return null;
        }

        return new SitterWorkExperienceSummaryDTO(
                entity.getId(),
                entity.getCompanyName(),
                entity.getJobTitle(),
                entity.getStartDate(),
                entity.getEndDate()
        );
    }
}