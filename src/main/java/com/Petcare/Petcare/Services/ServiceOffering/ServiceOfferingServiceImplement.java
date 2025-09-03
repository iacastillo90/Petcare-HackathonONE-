package com.Petcare.Petcare.Services.ServiceOffering;

import com.Petcare.Petcare.DTOs.ServiceOffering.CreateServiceOfferingDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.ServiceOfferingDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.UpdateServiceOfferingDTO;
import com.Petcare.Petcare.Models.ServiceOffering.ServiceOffering;
import com.Petcare.Petcare.Repositories.ServiceOfferingRepository;
import com.Petcare.Petcare.Repositories.SitterProfileRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceOfferingServiceImplement implements ServiceOfferingService {
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final SitterProfileRepository sitterProfileRepository;


    @Override
    public List< ServiceOfferingDTO > getAllServices() {
        return serviceOfferingRepository.findAll()
                .stream()
                .map(ServiceOfferingDTO::new)
                .toList();
    }

    @Override
    public ServiceOfferingDTO createServiceOffering( CreateServiceOfferingDTO createServiceOfferingDTO) {
        // Validar que el sitter existe
        if (!sitterProfileRepository.existsById(createServiceOfferingDTO.sitterId())) {
            throw new IllegalArgumentException("El cuidador especificado no existe");
        }

        // Validar que no exista un servicio con el mismo nombre para el mismo sitter
        if (serviceOfferingRepository.existsBySitterIdAndName(createServiceOfferingDTO.sitterId(),
                createServiceOfferingDTO.name())) {
            throw new IllegalArgumentException("Ya existe un servicio con el mismo nombre para este cuidador");
        }

        ServiceOffering serviceOffering = new ServiceOffering ();
        serviceOffering.setSitterId( serviceOfferingRepository.findBySitterId ( serviceOffering.getSitterId () ));
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