package com.Petcare.Petcare.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Representa una reseña que un usuario puede dejar sobre un servicio o mascota.
 * Contiene información sobre el rating, comentario y fechas de creación/actualización.
 *
 * Esta entidad se mapea a la base de datos usando JPA.
 */
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario que realizó la reseña.
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * Mascota o servicio al que se refiere la reseña.
     */
    @Column(nullable = false)
    private Long petId;

    /**
     * Calificación de la reseña, de 1 a 5.
     */
    @Column(nullable = false)
    private Integer rating;

    /**
     * Comentario textual de la reseña.
     */
    @Column(length = 1000)
    private String comment;

    /**
     * Fecha y hora de creación de la reseña.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de última actualización de la reseña.
     */
    private LocalDateTime updatedAt;

    // ----- Constructores -----

    public Review() {
        this.createdAt = LocalDateTime.now();
    }

    public Review(Long userId, Long petId, Integer rating, String comment) {
        this.userId = userId;
        this.petId = petId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }

    // ----- Getters y Setters -----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    // ----- Métodos adicionales -----

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
