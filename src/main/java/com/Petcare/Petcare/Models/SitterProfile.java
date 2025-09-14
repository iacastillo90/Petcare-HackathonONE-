package com.Petcare.Petcare.Models;

import com.Petcare.Petcare.Models.User.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa el perfil profesional de un cuidador de mascotas.
 *
 * <p>Esta entidad extiende la información básica de un usuario con rol SITTER,
 * proporcionando datos específicos necesarios para ofrecer servicios de cuidado
 * de mascotas. Incluye información profesional, tarifas, disponibilidad y
 * métricas de calidad del servicio.</p>
 *
 * <p><strong>Relación principal:</strong></p>
 * <ul>
 *   <li>Asociado a un usuario único con rol SITTER ({@link User})</li>
 * </ul>
 *
 * <p><strong>Información del perfil:</strong></p>
 * <ul>
 *   <li>Biografía profesional y experiencia</li>
 *   <li>Tarifa por hora de servicio</li>
 *   <li>Radio de servicio en kilómetros</li>
 *   <li>Imagen de perfil profesional</li>
 *   <li>Estado de verificación de identidad</li>
 *   <li>Calificación promedio de clientes</li>
 *   <li>Disponibilidad para nuevas reservas</li>
 * </ul>
 *
 * <p><strong>Estados del cuidador:</strong></p>
 * <ul>
 *   <li>VERIFICADO: Identidad y antecedentes verificados por Petcare</li>
 *   <li>NO_VERIFICADO: Perfil creado pero pendiente de verificación</li>
 *   <li>DISPONIBLE: Acepta nuevas reservas</li>
 *   <li>NO_DISPONIBLE: Temporalmente no acepta reservas</li>
 * </ul>
 *
 * <p><strong>Nota:</strong> Las validaciones de negocio complejas, cálculo
 * de calificaciones y gestión de disponibilidad se manejan en el service layer.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see User
 */
@Entity
@Table(name = "sitter_profiles",
        indexes = {
                @Index(name = "idx_sitterprofile_user_id", columnList = "user_id", unique = true),
                @Index(name = "idx_sitterprofile_verified", columnList = "is_verified"),
                @Index(name = "idx_sitterprofile_available", columnList = "is_available_for_bookings"),
                @Index(name = "idx_sitterprofile_rating", columnList = "average_rating"),
                @Index(name = "idx_sitterprofile_hourly_rate", columnList = "hourly_rate"),
                @Index(name = "idx_sitterprofile_created_at", columnList = "created_at")
        })
@EntityListeners(AuditingEntityListener.class)
public class SitterProfile {

    /**
     * Identificador único del perfil de cuidador.
     * Generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Usuario al que pertenece este perfil de cuidador.
     *
     * <p>Relación uno-a-uno obligatoria con un usuario que debe tener rol SITTER.
     * Un usuario solo puede tener un perfil de cuidador activo.</p>
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_sitterprofile_user"))
    @NotNull(message = "El usuario es obligatorio para el perfil de cuidador")
    private User user;

    /**
     * Biografía profesional del cuidador.
     *
     * <p>Descripción personal donde el cuidador puede explicar su experiencia,
     * filosofía de cuidado, especialidades y cualquier información que ayude
     * a los clientes a conocerlo mejor.</p>
     */
    @Column(name = "bio", columnDefinition = "TEXT")
    @Size(max = 2000, message = "La biografía no puede exceder 2000 caracteres")
    private String bio;

