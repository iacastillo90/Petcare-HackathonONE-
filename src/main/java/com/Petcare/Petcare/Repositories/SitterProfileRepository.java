package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.SitterProfile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SitterProfileRepository extends JpaRepository<SitterProfile, Long> {

    Optional<SitterProfile> findByUserId(Long userId);

    /**
     * Busca todos los perfiles de cuidadores que están verificados y disponibles.
     */
    List<SitterProfile> findByIsVerifiedTrueAndIsAvailableForBookingsTrue();

    /**
     * Busca perfiles verificados y disponibles cuya dirección de usuario contenga el texto de la ciudad.
     * Spring Data JPA creará la consulta con el JOIN a la tabla de usuarios automáticamente.
     */
    List<SitterProfile> findByIsVerifiedTrueAndIsAvailableForBookingsTrueAndUser_AddressContainingIgnoreCase(String city);

    @Query("SELECT sp FROM SitterProfile sp JOIN FETCH sp.user ORDER BY sp.updatedAt DESC")
    List<SitterProfile> findRecentWithUser(Pageable pageable);
}