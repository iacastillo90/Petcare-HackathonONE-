package com.Petcare.Petcare.DTOs.Pet;

import com.Petcare.Petcare.Models.Pet;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * DTO optimizado para listados y resúmenes de mascotas.
 *
 * <p>Esta versión simplificada del DTO de mascota está diseñada para operaciones
 * de listado masivo y respuestas que requieren optimización de rendimiento.
 * Incluye solo los campos esenciales para mostrar información básica de la mascota
 * sin cargar datos pesados como notas detalladas.</p>
 *
 * <p><strong>Campos incluidos:</strong></p>
 * <ul>
 * <li>Identificación: ID, nombre de mascota</li>
 * <li>Información básica: especie, raza, edad</li>
 * <li>Relación: ID y nombre de cuenta propietaria</li>
 * <li>Estado: activo/inactivo</li>
 * <li>Timestamps: fecha de creación</li>
 * </ul>
 *
 * <p><strong>Casos de uso típicos:</strong></p>
 * <ul>
 * <li>Listados de mascotas en interfaces de usuario</li>
 * <li>Búsquedas y filtros rápidos</li>
 * <li>APIs que requieren respuestas optimizadas</li>
 * <li>Dashboards y vistas resumidas</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see Pet
 * @see PetResponse
 */
public class PetSummaryResponse {

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
     * Estado de actividad de la mascota.
     */
    private boolean isActive;

    /**
     * Fecha y hora de creación.
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor por defecto.
     */
    public PetSummaryResponse() {}

    // ========== MÉTODO DE FÁBRICA ==========

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

        PetSummaryResponse dto = new PetSummaryResponse();
        dto.setId(pet.getId());
        dto.setName(pet.getName());
        dto.setSpecies(pet.getSpecies());
        dto.setBreed(pet.getBreed());
        dto.setAge(pet.getAge());
        dto.setActive(pet.isActive());
        dto.setCreatedAt(pet.getCreatedAt());

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
}