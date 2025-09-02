package com.Petcare.Petcare.Models;

import com.Petcare.Petcare.Models.Account.Account;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entidad que representa un método de pago asociado a una cuenta.
 *
 * <p>Un método de pago puede ser una tarjeta de crédito, débito, cuenta bancaria, etc.
 * Está vinculado a una cuenta y puede ser utilizado para realizar transacciones.</p>
 *
 * <ul>
 *   <li>Pertenece a una {@link Account}</li>
 * </ul>
 *
 * @author Equipo Petcare
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "payment_methods")
public class PaymentMethod {

    /**
     * Identificador único del método de pago.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre descriptivo del método de pago (ej: "Visa de Juan", "Cuenta Banco Chile").
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "El nombre del método de pago es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String name;

    /**
     * Tipo de método de pago (ej: "Tarjeta de crédito", "Cuenta bancaria").
     */
    @Column(nullable = false, length = 50)
    @NotBlank(message = "El tipo de método de pago es obligatorio")
    @Size(max = 50, message = "El tipo no puede exceder los 50 caracteres")
    private String type;

    /**
     * Últimos 4 dígitos o identificador parcial (opcional).
     */
    @Column(length = 10)
    private String lastDigits;

    /**
     * Cuenta a la que pertenece este método de pago.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payment_method_account"))
    @NotNull
    private Account account;

    /**
     * Indica si el método de pago está activo.
     */
    @Column(nullable = false)
    private boolean isActive = true;

    /**
     * Fecha de creación del método de pago.
     */
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ========== CONSTRUCTORES ==========

    public PaymentMethod() {}

    public PaymentMethod(String name, String type, String lastDigits, Account account) {
        this.name = name;
        this.type = type;
        this.lastDigits = lastDigits;
        this.account = account;
    }

    // ========== GETTERS Y SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLastDigits() {
        return lastDigits;
    }

    public void setLastDigits(String lastDigits) {
        this.lastDigits = lastDigits;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}