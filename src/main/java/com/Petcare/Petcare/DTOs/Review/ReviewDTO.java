package com.Petcare.Petcare.DTOs.Review;

import jakarta.validation.constraints.*;

/**
 * Data Transfer Object para crear o actualizar una reseña.
 */
public class ReviewDTO {

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long userId;

    @NotNull(message = "El ID de la mascota es obligatorio")
    private Long petId;

    @NotNull(message = "El rating es obligatorio")
    @Min(value = 1, message = "El rating mínimo es 1")
    @Max(value = 5, message = "El rating máximo es 5")
    private Integer rating;

    @Size(max = 1000, message = "El comentario no puede superar 1000 caracteres")
    private String comment;

    // ----- Getters y Setters -----

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
}
