package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.SitterProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SitterProfileRepository extends JpaRepository<SitterProfile, Long> {

    Optional<SitterProfile> findByUserId(Long userId);
}