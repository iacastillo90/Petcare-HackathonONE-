package com.Petcare.Petcare.DTOs.SitterWorkExperience;

import com.Petcare.Petcare.Models.SitterWorkExperience;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * DTO que representa un resumen optimizado de una experiencia laboral para listados.
 *
 * <p>Esta clase está diseñada para ser una versión ligera de la experiencia laboral,
 * ideal para su uso en respuestas de API que devuelven listas de múltiples registros.
 * Excluye deliberadamente campos con contenido extenso (como {@code responsibilities})
 * para reducir el tamaño de la carga útil (payload) y mejorar el rendimiento.</p>
 *
 * @see com.Petcare.Petcare.Controllers.SitterWorkExperienceController
 * @see com.Petcare.Petcare.Models.SitterWorkExperience
 * @see SitterWorkExperienceResponseDTO
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 */
@Schema(description = "DTO de resumen optimizado de una experiencia laboral para listados.")
public record SitterWorkExperienceSummaryDTO(
        @Schema(description = "El identificador único de la entrada de experiencia laboral.", example = "1")
        Long id,

        @Schema(description = "Nombre de la empresa, organización o lugar donde se adquirió la experiencia.", example = "Clínica Veterinaria Amigos Fieles")
        String companyName,

        @Schema(description = "El título del puesto o el rol desempeñado.", example = "Paseador Canino Profesional")
        String jobTitle,

        @Schema(description = "Fecha en que comenzó la experiencia laboral.", example = "2020-01-15")
        LocalDate startDate,

        @Schema(description = "Fecha en que finalizó la experiencia laboral. Es nulo si es el trabajo actual.", example = "2023-06-30")
        LocalDate endDate
) {

    /**
     * Crea una instancia de SitterWorkExperienceSummaryDTO desde una entidad SitterWorkExperience.
     *
     * @param entity la entidad SitterWorkExperience a convertir
     * @return nueva instancia de SitterWorkExperienceSummaryDTO con datos poblados
     * @throws IllegalArgumentException si entity es null
     */
    public static SitterWorkExperienceSummaryDTO fromEntity(SitterWorkExperience entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad SitterWorkExperience no puede ser null");
        }
        return new SitterWorkExperienceSummaryDTO(
                entity.getId(),
                entity.getCompanyName(),
                entity.getJobTitle(),
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
