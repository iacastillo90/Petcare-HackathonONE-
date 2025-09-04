package com.Petcare.Petcare.DTOs.Invoice;

import com.Petcare.Petcare.DTOs.Account.AccountInfo;
import com.Petcare.Petcare.DTOs.Booking.BookingInfo;
import com.Petcare.Petcare.DTOs.Invoice.InvoiceItem.InvoiceItemResponse;
import com.Petcare.Petcare.DTOs.Payment.PaymentSummary;
import com.Petcare.Petcare.Models.Invoice.Invoice;
import com.Petcare.Petcare.Models.Invoice.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO de respuesta con información completa y detallada de facturas.
 *
 * <p>Este DTO incluye toda la información relevante de una factura, con datos
 * denormalizados de las entidades relacionadas para optimizar la presentación
 * en el frontend y minimizar consultas adicionales.</p>
 *
 * <p><strong>Datos denormalizados incluidos:</strong></p>
 * <ul>
 *   <li>Información completa de la cuenta facturada</li>
 *   <li>Detalles de la reserva que origina la factura</li>
 *   <li>Items detallados con cantidades y precios</li>
 *   <li>Historial completo de pagos asociados</li>
 *   <li>Cálculos financieros pre-computados</li>
 * </ul>
 *
 * <p><strong>Casos de uso principales:</strong></p>
 * <ul>
 *   <li>Vista detallada de factura individual</li>
 *   <li>Respuestas de API para operaciones GET específicas</li>
 *   <li>Pantallas de pago y confirmación</li>
 *   <li>Reportes financieros y auditorías</li>
 *   <li>Exportación de facturas (PDF, Excel)</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 2.0
 * @since 1.0
 * @see Invoice
 * @see InvoiceSummaryResponse
 */
public class InvoiceDetailResponse {

    /**
     * Identificador único de la factura.
     */
    private Long id;

    /**
     * Número único de factura para identificación externa.
     *
     * <p>Formato típico: INV-YYYY-timestamp. Se muestra al cliente
     * en todas las comunicaciones relacionadas con la factura.</p>
     */
    private String invoiceNumber;

    /**
     * Información resumida de la cuenta facturada.
     *
     * <p>Incluye datos necesarios para identificar al cliente
     * sin exponer información sensible innecesaria.</p>
     */
    private AccountInfo account;

    /**
     * Información resumida de la reserva que origina la factura.
     *
     * <p>Proporciona contexto sobre el servicio prestado
     * sin cargar la entidad completa de la reserva.</p>
     */
    private BookingInfo booking;

    /**
     * Fecha y hora de emisión de la factura.
     *
     * <p>Momento en que se generó la factura.
     * Importante para términos de pago y auditoría.</p>
     */
    private LocalDateTime issueDate;

    /**
     * Fecha límite para el pago de la factura.
     *
     * <p>Después de esta fecha, la factura puede considerarse vencida
     * y aplicar recargos o penalizaciones según políticas.</p>
     */
    private LocalDateTime dueDate;

    /**
     * Subtotal antes de aplicar tarifas de plataforma.
     *
     * <p>Suma de todos los items de la factura antes de
     * comisiones y cargos adicionales.</p>
     */
    private BigDecimal subtotal;

    /**
     * Tarifa de plataforma aplicada.
     *
     * <p>Comisión que cobra Petcare por facilitar la transacción.
     * Calculada como porcentaje del subtotal.</p>
     */
    private BigDecimal platformFee;

    /**
     * Monto total final de la factura.
     *
     * <p>Cantidad total que debe pagar el cliente:
     * subtotal + platformFee + otros cargos.</p>
     */
    private BigDecimal totalAmount;

    /**
     * Estado actual de la factura.
     *
     * <p>Determina las acciones disponibles y el flujo de trabajo
     * aplicable a esta factura.</p>
     *
     * @see InvoiceStatus
     */
    private InvoiceStatus status;

    /**
     * Notas adicionales incluidas en la factura.
     *
     * <p>Información adicional, términos especiales, o
     * instrucciones relevantes para el cliente.</p>
     */
    private String notes;

    /**
     * Fecha y hora de creación de la factura.
     */
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última actualización.
     */
    private LocalDateTime updatedAt;

    /**
     * Lista detallada de items incluidos en la factura.
     *
     * <p>Cada item incluye descripción, cantidad, precio unitario
     * y total calculado para transparencia completa.</p>
     */
    private List<InvoiceItemResponse> items;

    /**
     * Historial completo de pagos asociados a esta factura.
     *
     * <p>Incluye tanto pagos exitosos como fallidos para
     * proporcionar un registro completo de transacciones.</p>
     */
    private List<PaymentSummary> payments;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido para frameworks de serialización.
     */
    public InvoiceDetailResponse() {
    }

