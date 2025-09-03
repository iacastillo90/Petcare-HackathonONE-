package com.Petcare.Petcare.Services.ServiceOffering;

import com.Petcare.Petcare.DTOs.ServiceOffering.CreateServiceOfferingDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.ServiceOfferingDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.UpdateServiceOfferingDTO;

import java.util.List;

public interface ServiceOfferingService {
    List < ServiceOfferingDTO > getAllServices();
    ServiceOfferingDTO createServiceOffering( CreateServiceOfferingDTO createServiceOfferingDTO);
    ServiceOfferingDTO getServiceById(Long id);
    ServiceOfferingDTO updateServiceOffering(Long id, UpdateServiceOfferingDTO updateService);
    void deleteServiceOffering(Long id);
}