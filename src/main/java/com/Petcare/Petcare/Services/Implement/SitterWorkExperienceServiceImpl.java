package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceRequestDTO;
import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceResponseDTO;
import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceSummaryDTO;
import com.Petcare.Petcare.Models.SitterProfile;
import com.Petcare.Petcare.Models.SitterWorkExperience;
import com.Petcare.Petcare.Repositories.SitterProfileRepository;
import com.Petcare.Petcare.Repositories.SitterWorkExperienceRepository;
import com.Petcare.Petcare.Services.SitterWorkExperienceService;
import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class SitterWorkExperienceServiceImpl implements SitterWorkExperienceService {

    private final SitterWorkExperienceRepository workExperienceRepository;
    private final SitterProfileRepository sitterProfileRepository;

    @Autowired
    public SitterWorkExperienceServiceImpl(SitterWorkExperienceRepository workExperienceRepository,
                                           SitterProfileRepository sitterProfileRepository) {
        this.workExperienceRepository = workExperienceRepository;
        this.sitterProfileRepository = sitterProfileRepository;
    }

    // ================= CREATE =================

    @Override
    @Async
    @Transactional
    public CompletableFuture<SitterWorkExperienceResponseDTO> createWorkExperienceAsync(SitterWorkExperienceRequestDTO requestDTO) {
        SitterProfile sitterProfile = sitterProfileRepository.findById(requestDTO.getSitterProfileId())
                .orElseThrow(() -> new IllegalArgumentException("SitterProfile no encontrado con id: " + requestDTO.getSitterProfileId()));

        SitterWorkExperience experience = SitterWorkExperienceMapper.toEntity(requestDTO, sitterProfile);

        SitterWorkExperience saved = workExperienceRepository.save(experience);

        return CompletableFuture.completedFuture(SitterWorkExperienceMapper.toResponseDTO(saved));
    }

    @Override
    @Transactional
    public SitterWorkExperienceResponseDTO createWorkExperience(SitterWorkExperienceRequestDTO requestDTO) {
        return createWorkExperienceAsync(requestDTO).join();
    }

    // ================= READ =================

    @Override
    @Transactional(readOnly = true)
    public List<SitterWorkExperienceSummaryDTO> getWorkExperiencesBySitterId(Integer sitterId) {
        SitterProfile sitterProfile = sitterProfileRepository.findById(sitterId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("SitterProfile no encontrado con id: " + sitterId));

        return sitterProfile.getWorkExperiences().stream()
                .map(SitterWorkExperienceMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SitterWorkExperienceResponseDTO getWorkExperienceById(Long id) {
        SitterWorkExperience experience = workExperienceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Experiencia no encontrada con id: " + id));
        return SitterWorkExperienceMapper.toResponseDTO(experience);
    }

    // ================= UPDATE =================

    @Override
    @Transactional
    public SitterWorkExperienceResponseDTO updateWorkExperience(Long id, SitterWorkExperienceRequestDTO requestDTO) {
        SitterWorkExperience experience = workExperienceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Experiencia no encontrada con id: " + id));

        experience.setCompanyName(requestDTO.getCompanyName());
        experience.setJobTitle(requestDTO.getJobTitle());
        experience.setResponsibilities(requestDTO.getResponsibilities());
        experience.setStartDate(requestDTO.getStartDate());
        experience.setEndDate(requestDTO.getEndDate());

        SitterProfile sitterProfile = sitterProfileRepository.findById(requestDTO.getSitterProfileId())
                .orElseThrow(() -> new IllegalArgumentException("SitterProfile no encontrado con id: " + requestDTO.getSitterProfileId()));

        experience.setSitterProfile(sitterProfile);

        SitterWorkExperience updated = workExperienceRepository.save(experience);
        return SitterWorkExperienceMapper.toResponseDTO(updated);
    }

    // ================= DELETE =================

    @Override
    @Transactional
    public void deleteWorkExperience(Long id) {
        SitterWorkExperience experience = workExperienceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Experiencia no encontrada con id: " + id));
        workExperienceRepository.delete(experience);
    }
}
