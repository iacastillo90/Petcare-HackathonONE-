package com.Petcare.Petcare.DTOs.Review;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Data Transfer Object para crear o actualizar una reseña.
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 */
@Schema(description = "Data Transfer Object para crear o actualizar una reseña.")
public record ReviewDTO(
        @Schema(description = "El ID del usuario que crea la reseña.", example = "1")
        Long userId,

        @Schema(description = "El ID de la mascota reseñada.", example = "1")
        Long petId,

        @Schema(description = "Puntuación de 1 a 5 estrellas.", example = "5")
        Integer rating,

        @Schema(description = "Comentario de la reseña.", example = "Excelente servicio de cuidado.")
        String comment
) {
}
