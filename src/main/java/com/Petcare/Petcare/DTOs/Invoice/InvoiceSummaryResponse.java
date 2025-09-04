package com.Petcare.Petcare.DTOs.Invoice;

import com.Petcare.Petcare.Models.Invoice.Invoice;
import com.Petcare.Petcare.Models.Invoice.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta con información resumida y optimizada de facturas.
 *
 * <p>Este DTO está diseñado específicamente para listados y vistas donde se
 * requiere información esencial sin la sobrecarga de datos completos. Optimiza
 * el rendimiento al transferir solo los campos más relevantes para operaciones
 * de listado y búsqueda.</p>
 *
 * <p><strong>Información incluida:</strong></p>
 * <ul>
 *   <li>Identificadores únicos (factura, cuenta, reserva)</li>
 *   <li>Información básica de presentación (números, nombres)</li>
 *   <li>Datos financieros esenciales (monto total)</li>
 *   <li>Estado y fechas para toma de decisiones rápidas</li>
 * </ul>
 *
 * <p><strong>Casos de uso principales:</strong></p>
 * <ul>
 *   <li>Listados de facturas paginados</li>
 *   <li>Dashboards financieros con múltiples facturas</li>
 *   <li>Respuestas de API para operaciones de búsqueda</li>
 *   <li>Vistas móviles con espacio limitado</li>
 *   <li>Reportes de estado de facturas</li>
 *   <li>Feeds de actividad de facturación</li>
 * </ul>
 *
 * <p><strong>Optimizaciones aplicadas:</strong></p>
 * <ul>
 *   <li>Solo campos esenciales para reducir payload</li>
 *   <li>Sin relaciones anidadas complejas</li>
 *   <li>Datos preformateados para presentación directa</li>
 *   <li>Nombres denormalizados para evitar JOINs adicionales</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 2.0
 * @since 1.0
 * @see Invoice
 * @see InvoiceDetailResponse
 */
public class InvoiceSummaryResponse {

    /**
     * Identificador único de la factura.
     */
    private Long id;

    /**
     * Número único de factura para identificación externa.
     *
     * <p>Formato típico: INV-YYYY-timestamp. Información clave
     * para referencias rápidas en listados.</p>
     */
    private String invoiceNumber;

    /**
     * Identificador de la cuenta facturada.
     *
     * <p>Permite filtrado y agrupación por cuenta sin
     * necesidad de cargar la entidad completa.</p>
     */
    private Long accountId;

    /**
     * Número de cuenta para presentación.
     *
     * <p>Información denormalizada para mostrar directamente
     * en listados sin consultas adicionales.</p>
     */
    private String accountNumber;

    /**
     * Identificador de la reserva que origina la factura.
     *
     * <p>Útil para navegación directa a detalles de la reserva
     * desde vistas de facturas.</p>
     */
    private Long bookingId;

    /**
     * Fecha de emisión de la factura.
     *
     * <p>Campo clave para ordenamiento temporal y identificación
     * de período de facturación.</p>
     */
    private LocalDateTime issueDate;

    /**
     * Fecha límite para el pago.
     *
     * <p>Permite identificación rápida de facturas próximas
     * a vencer o ya vencidas para priorización.</p>
     */
    private LocalDateTime dueDate;

    /**
     * Monto total de la factura.
     *
     * <p>Información financiera principal para cálculos rápidos
     * y presentación en listados de facturación.</p>
     */
    private BigDecimal totalAmount;

    /**
     * Estado actual de la factura.
     *
     * <p>Permite filtrado rápido por estado y identificación visual
     * del flujo de trabajo sin cargar información completa.</p>
     *
     * @see InvoiceStatus
     */
    private InvoiceStatus status;

    /**
     * Nombre completo del cliente facturado.
     *
     * <p>Información denormalizada para presentación directa
     * sin necesidad de resolver relaciones con usuarios.</p>
     */
    private String clientName;

    /**
     * Nombre completo del cuidador que proporcionó el servicio.
     *
     * <p>Contexto adicional útil para identificar rápidamente
     * el servicio asociado a la factura.</p>
     */
    private String sitterName;

    /**
     * Fecha y hora de creación de la factura.
     *
     * <p>Útil para ordenamiento cronológico y auditoría
     * del proceso de facturación.</p>
     */
    private LocalDateTime createdAt;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido para frameworks de serialización.
     */
    public InvoiceSummaryResponse() {
    }

