package com.Petcare.Petcare.Models.Invoice;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa un item individual dentro de una factura.
 *
 * <p>Esta entidad permite el desglose granular de los servicios y productos
 * incluidos en una factura, proporcionando transparencia completa al cliente
 * sobre los cargos aplicados. Cada item representa un concepto facturable
 * específico con su cantidad, precio unitario y total calculado.</p>
 *
 * <p><strong>Relación principal:</strong></p>
 * <ul>
 *   <li>Pertenece a una factura específica ({@link Invoice})</li>
 * </ul>
 *
 * <p><strong>Tipos de items comunes:</strong></p>
 * <ul>
 *   <li>Servicio de cuidado base (por horas)</li>
 *   <li>Servicios adicionales (paseos extra, alimentación especial)</li>
 *   <li>Cargos por tiempo extendido</li>
 *   <li>Descuentos aplicados (como valores negativos)</li>
 * </ul>
 *
 * <p><strong>Nota:</strong> El cálculo del total de línea se realiza
 * automáticamente en el service layer al crear o modificar el item.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see Invoice
 */
@Entity
@Table(name = "invoice_items",
        indexes = {
                @Index(name = "idx_invoiceitem_invoice_id", columnList = "invoice_id"),
                @Index(name = "idx_invoiceitem_created_at", columnList = "created_at")
        })
@EntityListeners(AuditingEntityListener.class)
public class InvoiceItem {

    /**
     * Identificador único del item de factura.
     * Generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Factura a la cual pertenece este item.
     *
     * <p>Relación obligatoria que establece a qué factura pertenece el item.
     * La carga es lazy para optimizar el rendimiento.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false, foreignKey = @ForeignKey(name = "fk_invoiceitem_invoice"))
    @NotNull(message = "La factura es obligatoria para crear un item")
    private Invoice invoice;

    /**
     * Descripción detallada del producto o servicio facturado.
     *
     * <p>Debe ser lo suficientemente descriptiva para que el cliente
     * entienda exactamente qué se le está cobrando. Ejemplos:
     * "Cuidado de mascotas - 4 horas", "Paseo adicional", "Alimentación especial".</p>
     */
    @Column(name = "description", nullable = false, length = 500)
    @NotBlank(message = "La descripción del item es obligatoria")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description;

    /**
     * Cantidad de unidades del servicio o producto.
     *
     * <p>Para servicios por tiempo generalmente será las horas.
     * Para productos específicos será la cantidad de items.
     * Debe ser un número positivo.</p>
     */
    @Column(name = "quantity", nullable = false)
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Max(value = 999, message = "La cantidad no puede exceder 999")
    private Integer quantity;

    /**
     * Precio unitario del servicio o producto.
     *
     * <p>Precio por cada unidad definida en quantity.
     * Para servicios por horas, será el precio por hora.
     * Para productos, el precio por unidad del producto.</p>
     */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio unitario debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El precio unitario debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal unitPrice;

    /**
     * Total de la línea calculado (quantity * unitPrice).
     *
     * <p>Representa el costo total de este item específico antes de
     * aplicar descuentos globales o tarifas de plataforma.
     * Se calcula automáticamente en el service layer.</p>
     */
    @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El total de línea es obligatorio")
    @DecimalMin(value = "0.0", message = "El total de línea debe ser positivo")
    @Digits(integer = 10, fraction = 2, message = "El total de línea debe tener máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal lineTotal;

    /**
     * Fecha y hora de creación del item.
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

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido por JPA.
     */
    public InvoiceItem() {
    }

    /**
     * Constructor principal para crear un nuevo item de factura.
     *
     * <p>Solo incluye validaciones básicas de nulos. Las validaciones de negocio
     * complejas y el cálculo del total se manejan en el service layer.</p>
     *
     * @param invoice la factura a la que pertenece este item
     * @param description descripción detallada del item
     * @param quantity cantidad de unidades
     * @param unitPrice precio por unidad
     * @param lineTotal total calculado de la línea
     */
    public InvoiceItem(Invoice invoice, String description, Integer quantity,
                       BigDecimal unitPrice, BigDecimal lineTotal) {
        this.invoice = invoice;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    /**
     * Constructor completo con todos los campos.
     * Principalmente para uso interno y testing.
     */
    public InvoiceItem(Long id, Invoice invoice, String description, Integer quantity,
                       BigDecimal unitPrice, BigDecimal lineTotal, LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this.id = id;
        this.invoice = invoice;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
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

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

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

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
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

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Calcula el total de línea basado en cantidad y precio unitario.
     *
     * <p>Método de conveniencia para recalcular el total cuando cambian
     * la cantidad o el precio unitario. Normalmente usado en el service layer.</p>
     *
     * @return el total calculado (quantity * unitPrice)
     */
    public BigDecimal calculateLineTotal() {
        if (quantity != null && unitPrice != null) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Actualiza el total de línea recalculándolo automáticamente.
     *
     * <p>Método de conveniencia que actualiza el campo lineTotal
     * basándose en los valores actuales de quantity y unitPrice.</p>
     */
    public void updateLineTotal() {
        this.lineTotal = calculateLineTotal();
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
        if (!(o instanceof InvoiceItem)) return false;
        InvoiceItem that = (InvoiceItem) o;

        // Si ambos tienen ID, comparar por ID
        if (this.id != null && that.id != null) {
            return Objects.equals(this.id, that.id);
        }

        // Si no tienen ID, comparar por campos de negocio únicos
        return Objects.equals(getInvoice(), that.getInvoice()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getQuantity(), that.getQuantity()) &&
                Objects.equals(getUnitPrice(), that.getUnitPrice());
    }

    /**
     * Implementación de hashCode consistente con equals.
     */
    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(getInvoice(), getDescription(), getQuantity(), getUnitPrice());
    }

    /**
     * Representación de cadena para debugging y logging.
     *
     * <p>Incluye solo información básica para evitar lazy loading exceptions
     * y problemas de rendimiento.</p>
     */
    @Override
    public String toString() {
        return String.format("InvoiceItem{id=%d, description='%s', quantity=%d, unitPrice=%s, lineTotal=%s, createdAt=%s}",
                id, description, quantity, unitPrice, lineTotal, createdAt);
    }
}