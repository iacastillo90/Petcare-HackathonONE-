package com.Petcare.Petcare.Services.ServiceOffering;

import com.Petcare.Petcare.DTOs.ServiceOffering.CreateServiceOfferingDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.ServiceOfferingDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.UpdateServiceOfferingDTO;
import com.Petcare.Petcare.Models.ServiceOffering.ServiceOffering;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.ServiceOfferingRepository;
import com.Petcare.Petcare.Repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

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


    /**
     * Obtiene todos los servicios disponibles en la plataforma.
     * 
     * @return Lista de DTOs con todos los servicios registrados
     */
    @Override
    public List< ServiceOfferingDTO > getAllServices() {
        return serviceOfferingRepository.findAll()
                .stream()
                .map(ServiceOfferingDTO::new)
                .toList();
    }

    @Override
    public ServiceOfferingDTO createServiceOffering(CreateServiceOfferingDTO createServiceOfferingDTO, Long id) {
        //Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //String username = authentication.getName();
        User currentUser = userRepository.findById (id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (serviceOfferingRepository.existsBySitterIdAndName(currentUser.getId(), createServiceOfferingDTO.name())) {
            throw new IllegalArgumentException("Ya existe un servicio con este nombre para este usuario");
        }

        //crear una validacion para que no hayan dos servicios con elmismo nombre
        if (serviceOfferingRepository.existsByName(createServiceOfferingDTO.name())) {
            throw new IllegalArgumentException("Ya existe un servicio con este nombre");
        }

        if (createServiceOfferingDTO.durationInMinutes() < 15) {
            throw new IllegalArgumentException("La duración mínima del servicio es 15 minutos");
        }
        
        ServiceOffering serviceOffering = new ServiceOffering();
        serviceOffering.setSitterId(currentUser.getId());
        serviceOffering.setServiceType(createServiceOfferingDTO.serviceType());
        serviceOffering.setName(createServiceOfferingDTO.name());
        serviceOffering.setDescription(createServiceOfferingDTO.description());
        serviceOffering.setDurationInMinutes(createServiceOfferingDTO.durationInMinutes());
        serviceOffering.setPrice(createServiceOfferingDTO.price());
        serviceOffering.setActive(true);
        serviceOffering.setCreatedAt(Timestamp.from(Instant.now()));
        
        ServiceOffering savedService = serviceOfferingRepository.save(serviceOffering);
        return new ServiceOfferingDTO(savedService);
    }

    /**
     * Obtiene todos los servicios ofrecidos por un cuidador específico.
     * 
     * @param userId identificador del cuidador
     * @return Lista de DTOs con los servicios del cuidador
     * @throws IllegalArgumentException si el usuario no existe
     */
    @Override
    public List < ServiceOfferingDTO > getAllSetvicesByUserId(Long userId) {
        User currentUser = userRepository.findById (userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        return serviceOfferingRepository.findBySitterId(currentUser.getId())
                .stream()
                .map(ServiceOfferingDTO::new)
                .toList();
    }

    /**
     * Obtiene un servicio específico por su identificador.
     * 
     * @param id identificador único del servicio
     * @return DTO con los detalles del servicio
     * @throws IllegalArgumentException si el servicio no existe
     */
    @Override
    public ServiceOfferingDTO getServiceById(Long id) {
        ServiceOffering service = serviceOfferingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con ID: " + id));
        return new ServiceOfferingDTO(service);
    }

    @Override
    public ServiceOfferingDTO updateServiceOffering(Long id, UpdateServiceOfferingDTO updateService) {
        ServiceOffering existingService = serviceOfferingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con ID: " + id));

        existingService.setServiceType(updateService.serviceType());
        existingService.setName(updateService.name());
        existingService.setDescription(updateService.description());
        existingService.setPrice(updateService.price());
        existingService.setDurationInMinutes(updateService.durationInMinutes());

        ServiceOffering updatedService = serviceOfferingRepository.save(existingService);
        return new ServiceOfferingDTO(updatedService);
    }

    @Override
    public void deleteServiceOffering ( Long id ) {
        if (!serviceOfferingRepository.existsById(id)) {
            throw new IllegalArgumentException("El servicio no existe");
        }

        ServiceOffering serviceOffering = new ServiceOffering();
        serviceOffering.setActive(false);
        serviceOfferingRepository.save(serviceOffering);
    }
}