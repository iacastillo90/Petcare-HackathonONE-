package com.Petcare.Petcare.DTOs.Invoice;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la actualización de facturas existentes.
 *
 * <p>Este DTO contiene únicamente los campos que pueden ser modificados
 * en una factura existente, aplicando validaciones específicas según
 * el contexto y estado de la factura.</p>
 *
 * <p><strong>Campos modificables por estado:</strong></p>
 * <ul>
 *   <li>DRAFT: Todos los campos disponibles</li>
 *   <li>SENT: Solo fecha de vencimiento y notas</li>
 *   <li>PAID/CANCELLED/REFUNDED: Solo notas para auditoría</li>
 *   <li>OVERDUE: Fecha de vencimiento y notas</li>
 *   <li>PARTIALLY_PAID: Fecha de vencimiento y notas</li>
 * </ul>
 *
 * <p><strong>Casos de uso principales:</strong></p>
 * <ul>
 *   <li>Extensión de fechas de vencimiento</li>
 *   <li>Actualización de montos antes del envío</li>
 *   <li>Adición de notas y observaciones</li>
 *   <li>Correcciones administrativas</li>
 *   <li>Ajustes por políticas de negocio</li>
 * </ul>
 *
 * <p><strong>Validaciones aplicadas:</strong></p>
 * <ul>
 *   <li>Montos deben ser positivos y con formato correcto</li>
 *   <li>Fechas de vencimiento deben ser futuras (en la mayoría de casos)</li>
 *   <li>Notas tienen límite de caracteres</li>
 *   <li>Coherencia entre subtotal, tarifa y total</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 2.0
 * @since 1.0
 */
public class UpdateInvoiceRequest {

    /**
     * Nueva fecha límite para el pago de la factura.
     *
     * <p>Permite extender o modificar la fecha de vencimiento.
     * Útil para acuerdos de pago especiales o políticas de gracia.
     * Campo opcional - si no se proporciona, no se modifica.</p>
     */
    private LocalDateTime dueDate;

    /**
     * Notas adicionales o actualizadas sobre la factura.
     *
     * <p>Campo modificable en todos los estados para permitir
     * documentación de cambios, acuerdos especiales, o información
     * de auditoría. Campo opcional.</p>
     */
    @Size(max = 1000, message = "Las notas no pueden exceder 1000 caracteres")
    private String notes;

    /**
     * Subtotal actualizado antes de aplicar tarifas.
     *
     * <p>Solo modificable en estados DRAFT o por administradores.
     * Requiere recálculo automático del total cuando se modifica.
     * Campo opcional - si no se proporciona, no se modifica.</p>
     */
    @DecimalMin(value = "0.0", message = "El subtotal debe ser positivo")
    @Digits(integer = 8, fraction = 2, message = "El subtotal debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal subtotal;

    /**
     * Tarifa de plataforma actualizada.
     *
     * <p>Normalmente se calcula automáticamente basándose en el subtotal,
     * pero puede requerir ajuste manual en casos especiales.
     * Campo opcional - si no se proporciona, no se modifica.</p>
     */
    @DecimalMin(value = "0.0", message = "La tarifa de plataforma debe ser positiva")
    @Digits(integer = 8, fraction = 2, message = "La tarifa debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal platformFee;

    /**
     * Monto total actualizado de la factura.
     *
     * <p>Debe coincidir con la suma de subtotal + platformFee.
     * Solo modificable en estados que permiten cambios financieros.
     * Campo opcional - si no se proporciona, no se modifica.</p>
     */
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto total debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El monto total debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal totalAmount;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido para frameworks de serialización.
     */
    public UpdateInvoiceRequest() {
    }

    /**
     * Constructor para actualizaciones básicas (fechas y notas).
     *
     * @param dueDate nueva fecha de vencimiento (puede ser null)
     * @param notes notas adicionales (puede ser null)
     */
    public UpdateInvoiceRequest(LocalDateTime dueDate, String notes) {
        this.dueDate = dueDate;
        this.notes = notes;
    }

    /**
     * Constructor completo para actualizaciones financieras.
     *
     * @param dueDate nueva fecha de vencimiento
     * @param notes notas adicionales
     * @param subtotal nuevo subtotal
     * @param platformFee nueva tarifa de plataforma
     * @param totalAmount nuevo monto total
     */
    public UpdateInvoiceRequest(LocalDateTime dueDate, String notes, BigDecimal subtotal,
                                BigDecimal platformFee, BigDecimal totalAmount) {
        this.dueDate = dueDate;
        this.notes = notes;
        this.subtotal = subtotal;
        this.platformFee = platformFee;
        this.totalAmount = totalAmount;
    }

    // ========== GETTERS Y SETTERS ==========

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getPlatformFee() {
        return platformFee;
    }

    public void setPlatformFee(BigDecimal platformFee) {
        this.platformFee = platformFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    /**
     * Verifica si la solicitud incluye cambios que afectan la visualización de la factura.
     *
     * @return true si se ha proporcionado algún campo para actualizar.
     */
    public boolean hasVisualChanges() {
        // Un cambio visual es cualquier cambio que no esté vacío.
        // Esto incluye cambios financieros, de fecha o de notas.
        return !isEmpty();
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Verifica si la solicitud incluye cambios financieros.
     *
     * @return true si se han proporcionado montos para actualizar
     */
    public boolean hasFinancialChanges() {
        return subtotal != null || platformFee != null || totalAmount != null;
    }

    /**
     * Verifica si la solicitud solo incluye cambios administrativos.
     *
     * @return true si solo se modifican campos no financieros
     */
    public boolean isAdministrativeOnly() {
        return !hasFinancialChanges() && (dueDate != null || notes != null);
    }

    /**
     * Verifica si la solicitud está vacía (sin cambios).
     *
     * @return true si no se han proporcionado campos para actualizar
     */
    public boolean isEmpty() {
        return dueDate == null && notes == null && subtotal == null &&
                platformFee == null && totalAmount == null;
    }

    /**
     * Verifica si los montos financieros son coherentes entre sí.
     *
     * <p>Valida que si se proporcionan múltiples campos financieros,
     * la suma sea coherente: subtotal + platformFee = totalAmount</p>
     *
     * @return true si los montos son coherentes o si no hay suficientes para validar
     */
    public boolean areAmountsConsistent() {
        // Si no tenemos los tres valores, no podemos validar consistencia
        if (subtotal == null || platformFee == null || totalAmount == null) {
            return true; // No hay inconsistencia si faltan valores
        }

        BigDecimal calculatedTotal = subtotal.add(platformFee);
        return calculatedTotal.compareTo(totalAmount) == 0;
    }

    /**
     * Calcula el total basándose en subtotal y tarifa de plataforma proporcionados.
     *
     * @return total calculado, o null si no hay suficientes datos
     */
    public BigDecimal getCalculatedTotal() {
        if (subtotal != null && platformFee != null) {
            return subtotal.add(platformFee);
        }
        return null;
    }

    @Override
    public String toString() {
        return "UpdateInvoiceRequest{" +
                "dueDate=" + dueDate +
                ", notes='" + notes + '\'' +
                ", subtotal=" + subtotal +
                ", platformFee=" + platformFee +
                ", totalAmount=" + totalAmount +
                ", hasFinancialChanges=" + hasFinancialChanges() +
                ", isAdministrativeOnly=" + isAdministrativeOnly() +
                '}';
    }
}