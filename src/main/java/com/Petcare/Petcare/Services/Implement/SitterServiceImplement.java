package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.DTOs.Sitter.SitterProfileDTO;
import com.Petcare.Petcare.DTOs.Sitter.SitterProfileMapper;
import com.Petcare.Petcare.DTOs.Sitter.SitterProfileSummary;
import com.Petcare.Petcare.Exception.Business.SitterProfileAlreadyExistsException;
import com.Petcare.Petcare.Exception.Business.SitterProfileNotFoundException;
import com.Petcare.Petcare.Models.SitterProfile;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.SitterProfileRepository;
import com.Petcare.Petcare.Repositories.UserRepository;
import com.Petcare.Petcare.Services.SitterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;

/**
 * Implementación del servicio para la gestión de perfiles de cuidadores (Sitters).
 *
 * <p>Esta clase encapsula toda la lógica de negocio relacionada con los perfiles
 * profesionales de los cuidadores en la plataforma Petcare. Se encarga de las operaciones
 * CRUD, las validaciones de negocio y las consultas especializadas para encontrar cuidadores.</p>
 *
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 * <li>Creación de nuevos perfiles de cuidador para usuarios existentes.</li>
 * <li>Operaciones CRUD completas (Crear, Leer, Actualizar, Eliminar) sobre los perfiles.</li>
 * <li>Validación para prevenir perfiles duplicados por usuario.</li>
 * <li>Consultas especializadas para buscar cuidadores disponibles y verificados, con filtros opcionales.</li>
 * <li>Mapeo entre las entidades de la base de datos (SitterProfile) y los DTOs (Data Transfer Objects).</li>
 * </ul>
 *
 * <p><strong>Patrones implementados:</strong></p>
 * <ul>
 * <li>Capa de Servicio (Service Layer) para separar la lógica de negocio de los controladores.</li>
 * <li>Inyección de Dependencias para gestionar los repositorios y mappers.</li>
 * <li>Patrón DTO para una comunicación limpia y segura con la capa de presentación.</li>
 * <li>Uso de transacciones (@Transactional) para garantizar la consistencia de los datos.</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see SitterService
 * @see SitterProfile
 */
@Service
public class SitterServiceImplement implements SitterService {

    // Dependencia del repositorio de usuarios para validar y obtener información del usuario.
    private final UserRepository userRepository;
    // Dependencia del repositorio de perfiles de cuidador para interactuar con la base de datos.
    private final SitterProfileRepository sitterProfileRepository;
    // Dependencia del mapper para convertir entre la entidad SitterProfile y sus DTOs.
    private final SitterProfileMapper sitterProfileMapper;

    /**
     * Constructor para la inyección de dependencias de SitterServiceImplement.
     *
     * @param userRepository Repositorio para acceder a los datos de los usuarios.
     * @param sitterProfileRepository Repositorio para acceder a los perfiles de los cuidadores.
     * @param sitterProfileMapper Mapper para convertir entre entidades SitterProfile y sus DTOs.
     */
    public SitterServiceImplement(UserRepository userRepository,
                                  SitterProfileRepository sitterProfileRepository, SitterProfileMapper sitterProfileMapper) {
        this.userRepository = userRepository;
        this.sitterProfileRepository = sitterProfileRepository;
        this.sitterProfileMapper = sitterProfileMapper;
    }

