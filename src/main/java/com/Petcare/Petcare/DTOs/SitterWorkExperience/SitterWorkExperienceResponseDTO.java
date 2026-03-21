package com.Petcare.Petcare.DTOs.SitterWorkExperience;

import com.Petcare.Petcare.Models.SitterWorkExperience;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * DTO para las respuestas de la API que contienen los detalles completos de una experiencia laboral.
 *
 * <p>Esta clase se utiliza como el cuerpo de la respuesta para los endpoints que devuelven un
 * registro único y detallado de {@link com.Petcare.Petcare.Models.SitterWorkExperience}.
 * A diferencia del DTO de resumen, este incluye todos los campos relevantes, como
 * el texto completo de las responsabilidades.</p>
 *
 * @see com.Petcare.Petcare.Controllers.SitterWorkExperienceController
 * @see com.Petcare.Petcare.Models.SitterWorkExperience
 * @see SitterWorkExperienceSummaryDTO
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 */
@Schema(description = "DTO para las respuestas con los detalles completos de una experiencia laboral.")
public record SitterWorkExperienceResponseDTO(
        @Schema(description = "El identificador único de la entrada de experiencia laboral.", example = "1")
        Long id,

        @Schema(description = "Nombre de la empresa, organización o lugar donde se adquirió la experiencia.", example = "Clínica Veterinaria Amigos Fieles")
        String companyName,

        @Schema(description = "El título del puesto o el rol desempeñado.", example = "Paseador Canino Profesional")
        String jobTitle,

        @Schema(description = "Una descripción detallada de las tareas, logros y responsabilidades del puesto.")
        String responsibilities,

        @Schema(description = "Fecha en que comenzó la experiencia laboral.", example = "2020-01-15")
        LocalDate startDate,

        @Schema(description = "Fecha en que finalizó la experiencia laboral. Es nulo si es el trabajo actual.", example = "2023-06-30")
        LocalDate endDate
) {

    /**
     * Crea una instancia de SitterWorkExperienceResponseDTO desde una entidad SitterWorkExperience.
     *
     * @param entity la entidad SitterWorkExperience a convertir
     * @return nueva instancia de SitterWorkExperienceResponseDTO con datos poblados
     * @throws IllegalArgumentException si entity es null
     */
    public static SitterWorkExperienceResponseDTO fromEntity(SitterWorkExperience entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad SitterWorkExperience no puede ser null");
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
     * Verifica si la experiencia laboral es actual (sin fecha de fin).
     *
     * @return true si endDate es null
     */
    public boolean isCurrentJob() {
        return endDate == null;
    }
}
