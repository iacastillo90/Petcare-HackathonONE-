package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceRequestDTO;
import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceResponseDTO;
import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceSummaryDTO;
import com.Petcare.Petcare.Exception.Business.SitterProfileNotFoundException;
import com.Petcare.Petcare.Exception.Business.WorkExperienceNotFoundException;
import com.Petcare.Petcare.Models.SitterProfile;
import com.Petcare.Petcare.Models.SitterWorkExperience;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.SitterProfileRepository;
import com.Petcare.Petcare.Repositories.SitterWorkExperienceRepository;
import com.Petcare.Petcare.Services.SitterWorkExperienceService;
import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para la gestión de experiencias laborales de cuidadores.
 * <p>
 * Esta clase contiene la lógica de negocio para todas las operaciones CRUD relacionadas
 * con el historial profesional de los cuidadores. Se encarga de las validaciones,
 * la lógica de autorización (verificando que un cuidador solo pueda modificar su
 * propia experiencia) y la interacción con la capa de persistencia.
 *
 * @see SitterWorkExperienceService
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 */
@Service
public class SitterWorkExperienceServiceImplement implements SitterWorkExperienceService {

    private final SitterWorkExperienceRepository workExperienceRepository;
    private final SitterProfileRepository sitterProfileRepository;

    @Autowired
    public SitterWorkExperienceServiceImplement(SitterWorkExperienceRepository workExperienceRepository,
                                                SitterProfileRepository sitterProfileRepository) {
        this.workExperienceRepository = workExperienceRepository;
        this.sitterProfileRepository = sitterProfileRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SitterWorkExperienceResponseDTO createWorkExperience(SitterWorkExperienceRequestDTO requestDTO) {
        SitterProfile sitterProfile = sitterProfileRepository.findById(requestDTO.getSitterProfileId())
                .orElseThrow(() -> new SitterProfileNotFoundException("No se encontró un perfil de cuidador para el ID: " + requestDTO.getSitterProfileId()));

        // Aquí se podría añadir una validación para prevenir duplicados si fuera un requisito de negocio.
        // Por ejemplo:
        // if (workExperienceRepository.existsBySitterProfileAndCompanyNameAndJobTitleAndStartDate(sitterProfile, requestDTO.getCompanyName(), ...)) {
        //     throw new WorkExperienceConflictException("Ya existe una experiencia laboral idéntica para este perfil.");
        // }

        SitterWorkExperience experience = SitterWorkExperienceMapper.toEntity(requestDTO, sitterProfile);
        SitterWorkExperience savedExperience = workExperienceRepository.save(experience);

        return SitterWorkExperienceMapper.toResponseDTO(savedExperience);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<SitterWorkExperienceSummaryDTO> getWorkExperiencesBySitterProfileId(Long sitterProfileId) {
        SitterProfile sitterProfile = sitterProfileRepository.findById(sitterProfileId)
                .orElseThrow(() -> new SitterProfileNotFoundException("No se encontró un perfil de cuidador para el ID: " + sitterProfileId));

        return sitterProfile.getWorkExperiences().stream()
                .map(SitterWorkExperienceMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public SitterWorkExperienceResponseDTO getWorkExperienceById(Long id) {
        SitterWorkExperience experience = workExperienceRepository.findById(id)
                .orElseThrow(() -> new WorkExperienceNotFoundException("Experiencia laboral no encontrada con el ID: " + id));
        return SitterWorkExperienceMapper.toResponseDTO(experience);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esta implementación incluye una capa de seguridad crucial que válida si el usuario
     * autenticado es el propietario del perfil al que pertenece la experiencia o si es
     * un administrador.
     * </p>
     */
    @Override
    @Transactional
    public SitterWorkExperienceResponseDTO updateWorkExperience(Long id, SitterWorkExperienceRequestDTO requestDTO) {
        SitterWorkExperience experience = workExperienceRepository.findById(id)
                .orElseThrow(() -> new WorkExperienceNotFoundException("Experiencia laboral no encontrada con el ID: " + id));

        validateOwnershipOrAdmin(experience);

        experience.setCompanyName(requestDTO.getCompanyName());
        experience.setJobTitle(requestDTO.getJobTitle());
        experience.setResponsibilities(requestDTO.getResponsibilities());
        experience.setStartDate(requestDTO.getStartDate());
        experience.setEndDate(requestDTO.getEndDate());

        SitterWorkExperience updated = workExperienceRepository.save(experience);
        return SitterWorkExperienceMapper.toResponseDTO(updated);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esta implementación incluye una capa de seguridad que válida si el usuario
     * autenticado es el propietario del perfil al que pertenece la experiencia o si es
     * un administrador antes de proceder con la eliminación.
     * </p>
     */
    @Override
    @Transactional
    public void deleteWorkExperience(Long id) {
        SitterWorkExperience experience = workExperienceRepository.findById(id)
                .orElseThrow(() -> new WorkExperienceNotFoundException("Experiencia laboral no encontrada con el ID: " + id));

        validateOwnershipOrAdmin(experience);

        workExperienceRepository.delete(experience);
    }

    /**
     * Método de utilidad privado para centralizar la lógica de autorización.
     * <p>
     * Verifica si el usuario actual en el contexto de seguridad es un administrador o
     * el propietario del perfil al que pertenece la experiencia laboral.
     * Lanza una {@link AccessDeniedException} si no se cumple ninguna de las condiciones.
     *
     * @param experience La experiencia laboral cuya propiedad se va a verificar.
     * @throws AccessDeniedException Si el usuario no tiene permisos para la operación.
     */
    private void validateOwnershipOrAdmin(SitterWorkExperience experience) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        Long ownerId = experience.getSitterProfile().getUser().getId();

        if (!isAdmin && !ownerId.equals(currentUser.getId())) {
            throw new AccessDeniedException("No tiene permisos para modificar la experiencia laboral de otro cuidador.");
        }
    }
}