package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.SitterWorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SitterWorkExperienceRepository extends JpaRepository<SitterWorkExperience, Long> {

    // Buscar todas las experiencias de un SitterProfile específico
    List<SitterWorkExperience> findBySitterProfileId(Long sitterProfileId);
}
