package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.DTOs.Review.ReviewDTO;
import com.Petcare.Petcare.DTOs.Review.ReviewResponse;
import com.Petcare.Petcare.Models.Review;
import com.Petcare.Petcare.Repositories.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar reseñas.
 * Encapsula la lógica de negocio y la comunicación con el repositorio.
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    /**
     * Crea una nueva reseña a partir de un ReviewDTO.
     *
     * @param reviewDTO datos de la reseña a crear
     * @return ReviewResponse con los datos guardados
     */
    public ReviewResponse createReview(ReviewDTO reviewDTO) {
        Review review = new Review(
                reviewDTO.getUserId(),
                reviewDTO.getPetId(),
                reviewDTO.getRating(),
                reviewDTO.getComment()
        );

        Review saved = reviewRepository.save(review);
        return mapToResponse(saved);
    }

    /**
     * Obtiene todas las reseñas de una mascota.
     *
     * @param petId ID de la mascota
     * @return lista de ReviewResponse
     */
    public List<ReviewResponse> getReviewsByPetId(Long petId) {
        return reviewRepository.findByPetId(petId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las reseñas de un usuario.
     *
     * @param userId ID del usuario
     * @return lista de ReviewResponse
     */
    public List<ReviewResponse> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Elimina una reseña por su ID.
     *
     * @param id ID de la reseña a eliminar
     */
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    /**
     * Convierte una entidad Review a ReviewResponse.
     *
     * @param review entidad Review
     * @return ReviewResponse
     */
    private ReviewResponse mapToResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUserId(),
                review.getPetId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
