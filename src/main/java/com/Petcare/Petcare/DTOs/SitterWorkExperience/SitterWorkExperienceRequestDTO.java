package com.Petcare.Petcare.DTOs.SitterWorkExperience;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO para las solicitudes de creación o actualización de una experiencia laboral.
 * <p>
 * Esta clase define la estructura de datos que el cliente de la API debe enviar
 * para registrar o modificar una entrada en el historial profesional de un cuidador.
 * Utiliza anotaciones de Jakarta Bean Validation para asegurar la integridad de los
 * datos desde la capa del controlador.
 *
 * @see cSitterWorkExperienceController
 * @see SitterWorkExperience
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 */
public class SitterWorkExperienceRequestDTO {

    /**
     * ID del perfil de cuidador ({@link com.Petcare.Petcare.Models.SitterProfile}) al que se asociará esta experiencia.
     * Es un campo obligatorio para vincular el registro con el cuidador correcto.
     */
    @NotNull(message = "El ID del perfil de cuidador es obligatorio")
    private Long sitterProfileId;

    /**
     * Nombre de la empresa, organización o lugar donde se adquirió la experiencia.
     * Opcional, para permitir registrar experiencias informales.
     */
    @Size(max = 250, message = "El nombre de la empresa no puede exceder 250 caracteres")
    private String companyName;

    /**
     * El título del puesto o el rol desempeñado (ej. "Paseador Canino Profesional").
     * Opcional.
     */
    @Size(max = 250, message = "El título del puesto no puede exceder 250 caracteres")
    private String jobTitle;

    /**
     * Descripción de las tareas y responsabilidades asociadas a la experiencia.
     * Opcional.
     */
    private String responsibilities;

    /**
     * Fecha en que comenzó la experiencia laboral.
     * Es un campo obligatorio y no puede ser una fecha futura.
     */
    @NotNull(message = "La fecha de inicio es obligatoria")
    @PastOrPresent(message = "La fecha de inicio no puede ser en el futuro")
    private LocalDate startDate;

    /**
     * Fecha en que finalizó la experiencia laboral.
     * Si se omite (se envía como nulo), se interpreta que es el trabajo actual.
     * No puede ser una fecha futura.
     */
    @PastOrPresent(message = "La fecha de finalización no puede ser en el futuro")
    private LocalDate endDate;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor por defecto, requerido por frameworks de serialización como Jackson.
     */
    public SitterWorkExperienceRequestDTO() {
    }

    /**
     * Constructor completo para facilitar la creación de instancias en el código, especialmente para pruebas.
     *
     * @param sitterProfileId  ID del perfil del cuidador asociado.
     * @param companyName      Nombre de la empresa.
     * @param jobTitle         Título del puesto.
     * @param responsibilities Descripción de las responsabilidades.
     * @param startDate        Fecha de inicio.
     * @param endDate          Fecha de finalización.
     */
    public SitterWorkExperienceRequestDTO(Long sitterProfileId, String companyName, String jobTitle, String responsibilities, LocalDate startDate, LocalDate endDate) {
        this.sitterProfileId = sitterProfileId;
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.responsibilities = responsibilities;
        this.startDate = startDate;
        this.endDate = endDate;
    }


    // ========== GETTERS Y SETTERS ==========

    /**
     * Obtiene el ID del perfil de cuidador asociado.
     * @return El ID del perfil.
     */
    public Long getSitterProfileId() {
        return sitterProfileId;
    }

    /**
     * Establece el ID del perfil de cuidador asociado.
     * @param sitterProfileId El nuevo ID del perfil.
     */
    public void setSitterProfileId(Long sitterProfileId) {
        this.sitterProfileId = sitterProfileId;
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
     * @return La descripción.
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
     * Obtiene la fecha de inicio.
     * @return La fecha de inicio.
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Establece la fecha de inicio.
     * @param startDate La nueva fecha de inicio.
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Obtiene la fecha de finalización.
     * @return La fecha de finalización, o nulo si es el trabajo actual.
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Establece la fecha de finalización.
     * @param endDate La nueva fecha de finalización.
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}