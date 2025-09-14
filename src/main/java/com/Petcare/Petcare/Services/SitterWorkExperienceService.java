package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceRequestDTO;
import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceResponseDTO;
import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceSummaryDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SitterWorkExperienceService {

    // Crear nueva experiencia
    SitterWorkExperienceResponseDTO createWorkExperience(SitterWorkExperienceRequestDTO requestDTO);

    // Obtener todas las experiencias por sitterId
    List<SitterWorkExperienceSummaryDTO> getWorkExperiencesBySitterId(Integer sitterId);

    // Obtener una experiencia por su id
    SitterWorkExperienceResponseDTO getWorkExperienceById(Long id);

    // Actualizar experiencia
    SitterWorkExperienceResponseDTO updateWorkExperience(Long id, SitterWorkExperienceRequestDTO requestDTO);

    // Eliminar experiencia
    void deleteWorkExperience(Long id);

    // Registrar experiencia en segundo plano (async)
    CompletableFuture<SitterWorkExperienceResponseDTO> createWorkExperienceAsync(SitterWorkExperienceRequestDTO requestDTO);
}
