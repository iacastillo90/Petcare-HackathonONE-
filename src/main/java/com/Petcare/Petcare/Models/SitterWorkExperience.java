package com.Petcare.Petcare.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad que representa una entrada de experiencia laboral en el perfil de un cuidador.
 * <p>
 * Cada instancia de esta clase corresponde a un trabajo o voluntariado previo que un cuidador
 * (Sitter) desea mostrar en su perfil. Esta información es fundamental para que los clientes
 * puedan evaluar la trayectoria, experiencia y fiabilidad del cuidador antes de contratar sus servicios.
 * </p>
 * <p><b>Relación Principal:</b></p>
 * <ul>
 * <li><b>Many-to-One con {@link SitterProfile}:</b> Cada experiencia laboral pertenece a un único perfil de cuidador.</li>
 * </ul>
 *
 * @see SitterProfile
 * @see com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceResponseDTO
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 */
@Entity
@Table(name = "sitter_work_experience", indexes = {
        @Index(name = "idx_swe_sitter_profile_id", columnList = "sitter_profile_id")
})
public class SitterWorkExperience {

    /**
     * Identificador único de la experiencia laboral, generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * El perfil del cuidador al que pertenece esta experiencia laboral.
     * <p>
     * Esta es la relación propietaria que vincula de forma obligatoria la experiencia con
     * el perfil de un cuidador específico.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sitter_profile_id", nullable = false, foreignKey = @ForeignKey(name = "fk_workexperience_sitterprofile"))
    @NotNull(message = "El perfil de cuidador es obligatorio")
    private SitterProfile sitterProfile;

    /**
     * Nombre de la empresa, organización o lugar donde se adquirió la experiencia.
     * Puede ser nulo si la experiencia fue informal (ej. "Cuidado de mascotas de vecinos").
     */
    @Column(name = "company_name", length = 250)
    @Size(max = 250, message = "El nombre de la empresa no puede exceder 250 caracteres")
    private String companyName;

    /**
     * El puesto o rol desempeñado durante la experiencia laboral (ej. "Paseador de Perros", "Asistente Veterinario").
     */
    @Column(name = "job_title", length = 250)
    @Size(max = 250, message = "El título del puesto no puede exceder 250 caracteres")
    private String jobTitle;

    /**
     * Una descripción detallada de las tareas, logros y responsabilidades del puesto.
     * Este campo permite al cuidador destacar sus habilidades y experiencia relevante.
     */
    @Column(name = "responsibilities", columnDefinition = "TEXT")
    private String responsibilities;

    /**
     * Fecha en la que comenzó la experiencia laboral. Es un campo obligatorio.
     */
    @Column(name = "start_date", nullable = false)
    @NotNull(message = "La fecha de inicio es obligatoria")
    @PastOrPresent(message = "La fecha de inicio no puede ser en el futuro")
    private LocalDate startDate;

    /**
     * Fecha en la que finalizó la experiencia laboral.
     * <p>
     * Si este campo es <b>nulo</b>, se interpreta que es el trabajo actual del cuidador.
     * </p>
     */
    @Column(name = "end_date")
    @PastOrPresent(message = "La fecha de finalización no puede ser en el futuro")
    private LocalDate endDate;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor por defecto requerido por JPA.
     */
    public SitterWorkExperience() {
    }

    /**
     * Constructor para crear una nueva instancia de experiencia laboral con todos sus campos.
     *
     * @param sitterProfile El perfil del cuidador asociado.
     * @param companyName El nombre de la empresa.
     * @param jobTitle El título del puesto.
     * @param responsibilities Las responsabilidades del puesto.
     * @param startDate La fecha de inicio.
     * @param endDate La fecha de finalización (puede ser nula).
     */
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

    /**
     * Obtiene el ID único de la experiencia laboral.
     * @return El ID de la entidad.
     */
    public Long getId() {
        return id;
    }

    /**
     * Establece el ID único de la experiencia laboral.
     * @param id El nuevo ID para la entidad.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtiene el perfil de cuidador asociado a esta experiencia.
     * @return La entidad {@link SitterProfile} propietaria.
     */
    public SitterProfile getSitterProfile() {
        return sitterProfile;
    }

    /**
     * Establece el perfil de cuidador para esta experiencia.
     * @param sitterProfile La entidad {@link SitterProfile} a asociar.
     */
    public void setSitterProfile(SitterProfile sitterProfile) {
        this.sitterProfile = sitterProfile;
    }

    /**
     * Obtiene el nombre de la empresa.
     * @return El nombre de la empresa o lugar de trabajo.
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
     * @return El título o rol desempeñado.
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
     * @return El texto detallado de las responsabilidades.
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
     * @return La fecha de finalización, o {@code null} si es el trabajo actual.
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Establece la fecha de finalización de la experiencia.
     * @param endDate La nueva fecha de finalización. Puede ser nula.
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Verifica si esta experiencia laboral corresponde al trabajo actual del cuidador.
     * <p>
     * Es útil para la lógica de la interfaz de usuario, para mostrar "Presente"
     * en lugar de una fecha de finalización.
     * </p>
     * @return {@code true} si la fecha de finalización es nula, {@code false} en caso contrario.
     */
    public boolean isCurrentJob() {
        return endDate == null;
    }

    // ========== EQUALS, HASHCODE Y TOSTRING ==========

    /**
     * Compara dos objetos de experiencia laboral basándose en su ID único.
     * Dos instancias son consideradas iguales si tienen el mismo ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SitterWorkExperience that = (SitterWorkExperience) o;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    /**
     * Genera un código hash basado en la clase.
     * Se usa el ID si está presente para consistencia con equals.
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Devuelve una representación en cadena del objeto para logging y depuración.
     * Incluye información clave sin cargar relaciones lazy.
     */
    @Override
    public String toString() {
        return String.format(
                "SitterWorkExperience{id=%d, sitterProfileId=%d, companyName='%s', jobTitle='%s', startDate=%s, endDate=%s}",
                id,
                (sitterProfile != null ? sitterProfile.getId() : "null"),
                companyName,
                jobTitle,
                startDate,
                endDate
        );
    }
}