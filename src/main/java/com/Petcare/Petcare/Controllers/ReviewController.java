package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.Review.ReviewDTO;
import com.Petcare.Petcare.DTOs.Review.ReviewResponse;
import com.Petcare.Petcare.Services.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar reseñas.
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Crea una nueva reseña.
     *
     * @param reviewDTO datos de la reseña
     * @return ReviewResponse con los datos guardados
     */
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewDTO reviewDTO) {
        ReviewResponse response = reviewService.createReview(reviewDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Obtiene todas las reseñas de una mascota.
     *
     * @param petId ID de la mascota
     * @return lista de ReviewResponse
     */
    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByPet(@PathVariable Long petId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByPetId(petId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Obtiene todas las reseñas de un usuario.
     *
     * @param userId ID del usuario
     * @return lista de ReviewResponse
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUser(@PathVariable Long userId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Elimina una reseña por su ID.
     *
     * @param id ID de la reseña
     * @return mensaje de confirmación
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok("Reseña eliminada correctamente");
    }
}
