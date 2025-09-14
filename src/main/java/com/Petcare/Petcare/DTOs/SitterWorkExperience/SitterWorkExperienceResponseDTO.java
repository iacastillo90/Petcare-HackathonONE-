package com.Petcare.Petcare.DTOs.SitterWorkExperience;

import java.time.LocalDate;

/**
 * DTO para las respuestas de la API que contienen los detalles completos de una experiencia laboral.
 * <p>
 * Esta clase se utiliza como el cuerpo de la respuesta para los endpoints que devuelven un
 * registro único y detallado de {@link com.Petcare.Petcare.Models.SitterWorkExperience}.
 * A diferencia del DTO de resumen, este incluye todos los campos relevantes, como
 * el texto completo de las responsabilidades.
 *
 * @see com.Petcare.Petcare.Controllers.SitterWorkExperienceController
 * @see com.Petcare.Petcare.Models.SitterWorkExperience
 * @see SitterWorkExperienceSummaryDTO
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 */
public class SitterWorkExperienceResponseDTO {

    /**
     * El identificador único de la entrada de experiencia laboral.
     */
    private Long id;

    /**
     * Nombre de la empresa, organización o lugar donde se adquirió la experiencia.
     */
    private String companyName;

    /**
     * El título del puesto o el rol desempeñado (ej. "Paseador Canino Profesional").
     */
    private String jobTitle;

    /**
     * Una descripción detallada de las tareas, logros y responsabilidades del puesto.
     */
    private String responsibilities;

    /**
     * Fecha en que comenzó la experiencia laboral.
     */
    private LocalDate startDate;

    /**
     * Fecha en que finalizó la experiencia laboral. Es nulo si es el trabajo actual.
     */
    private LocalDate endDate;

    // ===== CONSTRUCTORES =====

    /**
     * Constructor por defecto, requerido por frameworks de serialización como Jackson.
     */
    public SitterWorkExperienceResponseDTO() {
    }

    /**
     * Constructor completo para crear una instancia del DTO con todos sus campos.
     *
     * @param id El ID único del registro.
     * @param companyName El nombre de la empresa.
     * @param jobTitle El título del puesto.
     * @param responsibilities La descripción de las responsabilidades.
     * @param startDate La fecha de inicio.
     * @param endDate La fecha de finalización.
     */
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

    /**
     * Obtiene el ID único de la experiencia laboral.
     * @return El ID del registro.
     */
    public Long getId() {
        return id;
    }

    /**
     * Establece el ID único de la experiencia laboral.
     * @param id El nuevo ID para el registro.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtiene el nombre de la empresa.
     * @return El nombre de la empresa.
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Establece el nombre de la empresa.
     * @param companyName El nuevo nombre de la empresa.
     */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * Obtiene el título del puesto.
     * @return El título del puesto.
     */
    public String getJobTitle() {
        return jobTitle;
    }

    /**
     * Establece el título del puesto.
     * @param jobTitle El nuevo título del puesto.
     */
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    /**
     * Obtiene la descripción de las responsabilidades.
     * @return La descripción detallada de las tareas.
     */
    public String getResponsibilities() {
        return responsibilities;
    }

    /**
     * Establece la descripción de las responsabilidades.
     * @param responsibilities La nueva descripción.
     */
    public void setResponsibilities(String responsibilities) {
        this.responsibilities = responsibilities;
    }

    /**
     * Obtiene la fecha de inicio de la experiencia.
     * @return La fecha de inicio.
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Establece la fecha de inicio de la experiencia.
     * @param startDate La nueva fecha de inicio.
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Obtiene la fecha de finalización de la experiencia.
     * @return La fecha de finalización, o nulo si es el trabajo actual.
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Establece la fecha de finalización de la experiencia.
     * @param endDate La nueva fecha de finalización.
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}