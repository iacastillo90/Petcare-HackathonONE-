package com.Petcare.Petcare.DTOs.SitterWorkExperience;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class SitterWorkExperienceRequestDTO {

    @NotNull(message = "El ID del perfil de cuidador es obligatorio")
    private Long sitterProfileId;

    @Size(max = 250, message = "El nombre de la empresa no puede exceder 250 caracteres")
    private String companyName;

    @Size(max = 250, message = "El título del puesto no puede exceder 250 caracteres")
    private String jobTitle;

    private String responsibilities;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;

    private LocalDate endDate;

    // ========== GETTERS Y SETTERS ==========

    public Long getSitterProfileId() {
        return sitterProfileId;
    }

    public void setSitterProfileId(Long sitterProfileId) {
        this.sitterProfileId = sitterProfileId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getResponsibilities() {
        return responsibilities;
    }

    public void setResponsibilities(String responsibilities) {
        this.responsibilities = responsibilities;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