    /**
     * Crea un nuevo perfil de cuidador para un usuario existente en el sistema.
     * <p>
     * Este método realiza las siguientes validaciones antes de la creación: </p>
     * <ul>
     * <li>Verifica que el usuario con el ID proporcionado realmente exista.</li>
     * <li>Asegura que el usuario no tenga ya un perfil de cuidador asociado para evitar duplicados.</li>
     * </ul>
     *
     * @param userId El ID del usuario para el cual se va a crear el perfil de cuidador.
     * @param sitterProfileDTO El DTO que contiene toda la información del nuevo perfil.
     * @return Un DTO que representa el perfil del cuidador recién creado y guardado.
     * @throws SitterProfileAlreadyExistsException Si el usuario no se encuentra o si ya tiene un perfil.
     */
    @Override
    @Transactional
    public SitterProfileDTO createSitterProfile(Long userId, SitterProfileDTO sitterProfileDTO) {
        // --- Validación Interna ---
        // Buscamos al usuario por su ID. Si no lo encontramos, es un error irrecuperable.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cuidador no encontrado con ID: " + userId));


        // Verificamos que no estemos creando un perfil para un usuario que ya tiene uno.
        if (sitterProfileRepository.findByUserId(user.getId()).isPresent()) {
            throw new SitterProfileAlreadyExistsException("Cuidador ya tiene un perfil");
        }

        // --- Lógica de Negocio ---
        // Creamos una nueva entidad SitterProfile a partir de los datos del DTO.
        SitterProfile profile = new SitterProfile(
                user,
                sitterProfileDTO.getBio(),
                sitterProfileDTO.getHourlyRate(),
                sitterProfileDTO.getServicingRadius(),
                sitterProfileDTO.getProfileImageUrl()
        );

        // Establecemos los valores booleanos iniciales desde el DTO.
        profile.setVerified(sitterProfileDTO.isVerified());
        profile.setAvailableForBookings(sitterProfileDTO.isAvailableForBookings());

        // --- Persistencia ---
        // Guardamos la nueva entidad en la base de datos. La transacción asegura que esto sea atómico.
        SitterProfile saved = sitterProfileRepository.save(profile);

        // Convertimos la entidad guardada (con su nuevo ID) a un DTO para la respuesta.
        return mapToDTO(saved);
    }

    /**
     * {@inheritDoc}
     * Obtiene el perfil de un cuidador basándose en el ID del usuario asociado.
     *
     * @param userId El ID del usuario cuyo perfil de cuidador se desea obtener.
     * @return Un DTO con la información del perfil del cuidador.
     * @throws RuntimeException si no se encuentra un perfil para el ID de usuario proporcionado.
     */
    @Override
    @Transactional(readOnly = true)
    public SitterProfileDTO getSitterProfile(Long userId) {
        // Buscamos el perfil por el ID de usuario.
        // Usamos .map() para transformar el resultado si está presente.
        // Usamos .orElseThrow() para lanzar una excepción si el Optional está vacío.
        return sitterProfileRepository.findByUserId(userId)
                .map(this::mapToDTO)
                .orElseThrow(() -> new SitterProfileNotFoundException("No se encontró un perfil de cuidador para el usuario con ID: " + userId));
    }

    /**
     * {@inheritDoc}
     * Actualiza la información de un perfil de cuidador existente.
     *
     * @param userId El ID del usuario cuyo perfil se va a actualizar.
     * @param sitterProfileDTO Un DTO que contiene los nuevos datos para el perfil.
     * @return Un DTO del perfil del cuidador con la información ya actualizada.
     * @throws RuntimeException si no se encuentra un perfil para el ID de usuario proporcionado.
     */
    @Override
    @Transactional
    public SitterProfileDTO updateSitterProfile(Long userId, SitterProfileDTO sitterProfileDTO) {
        // Primero, obtenemos el perfil existente de la base de datos.
        SitterProfile profile = sitterProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new SitterProfileNotFoundException("El perfil no se encuentra"));

        // Actualizamos cada uno de los campos de la entidad con los valores del DTO.
        profile.setBio(sitterProfileDTO.getBio());
        profile.setHourlyRate(sitterProfileDTO.getHourlyRate());
        profile.setServicingRadius(sitterProfileDTO.getServicingRadius());
        profile.setProfileImageUrl(sitterProfileDTO.getProfileImageUrl());
        profile.setVerified(sitterProfileDTO.isVerified());
        profile.setAvailableForBookings(sitterProfileDTO.isAvailableForBookings());