    /**
     * Constructor completo para creación programática y testing.
     *
     * @param id identificador de la factura
     * @param invoiceNumber número de factura
     * @param accountId identificador de cuenta
     * @param accountNumber número de cuenta
     * @param bookingId identificador de reserva
     * @param issueDate fecha de emisión
     * @param dueDate fecha de vencimiento
     * @param totalAmount monto total
     * @param status estado actual
     * @param clientName nombre del cliente
     * @param sitterName nombre del cuidador
     * @param createdAt fecha de creación
     */
    public InvoiceSummaryResponse(Long id, String invoiceNumber, Long accountId, String accountNumber,
                                  Long bookingId, LocalDateTime issueDate, LocalDateTime dueDate,
                                  BigDecimal totalAmount, InvoiceStatus status, String clientName,
                                  String sitterName, LocalDateTime createdAt) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.bookingId = bookingId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.clientName = clientName;
        this.sitterName = sitterName;
        this.createdAt = createdAt;
    }

    // ========== MÉTODO DE FÁBRICA ==========

    /**
     * Crea una instancia de InvoiceSummaryResponse desde una entidad Invoice.
     *
     * <p>Este método de fábrica extrae solo la información esencial y maneja
     * de forma segura los casos donde las relaciones pueden ser null.</p>
     *
     * <p><strong>Comportamiento con valores null:</strong></p>
     * <ul>
     *   <li>Si account es null, accountId y accountNumber serán null</li>
     *   <li>Si booking es null, bookingId será null</li>
     *   <li>Si usuarios relacionados son null, nombres serán null</li>
     *   <li>Los campos primitivos se copian directamente</li>
     * </ul>
     *
     * @param invoice la entidad Invoice a convertir
     * @return nueva instancia de InvoiceSummaryResponse
     * @throws IllegalArgumentException si invoice es null
     */
    public static InvoiceSummaryResponse fromEntity(Invoice invoice) {
        if (invoice == null) {
            throw new IllegalArgumentException("La entidad Invoice no puede ser null");
        }

        // Extraer información de la cuenta
        Long accountId = null;
        String accountNumber = null;
        if (invoice.getAccount() != null) {
            accountId = invoice.getAccount().getId();
            accountNumber = invoice.getAccount().getAccountNumber();
        }

        // Extraer ID de la reserva
        Long bookingId = null;
        if (invoice.getBooking() != null) {
            bookingId = invoice.getBooking().getId();
        }

        // Formatear nombres de usuarios
        String clientName = null;
        String sitterName = null;

        if (invoice.getBooking() != null) {
            if (invoice.getBooking().getBookedByUser() != null) {
                clientName = formatUserName(
                        invoice.getBooking().getBookedByUser().getFirstName(),
                        invoice.getBooking().getBookedByUser().getLastName()
                );
            }

            if (invoice.getBooking().getSitter() != null) {
                sitterName = formatUserName(
                        invoice.getBooking().getSitter().getFirstName(),
                        invoice.getBooking().getSitter().getLastName()
                );
            }
        }

        return new InvoiceSummaryResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                accountId,
                accountNumber,
                bookingId,
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getTotalAmount(),
                invoice.getStatus(),
                clientName,
                sitterName,
                invoice.getCreatedAt()
        );
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
     * Verifica si la factura requiere atención inmediata.
     *
     * <p>Útil para destacar facturas en dashboards que necesitan
     * seguimiento o están próximas a vencer.</p>
     *
     * @return true si la factura está vencida o próxima a vencer
     */
    public boolean requiresAttention() {
        return status == InvoiceStatus.OVERDUE ||
                (status == InvoiceStatus.SENT && isNearDueDate());
    }

    /**
     * Verifica si la factura está próxima a la fecha de vencimiento.
     *
     * @return true si faltan 3 días o menos para vencimiento
     */
    public boolean isNearDueDate() {
        if (dueDate == null) return false;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysFromNow = now.plusDays(3);
        return dueDate.isBefore(threeDaysFromNow) && dueDate.isAfter(now);
    }

    /**
     * Verifica si la factura está en un estado final.
     *
     * @return true si la factura está pagada, cancelada o reembolsada
     */
    public boolean isFinalState() {
        return status == InvoiceStatus.PAID ||
                status == InvoiceStatus.CANCELLED ||
                status == InvoiceStatus.REFUNDED;
    }

    /**
     * Verifica si la factura puede recibir pagos.
     *
     * @return true si está en estado que permite pagos
     */
    public boolean canReceivePayments() {
        return status == InvoiceStatus.SENT ||
                status == InvoiceStatus.OVERDUE ||
                status == InvoiceStatus.PARTIALLY_PAID;
    }

    /**
     * Proporciona una etiqueta de estado localizada para la UI.
     *
     * @return string representativo del estado para mostrar al usuario
     */
    public String getStatusLabel() {
        if (status == null) return "Desconocido";

        switch (status) {
            case DRAFT: return "Borrador";
            case SENT: return "Enviada";
            case PAID: return "Pagada";
            case PARTIALLY_PAID: return "Pago Parcial";
            case OVERDUE: return "Vencida";
            case CANCELLED: return "Cancelada";
            case REFUNDED: return "Reembolsada";
            default: return status.toString();
        }
    }

    /**
     * Calcula los días restantes hasta el vencimiento.
     *
     * @return días hasta vencimiento, negativo si ya venció
     */
    public long getDaysUntilDue() {
        if (dueDate == null) return 0;
        return java.time.Duration.between(LocalDateTime.now(), dueDate).toDays();
    }

    // ========== GETTERS Y SETTERS ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public LocalDateTime getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDateTime issueDate) { this.issueDate = issueDate; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getSitterName() { return sitterName; }
    public void setSitterName(String sitterName) { this.sitterName = sitterName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "InvoiceSummaryResponse{" +
                "id=" + id +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", issueDate=" + issueDate +
                ", dueDate=" + dueDate +
                ", clientName='" + clientName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}