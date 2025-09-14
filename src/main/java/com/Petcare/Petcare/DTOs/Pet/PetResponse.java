package com.Petcare.Petcare.DTOs.Pet;

import com.Petcare.Petcare.Models.Pet;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta de información de una mascota.
 * Incluye todos los campos relevantes de la entidad Pet.
 */
public class PetResponse {

    private Long id;
    private Long accountId;
    private String name;
    private String species;
    private String breed;
    private Integer age;
    private BigDecimal weight;
    private String gender;
    private String color;
    private String physicalDescription;
    private String medications;
    private String allergies;
    private String vaccinations; // campo incluido
    private String specialNotes;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ========== GETTERS Y SETTERS ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getPhysicalDescription() { return physicalDescription; }
    public void setPhysicalDescription(String physicalDescription) { this.physicalDescription = physicalDescription; }

    public String getMedications() { return medications; }
    public void setMedications(String medications) { this.medications = medications; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getVaccinations() { return vaccinations; }
    public void setVaccinations(String vaccinations) { this.vaccinations = vaccinations; }

    public String getSpecialNotes() { return specialNotes; }
    public void setSpecialNotes(String specialNotes) { this.specialNotes = specialNotes; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ========== MÉTODO DE CONVERSIÓN ==========

    /**
     * Convierte una entidad Pet a PetResponse DTO.
     *
     * @param pet entidad Pet
     * @return PetResponse con todos los campos
     */
    public static PetResponse fromEntity(Pet pet) {
        if (pet == null) return null;

        PetResponse response = new PetResponse();
        response.setId(pet.getId());
        response.setAccountId(pet.getAccount() != null ? pet.getAccount().getId() : null);
        response.setName(pet.getName());
        response.setSpecies(pet.getSpecies());
        response.setBreed(pet.getBreed());
        response.setAge(pet.getAge());
        response.setWeight(pet.getWeight());
        response.setGender(pet.getGender());
        response.setColor(pet.getColor());
        response.setPhysicalDescription(pet.getPhysicalDescription());
        response.setMedications(pet.getMedications());
        response.setAllergies(pet.getAllergies());
        response.setVaccinations(pet.getVaccinations());
        response.setSpecialNotes(pet.getSpecialNotes());
        response.setActive(pet.isActive());
        response.setCreatedAt(pet.getCreatedAt());
        response.setUpdatedAt(pet.getUpdatedAt());
        return response;
    }
}


