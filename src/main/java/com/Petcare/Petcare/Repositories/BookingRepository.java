package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.Booking.Booking;

import com.Petcare.Petcare.Models.Booking.BookingStatus;
import com.Petcare.Petcare.Models.User.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.pet LEFT JOIN FETCH b.sitter LEFT JOIN FETCH b.serviceOffering LEFT JOIN FETCH b.bookedByUser WHERE b.id = :id")
    Optional<Booking> findByIdWithAllRelations(Long id);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.pet LEFT JOIN FETCH b.sitter")
    Page<Booking> findAllWithBasicInfo(Pageable pageable);

    Page<Booking> findBySitterId(Long sitterId, Pageable pageable);
    Page<Booking> findBySitterIdAndStatus(Long sitterId, BookingStatus status, Pageable pageable);
    Page<Booking> findByBookedByUserId(Long userId, Pageable pageable);
    Page<Booking> findByBookedByUserIdAndStatus(Long userId, BookingStatus status, Pageable pageable);

    long countByBookedByUserAndStatus(User user, BookingStatus status);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.sitter.id = :sitterId AND " +
            "((b.startTime < :endTime AND b.endTime > :startTime)) AND " +
            "b.status IN ('CONFIRMED', 'IN_PROGRESS')")
    boolean existsConflictingBooking(Long sitterId, LocalDateTime startTime, LocalDateTime endTime);
}
