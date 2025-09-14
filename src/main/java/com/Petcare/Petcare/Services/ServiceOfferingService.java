package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.DTOs.ServiceOffering.CreateServiceOfferingDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.ServiceOfferingDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.UpdateServiceOfferingDTO;

import java.util.List;

public interface ServiceOfferingService {
    List < ServiceOfferingDTO > getAllServices();
    ServiceOfferingDTO createServiceOffering( CreateServiceOfferingDTO createServiceOfferingDTO, Long id);
    List <ServiceOfferingDTO> getAllServicesByUserId(Long userId);
    ServiceOfferingDTO getServiceById(Long id);
    ServiceOfferingDTO updateServiceOffering(Long id, UpdateServiceOfferingDTO updateService);
    void deleteServiceOffering(Long id);
}