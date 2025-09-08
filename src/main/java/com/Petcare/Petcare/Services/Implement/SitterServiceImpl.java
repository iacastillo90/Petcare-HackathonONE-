package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.DTOs.Sitter.SitterProfileDTO;
import com.Petcare.Petcare.Models.SitterProfile;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.SitterProfileRepository;
import com.Petcare.Petcare.Repositories.UserRepository;
import com.Petcare.Petcare.Services.SitterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SitterServiceImpl implements SitterService {

    private final UserRepository userRepository;
    private final SitterProfileRepository sitterProfileRepository;

    public SitterServiceImpl(UserRepository userRepository,
                             SitterProfileRepository sitterProfileRepository) {
        this.userRepository = userRepository;
        this.sitterProfileRepository = sitterProfileRepository;
    }

    @Override
    @Transactional
    public SitterProfileDTO createSitterProfile(Long userId, SitterProfileDTO sitterProfileDTO) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        User user = userOpt.get();

        if (sitterProfileRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("Sitter profile already exists for this user");
        }

        SitterProfile profile = new SitterProfile(
                user,
                sitterProfileDTO.getBio(),
                sitterProfileDTO.getHourlyRate(),         // BigDecimal
                sitterProfileDTO.getServicingRadius(),    // Integer
                sitterProfileDTO.getProfileImageUrl()
        );

        // Valores por defecto
        profile.setVerified(sitterProfileDTO.isVerified());
        profile.setAvailableForBookings(sitterProfileDTO.isAvailableForBookings());

        SitterProfile saved = sitterProfileRepository.save(profile);

        return mapToDTO(saved);
    }

    @Override
    public SitterProfileDTO getSitterProfile(Long userId) {
        return sitterProfileRepository.findByUserId(userId)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Sitter profile not found"));
    }

    @Override
    @Transactional
    public SitterProfileDTO updateSitterProfile(Long userId, SitterProfileDTO sitterProfileDTO) {
        SitterProfile profile = sitterProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Sitter profile not found"));

        profile.setBio(sitterProfileDTO.getBio());
        profile.setHourlyRate(sitterProfileDTO.getHourlyRate());             // BigDecimal
        profile.setServicingRadius(sitterProfileDTO.getServicingRadius());   // Integer
        profile.setProfileImageUrl(sitterProfileDTO.getProfileImageUrl());
        profile.setVerified(sitterProfileDTO.isVerified());
        profile.setAvailableForBookings(sitterProfileDTO.isAvailableForBookings());

        SitterProfile updated = sitterProfileRepository.save(profile);
        return mapToDTO(updated);
    }

    private SitterProfileDTO mapToDTO(SitterProfile sitterProfile) {
        return new SitterProfileDTO(
                sitterProfile.getId(),
                sitterProfile.getUser().getId(),
                sitterProfile.getBio(),
                sitterProfile.getHourlyRate(),                  // BigDecimal
                sitterProfile.getServicingRadius(),             // Integer
                sitterProfile.getProfileImageUrl(),
                sitterProfile.isVerified(),                     // boolean
                sitterProfile.isAvailableForBookings()         // boolean
        );
    }

    @Override
    public List<SitterProfileDTO> getAllSitterProfiles() {
        List<SitterProfile> profiles = sitterProfileRepository.findAll();
        List<SitterProfileDTO> dtos = new ArrayList<>();
        for (SitterProfile profile : profiles) {
            dtos.add(mapToDTO(profile));
        }
        return dtos;
    }

    @Override
    @Transactional
    public void deleteSitterProfile(Long userId) {
        SitterProfile profile = sitterProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Sitter profile not found"));
        sitterProfileRepository.delete(profile);
    }



}
