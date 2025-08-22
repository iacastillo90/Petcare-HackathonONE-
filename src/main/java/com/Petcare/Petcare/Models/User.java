package com.Petcare.Petcare.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users") // Es una buena práctica nombrar las tablas en plural y minúsculas
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 250)
    private String firstName;

    @Column(length = 250) // Por defecto, nullable es true
    private String lastName;

    @Column(nullable = false, unique = true, length = 250)
    private String email;

    @Column(nullable = false)
    private String password; // Importante: Este campo debe almacenar el hash de la contraseña

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 250)
    private String phoneNumber;

    // @Enumerated(EnumType.STRING)
   // @Column(nullable = false)
   // private Role role;

   // @Enumerated(EnumType.STRING)
   // private PermissionLevel permissionLevel;

    @Column(nullable = false)
    private boolean isActive;

    private LocalDateTime emailVerifiedAt;

    private LocalDateTime lastLoginAt;

    @CreationTimestamp // Gestionado automáticamente por Hibernate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // Gestionado automáticamente por Hibernate
    private LocalDateTime updatedAt;


    public User() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
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
}