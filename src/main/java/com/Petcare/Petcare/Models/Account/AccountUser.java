package com.Petcare.Petcare.Models.Account;

import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad intermedia que representa la membresía de un Usuario en una Cuenta.
 *
 * <p>Define la relación Muchos a Muchos entre Cuentas y Usuarios, y especifica
 * los permisos granulares que cada usuario tiene dentro de una cuenta específica.</p>
 *
 * @author Equipo Petcare
 * @version 1.0
 * @since 1.0
 * @see Account
 * @see User
 */
@Entity
@Table(name = "account_users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_account_user", columnNames = {"account_id", "user_id"})
})
@EntityListeners(AuditingEntityListener.class)
public class AccountUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_accountuser_account"))
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_accountuser_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean canManagePayments = false;

    @Column(nullable = false)
    private boolean canManagePets = false;

    @Column(nullable = false)
    private boolean canMakeBookings = true;

    @CreatedDate
    @Column(name = "added_at", updatable = false, nullable = false)
    private LocalDateTime addedAt;

    // ========== CONSTRUCTORES, GETTERS Y SETTERS ==========

    public AccountUser() {}

    public AccountUser(Account account, User user, Role role) {
        this.account = account;
        this.user = user;
        this.role = role;
    }

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isCanManagePayments() {
        return canManagePayments;
    }

    public void setCanManagePayments(boolean canManagePayments) {
        this.canManagePayments = canManagePayments;
    }

    public boolean isCanManagePets() {
        return canManagePets;
    }

    public void setCanManagePets(boolean canManagePets) {
        this.canManagePets = canManagePets;
    }

    public boolean isCanMakeBookings() {
        return canMakeBookings;
    }

    public void setCanMakeBookings(boolean canMakeBookings) {
        this.canMakeBookings = canMakeBookings;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}
