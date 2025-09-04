package com.Petcare.Petcare.Models.Booking;

import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.Invoice.Invoice;
import com.Petcare.Petcare.Models.Pet;
import com.Petcare.Petcare.Models.PlatformFee;
import com.Petcare.Petcare.Models.ServiceOffering.ServiceOffering;
import com.Petcare.Petcare.Models.User.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad que representa una reserva de servicios de cuidado de mascotas.
 *
 * <p>Esta entidad almacena la información básica de una reserva, incluyendo
 * las relaciones con otras entidades del sistema y los timestamps de auditoría.
 * Es el núcleo del flujo de negocio, conectando clientes, cuidadores, mascotas
 * y servicios en una transacción completa.</p>
 *
 * <p><strong>Relaciones principales:</strong></p>
 * <ul>
 *   <li>Pertenece a una cuenta específica ({@link Account})</li>
 *   <li>Incluye una mascota específica ({@link Pet})</li>
 *   <li>Asignada a un cuidador ({@link User} con rol SITTER)</li>
 *   <li>Basada en una oferta de servicio ({@link ServiceOffering})</li>
 *   <li>Creada por un usuario propietario ({@link User} con rol CLIENT)</li>
 *   <li>Genera una factura ({@link Invoice})</li>
 *   <li>Calcula tarifas de plataforma ({@link PlatformFee})</li>
 * </ul>
 *
 * <p><strong>Estados posibles:</strong></p>
 * <ul>
 *   <li>PENDING: Reserva creada, esperando confirmación</li>
 *   <li>CONFIRMED: Confirmada por el cuidador</li>
 *   <li>IN_PROGRESS: Servicio en curso</li>
 *   <li>COMPLETED: Servicio completado</li>
 *   <li>CANCELLED: Reserva cancelada</li>
 * </ul>
 *
 * <p><strong>Flujo de negocio:</strong></p>
 * <ol>
 *   <li>Cliente crea reserva (PENDING)</li>
 *   <li>Cuidador confirma (CONFIRMED)</li>
 *   <li>Servicio inicia (IN_PROGRESS)</li>
 *   <li>Servicio termina (COMPLETED)</li>
 *   <li>Se genera factura automáticamente</li>
 * </ol>
 *
 * <p><strong>Nota:</strong> La lógica de negocio y validaciones complejas
 * se manejan en el service layer, no en esta entidad.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see Account
 * @see Pet
 * @see User
 * @see ServiceOffering
 * @see Invoice
 * @see PlatformFee
 * @see BookingStatus
 */
@Entity
@Table(name = "bookings",
        indexes = {
                @Index(name = "idx_booking_account", columnList = "account_id"),
                @Index(name = "idx_booking_status", columnList = "status"),
                @Index(name = "idx_booking_dates", columnList = "start_time, end_time"),
                @Index(name = "idx_booking_sitter", columnList = "sitter_id"),
                @Index(name = "idx_booking_client", columnList = "booked_by_user_id"),
                @Index(name = "idx_booking_pet", columnList = "pet_id"),
                @Index(name = "idx_booking_created_at", columnList = "created_at")
        })
@EntityListeners(AuditingEntityListener.class)
public class Booking {

    /**
     * Identificador único de la reserva.
     * Generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Cuenta a la cual pertenece esta reserva.
     *
     * <p>Relación obligatoria que establece la entidad financiera responsable
     * de los pagos y a la cual se enviará la facturación. Es derivada de la
     * mascota seleccionada, ya que cada mascota pertenece a una cuenta.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_account"))
    @NotNull(message = "La cuenta es obligatoria para crear una reserva")
    private Account account;

    /**
     * Mascota para la cual se solicita el servicio.
     *
     * <p>Relación obligatoria que establece qué mascota recibirá el cuidado.
     * La carga es lazy para optimizar el rendimiento.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_pet"))
    @NotNull(message = "La mascota es obligatoria para crear una reserva")
    private Pet pet;

    /**
     * Usuario cuidador asignado a esta reserva.
     *
     * <p>Debe ser un usuario con rol SITTER. El cuidador es responsable
     * de proporcionar el servicio acordado durante el período establecido.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sitter_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_sitter"))
    @NotNull(message = "El cuidador es obligatorio para crear una reserva")
    private User sitter;

    /**
     * Oferta de servicio específica reservada.
     *
     * <p>Define el tipo de servicio, duración base y precio que se aplicará
     * a esta reserva. Cada reserva debe estar basada en una oferta existente.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_offering_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_service_offering"))
    @NotNull(message = "La oferta de servicio es obligatoria")
    private ServiceOffering serviceOffering;

    /**
     * Usuario propietario que creó la reserva.
     *
     * <p>Generalmente el dueño de la mascota o un usuario autorizado
     * en la cuenta familiar. Debe tener permisos para crear reservas.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booked_by_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_booked_by_user"))
    @NotNull(message = "El usuario que crea la reserva es obligatorio")
    private User bookedByUser;

    /**
     * Fecha y hora programada de inicio del servicio.
     *
     * <p>Representa el momento acordado para comenzar el servicio.
     * Debe ser una fecha futura al momento de crear la reserva.</p>
     */
    @Column(name = "start_time", nullable = false)
    @NotNull(message = "La fecha y hora de inicio son obligatorias")
    private LocalDateTime startTime;

