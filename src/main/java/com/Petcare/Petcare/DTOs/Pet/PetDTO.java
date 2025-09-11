package com.Petcare.Petcare.DTOs.Pet;

import com.Petcare.Petcare.Models.Pet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO intermedio para transferencia de datos de mascotas con validaciones completas.
 *
 * <p>Este DTO sirve como clase intermedia para operaciones internas del sistema
 * que requieren validación completa de datos de mascotas. A diferencia de los DTOs
 * de entrada y salida especializados, este incluye tanto el ID como validaciones
 * Bean Validation para uso en servicios internos.</p>
 *
 * <p><strong>Funcionalidades:</strong></p>
 * <ul>
 * <li>Validación Bean Validation completa con mensajes en español</li>
 * <li>Conversión bidireccional con la entidad Pet</li>
 * <li>Soporte para operaciones de transferencia interna</li>
 * <li>Incluye ID para operaciones de actualización</li>
 * </ul>
 *
 * <p><strong>Casos de uso:</strong></p>
 * <ul>
 * <li>Operaciones internas entre capas de servicio</li>
 * <li>Validación de datos en procesos batch</li>
 * <li>Transferencia entre microservicios (futuro)</li>
 * <li>Cache intermedio con validación</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see Pet
 * @see CreatePetRequest
 * @see PetResponse
 */
public class PetDTO {

    /**
     * Identificador único de la mascota.
     * <p>Puede ser null para nuevas mascotas.</p>
     */
    private Long id;

    /**
     * ID de la cuenta propietaria.
     */
    @NotNull(message = "El ID de la cuenta es obligatorio")
    private Long accountId;

    /**
     * Nombre de la cuenta propietaria (solo lectura).
     */
    private String accountName;

    /**
     * Nombre de la mascota.
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String name;

    /**
     * Especie de la mascota.
     */
    @Size(max = 50, message = "La especie no puede exceder los 50 caracteres")
    private String species;

    /**
     * Raza de la mascota.
     */
    @Size(max = 100, message = "La raza no puede exceder los 100 caracteres")
    private String breed;

    /**
     * Edad de la mascota en años.
     */
    @PositiveOrZero(message = "La edad no puede ser negativa")
    private Integer age;

    /**
     * Peso de la mascota en kilogramos.
     */
    @PositiveOrZero(message = "El peso no puede ser negativo")
    private BigDecimal weight;

    /**
     * Género de la mascota.
     */
    @Size(max = 20, message = "El género no puede exceder los 20 caracteres")
    private String gender;

    /**
     * Color principal de la mascota.
     */
    @Size(max = 50, message = "El color no puede exceder los 50 caracteres")
    private String color;

    /**
     * Descripción física detallada.
     */
    @Size(max = 1000, message = "La descripción física no puede exceder los 1000 caracteres")
    private String physicalDescription;

    /**
     * Información sobre medicamentos actuales.
     */
    @Size(max = 1000, message = "La información de medicamentos no puede exceder los 1000 caracteres")
    private String medications;

    /**
     * Información sobre alergias conocidas.
     */
    @Size(max = 1000, message = "La información de alergias no puede exceder los 1000 caracteres")
    private String allergies;

    /**
     * Notas especiales para cuidadores.
     */
    @Size(max = 2000, message = "Las notas especiales no pueden exceder los 2000 caracteres")
    private String specialNotes;

    /**
     * Estado de actividad de la mascota.
     */
    private boolean isActive = true;

    /**
     * Fecha y hora de creación.
     */
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de última actualización.
     */
    private LocalDateTime updatedAt;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor por defecto.
     */
    public PetDTO() {}

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
        this.accountId = accountId;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.age = age;
    }

    // ========== MÉTODOS DE FÁBRICA ==========

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

        PetDTO dto = new PetDTO();
        dto.setId(pet.getId());
        dto.setName(pet.getName());
        dto.setSpecies(pet.getSpecies());
        dto.setBreed(pet.getBreed());
        dto.setAge(pet.getAge());
        dto.setWeight(pet.getWeight());
        dto.setGender(pet.getGender());
        dto.setColor(pet.getColor());
        dto.setPhysicalDescription(pet.getPhysicalDescription());
        dto.setMedications(pet.getMedications());
        dto.setAllergies(pet.getAllergies());
        dto.setSpecialNotes(pet.getSpecialNotes());
        dto.setActive(pet.isActive());
        dto.setCreatedAt(pet.getCreatedAt());
        dto.setUpdatedAt(pet.getUpdatedAt());

        // Información de la cuenta si está disponible
        if (pet.getAccount() != null) {
            dto.setAccountId(pet.getAccount().getId());
            dto.setAccountName(pet.getAccount().getAccountName());
        }

        return dto;
    }

    /**
     * Convierte este DTO a una entidad Pet.
     * <p>Nota: La cuenta debe ser establecida por separado ya que requiere
     * una consulta a la base de datos.</p>
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
        // Note: account, createdAt, updatedAt son manejados por el servicio
        return pet;
    }

    // ========== GETTERS Y SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getPhysicalDescription() {
        return physicalDescription;
    }

    public void setPhysicalDescription(String physicalDescription) {
        this.physicalDescription = physicalDescription;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getSpecialNotes() {
        return specialNotes;
    }

    public void setSpecialNotes(String specialNotes) {
        this.specialNotes = specialNotes;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}