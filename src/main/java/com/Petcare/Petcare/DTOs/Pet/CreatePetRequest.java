package com.Petcare.Petcare.DTOs.Pet;

import com.Petcare.Petcare.Models.Pet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO para la solicitud de creación de una nueva mascota.
 *
 * <p>Este DTO encapsula todos los datos necesarios para registrar una nueva mascota
 * en el sistema, excluyendo campos de sistema como ID, timestamps y estado activo
 * que son gestionados automáticamente.</p>
 *
 * <p><strong>Validaciones aplicadas:</strong></p>
 * <ul>
 * <li>Cuenta obligatoria y debe existir</li>
 * <li>Nombre obligatorio y limitado a 100 caracteres</li>
 * <li>Campos opcionales con límites apropiados</li>
 * <li>Edad y peso no pueden ser negativos</li>
 * <li>Notas limitadas para optimización de base de datos</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see Pet
 * @see PetResponse
 */
public class CreatePetRequest {

    /**
     * ID de la cuenta a la que pertenecerá la mascota.
     */
    @NotNull(message = "El ID de la cuenta es obligatorio")
    private Long accountId;

    /**
     * Nombre de la mascota.
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String name;

    /**
     * Especie de la mascota (ej: "Perro", "Gato").
     */
    @Size(max = 50, message = "La especie no puede exceder los 50 caracteres")
    private String species;

    /**
     * Raza específica de la mascota.
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
     * Información sobre vacunas aplicadas a la mascota.

     */
    @Size(max = 2000, message = "La información de vacunas no puede exceder los 2000 caracteres")
    private String vaccinations;


    /**
     * Notas especiales para cuidadores.
     */
    @Size(max = 2000, message = "Las notas especiales no pueden exceder los 2000 caracteres")
    private String specialNotes;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor por defecto.
     */
    public CreatePetRequest() {}

    /**
     * Constructor con campos principales.
     *
     * @param accountId ID de la cuenta propietaria
     * @param name Nombre de la mascota
     * @param species Especie de la mascota
     * @param breed Raza de la mascota
     * @param age Edad de la mascota
     */
    public CreatePetRequest(Long accountId, String name, String species, String breed, Integer age) {
        this.accountId = accountId;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.age = age;
    }

    // ========== GETTERS Y SETTERS ==========

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
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

    public String getVaccinations() {
        return vaccinations;
    }

    public void setVaccinations(String vaccinations) {
        this.vaccinations = vaccinations;
    }

    public String getSpecialNotes() {
        return specialNotes;
    }

    public void setSpecialNotes(String specialNotes) {
        this.specialNotes = specialNotes;
    }
}