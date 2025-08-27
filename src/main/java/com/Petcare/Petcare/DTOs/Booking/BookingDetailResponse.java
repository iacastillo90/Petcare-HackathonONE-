package com.Petcare.Petcare.DTOs.Booking;

import com.Petcare.Petcare.DTOs.PlatformFee.PlatformFeeDTO;
import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Booking.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para representar una vista completa y detallada de una reserva.
 *
 * <p>Este DTO incluye toda la información relevante de una reserva, con datos
 * denormalizados de las entidades relacionadas para optimizar la presentación
 * en el frontend y minimizar consultas adicionales.</p>
 *
 * <p><strong>Datos denormalizados incluidos:</strong></p>
 * <ul>
 *   <li>Nombres completos de usuarios (cuidador y cliente)</li>
 *   <li>Nombre de la mascota</li>
 *   <li>Título del servicio contratado</li>
 *   <li>Información completa de tarifas de plataforma</li>
 * </ul>
 *
 * <p><strong>Casos de uso principales:</strong></p>
 * <ul>
 *   <li>Vista detallada de reserva individual</li>
 *   <li>Respuestas de API para operaciones GET específicas</li>
 *   <li>Pantallas de confirmación y seguimiento</li>
 *   <li>Reportes y auditorías</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see Booking
 * @see BookingSummaryResponse
 */
public class BookingDetailResponse {

    private Long id;

    // ========== INFORMACIÓN DE LA MASCOTA ==========

    /**
     * Identificador de la mascota que recibe el servicio.
     */
    private Long petId;

    /**
     * Nombre de la mascota para mostrar en la interfaz.
     */
    private String petName;

    // ========== INFORMACIÓN DEL CUIDADOR ==========

    /**
     * Identificador del cuidador asignado.
     */
    private Long sitterId;

    /**
     * Nombre completo del cuidador responsable del servicio.
     */
    private String sitterName;

    // ========== INFORMACIÓN DEL SERVICIO ==========

    /**
     * Identificador de la oferta de servicio contratada.
     */
    private Long serviceOfferingId;

    /**
     * Título descriptivo del servicio contratado.
     */
    private String serviceName;

    // ========== INFORMACIÓN DEL CLIENTE ==========

    /**
     * Identificador del usuario que creó la reserva.
     */
    private Long bookedByUserId;

    /**
     * Nombre completo del usuario que realizó la reserva.
     */
    private String bookedByUserName;

    // ========== DATOS TEMPORALES ==========

    /**
     * Fecha y hora programada de inicio del servicio.
     */
    private LocalDateTime startTime;

    /**
     * Fecha y hora programada de finalización del servicio.
     */
    private LocalDateTime endTime;

    /**
     * Fecha y hora real de inicio del servicio.
     *
     * <p>Se actualiza cuando el cuidador marca el inicio real.
     * Null si el servicio no ha comenzado.</p>
     */
    private LocalDateTime actualStartTime;

    /**
     * Fecha y hora real de finalización del servicio.
     *
     * <p>Se actualiza cuando el cuidador marca la finalización.
     * Null si el servicio no ha terminado.</p>
     */
    private LocalDateTime actualEndTime;

    // ========== ESTADO Y PRECIO ==========

    /**
     * Estado actual de la reserva en el flujo de trabajo.
     *
     * @see BookingStatus
     */
    private BookingStatus status;

    /**
     * Precio total acordado para el servicio completo.
     */
    private BigDecimal totalPrice;

    // ========== INFORMACIÓN ADICIONAL ==========

    /**
     * Notas adicionales sobre la reserva o instrucciones especiales.
     */
    private String notes;

    /**
     * Motivo de cancelación si la reserva fue cancelada.
     *
     * <p>Solo presente cuando el status es CANCELLED.</p>
     */
    private String cancellationReason;

    // ========== METADATOS DE AUDITORÍA ==========

    /**
     * Fecha y hora de creación de la reserva.
     */
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última modificación.
     */
    private LocalDateTime updatedAt;

    // ========== INFORMACIÓN FINANCIERA ==========

    /**
     * Desglose detallado de las tarifas de plataforma aplicadas.
     *
     * <p>Incluye comisiones, montos netos y cálculos de facturación.
     * Null si aún no se han calculado las tarifas.</p>
     */
    private PlatformFeeDTO platformFee;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido para frameworks de serialización.
     */
    public BookingDetailResponse() {
    }

    // ========== MÉTODO DE FÁBRICA ==========

    /**
     * Crea una instancia de BookingDetailResponse desde una entidad Booking.
     *
     * <p>Este método de fábrica maneja de forma segura las relaciones que pueden
     * ser null y formatea los datos para su presentación óptima.</p>
     *
     * <p><strong>Manejo de valores null:</strong></p>
     * <ul>
     *   <li>Si una relación es null, se asigna null al campo correspondiente</li>
     *   <li>Los nombres se formatean concatenando first + last name</li>
     *   <li>Se preservan todos los campos opcionales como están</li>
     * </ul>
     *
     * @param booking la entidad Booking a convertir
     * @return nueva instancia de BookingDetailResponse con datos poblados
     * @throws IllegalArgumentException si booking es null
     */
    public static BookingDetailResponse fromEntity(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("La entidad Booking no puede ser null");
        }

