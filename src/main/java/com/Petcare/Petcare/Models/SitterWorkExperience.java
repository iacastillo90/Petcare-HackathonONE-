package com.Petcare.Petcare.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "sitter_work_experience")
public class SitterWorkExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación muchos a uno con SitterProfile, obligatorio
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sitter_profile_id", nullable = false, foreignKey = @ForeignKey(name = "fk_workexperience_sitterprofile"))
    @NotNull(message = "El perfil de cuidador es obligatorio")
    private SitterProfile sitterProfile;

    @Column(name = "company_name", length = 250)
    @Size(max = 250, message = "El nombre de la empresa no puede exceder 250 caracteres")
    private String companyName;

    @Column(name = "job_title", length = 250)
    @Size(max = 250, message = "El título del puesto no puede exceder 250 caracteres")
    private String jobTitle;

    @Column(name = "responsibilities", columnDefinition = "TEXT")
    private String responsibilities;

    @Column(name = "start_date", nullable = false)
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // ========== CONSTRUCTORES ==========

    public SitterWorkExperience() {
    }

    public SitterWorkExperience(SitterProfile sitterProfile, String companyName, String jobTitle,
                                String responsibilities, LocalDate startDate, LocalDate endDate) {
        this.sitterProfile = sitterProfile;
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.responsibilities = responsibilities;
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

    public SitterProfile getSitterProfile() {
        return sitterProfile;
    }

    public void setSitterProfile(SitterProfile sitterProfile) {
        this.sitterProfile = sitterProfile;
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

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Indica si este trabajo sigue vigente.
     * @return true si endDate es null o posterior a la fecha actual
     */
    public boolean isCurrentJob() {
        return endDate == null || endDate.isAfter(LocalDate.now());
    }

    // ========== EQUALS Y HASHCODE ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SitterWorkExperience)) return false;
        SitterWorkExperience that = (SitterWorkExperience) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ========== TOSTRING ==========

    @Override
    public String toString() {
        return String.format(
                "SitterWorkExperience{id=%d, companyName='%s', jobTitle='%s', startDate=%s, endDate=%s}",
                id, companyName, jobTitle, startDate, endDate
        );
    }
}
