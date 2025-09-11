package com.Petcare.Petcare.DTOs.Booking;

import jakarta.validation.constraints.*;
import com.Petcare.Petcare.Models.Booking.Booking;
import java.time.LocalDateTime;

/**
 * DTO para la creación de nuevas reservas de servicios de cuidado de mascotas.
 *
 * <p>Este DTO contiene únicamente los campos que el cliente debe proporcionar
 * para crear una nueva reserva. Los campos calculados como {@code endTime} y
 * {@code totalPrice} se generan automáticamente en el servidor.</p>
 *
 * <p><strong>Campos calculados automáticamente:</strong></p>
 * <ul>
 *   <li>{@code endTime}: Se calcula basándose en {@code startTime} y la duración del servicio</li>
 *   <li>{@code totalPrice}: Se calcula desde la oferta de servicio y duración</li>
 *   <li>{@code bookedByUser}: Se obtiene del contexto de autenticación</li>
 *   <li>{@code status}: Se inicializa como {@code PENDING}</li>
 * </ul>
 *
 * <p><strong>Validaciones aplicadas:</strong></p>
 * <ul>
 *   <li>Todos los IDs de referencia son obligatorios</li>
 *   <li>La fecha de inicio debe ser futura</li>
 *   <li>Las notas tienen límite de caracteres</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see Booking
 */
public class CreateBookingRequest {

    /**
     * Identificador de la mascota que recibirá el servicio.
     *
     * <p>Debe existir en el sistema y pertenecer al usuario autenticado
     * o estar en su círculo familiar autorizado.</p>
     */
    @NotNull(message = "El ID de la mascota es obligatorio")
    private Long petId;

    /**
     * Identificador del cuidador que proporcionará el servicio.
     *
     * <p>Debe ser un usuario con rol SITTER y tener la oferta de servicio
     * especificada disponible para la fecha solicitada.</p>
     */
    @NotNull(message = "El ID del cuidador es obligatorio")
    private Long sitterId;

    /**
     * Identificador de la oferta de servicio específica reservada.
     *
     * <p>Debe pertenecer al cuidador especificado y estar activa.
     * Define el tipo de servicio, precio base y duración.</p>
     */
    @NotNull(message = "El ID de la oferta de servicio es obligatorio")
    private Long serviceOfferingId;

    /**
     * Fecha y hora programada de inicio del servicio.
     *
     * <p>Debe ser una fecha futura y estar dentro del horario disponible
     * del cuidador. El sistema validará la disponibilidad antes de confirmar.</p>
     */
    @NotNull(message = "La fecha y hora de inicio son obligatorias")
    @Future(message = "La fecha de inicio debe ser futura")
    private LocalDateTime startTime;

    /**
     * Notas adicionales o instrucciones especiales para el cuidador.
     *
     * <p>Campo opcional que puede incluir información sobre rutinas especiales,
     * medicamentos, comportamiento de la mascota, o cualquier detalle relevante
     * para el servicio.</p>
     */
    @Size(max = 2000, message = "Las notas no pueden exceder 2000 caracteres")
    private String notes;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido para frameworks de serialización.
     */
    public CreateBookingRequest() {
    }

    /**
     * Constructor completo para facilitar testing y creación programática.
     *
     * @param petId identificador de la mascota
     * @param sitterId identificador del cuidador
     * @param serviceOfferingId identificador de la oferta de servicio
     * @param startTime fecha y hora de inicio programada
     * @param notes notas adicionales (puede ser null)
     */
    public CreateBookingRequest(Long petId, Long sitterId, Long serviceOfferingId,
                                LocalDateTime startTime, String notes) {
        this.petId = petId;
        this.sitterId = sitterId;
        this.serviceOfferingId = serviceOfferingId;
        this.startTime = startTime;
        this.notes = notes;
    }

    // ========== GETTERS ==========


    public Long getPetId() {
        return petId;
    }

    public Long getSitterId() {
        return sitterId;
    }

    public Long getServiceOfferingId() {
        return serviceOfferingId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public String getNotes() {
        return notes;
    }
}