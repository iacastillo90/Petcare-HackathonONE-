package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.SitterProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource
public interface SitterProfileRepository extends JpaRepository<SitterProfile, Long> {

    Optional<SitterProfile> findByUserId(Long userId);
}