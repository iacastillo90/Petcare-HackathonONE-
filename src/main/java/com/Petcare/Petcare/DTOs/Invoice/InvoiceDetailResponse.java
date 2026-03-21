package com.Petcare.Petcare.DTOs.Invoice;

import com.Petcare.Petcare.DTOs.Account.AccountInfo;
import com.Petcare.Petcare.DTOs.Booking.BookingInfo;
import com.Petcare.Petcare.DTOs.Invoice.InvoiceItem.InvoiceItemResponse;
import com.Petcare.Petcare.DTOs.Payment.PaymentSummary;
import com.Petcare.Petcare.Models.Invoice.Invoice;
import com.Petcare.Petcare.Models.Invoice.InvoiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * DTO de respuesta con información completa y detallada de facturas.
 *
 * <p>Este DTO incluye toda la información relevante de una factura, con datos
 * denormalizados de las entidades relacionadas para optimizar la presentación
 * en el frontend y minimizar consultas adicionales.</p>
 *
 * @author Equipo Petcare 10
 * @version 2.0
 * @since 1.0
 * @see Invoice
 * @see InvoiceSummaryResponse
 */
@Schema(description = "DTO de respuesta con información completa y detallada de facturas.")
public record InvoiceDetailResponse(
        @Schema(description = "Identificador único de la factura.", example = "1")
        Long id,

        @Schema(description = "Número único de factura para identificación externa.", example = "INV-2025-1707501234567")
        String invoiceNumber,

        @Schema(description = "Información resumida de la cuenta facturada.")
        AccountInfo account,

        @Schema(description = "Información resumida de la reserva que origina la factura.")
        BookingInfo booking,

        @Schema(description = "Fecha y hora de emisión de la factura.", example = "2025-02-10T15:30:00")
        LocalDateTime issueDate,

        @Schema(description = "Fecha límite para el pago de la factura.", example = "2025-02-20T15:30:00")
        LocalDateTime dueDate,

        @Schema(description = "Subtotal antes de aplicar tarifas de plataforma.", example = "150.00")
        BigDecimal subtotal,

        @Schema(description = "Tarifa de plataforma aplicada.", example = "10.00")
        BigDecimal platformFee,

        @Schema(description = "Monto total final de la factura.", example = "160.00")
        BigDecimal totalAmount,

        @Schema(description = "Estado actual de la factura.", example = "SENT")
        InvoiceStatus status,

        @Schema(description = "Notas adicionales incluidas en la factura.")
        String notes,

        @Schema(description = "Fecha y hora de creación de la factura.", example = "2025-02-10T15:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Fecha y hora de la última actualización.", example = "2025-02-10T15:30:00")
        LocalDateTime updatedAt,

        @Schema(description = "Lista detallada de items incluidos en la factura.")
        List<InvoiceItemResponse> items,

        @Schema(description = "Historial completo de pagos asociados a esta factura.")
        List<PaymentSummary> payments
) {

    /**
     * Crea una instancia de InvoiceDetailResponse desde una entidad Invoice.
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
     * Verifica si la factura puede recibir pagos.
     */
    public boolean canReceivePayments() {
        return status == InvoiceStatus.SENT ||
                status == InvoiceStatus.OVERDUE ||
                status == InvoiceStatus.PARTIALLY_PAID;
    }

    /**
     * Verifica si la factura está completamente pagada.
     */
    public boolean isPaid() {
        return status == InvoiceStatus.PAID;
    }

    /**
     * Verifica si la factura está vencida.
     */
    public boolean isOverdue() {
        return status == InvoiceStatus.OVERDUE ||
                (dueDate != null && LocalDateTime.now().isAfter(dueDate) && canReceivePayments());
    }

    /**
     * Calcula los días restantes hasta el vencimiento.
     */
    public long getDaysUntilDue() {
        if (dueDate == null) return 0;
        return java.time.Duration.between(LocalDateTime.now(), dueDate).toDays();
    }

    /**
     * Calcula el porcentaje de la tarifa de plataforma sobre el subtotal.
     */
    public BigDecimal getPlatformFeePercentage() {
        if (subtotal == null || subtotal.equals(BigDecimal.ZERO) || platformFee == null) {
            return BigDecimal.ZERO;
        }
        return platformFee.divide(subtotal, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * Obtiene la cantidad total de items en la factura.
     */
    public int getTotalItemCount() {
        return items != null ? items.size() : 0;
    }

    /**
     * Obtiene la cantidad total de pagos realizados.
     */
    public int getTotalPaymentCount() {
        return payments != null ? payments.size() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceDetailResponse that = (InvoiceDetailResponse) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

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
