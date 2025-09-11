package com.Petcare.Petcare.DTOs.Booking;

import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Booking.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para representar una vista resumida y optimizada de una reserva.
 *
 * <p>Este DTO está diseñado específicamente para listados y vistas donde se
 * requiere información esencial sin la sobrecarga de datos completos. Optimiza
 * el rendimiento al transferir solo los campos más relevantes.</p>
 *
 * <p><strong>Información incluida:</strong></p>
 * <ul>
 *   <li>Identificador único de la reserva</li>
 *   <li>Información básica de presentación (nombres)</li>
 *   <li>Datos temporales esenciales</li>
 *   <li>Estado y precio para toma de decisiones rápidas</li>
 * </ul>
 *
 * <p><strong>Casos de uso principales:</strong></p>
 * <ul>
 *   <li>Listados de reservas paginados</li>
 *   <li>Dashboards con múltiples reservas</li>
 *   <li>Respuestas de API para operaciones de búsqueda</li>
 *   <li>Vistas móviles con espacio limitado</li>
 *   <li>Feeds de actividad reciente</li>
 * </ul>
 *
 * <p><strong>Optimizaciones aplicadas:</strong></p>
 * <ul>
 *   <li>Solo campos esenciales para reducir payload</li>
 *   <li>Sin relaciones anidadas complejas</li>
 *   <li>Datos preformateados para presentación directa</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see Booking
 * @see BookingDetailResponse
 */
public class BookingSummaryResponse {

    /**
     * Identificador único de la reserva.
     */
    private Long id;

    /**
     * Nombre de la mascota que recibe el servicio.
     *
     * <p>Proporciona contexto inmediato sobre qué mascota está involucrada
     * sin necesidad de consultas adicionales.</p>
     */
    private String petName;

    /**
     * Nombre completo del cuidador asignado.
     *
     * <p>Información esencial para identificar rápidamente quién proporcionará
     * el servicio, especialmente útil en listados.</p>
     */
    private String sitterName;

    /**
     * Fecha y hora programada de inicio del servicio.
     *
     * <p>Campo clave para ordenamiento temporal y identificación rápida
     * de cuándo ocurrirá o ocurrió el servicio.</p>
     */
    private LocalDateTime startTime;

    /**
     * Estado actual de la reserva.
     *
     * <p>Permite filtrado rápido y identificación visual del estado
     * sin necesidad de cargar información completa.</p>
     *
     * @see BookingStatus
     */
    private BookingStatus status;

    /**
     * Precio total del servicio.
     *
     * <p>Información financiera básica para cálculos rápidos y
     * presentación en listados de facturación.</p>
     */
    private BigDecimal totalPrice;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido para frameworks de serialización.
     */
    public BookingSummaryResponse() {
    }

    /**
     * Constructor completo para creación programática y testing.
     *
     * @param id identificador de la reserva
     * @param petName nombre de la mascota
     * @param sitterName nombre del cuidador
     * @param startTime fecha de inicio programada
     * @param status estado actual
     * @param totalPrice precio total
     */
    public BookingSummaryResponse(Long id, String petName, String sitterName,
                                  LocalDateTime startTime, BookingStatus status,
                                  BigDecimal totalPrice) {
        this.id = id;
        this.petName = petName;
        this.sitterName = sitterName;
        this.startTime = startTime;
        this.status = status;
        this.totalPrice = totalPrice;
    }

    // ========== MÉTODO DE FÁBRICA ==========

    /**
     * Crea una instancia de BookingSummaryResponse desde una entidad Booking.
     *
     * <p>Este método de fábrica extrae solo la información esencial y maneja
     * de forma segura los casos donde las relaciones pueden ser null.</p>
     *
     * <p><strong>Comportamiento con valores null:</strong></p>
     * <ul>
     *   <li>Si pet es null, petName será null</li>
     *   <li>Si sitter es null, sitterName será null</li>
     *   <li>Los campos primitivos se copian directamente</li>
     * </ul>
     *
     * @param booking la entidad Booking a convertir
     * @return nueva instancia de BookingSummaryResponse
     * @throws IllegalArgumentException si booking es null
     */
    public static BookingSummaryResponse fromEntity(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("La entidad Booking no puede ser null");
        }

        BookingSummaryResponse dto = new BookingSummaryResponse();
        dto.setId(booking.getId());

        // Información de la mascota
        dto.setPetName(booking.getPet() != null ? booking.getPet().getName() : null);

        // Información del cuidador
        if (booking.getSitter() != null) {
            dto.setSitterName(formatUserName(
                    booking.getSitter().getFirstName(),
                    booking.getSitter().getLastName()
            ));
        }

        // Datos esenciales
        dto.setStartTime(booking.getStartTime());
        dto.setStatus(booking.getStatus());
        dto.setTotalPrice(booking.getTotalPrice());

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
     * Verifica si la reserva requiere atención inmediata.
     *
     * <p>Útil para destacar reservas en dashboards que necesitan
     * confirmación o están próximas a comenzar.</p>
     *
     * @return true si la reserva está pendiente o confirmada
     */
    public boolean requiresAttention() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }

    /**
     * Verifica si la reserva está en un estado final.
     *
     * @return true si la reserva está completada o cancelada
     */
    public boolean isFinalized() {
        return status == BookingStatus.COMPLETED || status == BookingStatus.CANCELLED;
    }

    /**
     * Proporciona una etiqueta de estado localizada para la UI.
     *
     * @return string representativo del estado para mostrar al usuario
     */
    public String getStatusLabel() {
        if (status == null) return "Desconocido";

        switch (status) {
            case PENDING: return "Pendiente";
            case CONFIRMED: return "Confirmada";
            case IN_PROGRESS: return "En Progreso";
            case COMPLETED: return "Completada";
            case CANCELLED: return "Cancelada";
            default: return status.toString();
        }
    }

    // ========== GETTERS Y SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getSitterName() {
        return sitterName;
    }

    public void setSitterName(String sitterName) {
        this.sitterName = sitterName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "BookingSummaryResponse{" +
                "id=" + id +
                ", petName='" + petName + '\'' +
                ", sitterName='" + sitterName + '\'' +
                ", startTime=" + startTime +
                ", status=" + status +
                ", totalPrice=" + totalPrice +
                '}';
    }
}