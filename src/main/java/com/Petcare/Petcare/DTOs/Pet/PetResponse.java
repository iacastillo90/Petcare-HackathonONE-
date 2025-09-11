package com.Petcare.Petcare.DTOs.Pet;

import com.Petcare.Petcare.Models.Pet;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para las respuestas de la API que contienen información completa de una mascota.
 *
 * <p>Esta clase representa la información completa de una mascota que se devuelve
 * a los clientes de la API, incluyendo datos de la cuenta asociada y timestamps
 * de auditoría. Excluye información sensible pero incluye todos los campos
 * necesarios para la gestión de mascotas.</p>
 *
 * <p><strong>Características:</strong></p>
 * <ul>
 * <li>Incluye información básica y detallada de la mascota</li>
 * <li>Contiene datos de la cuenta propietaria (ID y nombre)</li>
 * <li>Incluye timestamps formateados para el frontend</li>
 * <li>Método de fábrica estático para conversión desde entidad</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see Pet
 * @see CreatePetRequest
 */
public class PetResponse {

    /**
     * Identificador único de la mascota.
     */
    private Long id;

    /**
     * ID de la cuenta propietaria.
     */
    private Long accountId;

    /**
     * Nombre de la cuenta propietaria.
     */
    private String accountName;

    /**
     * Nombre de la mascota.
     */
    private String name;

    /**
     * Especie de la mascota.
     */
    private String species;

    /**
     * Raza de la mascota.
     */
    private String breed;

    /**
     * Edad de la mascota en años.
     */
    private Integer age;

    /**
     * Peso de la mascota en kilogramos.
     */
    private BigDecimal weight;

    /**
     * Género de la mascota.
     */
    private String gender;

    /**
     * Color principal de la mascota.
     */
    private String color;

    /**
     * Descripción física detallada.
     */
    private String physicalDescription;

    /**
     * Información sobre medicamentos actuales.
     */
    private String medications;

    /**
     * Información sobre alergias conocidas.
     */
    private String allergies;

    /**
     * Notas especiales para cuidadores.
     */
    private String specialNotes;

    /**
     * Estado de actividad de la mascota.
     */
    private boolean isActive;

    /**
     * Fecha y hora de creación.
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de última actualización.
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor por defecto.
     */
    public PetResponse() {}

    // ========== MÉTODO DE FÁBRICA ==========

    /**
     * Método de fábrica estático para convertir una entidad Pet a su DTO de respuesta.
     *
     * @param pet La entidad Pet a convertir
     * @return PetResponse DTO con la información de la mascota, o null si pet es null
     */
    public static PetResponse fromEntity(Pet pet) {
        if (pet == null) {
            return null;
        }

        PetResponse dto = new PetResponse();
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



    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getColor() {
        return color;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
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