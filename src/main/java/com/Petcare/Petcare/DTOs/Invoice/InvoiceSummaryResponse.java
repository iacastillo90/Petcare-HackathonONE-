package com.Petcare.Petcare.DTOs.Invoice;

import com.Petcare.Petcare.Models.Invoice.Invoice;
import com.Petcare.Petcare.Models.Invoice.InvoiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO de respuesta con información resumida y optimizada de facturas.
 *
 * <p>Este DTO está diseñado específicamente para listados y vistas donde se
 * requiere información esencial sin la sobrecarga de datos completos. Optimiza
 * el rendimiento al transferir solo los campos más relevantes para operaciones
 * de listado y búsqueda.</p>
 *
 * @author Equipo Petcare 10
 * @version 2.0
 * @since 1.0
 * @see Invoice
 * @see InvoiceDetailResponse
 */
@Schema(description = "DTO de respuesta con información resumida y optimizada de facturas.")
public record InvoiceSummaryResponse(
        @Schema(description = "Identificador único de la factura.", example = "1")
        Long id,

        @Schema(description = "Número único de factura para identificación externa.", example = "INV-2025-1707501234567")
        String invoiceNumber,

        @Schema(description = "Identificador de la cuenta facturada.", example = "1")
        Long accountId,

        @Schema(description = "Número de cuenta para presentación.", example = "ACC-123456")
        String accountNumber,

        @Schema(description = "Identificador de la reserva que origina la factura.", example = "1")
        Long bookingId,

        @Schema(description = "Fecha de emisión de la factura.", example = "2025-02-10T15:30:00")
        LocalDateTime issueDate,

        @Schema(description = "Fecha límite para el pago.", example = "2025-02-20T15:30:00")
        LocalDateTime dueDate,

        @Schema(description = "Monto total de la factura.", example = "150.00")
        BigDecimal totalAmount,

        @Schema(description = "Estado actual de la factura.", example = "SENT")
        InvoiceStatus status,

        @Schema(description = "Nombre completo del cliente facturado.", example = "Juan Pérez")
        String clientName,

        @Schema(description = "Nombre completo del cuidador que proporcionó el servicio.", example = "María García")
        String sitterName,

        @Schema(description = "Fecha y hora de creación de la factura.", example = "2025-02-10T15:30:00")
        LocalDateTime createdAt
) {

    /**
     * Crea una instancia de InvoiceSummaryResponse desde una entidad Invoice.
     *
     * @param invoice la entidad Invoice a convertir
     * @return nueva instancia de InvoiceSummaryResponse con datos poblados
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
     */
    private static String formatUserName(String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            return null;
        }

        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";

        return (first + " " + last).trim();
    }

    /**
     * Verifica si la factura requiere atención inmediata.
     */
    public boolean requiresAttention() {
        return status == InvoiceStatus.OVERDUE ||
                (status == InvoiceStatus.SENT && isNearDueDate());
    }

    /**
     * Verifica si la factura está próxima a la fecha de vencimiento.
     */
    public boolean isNearDueDate() {
        if (dueDate == null) return false;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysFromNow = now.plusDays(3);
        return dueDate.isBefore(threeDaysFromNow) && dueDate.isAfter(now);
    }

    /**
     * Verifica si la factura está en un estado final.
     */
    public boolean isFinalState() {
        return status == InvoiceStatus.PAID ||
                status == InvoiceStatus.CANCELLED ||
                status == InvoiceStatus.REFUNDED;
    }

    /**
     * Verifica si la factura puede recibir pagos.
     */
    public boolean canReceivePayments() {
        return status == InvoiceStatus.SENT ||
                status == InvoiceStatus.OVERDUE ||
                status == InvoiceStatus.PARTIALLY_PAID;
    }

    /**
     * Proporciona una etiqueta de estado localizada para la UI.
     */
    public String getStatusLabel() {
        if (status == null) return "Desconocido";

        return switch (status) {
            case DRAFT -> "Borrador";
            case SENT -> "Enviada";
            case PAID -> "Pagada";
            case PARTIALLY_PAID -> "Pago Parcial";
            case OVERDUE -> "Vencida";
            case CANCELLED -> "Cancelada";
            case REFUNDED -> "Reembolsada";
        };
    }

    /**
     * Calcula los días restantes hasta el vencimiento.
     */
    public long getDaysUntilDue() {
        if (dueDate == null) return 0;
        return java.time.Duration.between(LocalDateTime.now(), dueDate).toDays();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceSummaryResponse that = (InvoiceSummaryResponse) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

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