    /**
     * Constructor completo para creación programática.
     */
    public InvoiceDetailResponse(Long id, String invoiceNumber, AccountInfo account, BookingInfo booking,
                                 LocalDateTime issueDate, LocalDateTime dueDate, BigDecimal subtotal,
                                 BigDecimal platformFee, BigDecimal totalAmount, InvoiceStatus status,
                                 String notes, LocalDateTime createdAt, LocalDateTime updatedAt,
                                 List<InvoiceItemResponse> items, List<PaymentSummary> payments) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.account = account;
        this.booking = booking;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.subtotal = subtotal;
        this.platformFee = platformFee;
        this.totalAmount = totalAmount;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = items;
        this.payments = payments;
    }

    // ========== MÉTODO DE FÁBRICA ==========

    /**
     * Crea una instancia de InvoiceDetailResponse desde una entidad Invoice.
     *
     * <p>Este método de fábrica maneja de forma segura las relaciones que pueden
     * ser null y construye los DTOs anidados necesarios para una respuesta completa.</p>
     *
     * <p><strong>Manejo de valores null:</strong></p>
     * <ul>
     *   <li>Si una relación es null, se asigna null al campo correspondiente</li>
     *   <li>Las listas vacías se manejan correctamente</li>
     *   <li>Se preservan todos los campos opcionales como están</li>
     * </ul>
     *
     * @param invoice la entidad Invoice a convertir
     * @return nueva instancia de InvoiceDetailResponse con datos poblados
     * @throws IllegalArgumentException si invoice es null
     */
    public static InvoiceDetailResponse fromEntity(Invoice invoice) {
        if (invoice == null) {
            throw new IllegalArgumentException("La entidad Invoice no puede ser null");
        }

        // Construir información de la cuenta
        AccountInfo accountInfo = null;
        if (invoice.getAccount() != null) {
            accountInfo = new AccountInfo(
                    invoice.getAccount().getId(),
                    invoice.getAccount().getAccountNumber(),
                    formatUserName(
                            invoice.getAccount().getOwnerUser().getFirstName(),
                            invoice.getAccount().getOwnerUser().getLastName()
                    ),
                    invoice.getAccount().getOwnerUser().getEmail()
            );
        }

        // Construir información de la reserva
        BookingInfo bookingInfo = null;
        if (invoice.getBooking() != null) {
            bookingInfo = new BookingInfo(
                    invoice.getBooking().getId(),
                    invoice.getBooking().getPet() != null ? invoice.getBooking().getPet().getName() : null,
                    invoice.getBooking().getServiceOffering() != null ?
                            invoice.getBooking().getServiceOffering().getName() : null,
                    invoice.getBooking().getSitter() != null ?
                            formatUserName(
                                    invoice.getBooking().getSitter().getFirstName(),
                                    invoice.getBooking().getSitter().getLastName()
                            ) : null,
                    invoice.getBooking().getStartTime(),
                    invoice.getBooking().getEndTime()
            );
        }

        // Convertir items
        List<InvoiceItemResponse> items = invoice.getInvoiceItems().stream()
                .map(item -> new InvoiceItemResponse(
                        item.getId(),
                        item.getDescription(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getLineTotal()
                ))
                .collect(Collectors.toList());

        // Convertir pagos
        List<PaymentSummary> payments = invoice.getPayments().stream()
                .map(payment -> new PaymentSummary(
                        payment.getId(),
                        payment.getAmount(),
                        payment.getStatus().toString(),
                        payment.getProcessedAt()
                ))
                .collect(Collectors.toList());

        return new InvoiceDetailResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                accountInfo,
                bookingInfo,
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getSubtotal(),
                invoice.getPlatformFee(),
                invoice.getTotalAmount(),
                invoice.getStatus(),
                invoice.getNotes(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt(),
                items,
                payments
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
     * Verifica si la factura está completamente pagada.
     *
     * @return true si está pagada completamente
     */
    public boolean isPaid() {
        return status == InvoiceStatus.PAID;
    }

    /**
     * Verifica si la factura está vencida.
     *
     * @return true si la fecha actual supera la fecha de vencimiento
     */
    public boolean isOverdue() {
        return status == InvoiceStatus.OVERDUE ||
                (dueDate != null && LocalDateTime.now().isAfter(dueDate) && canReceivePayments());
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

    /**
     * Calcula el porcentaje de la tarifa de plataforma sobre el subtotal.
     *
     * @return porcentaje de la tarifa, 0 si no hay subtotal
     */
    public BigDecimal getPlatformFeePercentage() {
        if (subtotal == null || subtotal.equals(BigDecimal.ZERO) || platformFee == null) {
            return BigDecimal.ZERO;
        }
        return platformFee.divide(subtotal, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * Obtiene la cantidad total de items en la factura.
     *
     * @return número total de items
     */
    public int getTotalItemCount() {
        return items != null ? items.size() : 0;
    }

    /**
     * Obtiene la cantidad total de pagos realizados.
     *
     * @return número total de intentos de pago
     */
    public int getTotalPaymentCount() {
        return payments != null ? payments.size() : 0;
    }

    // ========== GETTERS Y SETTERS ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public AccountInfo getAccount() { return account; }
    public void setAccount(AccountInfo account) { this.account = account; }

    public BookingInfo getBooking() { return booking; }
    public void setBooking(BookingInfo booking) { this.booking = booking; }

    public LocalDateTime getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDateTime issueDate) { this.issueDate = issueDate; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getPlatformFee() { return platformFee; }
    public void setPlatformFee(BigDecimal platformFee) { this.platformFee = platformFee; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<InvoiceItemResponse> getItems() { return items; }
    public void setItems(List<InvoiceItemResponse> items) { this.items = items; }

    public List<PaymentSummary> getPayments() { return payments; }
    public void setPayments(List<PaymentSummary> payments) { this.payments = payments; }

    @Override
    public String toString() {
        return "InvoiceDetailResponse{" +
                "id=" + id +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", issueDate=" + issueDate +
                ", dueDate=" + dueDate +
                ", itemCount=" + getTotalItemCount() +
                ", paymentCount=" + getTotalPaymentCount() +
                '}';
    }
}