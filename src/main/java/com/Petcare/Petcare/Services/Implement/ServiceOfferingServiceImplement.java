package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.DTOs.ServiceOffering.CreateServiceOfferingDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.ServiceOfferingDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.UpdateServiceOfferingDTO;
import com.Petcare.Petcare.Exception.Business.ServiceOfferingConflictException;
import com.Petcare.Petcare.Exception.Business.ServiceOfferingNotFoundException;
import com.Petcare.Petcare.Exception.Business.SitterProfileNotFoundException;
import com.Petcare.Petcare.Exception.Business.UserNotFoundException;
import com.Petcare.Petcare.Models.ServiceOffering.ServiceOffering;
import com.Petcare.Petcare.Models.SitterProfile;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.ServiceOfferingRepository;
import com.Petcare.Petcare.Repositories.SitterProfileRepository;
import com.Petcare.Petcare.Repositories.UserRepository;
import com.Petcare.Petcare.Services.ServiceOfferingService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para la gestión de ofertas de servicios de cuidado de mascotas.
 * 
 * <p>Esta clase contiene la lógica de negocio para todas las operaciones relacionadas
 * con los servicios que ofrecen los cuidadores en la plataforma Petcare. Incluye
 * validaciones de negocio, transformaciones de datos y coordinación con la capa
 * de persistencia.</p>
 * 
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li>Creación de nuevos servicios con validaciones de negocio</li>
 *   <li>Consulta de servicios por diferentes criterios</li>
 *   <li>Actualización de servicios existentes</li>
 *   <li>Eliminación lógica de servicios</li>
 *   <li>Validación de duración mínima y otros parámetros</li>
 * </ul>
 * 
 * @author Equipo Petcare
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class ServiceOfferingServiceImplement implements ServiceOfferingService {
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final UserRepository userRepository;
    private final SitterProfileRepository sitterProfileRepository;


    /**
     * Recupera una lista de todas las ofertas de servicio activas en la plataforma.
     * <p>
     * Este método está diseñado para poblar catálogos de servicios públicos, asegurando que
     * solo las ofertas que están actualmente disponibles y activas sean mostradas a los clientes.
     * </p>
     * <b>Desglose del Proceso:</b>
     * <ul>
     * <li>Consulta al repositorio para obtener todas las entidades {@code ServiceOffering} cuyo estado es {@code isActive = true}.</li>
     * <li>Transforma (mapea) cada entidad encontrada a su correspondiente {@link ServiceOfferingDTO}.</li>
     * <li>Devuelve la lista de DTOs. Si no se encuentran servicios activos, devuelve una lista vacía.</li>
     * </ul>
     *
     * @return Una {@link List} de {@link ServiceOfferingDTO} con todos los servicios activos.
     * Devolverá una lista vacía si no hay ninguno disponible.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ServiceOfferingDTO> getAllServices() {
        return serviceOfferingRepository.findAllByIsActiveTrue()
                .stream()
                .map(ServiceOfferingDTO::new)
                .toList();
    }

    /**
     * Crea una nueva oferta de servicio y la asocia a un cuidador.
     * <p>
     * Este método implementa la lógica de negocio para registrar un nuevo servicio en el catálogo
     * de un cuidador. Realiza validaciones críticas antes de persistir los datos para garantizar
     * la integridad del sistema.
     * </p>
     * <b>Desglose del Proceso de Negocio:</b>
     * <ul>
     * <li><b>Búsqueda y Validación del Cuidador:</b> Verifica que el ID proporcionado corresponda a un usuario existente. Si no, lanza {@link UserNotFoundException}.</li>
     * <li><b>Verificación de Duplicados:</b> Comprueba si el cuidador ya tiene un servicio con el mismo nombre para evitar duplicados en su catálogo. Si es así, lanza {@link ServiceOfferingConflictException}.</li>
     * <li><b>Validación de Reglas de Negocio:</b> Asegura que los datos del servicio cumplan con las reglas de la plataforma (ej. duración mínima de 15 minutos).</li>
     * <li><b>Creación de la Entidad:</b> Instancia una nueva entidad {@code ServiceOffering} con los datos validados.</li>
     * <li><b>Persistencia:</b> Guarda la nueva entidad en la base de datos dentro de una transacción.</li>
     * <li><b>Respuesta:</b> Mapea la entidad persistida (con su nuevo ID) a un DTO para devolverla al controlador.</li>
     * </ul>
     *
     * @param createServiceOfferingDTO DTO con los datos del nuevo servicio a crear.
     * @param sitterId ID del usuario (cuidador) al que se le asignará el servicio.
     * @return Un {@link ServiceOfferingDTO} que representa el servicio recién creado.
     * @throws UserNotFoundException Si no se encuentra ningún usuario con el {@code sitterId} proporcionado.
     * @throws ServiceOfferingConflictException Si el cuidador ya ofrece un servicio con el mismo nombre.
     * @throws IllegalArgumentException Si no se cumplen otras reglas de negocio, como la duración mínima.
     */
    @Override
    @Transactional
    public ServiceOfferingDTO createServiceOffering(CreateServiceOfferingDTO createServiceOfferingDTO, Long sitterId) {
        // 1. Buscar y validar que el usuario cuidador exista.
        User sitter = userRepository.findById(sitterId)
                .orElseThrow(() -> new UserNotFoundException("Usuario cuidador no encontrado con ID: " + sitterId));

        // 2. Validar que no exista un servicio con el mismo nombre PARA ESTE CUIDADOR.
        if (serviceOfferingRepository.existsBySitterIdAndName(sitter.getId(), createServiceOfferingDTO.name())) {
            throw new ServiceOfferingConflictException("Ya existe un servicio con el nombre '" + createServiceOfferingDTO.name() + "' para este cuidador.");
        }

        // 3. Validar reglas de negocio adicionales.
        if (createServiceOfferingDTO.durationInMinutes() < 15) {
            throw new IllegalArgumentException("La duración mínima del servicio es 15 minutos.");
        }

        // 4. Crear la entidad a partir del DTO y el cuidador encontrado.
        // (Se asume un nuevo constructor en la entidad ServiceOffering para mayor limpieza).
        ServiceOffering serviceOffering = new ServiceOffering(sitter.getId(), createServiceOfferingDTO);

        // 5. Persistir la nueva entidad.
        ServiceOffering savedService = serviceOfferingRepository.save(serviceOffering);

        // 6. Mapear la entidad guardada a un DTO y retornarla.
        return new ServiceOfferingDTO(savedService);
    }


    /**
     * Recupera todas las ofertas de servicio asociadas a un cuidador específico.
     * <p>
     * Este método permite obtener el catálogo completo de servicios que un cuidador
     * ha registrado en la plataforma. Es utilizado tanto por los clientes para explorar
     * las opciones de un cuidador como por los propios cuidadores para ver sus servicios.
     * </p>
     * <b>Desglose del Proceso de Negocio:</b>
     * <ul>
     * <li><b>Búsqueda y Validación del Cuidador:</b> Se asegura de que el ID proporcionado
     * corresponda a un usuario existente. Si no se encuentra, se lanza una
     * excepción {@link UserNotFoundException}.</li>
     * <li><b>Recuperación de Servicios:</b> Utiliza el ID del cuidador validado para
     * consultar todas sus ofertas de servicio asociadas desde el repositorio.</li>
     * <li><b>Mapeo a DTO:</b> Transforma la lista de entidades {@code ServiceOffering} a una
     * lista de DTOs {@code ServiceOfferingDTO} para ser devuelta.</li>
     * </ul>
     *
     * @param sitterId El ID del <strong>usuario</strong> (User) con rol SITTER cuyo catálogo de
     * servicios se desea obtener.
     * @return Una {@link List} de {@link ServiceOfferingDTO} con los servicios del cuidador.
     * Devuelve una lista vacía si el cuidador no tiene servicios registrados.
     * @throws UserNotFoundException Si no se encuentra ningún usuario con el {@code sitterId} proporcionado.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ServiceOfferingDTO> getAllServicesByUserId(Long sitterId) {
        // 1. Primero, busca el SitterProfile usando el ID que viene del frontend.
        SitterProfile sitterProfile = sitterProfileRepository.findById(sitterId)
                .orElseThrow(() -> new SitterProfileNotFoundException("Perfil de cuidador no encontrado con ID: " + sitterId));

        // 2. Del perfil, obtén el ID del usuario asociado.
        Long userId = sitterProfile.getUser().getId();

        // 3. Ahora, usa el ID de usuario correcto para buscar los servicios.
        return serviceOfferingRepository.findBySitterId(userId)
                .stream()
                .map(ServiceOfferingDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los detalles completos de una oferta de servicio específica por su ID.
     * <p>
     * Este método es fundamental para que los clientes puedan ver la información detallada de un
     * servicio antes de decidirse a reservarlo.
     * </p>
     * <b>Desglose del Proceso de Negocio:</b>
     * <ul>
     * <li><b>Búsqueda:</b> Intenta encontrar la entidad {@code ServiceOffering} en la base de datos utilizando su clave primaria (ID).</li>
     * <li><b>Validación:</b> Si no se encuentra ninguna entidad, lanza una excepción de negocio {@link ServiceOfferingNotFoundException}, lo que resultará en una respuesta HTTP 404.</li>
     * <li><b>Mapeo y Retorno:</b> Si la entidad se encuentra, la mapea a un {@link ServiceOfferingDTO} y la devuelve.</li>
     * </ul>
     *
     * @param id El identificador único de la oferta de servicio que se desea consultar.
     * @return Un {@link ServiceOfferingDTO} con los detalles completos del servicio encontrado.
     * @throws ServiceOfferingNotFoundException Si no se encuentra ninguna oferta de servicio con el ID proporcionado.
     */
    @Override
    @Transactional(readOnly = true)
    public ServiceOfferingDTO getServiceById(Long id) {
        // 1. Buscar el servicio por su ID. Si no se encuentra, lanzar una excepción de negocio específica.
        ServiceOffering service = serviceOfferingRepository.findById(id)
                .orElseThrow(() -> new ServiceOfferingNotFoundException("Oferta de servicio no encontrada con ID: " + id));

        // 2. Mapear la entidad encontrada a su DTO y devolverla.
        return new ServiceOfferingDTO(service);
    }

    /**
     * Actualiza una oferta de servicio existente con nuevos datos.
     * <p>
     * Este método gestiona la modificación de un servicio. Antes de aplicar cualquier cambio,
     * realiza validaciones críticas de seguridad y de negocio para mantener la integridad
     * y coherencia de los datos en la plataforma.
     * </p>
     * <b>Desglose del Proceso de Negocio y Seguridad:</b>
     * <ul>
     * <li><b>Búsqueda:</b> Localiza la oferta de servicio por su ID. Si no existe, lanza {@link ServiceOfferingNotFoundException}.</li>
     * <li><b>Autorización:</b> Llama a un método de validación interna para asegurar que el usuario autenticado
     * sea el propietario del servicio o un administrador. Si no lo es, lanza {@link AccessDeniedException}.</li>
     * <li><b>Validación de Conflicto:</b> Si el nombre del servicio está siendo modificado, verifica que el nuevo
     * nombre no esté ya en uso por otro servicio del mismo cuidador. Si lo está, lanza {@link ServiceOfferingConflictException}.</li>
     * <li><b>Aplicación de Cambios:</b> Actualiza los campos de la entidad con los valores proporcionados en el DTO.</li>
     * <li><b>Persistencia:</b> Guarda la entidad actualizada en la base de datos.</li>
     * </ul>
     *
     * @param serviceId El ID único de la oferta de servicio que se va a actualizar.
     * @param updateRequest DTO que contiene los nuevos datos para el servicio.
     * @return Un {@link ServiceOfferingDTO} que representa el servicio con sus datos actualizados.
     * @throws ServiceOfferingNotFoundException Si no se encuentra ninguna oferta de servicio con el ID proporcionado.
     * @throws ServiceOfferingConflictException Si el nuevo nombre del servicio ya existe para ese cuidador.
     * @throws AccessDeniedException Si el usuario autenticado no tiene permisos para modificar este recurso.
     */
    @Override
    @Transactional
    public ServiceOfferingDTO updateServiceOffering(Long serviceId, UpdateServiceOfferingDTO updateRequest) {
        // 1. Buscar el servicio existente o lanzar una excepción 404.
        ServiceOffering existingService = serviceOfferingRepository.findById(serviceId)
                .orElseThrow(() -> new ServiceOfferingNotFoundException("Oferta de servicio no encontrada con ID: " + serviceId));

        // 2. Validar que el usuario actual es el propietario del servicio o un administrador.
        validateOwnershipOrAdmin(existingService);

        // 3. Si el nombre ha cambiado, verificar que el nuevo no esté ya en uso por el mismo cuidador.
        if (updateRequest.name() != null && !updateRequest.name().equalsIgnoreCase(existingService.getName())) {
            if (serviceOfferingRepository.existsBySitterIdAndName(existingService.getSitterId(), updateRequest.name())) {
                throw new ServiceOfferingConflictException("Ya existe otro servicio con el nombre '" + updateRequest.name() + "' para este cuidador.");
            }
            existingService.setName(updateRequest.name());
        }

        // 4. Actualizar los campos de la entidad con los datos del DTO.
        existingService.setServiceType(updateRequest.serviceType());
        existingService.setDescription(updateRequest.description());
        existingService.setPrice(updateRequest.price());
        existingService.setDurationInMinutes(updateRequest.durationInMinutes());

        // La fecha de actualización (updatedAt) será manejada automáticamente por JPA Auditing.
        ServiceOffering updatedService = serviceOfferingRepository.save(existingService);

        return new ServiceOfferingDTO(updatedService);
    }

    /**
     * Desactiva (eliminación lógica) una oferta de servicio existente.
     * <p>
     * Este método no elimina el registro de la base de datos. En su lugar, realiza una
     * eliminación lógica (soft delete) estableciendo el campo {@code isActive} de la entidad a {@code false}.
     * Este enfoque es fundamental para mantener la integridad referencial con las reservas
     * históricas que puedan estar asociadas a este servicio.
     * </p>
     * <b>Desglose del Proceso de Negocio y Seguridad:</b>
     * <ul>
     * <li><b>Búsqueda:</b> Localiza la oferta de servicio por su ID. Si no se encuentra, lanza {@link ServiceOfferingNotFoundException}.</li>
     * <li><b>Autorización:</b> Valida que el usuario autenticado sea el propietario del servicio o un administrador. Si no lo es, lanza {@link AccessDeniedException}.</li>
     * <li><b>Actualización:</b> Establece la propiedad {@code isActive} del servicio a {@code false}.</li>
     * <li><b>Persistencia:</b> Guarda la entidad actualizada en la base de datos.</li>
     * </ul>
     *
     * @param serviceId El ID único de la oferta de servicio que se va a desactivar.
     * @throws ServiceOfferingNotFoundException Si no se encuentra ninguna oferta de servicio con el ID proporcionado.
     * @throws AccessDeniedException Si el usuario autenticado no tiene permisos para eliminar (desactivar) este recurso.
     */
    @Override
    @Transactional
    public void deleteServiceOffering(Long serviceId) {
        // 1. Buscar el servicio existente o lanzar una excepción 404.
        ServiceOffering existingService = serviceOfferingRepository.findById(serviceId)
                .orElseThrow(() -> new ServiceOfferingNotFoundException("Oferta de servicio no encontrada con ID: " + serviceId));

        // 2. Validar que el usuario actual es el propietario del servicio o un administrador.
        validateOwnershipOrAdmin(existingService);

        // 3. (Opcional pero recomendado) Verificar si ya está inactivo para evitar una escritura innecesaria.
        if (!existingService.isActive()) {
            return;
        }

        // 4. Realizar la eliminación lógica.
        existingService.setActive(false);

        // 5. Persistir el cambio.
        serviceOfferingRepository.save(existingService);
    }

    /**
     * Método de utilidad privado para centralizar la lógica de autorización.
     * <p>
     * Verifica si el usuario actual en el contexto de seguridad es un administrador o
     * el propietario de la oferta de servicio. Lanza una {@link AccessDeniedException}
     * si no se cumple ninguna de las condiciones.
     *
     * @param serviceOffering La oferta de servicio cuya propiedad se va a verificar.
     * @throws AccessDeniedException Si el usuario no tiene permisos para la operación.
     */
    private void validateOwnershipOrAdmin(ServiceOffering serviceOffering) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        Long ownerId = serviceOffering.getSitterId();

        if (!isAdmin && !ownerId.equals(currentUser.getId())) {
            throw new AccessDeniedException("No tiene permisos para modificar la oferta de servicio de otro cuidador.");
        }
    }
}