        // Guardamos la entidad actualizada. JPA se encargará de generar el UPDATE SQL.
        SitterProfile updated = sitterProfileRepository.save(profile);
        return mapToDTO(updated);
    }

    /**
     * Método auxiliar privado para mapear una entidad {@link SitterProfile} a su DTO {@link SitterProfileDTO}.
     * <p>Centraliza la lógica de conversión para promover la reutilización de código y facilitar el mantenimiento.</p>
     *
     * @param sitterProfile La entidad a convertir.
     * @return El DTO resultante con los datos de la entidad.
     */
    private SitterProfileDTO mapToDTO(SitterProfile sitterProfile) {
        // Simplemente crea y devuelve un nuevo DTO usando los datos de la entidad.
        return new SitterProfileDTO(
                sitterProfile.getId(),
                sitterProfile.getUser().getId(),
                sitterProfile.getBio(),
                sitterProfile.getHourlyRate(),
                sitterProfile.getServicingRadius(),
                sitterProfile.getProfileImageUrl(),
                sitterProfile.isVerified(),
                sitterProfile.isAvailableForBookings()
        );
    }

    /**
     * {@inheritDoc}
     * Obtiene una lista de todos los perfiles de cuidadores registrados en el sistema.
     *
     * @return Una lista de DTOs, donde cada DTO representa un perfil de cuidador.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SitterProfileDTO> getAllSitterProfiles() {
        // Obtenemos todas las entidades de perfil de la base de datos.
        List<SitterProfile> profiles = sitterProfileRepository.findAll();
        List<SitterProfileDTO> dtos = new ArrayList<>();
        // Iteramos sobre cada entidad y la convertimos a su DTO correspondiente.
        for (SitterProfile profile : profiles) {
            dtos.add(mapToDTO(profile));
        }
        return dtos;
    }

    /**
     * {@inheritDoc}
     * Elimina el perfil de un cuidador basándose en el ID del usuario asociado.
     *
     * @param userId El ID del usuario cuyo perfil de cuidador será eliminado.
     * @throws RuntimeException si no se encuentra un perfil para el ID de usuario proporcionado.
     */
    @Override
    @Transactional
    public void deleteSitterProfile(Long userId) {
        // Buscamos el perfil que se va a eliminar. Si no existe, lanzará una excepción.
        SitterProfile profile = sitterProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new SitterProfileNotFoundException("El perfil no se encuentra"));
        // Si se encuentra, lo eliminamos de la base de datos.
        sitterProfileRepository.delete(profile);
    }


    /**
     * {@inheritDoc}
     * Busca perfiles de cuidadores que cumplan con ciertos criterios de disponibilidad y verificación.
     * Puede filtrar opcionalmente por ciudad.
     *
     * @param city El filtro opcional de ciudad. Si es nulo o vacío, busca en todas las ciudades.
     * @return Una lista de DTOs de resumen (`SitterProfileSummary`) de los cuidadores encontrados.
     */
    @Override
    @Transactional(readOnly = true) // Marcado como solo lectura porque es una operación de consulta.
    public List<SitterProfileSummary> findSitters(String city) {

        List<SitterProfile> profiles;

        // --- Lógica de Filtrado ---
        // Verificamos si se proporcionó un filtro de ciudad válido.
        if (city != null && !city.trim().isEmpty()) {
            // Si hay ciudad, llamamos al método del repositorio que filtra por ella.
            profiles = sitterProfileRepository.findByIsVerifiedTrueAndIsAvailableForBookingsTrueAndUser_AddressContainingIgnoreCase(city);
        } else {
            // Si no hay ciudad, realizamos una búsqueda general de todos los cuidadores disponibles.
            profiles = sitterProfileRepository.findByIsVerifiedTrueAndIsAvailableForBookingsTrue();
        }

        // --- Mapeo a DTO ---
        // Usamos un stream para convertir la lista de entidades `SitterProfile` a una lista de DTOs `SitterProfileSummary`.
        // Delegamos la lógica de conversión al `sitterProfileMapper`.
        return profiles.stream()
                .map(sitterProfileMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

}
