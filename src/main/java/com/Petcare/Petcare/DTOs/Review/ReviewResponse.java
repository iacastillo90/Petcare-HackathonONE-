package com.Petcare.Petcare.DTOs.Review;

import com.Petcare.Petcare.Models.Review;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de la API al obtener una reseña.
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see Review
 */
@Schema(description = "DTO para la respuesta de la API al obtener una reseña.")
public record ReviewResponse(
        @Schema(description = "Identificador único de la reseña.", example = "1")
        Long id,

        @Schema(description = "Identificador del usuario que creó la reseña.", example = "1")
        Long userId,

        @Schema(description = "Identificador de la mascota reseñada.", example = "1")
        Long petId,

        @Schema(description = "Puntuación de 1 a 5 estrellas.", example = "5")
        Integer rating,

        @Schema(description = "Comentario de la reseña.", example = "Excelente servicio de cuidado.")
        String comment,

        @Schema(description = "Fecha y hora de creación de la reseña.", example = "2025-02-10T15:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Fecha y hora de la última actualización.", example = "2025-02-10T15:30:00")
        LocalDateTime updatedAt
) {

    /**
     * Crea una instancia de ReviewResponse desde una entidad Review.
     *
     * @param review la entidad Review a convertir
     * @return nueva instancia de ReviewResponse con datos poblados
     * @throws IllegalArgumentException si review es null
     */
    public static ReviewResponse fromEntity(Review review) {
        if (review == null) {
            throw new IllegalArgumentException("La entidad Review no puede ser null");
        }
        return new ReviewResponse(
                review.getId(),
                review.getUserId(),
                review.getPetId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