    /**
     * Fecha y hora programada de finalización del servicio.
     *
     * <p>Calculada basándose en la duración del servicio y la hora de inicio.
     * Debe ser posterior a la hora de inicio.</p>
     */
    @Column(name = "end_time", nullable = false)
    @NotNull(message = "La fecha y hora de finalización son obligatorias")
    private LocalDateTime endTime;

    /**
     * Fecha y hora real en que comenzó el servicio.
     *
     * <p>Campo opcional que se actualiza cuando el cuidador marca el inicio
     * real del servicio. Útil para calcular el tiempo real trabajado y
     * verificar puntualidad.</p>
     */
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    /**
     * Fecha y hora real en que finalizó el servicio.
     *
     * <p>Campo opcional que se actualiza cuando el cuidador marca la finalización
     * del servicio. Permite calcular la duración real y facturación precisa.</p>
     */
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    /**
     * Estado actual de la reserva.
     *
     * <p>Controla el flujo de trabajo y determina las acciones disponibles
     * para cada actor del sistema. Los cambios de estado se gestionan
     * en el service layer.</p>
     *
     * @see BookingStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "El estado de la reserva es obligatorio")
    private BookingStatus status = BookingStatus.PENDING;

    /**
     * Precio total acordado para el servicio.
     *
     * <p>Incluye el costo base del servicio más cualquier cargo adicional.
     * Se calcula al crear la reserva basándose en la duración y precio
     * de la oferta de servicio.</p>
     */
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El precio total es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El precio debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal totalPrice;

    /**
     * Notas adicionales sobre la reserva.
     *
     * <p>Campo de texto libre para instrucciones especiales, requisitos
     * particulares de la mascota, o cualquier información relevante
     * que deba conocer el cuidador.</p>
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Las notas no pueden exceder 2000 caracteres")
    private String notes;

    /**
     * Motivo de cancelación si aplica.
     *
     * <p>Campo requerido cuando el estado es CANCELLED. Proporciona
     * transparencia sobre las razones de cancelación y ayuda en el
     * análisis de calidad del servicio.</p>
     */
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    @Size(max = 1000, message = "El motivo de cancelación no puede exceder 1000 caracteres")
    private String cancellationReason;

    /**
     * Fecha y hora de creación de la reserva.
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
     * Útil para auditoría y control de concurrencia.</p>
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Tarifa de plataforma calculada para esta reserva.
     *
     * <p>Desglosa los costos de la plataforma, comisiones y montos netos
     * que corresponden tanto al cuidador como a la plataforma.</p>
     */
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PlatformFee platformFee;

    /**
     * Facturas generadas para esta reserva.
     *
     * <p>Normalmente una reserva genera una sola factura al completarse,
     * pero puede haber múltiples en casos de facturación parcial o
     * modificaciones posteriores.</p>
     */
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Invoice> invoices = new HashSet<>();

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido por JPA.
     */
    public Booking() {
        this.status = BookingStatus.PENDING;
    }

    /**
     * Constructor principal para crear una nueva reserva.
     *
     * <p>Solo incluye validaciones básicas de nulos. Las validaciones de negocio
     * complejas se manejan en el service layer.</p>
     *
     * @param account la cuenta responsable de esta reserva
     * @param pet la mascota que recibirá el servicio
     * @param sitter el cuidador asignado
     * @param serviceOffering la oferta de servicio seleccionada
     * @param bookedByUser el usuario que crea la reserva
     * @param startTime fecha y hora de inicio programada
     * @param endTime fecha y hora de finalización programada
     * @param totalPrice precio total acordado
     * @param notes notas adicionales (opcional)
     */
    public Booking(Account account, Pet pet, User sitter, ServiceOffering serviceOffering, User bookedByUser,
                   LocalDateTime startTime, LocalDateTime endTime, BigDecimal totalPrice, String notes) {
        this.account = account;
        this.pet = pet;
        this.sitter = sitter;
        this.serviceOffering = serviceOffering;
        this.bookedByUser = bookedByUser;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalPrice = totalPrice;
        this.notes = notes;
        this.status = BookingStatus.PENDING;
    }

