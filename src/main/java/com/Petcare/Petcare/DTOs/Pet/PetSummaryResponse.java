package com.Petcare.Petcare.DTOs.Pet;

import com.Petcare.Petcare.Models.Pet;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO optimizado para listados y resúmenes de mascotas.
 *
 * <p>Esta versión simplificada del DTO de mascota está diseñada para operaciones
 * de listado masivo y respuestas que requieren optimización de rendimiento.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see Pet
 * @see PetResponse
 */
@Schema(description = "DTO optimizado para listados y resúmenes de mascotas.")
public record PetSummaryResponse(
        @Schema(description = "Identificador único de la mascota.", example = "1")
        Long id,

        @Schema(description = "ID de la cuenta propietaria.", example = "1")
        Long accountId,

        @Schema(description = "Nombre de la cuenta propietaria.", example = "Cuenta García")
        String accountName,

        @Schema(description = "Nombre de la mascota.", example = "Fido")
        String name,

        @Schema(description = "Especie de la mascota.", example = "Perro")
        String species,

        @Schema(description = "Raza de la mascota.", example = "Labrador")
        String breed,

        @Schema(description = "Edad de la mascota en años.", example = "3")
        Integer age,

        @Schema(description = "Estado de actividad de la mascota.", example = "true")
        boolean isActive,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "Fecha y hora de creación.", example = "2025-01-15T10:30:00")
        LocalDateTime createdAt
) {

    /**
     * Método de fábrica estático para convertir una entidad Pet a su DTO de resumen.
     *
     * @param pet La entidad Pet a convertir
     * @return PetSummaryResponse DTO con información resumida, o null si pet es null
     */
    public static PetSummaryResponse fromEntity(Pet pet) {
        if (pet == null) {
            return null;
        }
        return new PetSummaryResponse(
                pet.getId(),
                pet.getAccount() != null ? pet.getAccount().getId() : null,
                pet.getAccount() != null ? pet.getAccount().getAccountName() : null,
                pet.getName(),
                pet.getSpecies(),
                pet.getBreed(),
                pet.getAge(),
                pet.isActive(),
                pet.getCreatedAt()
        );
    }
}
