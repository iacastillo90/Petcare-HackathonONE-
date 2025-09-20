package com.Petcare.Petcare.DTOs.Sitter;

import com.Petcare.Petcare.Models.SitterProfile;
import com.Petcare.Petcare.Models.User.User;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * DTO para transferir la información completa de un perfil de cuidador (Sitter).
 *
 * <p>Esta clase se utiliza como un objeto de transferencia de datos genérico para representar
 * la entidad {@link SitterProfile}. A diferencia de DTOs más específicos como los de creación o resumen,
 * este contiene todos los campos del perfil, siendo útil para respuestas de API que devuelven
 * el detalle completo de un perfil o para la lógica interna entre capas de servicio.</p>
 *
 * <p><b>Casos de uso:</b></p>
 * <ul>
 * <li>Como cuerpo de respuesta en endpoints que devuelven el perfil detallado de un cuidador.</li>
 * <li>Para crear o actualizar un perfil de cuidador en la capa de servicio.</li>
 * <li>Para transportar la información completa del perfil entre diferentes componentes de la aplicación.</li>
 * </ul>
 *
 * @see SitterProfile
 * @see SitterProfileSummary
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 */
public class SitterProfileDTO {

    /**
     * El identificador único del perfil de cuidador.
     */
    private Long id;

    /**
     * El ID del {@link User} al que está asociado este perfil.
     */
    @NotNull(message = "El ID del usuario es obligatorio.")
    private Long userId;

    /**
     * La biografía profesional del cuidador, donde describe su experiencia y servicios.
     */
    @NotBlank(message = "La biografía no puede estar vacía.")
    @Size(min = 10, max = 2000, message = "La biografía debe tener entre 10 y 2000 caracteres.")
    private String bio;

    /**
     * La tarifa por hora que el cuidador cobra por sus servicios básicos.
     */
    @NotNull(message = "La tarifa por hora es obligatoria.")
    @DecimalMin(value = "0.01", message = "La tarifa por hora debe ser mayor que cero.")
    @Digits(integer = 6, fraction = 2, message = "Formato de tarifa inválido.")
    private BigDecimal hourlyRate;

    /**
     * El radio de servicio en kilómetros que el cuidador está dispuesto a cubrir.
     */
    @NotNull(message = "El radio de servicio es obligatorio.")
    @Min(value = 1, message = "El radio de servicio debe ser de al menos 1 km.")
    @Max(value = 100, message = "El radio de servicio no puede exceder los 100 km.")
    private Integer servicingRadius;

    /**
     * La URL de la imagen de perfil profesional del cuidador.
     */
    private String profileImageUrl;

    /**
     * Indica si el perfil ha sido verificado por la plataforma Petcare.
     */
    private boolean verified;

    /**
     * Indica si el cuidador está actualmente disponible para aceptar nuevas reservas.
     */
    private boolean availableForBookings;

    /**
     * Constructor por defecto.
     * <p>Requerido por frameworks de serialización/deserialización como Jackson.</p>
     */
    public SitterProfileDTO() {
    }

    /**
     * Constructor completo para crear una instancia del DTO con todos sus campos.
     *
     * @param id El ID del perfil.
     * @param userId El ID del usuario asociado.
     * @param bio La biografía del cuidador.
     * @param hourlyRate La tarifa por hora.
     * @param servicingRadius El radio de servicio.
     * @param profileImageUrl La URL de la imagen de perfil.
     * @param verified El estado de verificación.
     * @param availableForBookings El estado de disponibilidad.
     */
    public SitterProfileDTO(Long id, Long userId, String bio, BigDecimal hourlyRate,
                            Integer servicingRadius, String profileImageUrl,
                            boolean verified, boolean availableForBookings) {
        this.id = id;
        this.userId = userId;
        this.bio = bio;
        this.hourlyRate = hourlyRate;
        this.servicingRadius = servicingRadius;
        this.profileImageUrl = profileImageUrl;
        this.verified = verified;
        this.availableForBookings = availableForBookings;
    }

    // ========== Getters y Setters ==========

    /**
     * Obtiene el ID del perfil.
     * @return El identificador único del perfil.
     */
    public Long getId() {
        return id;
    }

    /**
     * Establece el ID del perfil.
     * @param id El nuevo identificador único.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtiene el ID del usuario asociado.
     * @return El ID del usuario.
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Establece el ID del usuario asociado.
     * @param userId El nuevo ID de usuario.
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Obtiene la biografía del cuidador.
     * @return La biografía profesional.
     */
    public String getBio() {
        return bio;
    }

    /**
     * Establece la biografía del cuidador.
     * @param bio La nueva biografía.
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Obtiene la tarifa por hora del cuidador.
     * @return La tarifa por hora.
     */
    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    /**
     * Establece la tarifa por hora del cuidador.
     * @param hourlyRate La nueva tarifa.
     */
    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    /**
     * Obtiene el radio de servicio del cuidador.
     * @return El radio de servicio en kilómetros.
     */
    public Integer getServicingRadius() {
        return servicingRadius;
    }

    /**
     * Establece el radio de servicio del cuidador.
     * @param servicingRadius El nuevo radio de servicio.
     */
    public void setServicingRadius(Integer servicingRadius) {
        this.servicingRadius = servicingRadius;
    }

    /**
     * Obtiene la URL de la imagen de perfil.
     * @return La URL de la imagen.
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * Establece la URL de la imagen de perfil.
     * @param profileImageUrl La nueva URL.
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * Verifica si el perfil está verificado.
     * @return {@code true} si está verificado, de lo contrario {@code false}.
     */
    public boolean isVerified() {
        return verified;
    }

    /**
     * Establece el estado de verificación del perfil.
     * @param verified El nuevo estado de verificación.
     */
    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    /**
     * Verifica si el cuidador está disponible para reservas.
     * @return {@code true} si está disponible, de lo contrario {@code false}.
     */
    public boolean isAvailableForBookings() {
        return availableForBookings;
    }

    /**
     * Establece el estado de disponibilidad del cuidador.
     * @param availableForBookings El nuevo estado de disponibilidad.
     */
    public void setAvailableForBookings(boolean availableForBookings) {
        this.availableForBookings = availableForBookings;
    }
}