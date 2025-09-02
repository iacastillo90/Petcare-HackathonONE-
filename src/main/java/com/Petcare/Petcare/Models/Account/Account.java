package com.Petcare.Petcare.Models.Account;

import com.Petcare.Petcare.Models.PaymentMethod;
import com.Petcare.Petcare.Models.Pet;
import com.Petcare.Petcare.Models.User.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad que representa una cuenta de cliente o familiar en la plataforma.
 *
 * <p>La cuenta es la entidad principal para la gestión financiera y de recursos compartidos,
 * como mascotas y métodos de pago. Un usuario principal (propietario) está a cargo
 * de la cuenta, y otros usuarios pueden ser invitados a participar con diferentes
 * niveles de permisos.</p>
 *
 * <p><strong>Relaciones principales:</strong></p>
 * <ul>
 * <li>Tiene un propietario principal ({@link User})</li>
 * <li>Puede tener múltiples usuarios asociados ({@link AccountUser})</li>
 * <li>Posee múltiples mascotas ({@link Pet})</li>
 * <li>Gestiona múltiples métodos de pago ({@link PaymentMethod})</li>
 * </ul>
 *
 * @author Equipo Petcare
 * @version 1.0
 * @since 1.0
 * @see User
 * @see AccountUser
 * @see Pet
 */
@Entity
@Table(name = "accounts",
        indexes = {
                @Index(name = "idx_account_owner_user_id", columnList = "owner_user_id")
        })
@EntityListeners(AuditingEntityListener.class)
public class Account {

    /**
     * Identificador único de la cuenta.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * El usuario propietario principal de la cuenta.
     * <p>Este usuario tiene los permisos más altos sobre la cuenta y es el
     * responsable final de la facturación.</p>
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_account_owner_user"))
    @NotNull
    private User ownerUser;

    /**
     * Nombre descriptivo de la cuenta (ej: "Familia Pérez", "Cuenta de Fido y Luna").
     */
    @Column(name = "account_name", nullable = false, length = 100)
    @NotBlank(message = "El nombre de la cuenta es obligatorio")
    @Size(max = 100, message = "El nombre de la cuenta no puede exceder los 100 caracteres")
    private String accountName;

    /**
     * Saldo actual de la cuenta. Puede ser positivo o negativo.
     * <p>Se actualiza a través de la entidad AccountTransaction.</p>
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Código de moneda de 3 letras (ISO 4217), ej: "USD", "CLP".
     */
    @Column(nullable = false, length = 3)
    private String currency = "USD";

    /**
     * Indica si la cuenta está activa o ha sido suspendida.
     */
    @Column(nullable = false)
    private boolean isActive = true;

    /**
     * Fecha y hora de creación de la cuenta.
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última actualización.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Lista de usuarios asociados a esta cuenta.
     */
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<AccountUser> accountUsers = new HashSet<>();

    /**
     * Lista de mascotas que pertenecen a esta cuenta.
     */
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Pet> pets = new HashSet<>();

    // ========== CONSTRUCTORES ==========

    public Account() {}

    public Account(User ownerUser, String accountName) {
        this.ownerUser = ownerUser;
        this.accountName = accountName;
    }

    // ========== GETTERS Y SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwnerUser() {
        return ownerUser;
    }

    public void setOwnerUser(User ownerUser) {
        this.ownerUser = ownerUser;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<AccountUser> getAccountUsers() {
        return accountUsers;
    }

    public void setAccountUsers(Set<AccountUser> accountUsers) {
        this.accountUsers = accountUsers;
    }

    public Set<Pet> getPets() {
        return pets;
    }

    public void setPets(Set<Pet> pets) {
        this.pets = pets;
    }


    // ========== Lógica de Sincronización para Relaciones Bidireccionales ==========

    public void addUser(AccountUser user) {
        accountUsers.add(user);
        user.setAccount(this);
    }

    public void removeUser(AccountUser user) {
        accountUsers.remove(user);
        user.setAccount(null);
    }

    // ========== EQUALS, HASHCODE Y TOSTRING ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id != null && Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : getClass().hashCode();
    }
}