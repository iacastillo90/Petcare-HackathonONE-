package com.Petcare.Petcare.DTOs.SitterWorkExperience;

import java.time.LocalDate;

public class SitterWorkExperienceResponseDTO {

    private Long id;
    private String companyName;
    private String jobTitle;
    private String responsibilities;
    private LocalDate startDate;
    private LocalDate endDate;

    // ===== CONSTRUCTORES =====
    public SitterWorkExperienceResponseDTO() {
    }

    public SitterWorkExperienceResponseDTO(Long id, String companyName, String jobTitle,
                                           String responsibilities, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.responsibilities = responsibilities;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // ===== GETTERS & SETTERS =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
