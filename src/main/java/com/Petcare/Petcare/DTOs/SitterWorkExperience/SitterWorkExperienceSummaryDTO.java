package com.Petcare.Petcare.DTOs.SitterWorkExperience;

import java.time.LocalDate;

/**
 * DTO resumido para listados de experiencias laborales.
 * Solo incluye los campos principales que se muestran en listados.
 */
public class SitterWorkExperienceSummaryDTO {

    private Long id;
    private String companyName;
    private String jobTitle;
    private LocalDate startDate;
    private LocalDate endDate;

    // ========== CONSTRUCTORES ==========

    public SitterWorkExperienceSummaryDTO() {
    }

    public SitterWorkExperienceSummaryDTO(Long id, String companyName, String jobTitle,
                                          LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // ========== GETTERS Y SETTERS ==========

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