    /**
     * Constructor completo con todos los campos.
     * Principalmente para uso interno y testing.
     */
    public Booking(Long id, Account account, Pet pet, User sitter, ServiceOffering serviceOffering, User bookedByUser,
                   LocalDateTime startTime, LocalDateTime endTime, LocalDateTime actualStartTime,
                   LocalDateTime actualEndTime, BookingStatus status, BigDecimal totalPrice,
                   String notes, String cancellationReason, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.account = account;
        this.pet = pet;
        this.sitter = sitter;
        this.serviceOffering = serviceOffering;
        this.bookedByUser = bookedByUser;
        this.startTime = startTime;
        this.endTime = endTime;
        this.actualStartTime = actualStartTime;
        this.actualEndTime = actualEndTime;
        this.status = status;
        this.totalPrice = totalPrice;
        this.notes = notes;
        this.cancellationReason = cancellationReason;
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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public User getSitter() {
        return sitter;
    }

    public void setSitter(User sitter) {
        this.sitter = sitter;
    }

    public ServiceOffering getServiceOffering() {
        return serviceOffering;
    }

    public void setServiceOffering(ServiceOffering serviceOffering) {
        this.serviceOffering = serviceOffering;
    }

    public User getBookedByUser() {
        return bookedByUser;
    }

    public void setBookedByUser(User bookedByUser) {
        this.bookedByUser = bookedByUser;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(LocalDateTime actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public LocalDateTime getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(LocalDateTime actualEndTime) {
        this.actualEndTime = actualEndTime;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
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

    public PlatformFee getPlatformFee() {
        return platformFee;
    }

    public void setPlatformFee(PlatformFee platformFee) {
        this.platformFee = platformFee;
    }

    public Set<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(Set<Invoice> invoices) {
        this.invoices = invoices;
    }

    // ========== MÉTODOS DE UTILIDAD PARA RELACIONES BIDIRECCIONALES ==========

    /**
     * Agrega una factura a esta reserva manteniendo la consistencia bidireccional.
     */
    public void addInvoice(Invoice invoice) {
        invoices.add(invoice);
        invoice.setBooking(this);
    }

    /**
     * Remueve una factura de esta reserva manteniendo la consistencia bidireccional.
     */
    public void removeInvoice(Invoice invoice) {
        invoices.remove(invoice);
        invoice.setBooking(null);
    }

    // ========== MÉTODOS DE UTILIDAD DE NEGOCIO ==========

    /**
     * Verifica si la reserva está en un estado que permite generar factura.
     *
     * @return true si el estado es COMPLETED
     */
    public boolean canGenerateInvoice() {
        return BookingStatus.COMPLETED.equals(this.status);
    }

    /**
     * Verifica si la reserva puede ser cancelada.
     *
     * @return true si el estado es PENDING o CONFIRMED
     */
    public boolean canBeCancelled() {
        return BookingStatus.PENDING.equals(this.status) ||
                BookingStatus.CONFIRMED.equals(this.status);
    }

    /**
     * Verifica si el servicio está actualmente en progreso.
     *
     * @return true si el estado es IN_PROGRESS
     */
    public boolean isInProgress() {
        return BookingStatus.IN_PROGRESS.equals(this.status);
    }

    /**
     * Verifica si la reserva está en un estado final.
     *
     * @return true si el estado es COMPLETED o CANCELLED
     */
    public boolean isFinalState() {
        return BookingStatus.COMPLETED.equals(this.status) ||
                BookingStatus.CANCELLED.equals(this.status);
    }

    // ========== EQUALS, HASHCODE Y TOSTRING ==========

    /**
     * Implementación de equals basada en el ID único y campos de negocio clave.
     *
     * <p>Se incluyen campos inmutables para garantizar consistencia en colecciones
     * hash incluso durante el ciclo de vida de la entidad.</p>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        Booking booking = (Booking) o;

        // Si ambos tienen ID, comparar por ID
        if (this.id != null && booking.id != null) {
            return Objects.equals(this.id, booking.id);
        }

        // Si no tienen ID, comparar por campos de negocio únicos
        return Objects.equals(getStartTime(), booking.getStartTime()) &&
                Objects.equals(getEndTime(), booking.getEndTime()) &&
                Objects.equals(getPet(), booking.getPet()) &&
                Objects.equals(getSitter(), booking.getSitter()) &&
                Objects.equals(getServiceOffering(), booking.getServiceOffering());
    }

    /**
     * Implementación de hashCode consistente con equals.
     */
    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(getStartTime(), getEndTime(), getPet(), getSitter(), getServiceOffering());
    }

    /**
     * Representación de cadena para debugging y logging.
     *
     * <p>Incluye solo información básica para evitar lazy loading exceptions
     * y problemas de rendimiento.</p>
     */
    @Override
    public String toString() {
        return String.format("Booking{id=%d, status=%s, startTime=%s, endTime=%s, totalPrice=%s, createdAt=%s}",
                id, status, startTime, endTime, totalPrice, createdAt);
    }
}