package com.Petcare.Petcare.DTOs.SitterWorkExperience;

import com.Petcare.Petcare.Models.SitterWorkExperience;
import com.Petcare.Petcare.Models.SitterProfile;

public class SitterWorkExperienceMapper {

    public static SitterWorkExperience toEntity(SitterWorkExperienceRequestDTO dto, SitterProfile sitterProfile) {
        SitterWorkExperience entity = new SitterWorkExperience();
        entity.setSitterProfile(sitterProfile);
        entity.setCompanyName(dto.getCompanyName());
        entity.setJobTitle(dto.getJobTitle());
        entity.setResponsibilities(dto.getResponsibilities());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        return entity;
    }

    public static SitterWorkExperienceResponseDTO toResponseDTO(SitterWorkExperience entity) {
        return new SitterWorkExperienceResponseDTO(
                entity.getId(),
                entity.getCompanyName(),
                entity.getJobTitle(),
                entity.getResponsibilities(),
                entity.getStartDate(),
                entity.getEndDate()
        );
    }

    public static SitterWorkExperienceSummaryDTO toSummaryDTO(SitterWorkExperience entity) {
        return new SitterWorkExperienceSummaryDTO(
                entity.getId(),
                entity.getCompanyName(),
                entity.getJobTitle(),
                entity.getStartDate(),
                entity.getEndDate()
        );
    }
}
