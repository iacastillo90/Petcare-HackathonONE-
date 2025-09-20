package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad Review.
 * Permite operaciones CRUD y consultas adicionales si se necesitan.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Obtiene todas las reseñas de una mascota específica.
     *
     * @param petId el ID de la mascota
     * @return lista de reseñas asociadas
     */
    List<Review> findByPetId(Long petId);

    /**
     * Obtiene todas las reseñas de un usuario específico.
     *
     * @param userId el ID del usuario
     * @return lista de reseñas asociadas
     */
    List<Review> findByUserId(Long userId);
}