        BookingDetailResponse dto = new BookingDetailResponse();
        dto.setId(booking.getId());

        // Información de la mascota
        if (booking.getPet() != null) {
            dto.setPetId(booking.getPet().getId());
            dto.setPetName(booking.getPet().getName());
        }

        // Información del cuidador
        if (booking.getSitter() != null) {
            dto.setSitterId(booking.getSitter().getId());
            dto.setSitterName(formatUserName(
                    booking.getSitter().getFirstName(),
                    booking.getSitter().getLastName()
            ));
        }

        // Información del servicio
        if (booking.getServiceOffering() != null) {
            dto.setServiceOfferingId(booking.getServiceOffering().getId());
            dto.setServiceName(booking.getServiceOffering().getName());
        }

        // Información del cliente
        if (booking.getBookedByUser() != null) {
            dto.setBookedByUserId(booking.getBookedByUser().getId());
            dto.setBookedByUserName(formatUserName(
                    booking.getBookedByUser().getFirstName(),
                    booking.getBookedByUser().getLastName()
            ));
        }

        // Datos temporales
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setActualStartTime(booking.getActualStartTime());
        dto.setActualEndTime(booking.getActualEndTime());

        // Estado y precio
        dto.setStatus(booking.getStatus());
        dto.setTotalPrice(booking.getTotalPrice());

        // Información adicional
        dto.setNotes(booking.getNotes());
        dto.setCancellationReason(booking.getCancellationReason());

        // Metadatos de auditoría
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());

        // Información financiera
        if (booking.getPlatformFee() != null) {
            dto.setPlatformFee(new PlatformFeeDTO(booking.getPlatformFee()));
        }

        return dto;
    }

    /**
     * Formatea un nombre completo de usuario de manera segura.
     *
     * @param firstName nombre
     * @param lastName apellido
     * @return nombre completo formateado, o null si ambos son null
     */
    private static String formatUserName(String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            return null;
        }

        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";

        return (first + " " + last).trim();
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Verifica si la reserva está en un estado activo (confirmada o en progreso).
     *
     * @return true si la reserva está activa
     */
    public boolean isActive() {
        return status == BookingStatus.CONFIRMED || status == BookingStatus.IN_PROGRESS;
    }

    /**
     * Verifica si la reserva puede ser cancelada por el cliente.
     *
     * @return true si la reserva puede ser cancelada
     */
    public boolean isCancellable() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }

    /**
     * Verifica si el servicio ha sido completado exitosamente.
     *
     * @return true si la reserva está completada
     */
    public boolean isCompleted() {
        return status == BookingStatus.COMPLETED;
    }

    /**
     * Calcula la duración programada del servicio en horas.
     *
     * @return duración en horas, o 0 si las fechas no están disponibles
     */
    public long getScheduledDurationInHours() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toHours();
        }
        return 0;
    }

    /**
     * Calcula la duración real del servicio en horas.
     *
     * @return duración real en horas, o 0 si las fechas reales no están disponibles
     */
    public long getActualDurationInHours() {
        if (actualStartTime != null && actualEndTime != null) {
            return java.time.Duration.between(actualStartTime, actualEndTime).toHours();
        }
        return 0;
    }

    // ========== GETTERS ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }

    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }

    public Long getSitterId() { return sitterId; }
    public void setSitterId(Long sitterId) { this.sitterId = sitterId; }

    public String getSitterName() { return sitterName; }
    public void setSitterName(String sitterName) { this.sitterName = sitterName; }

    public Long getServiceOfferingId() { return serviceOfferingId; }
    public void setServiceOfferingId(Long serviceOfferingId) { this.serviceOfferingId = serviceOfferingId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public Long getBookedByUserId() { return bookedByUserId; }
    public void setBookedByUserId(Long bookedByUserId) { this.bookedByUserId = bookedByUserId; }

    public String getBookedByUserName() { return bookedByUserName; }
    public void setBookedByUserName(String bookedByUserName) { this.bookedByUserName = bookedByUserName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public LocalDateTime getActualStartTime() { return actualStartTime; }
    public void setActualStartTime(LocalDateTime actualStartTime) { this.actualStartTime = actualStartTime; }

    public LocalDateTime getActualEndTime() { return actualEndTime; }
    public void setActualEndTime(LocalDateTime actualEndTime) { this.actualEndTime = actualEndTime; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public PlatformFeeDTO getPlatformFee() { return platformFee; }
    public void setPlatformFee(PlatformFeeDTO platformFee) { this.platformFee = platformFee; }

    @Override
    public String toString() {
        return "BookingDetailResponse{" +
                "id=" + id +
                ", petName='" + petName + '\'' +
                ", sitterName='" + sitterName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", status=" + status +
                ", startTime=" + startTime +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