    /**
     * Tarifa por hora en la moneda base del sistema.
     *
     * <p>Precio que cobra el cuidador por cada hora de servicio base.
     * Servicios especializados o fuera de horario pueden tener tarifas diferentes
     * definidas en las ofertas de servicio específicas.</p>
     */
    @Column(name = "hourly_rate", nullable = false, precision = 8, scale = 2)
    @NotNull(message = "La tarifa por hora es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "La tarifa debe ser mayor a cero")
    @Digits(integer = 6, fraction = 2, message = "La tarifa debe tener máximo 6 dígitos enteros y 2 decimales")
    private BigDecimal hourlyRate;

    /**
     * Radio de servicio en kilómetros desde la ubicación base del cuidador.
     *
     * <p>Define la distancia máxima que el cuidador está dispuesto a viajar
     * para ofrecer servicios. Se usa para filtrar cuidadores por proximidad
     * en las búsquedas de clientes.</p>
     */
    @Column(name = "servicing_radius", nullable = false)
    @NotNull(message = "El radio de servicio es obligatorio")
    @Min(value = 1, message = "El radio de servicio debe ser al menos 1 km")
    @Max(value = 50, message = "El radio de servicio no puede exceder 50 km")
    private Integer servicingRadius;

    /**
     * URL de la imagen de perfil profesional del cuidador.
     *
     * <p>Enlace a una imagen que represente profesionalmente al cuidador.
     * Debe ser una URL válida apuntando a un servicio de almacenamiento seguro.</p>
     */
    @Column(name = "profile_image_url", length = 500)
    @Size(max = 500, message = "La URL de la imagen no puede exceder 500 caracteres")
    private String profileImageUrl;

    /**
     * Indica si el perfil del cuidador ha sido verificado por Petcare.
     *
     * <p>La verificación incluye validación de identidad, antecedentes penales
     * y referencias profesionales. Solo cuidadores verificados pueden recibir
     * reservas en la plataforma.</p>
     */
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    /**
     * Calificación promedio del cuidador basada en reseñas de clientes.
     *
     * <p>Valor entre 0.0 y 5.0 calculado automáticamente a partir de las
     * calificaciones de las reservas completadas. Se actualiza cada vez
     * que se recibe una nueva reseña.</p>
     */
    @Column(name = "average_rating", precision = 3, scale = 2)
    @DecimalMin(value = "0.0", message = "La calificación no puede ser negativa")
    @DecimalMax(value = "5.0", message = "La calificación no puede exceder 5.0")
    @Digits(integer = 1, fraction = 2, message = "La calificación debe tener máximo 1 dígito entero y 2 decimales")
    private BigDecimal averageRating;

    /**
     * Indica si el cuidador está disponible para recibir nuevas reservas.
     *
     * <p>Los cuidadores pueden desactivar temporalmente su disponibilidad
     * por vacaciones, sobrecarga de trabajo u otras razones personales
     * sin afectar su perfil permanentemente.</p>
     */
    @Column(name = "is_available_for_bookings", nullable = false)
    private boolean isAvailableForBookings = true;

    /**
     * Fecha y hora de creación del perfil.
     *
     * <p>Se establece automáticamente al persistir la entidad.
     * Inmutable después de la creación.</p>
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última actualización.
     *
     * <p>Se actualiza automáticamente cada vez que se modifica la entidad.
     * Útil para auditoría y seguimiento de cambios del perfil.</p>
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // ========== RELACIÓN CON EXPERIENCIA DE TRABAJO ==========
    @OneToMany(mappedBy = "sitterProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SitterWorkExperience> workExperiences = new ArrayList<>();

    // ========== MÉTODOS PARA MANEJAR EXPERIENCIAS ==========
    public void addExperience(SitterWorkExperience experience) {
        if (experience != null) {
            workExperiences.add(experience);
            experience.setSitterProfile(this);
        }
    }

    public void removeExperience(SitterWorkExperience experience) {
        if (experience != null && workExperiences.remove(experience)) {
            experience.setSitterProfile(null);
        }
    }

    public List<SitterWorkExperience> getWorkExperiences() {
        return workExperiences;
    }

    public void setWorkExperiences(List<SitterWorkExperience> workExperiences) {
        this.workExperiences = workExperiences;
    }



    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido por JPA.
     */
    public SitterProfile() {
    }

    /**
     * Constructor principal para crear un nuevo perfil de cuidador.
     *
     * <p>Solo incluye validaciones básicas de nulos. Las validaciones de negocio
     * complejas se manejan en el service layer.</p>
     *
     * @param user el usuario propietario del perfil
     * @param bio biografía profesional del cuidador
     * @param hourlyRate tarifa por hora
     * @param servicingRadius radio de servicio en kilómetros
     * @param profileImageUrl URL de la imagen de perfil
     */
    public SitterProfile(User user, String bio, BigDecimal hourlyRate,
                         Integer servicingRadius, String profileImageUrl) {
        this.user = user;
        this.bio = bio;
        this.hourlyRate = hourlyRate;
        this.servicingRadius = servicingRadius;
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * Constructor completo con todos los campos.
     * Principalmente para uso interno y testing.
     */
    public SitterProfile(Long id, User user, String bio, BigDecimal hourlyRate,
                         Integer servicingRadius, String profileImageUrl, boolean isVerified,
                         BigDecimal averageRating, boolean isAvailableForBookings,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.bio = bio;
        this.hourlyRate = hourlyRate;
        this.servicingRadius = servicingRadius;
        this.profileImageUrl = profileImageUrl;
        this.isVerified = isVerified;
        this.averageRating = averageRating;
        this.isAvailableForBookings = isAvailableForBookings;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ========== GETTERS Y SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Integer getServicingRadius() {
        return servicingRadius;
    }

    public void setServicingRadius(Integer servicingRadius) {
        this.servicingRadius = servicingRadius;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public boolean isAvailableForBookings() {
        return isAvailableForBookings;
    }

    public void setAvailableForBookings(boolean availableForBookings) {
        isAvailableForBookings = availableForBookings;
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

    // ========== MÉTODOS DE UTILIDAD DE NEGOCIO ==========

    /**
     * Verifica si el cuidador puede recibir nuevas reservas.
     *
     * @return true si está verificado, disponible y el usuario está activo
     */
    public boolean canAcceptBookings() {
        return isVerified && isAvailableForBookings &&
                (user != null && user.isActive());
    }

    /**
     * Verifica si el perfil tiene una calificación establecida.
     *
     * @return true si tiene al menos una calificación
     */
    public boolean hasRating() {
        return averageRating != null && averageRating.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Obtiene la calificación como un valor double para cálculos.
     *
     * @return la calificación como double, 0.0 si no tiene calificación
     */
    public double getRatingAsDouble() {
        return hasRating() ? averageRating.doubleValue() : 0.0;
    }

    /**
     * Verifica si el cuidador tiene una biografía completada.
     *
     * @return true si la biografía no está vacía
     */
    public boolean hasBio() {
        return bio != null && !bio.trim().isEmpty();
    }

    /**
     * Verifica si el perfil está completo para aparecer en búsquedas.
     *
     * @return true si tiene todos los campos esenciales completados
     */
    public boolean isProfileComplete() {
        return hasBio() && profileImageUrl != null &&
                !profileImageUrl.trim().isEmpty() &&
                hourlyRate != null && servicingRadius != null;
    }

    /**
     * Marca el perfil como verificado.
     *
     * <p>Solo debe ser llamado desde el service layer después de
     * completar el proceso de verificación.</p>
     */
    public void markAsVerified() {
        this.isVerified = true;
    }

    /**
     * Desactiva temporalmente la disponibilidad para nuevas reservas.
     */
    public void setUnavailable() {
        this.isAvailableForBookings = false;
    }

    /**
     * Activa la disponibilidad para nuevas reservas.
     */
    public void setAvailable() {
        this.isAvailableForBookings = true;
    }

    // ========== EQUALS, HASHCODE Y TOSTRING ==========

    /**
     * Implementación de equals basada en el ID único y usuario asociado.
     *
     * <p>Se incluye el usuario como campo de negocio único para garantizar
     * consistencia en colecciones hash incluso durante el ciclo de vida de la entidad.</p>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SitterProfile)) return false;
        SitterProfile that = (SitterProfile) o;

        // Si ambos tienen ID, comparar por ID
        if (this.id != null && that.id != null) {
            return Objects.equals(this.id, that.id);
        }

        // Si no tienen ID, comparar por usuario único
        return Objects.equals(getUser(), that.getUser());
    }

    /**
     * Implementación de hashCode consistente con equals.
     */
    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(getUser());
    }

    /**
     * Representación de cadena para debugging y logging.
     *
     * <p>Incluye solo información básica para evitar lazy loading exceptions
     * y problemas de rendimiento.</p>
     */
    @Override
    public String toString() {
        return String.format("SitterProfile{id=%d, hourlyRate=%s, servicingRadius=%d, isVerified=%s, averageRating=%s, isAvailable=%s, createdAt=%s}",
                id, hourlyRate, servicingRadius, isVerified, averageRating, isAvailableForBookings, createdAt);
    }
}