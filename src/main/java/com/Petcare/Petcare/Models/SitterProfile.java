package com.Petcare.Petcare.Models;

import com.Petcare.Petcare.Models.User.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sitter_profiles")
public class SitterProfile {

    // Prueba

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User  user;

    @Column(columnDefinition = "TEXT")
    private String bio;
    @Column(precision = 8, scale = 2)
    private BigDecimal hourlyRate;

    private Integer servicingRadius;

    private String profileImageUrl;

    @Column(nullable = false)
    private boolean isVerified;

    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(nullable = false)
    private boolean isAvailableForBookings;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}