package com.Petcare.Petcare.DTOs.Pet;

import com.Petcare.Petcare.Models.Pet;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO intermedio para transferencia de datos de mascotas.
 *
 * <p>Este DTO sirve como clase intermedia para operaciones internas del sistema
 * que requieren validación completa de datos de mascotas.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see Pet
 * @see CreatePetRequest
 * @see PetResponse
 */
@Schema(description = "DTO intermedio para transferencia de datos de mascotas con validaciones completas.")
public record PetDTO(
        @Schema(description = "Identificador único de la mascota. Puede ser null para nuevas mascotas.", example = "1")
        Long id,

        @Schema(description = "ID de la cuenta propietaria.", example = "1")
        Long accountId,

        @Schema(description = "Nombre de la cuenta propietaria (solo lectura).", example = "Cuenta García")
        String accountName,

        @Schema(description = "Nombre de la mascota.", example = "Fido")
        String name,

        @Schema(description = "Especie de la mascota.", example = "Perro")
        String species,

        @Schema(description = "Raza de la mascota.", example = "Labrador")
        String breed,

        @Schema(description = "Edad de la mascota en años.", example = "3")
        Integer age,

        @Schema(description = "Peso de la mascota en kilogramos.", example = "25.5")
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

        @Schema(description = "Notas especiales para cuidadores.", example = "Muy juguetón")
        String specialNotes,

        @Schema(description = "Estado de actividad de la mascota.", example = "true")
        boolean isActive,

        @Schema(description = "Fecha y hora de creación.", example = "2025-01-15T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Fecha y hora de última actualización.", example = "2025-01-20T14:00:00")
        LocalDateTime updatedAt
) {

    /**
     * Constructor con campos principales.
     *
     * @param accountId ID de la cuenta propietaria
     * @param name Nombre de la mascota
     * @param species Especie de la mascota
     * @param breed Raza de la mascota
     * @param age Edad de la mascota
     */
    public PetDTO(Long accountId, String name, String species, String breed, Integer age) {
        this(null, accountId, null, name, species, breed, age, null, null, null, null, null, null, null, true, null, null);
    }

    /**
     * Método de fábrica estático para convertir una entidad Pet a DTO.
     *
     * @param pet La entidad Pet a convertir
     * @return PetDTO con la información de la mascota, o null si pet es null
     */
    public static PetDTO fromEntity(Pet pet) {
        if (pet == null) {
            return null;
        }
        return new PetDTO(
                pet.getId(),
                pet.getAccount() != null ? pet.getAccount().getId() : null,
                pet.getAccount() != null ? pet.getAccount().getAccountName() : null,
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
                pet.getSpecialNotes(),
                pet.isActive(),
                pet.getCreatedAt(),
                pet.getUpdatedAt()
        );
    }

    /**
     * Convierte este DTO a una entidad Pet.
     *
     * @return Nueva instancia de Pet con los datos de este DTO
     */
    public Pet toEntity() {
        Pet pet = new Pet();
        pet.setId(this.id);
        pet.setName(this.name);
        pet.setSpecies(this.species);
        pet.setBreed(this.breed);
        pet.setAge(this.age);
        pet.setWeight(this.weight);
        pet.setGender(this.gender);
        pet.setColor(this.color);
        pet.setPhysicalDescription(this.physicalDescription);
        pet.setMedications(this.medications);
        pet.setAllergies(this.allergies);
        pet.setSpecialNotes(this.specialNotes);
        pet.setActive(this.isActive);
        return pet;
    }
}
