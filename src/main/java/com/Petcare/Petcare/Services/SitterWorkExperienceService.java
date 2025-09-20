package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceRequestDTO;
import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceResponseDTO;
import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceSummaryDTO;
import com.Petcare.Petcare.Services.Implement.SitterWorkExperienceServiceImplement;

import java.util.List;

/**
 * Define el contrato para el servicio de gestión de experiencias laborales de cuidadores (Sitters).
 * <p>
 * Este servicio encapsula toda la lógica de negocio relacionada con la creación, consulta,
 * actualización y eliminación de las entradas del historial profesional de los cuidadores.
 * Es responsable de validar los datos, asegurar la integridad (ej. que un cuidador solo
 * modifique su propia experiencia) y de interactuar con la capa de persistencia.
 *
 * @see com.Petcare.Petcare.Models.SitterWorkExperience
 * @see com.Petcare.Petcare.Controllers.SitterWorkExperienceController
 * @see SitterWorkExperienceServiceImplement
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 */
public interface SitterWorkExperienceService {

    /**
     * Crea una nueva experiencia laboral para un perfil de cuidador.
     * <p>
     * Valida la solicitud, busca el perfil de cuidador asociado y persiste
     * la nueva entrada de experiencia laboral en la base de datos.
     *
     * @param requestDTO El DTO que contiene los detalles de la nueva experiencia a registrar.
     * @return Un {@link SitterWorkExperienceResponseDTO} que representa el registro recién creado.
     * @throws com.Petcare.Petcare.Exception.Business.SitterProfileNotFoundException Si el ID del perfil en el DTO no corresponde a un cuidador existente.
     * @throws com.Petcare.Petcare.Exception.Business.WorkExperienceConflictException Si se detecta una entrada duplicada para el mismo perfil.
     */
    SitterWorkExperienceResponseDTO createWorkExperience(SitterWorkExperienceRequestDTO requestDTO);

    /**
     * Obtiene una lista resumida de todas las experiencias laborales de un cuidador.
     * <p>
     * Recupera todas las experiencias asociadas a un ID de perfil de cuidador específico,
     * optimizadas para ser mostradas en listados.
     *
     * @param sitterProfileId El ID del <b>perfil del cuidador</b> (no el ID del usuario).
     * @return Una lista de {@link SitterWorkExperienceSummaryDTO}. Si el perfil no tiene experiencias, devuelve una lista vacía.
     * @throws com.Petcare.Petcare.Exception.Business.SitterProfileNotFoundException Si no se encuentra un perfil con el ID proporcionado.
     */
    List<SitterWorkExperienceSummaryDTO> getWorkExperiencesBySitterProfileId(Long sitterProfileId);

    /**
     * Obtiene los detalles completos de una experiencia laboral por su ID único.
     *
     * @param id El ID único de la entrada de experiencia laboral a consultar.
     * @return Un {@link SitterWorkExperienceResponseDTO} con los detalles completos del registro.
     * @throws com.Petcare.Petcare.Exception.Business.WorkExperienceNotFoundException Si no se encuentra ninguna experiencia con el ID proporcionado.
     */
    SitterWorkExperienceResponseDTO getWorkExperienceById(Long id);

    /**
     * Actualiza una experiencia laboral existente.
     * <p>
     * Modifica un registro de experiencia laboral existente. La implementación debe
     * validar que el usuario que realiza la solicitud es el propietario de la experiencia
     * o un administrador.
     *
     * @param id El ID único de la experiencia laboral a actualizar.
     * @param requestDTO El DTO que contiene los nuevos datos para el registro.
     * @return Un {@link SitterWorkExperienceResponseDTO} que representa el registro con los datos actualizados.
     * @throws com.Petcare.Petcare.Exception.Business.WorkExperienceNotFoundException Si no se encuentra ninguna experiencia con el ID proporcionado.
     * @throws org.springframework.security.access.AccessDeniedException Si el usuario no tiene permisos para modificar este recurso.
     */
    SitterWorkExperienceResponseDTO updateWorkExperience(Long id, SitterWorkExperienceRequestDTO requestDTO);

    /**
     * Elimina permanentemente una experiencia laboral de la base de datos.
     * <p>
     * Es una operación destructiva. La implementación debe validar que el usuario que
     * realiza la solicitud es el propietario de la experiencia o un administrador.
     *
     * @param id El ID único de la experiencia laboral a eliminar.
     * @throws com.Petcare.Petcare.Exception.Business.WorkExperienceNotFoundException Si no se encuentra ninguna experiencia con el ID proporcionado para eliminar.
     * @throws org.springframework.security.access.AccessDeniedException Si el usuario no tiene permisos para eliminar este recurso.
     */
    void deleteWorkExperience(Long id);

}