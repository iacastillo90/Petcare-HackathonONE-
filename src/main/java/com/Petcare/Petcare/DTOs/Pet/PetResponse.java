package com.Petcare.Petcare.DTOs.Pet;

import com.Petcare.Petcare.Models.Pet;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta de información de una mascota.
 * Incluye todos los campos relevantes de la entidad Pet.
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see Pet
 */
@Schema(description = "DTO para la respuesta de información completa de una mascota.")
public record PetResponse(
        @Schema(description = "Identificador único de la mascota.", example = "1")
        Long id,

        @Schema(description = "ID de la cuenta propietaria.", example = "1")
        Long accountId,

        @Schema(description = "Nombre de la mascota.", example = "Fido")
        String name,

        @Schema(description = "Especie de la mascota.", example = "Perro")
        String species,

        @Schema(description = "Raza de la mascota.", example = "Labrador")
        String breed,

        @Schema(description = "Edad de la mascota en años.", example = "3")
        Integer age,

        @Schema(description = "Peso de la mascota en kg.", example = "25.5")
        BigDecimal weight,

        @Schema(description = "Género de la mascota.", example = "Macho")
        String gender,

        @Schema(description = "Color principal de la mascota.", example = "Marrón")
        String color,

        @Schema(description = "Descripción física detallada.", example = "Perro mediano de pelo corto")
        String physicalDescription,

        @Schema(description = "Información sobre medicamentos actuales.", example = "Ninguno")
        String medications,

        @Schema(description = "Información sobre alergias conocidas.", example = "Polvo")
        String allergies,

        @Schema(description = "Información de vacunas.", example = "Rabia, Moquillo")
        String vaccinations,

        @Schema(description = "Notas especiales para cuidadores.", example = "Muy juguetón")
        String specialNotes,

        @Schema(description = "Indica si la mascota está activa.", example = "true")
        boolean isActive,

        @Schema(description = "Fecha y hora de creación.", example = "2025-01-15T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Fecha y hora de la última actualización.", example = "2025-01-20T14:00:00")
        LocalDateTime updatedAt
) {

    /**
     * Convierte una entidad Pet a PetResponse DTO.
     *
     * @param pet entidad Pet
     * @return PetResponse con todos los campos, o null si pet es null
     */
    public static PetResponse fromEntity(Pet pet) {
        if (pet == null) return null;

        return new PetResponse(
                pet.getId(),
                pet.getAccount() != null ? pet.getAccount().getId() : null,
                pet.getName(),
                pet.getSpecies(),
                pet.getBreed(),
                pet.getAge(),
                pet.getWeight(),
                pet.getGender(),
                pet.getColor(),
                pet.getPhysicalDescription(),
                pet.getMedications(),
                pet.getAllergies(),
                pet.getVaccinations(),
                pet.getSpecialNotes(),
                pet.isActive(),
                pet.getCreatedAt(),
                pet.getUpdatedAt()
        );
    }
}
