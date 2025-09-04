package com.Petcare.Petcare.DTOs.Invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para la creación de nuevas facturas.
 *
 * <p>Contiene todos los datos necesarios para crear una factura,
 * incluyendo items detallados y configuraciones especiales.
 * Las validaciones se aplican a nivel de DTO para mantener
 * la entidad limpia de anotaciones de validación.</p>
 *
 * <p><strong>Campos calculados automáticamente:</strong></p>
 * <ul>
 *   <li>{@code invoiceNumber}: Se genera automáticamente siguiendo patrón INV-YYYY-timestamp</li>
 *   <li>{@code issueDate}: Se establece como la fecha actual</li>
 *   <li>{@code dueDate}: Se calcula basándose en términos de pago (default: +15 días)</li>
 *   <li>{@code status}: Se inicializa como DRAFT</li>
 *   <li>{@code account}: Se deriva de la reserva especificada</li>
 * </ul>
 *
 * <p><strong>Validaciones aplicadas:</strong></p>
 * <ul>
 *   <li>Reserva debe existir y estar en estado COMPLETED</li>
 *   <li>No debe existir una factura previa para la reserva</li>
 *   <li>Items opcionales deben tener datos válidos si se proporcionan</li>
 *   <li>Notas tienen límite de caracteres</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 2.0
 * @since 1.0
 */
public class CreateInvoiceRequest {


    /**
     * Identificador de la reserva para la cual se genera la factura.
     *
     * <p>Debe ser una reserva existente en estado COMPLETED.
     * El sistema validará que no exista una factura previa para esta reserva.</p>
     */
    @NotNull(message = "El ID de la reserva es obligatorio")
    private Long bookingId;

    /**
     * Notas adicionales para incluir en la factura.
     *
     * <p>Campo opcional que puede contener términos especiales,
     * instrucciones de pago, o cualquier observación relevante.</p>
     */
    @Size(max = 1000, message = "Las notas no pueden exceder 1000 caracteres")
    private String notes;

    /**
     * Items adicionales opcionales para incluir en la factura.
     *
     * <p>Si no se proporcionan, se creará automáticamente un item
     * basado en el servicio de la reserva. Si se proporcionan,
     * se validará que cada item tenga datos correctos.</p>
     */
    @Valid
    private List<CreateInvoiceItemRequest> items;

    private boolean autoSendEmail = true;

    public boolean isAutoSendEmail() {
        return autoSendEmail;
    }

    public void setAutoSendEmail(boolean autoSendEmail) {
        this.autoSendEmail = autoSendEmail;
    }
    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido para frameworks de serialización.
     */
    public CreateInvoiceRequest() {
    }

    /**
     * Constructor principal para crear solicitud de factura básica.
     *
     * @param bookingId identificador de la reserva
     * @param notes notas adicionales (puede ser null)
     */
    public CreateInvoiceRequest(Long bookingId, String notes) {
        this.bookingId = bookingId;
        this.notes = notes;
    }

    /**
     * Constructor completo para crear solicitud con items personalizados.
     *
     * @param bookingId identificador de la reserva
     * @param notes notas adicionales (puede ser null)
     * @param items lista de items personalizados (puede ser null)
     */
    public CreateInvoiceRequest(Long bookingId, String notes, List<CreateInvoiceItemRequest> items) {
        this.bookingId = bookingId;
        this.notes = notes;
        this.items = items;
    }

    // ========== GETTERS Y SETTERS ==========

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<CreateInvoiceItemRequest> getItems() {
        return items;
    }

    public void setItems(List<CreateInvoiceItemRequest> items) {
        this.items = items;
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Verifica si se han proporcionado items personalizados.
     *
     * @return true si hay items definidos
     */
    public boolean hasCustomItems() {
        return items != null && !items.isEmpty();
    }

    @Override
    public String toString() {
        return "CreateInvoiceRequest{" +
                "bookingId=" + bookingId +
                ", notes='" + notes + '\'' +
                ", items=" + (items != null ? items.size() : 0) + " items" +
                '}';
    }

    /**
     * DTO para crear items individuales de factura.
     *
     * <p>Representa un producto o servicio específico dentro de la factura
     * con su cantidad, precio unitario y descripción detallada.</p>
     */
    public static class CreateInvoiceItemRequest {

        /**
         * Descripción del producto o servicio facturado.
         *
         * <p>Debe ser descriptiva y clara para el cliente.
         * Aparecerá en la factura final.</p>
         */
        @NotBlank(message = "La descripción del item es obligatoria")
        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        private String description;

        /**
         * Cantidad del producto o servicio.
         *
         * <p>Debe ser un número positivo entero.
         * Para servicios que no son cuantificables, usar 1.</p>
         */
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        @Max(value = 999, message = "La cantidad no puede exceder 999")
        private Integer quantity;

        /**
         * Precio unitario del item.
         *
         * <p>El total del item se calcula como cantidad × precio unitario.
         * Debe ser un valor positivo con máximo 2 decimales.</p>
         */
        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio unitario debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "El precio unitario debe tener máximo 8 dígitos enteros y 2 decimales")
        private BigDecimal unitPrice;

        // ========== CONSTRUCTORES ==========

        /**
         * Constructor vacío requerido para serialización.
         */
        public CreateInvoiceItemRequest() {
        }

        /**
         * Constructor completo para crear item de factura.
         *
         * @param description descripción del item
         * @param quantity cantidad
         * @param unitPrice precio unitario
         */
        public CreateInvoiceItemRequest(String description, Integer quantity, BigDecimal unitPrice) {
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        // ========== GETTERS Y SETTERS ==========

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        // ========== MÉTODOS DE UTILIDAD ==========

        /**
         * Calcula el total de este item (cantidad × precio unitario).
         *
         * @return total del item
         */
        public BigDecimal getLineTotal() {
            if (quantity != null && unitPrice != null) {
                return unitPrice.multiply(new BigDecimal(quantity));
            }
            return BigDecimal.ZERO;
        }

        @Override
        public String toString() {
            return "CreateInvoiceItemRequest{" +
                    "description='" + description + '\'' +
                    ", quantity=" + quantity +
                    ", unitPrice=" + unitPrice +
                    ", lineTotal=" + getLineTotal() +
                    '}';
        }
    }
}