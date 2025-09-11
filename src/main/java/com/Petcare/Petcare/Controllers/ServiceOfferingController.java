package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.ServiceOffering.CreateServiceOfferingDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.ServiceOfferingDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.UpdateServiceOfferingDTO;
import com.Petcare.Petcare.Services.ServiceOffering.ServiceOfferingServiceImplement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping ("/api/services")
@RequiredArgsConstructor
public class ServiceOfferingController {
    private final ServiceOfferingServiceImplement serviceOfferingServiceImplement;

    @GetMapping ("/test")
    public ResponseEntity <String> test() {
        return ResponseEntity.ok("controller works!");
    }

    @GetMapping
    public ResponseEntity< List < ServiceOfferingDTO > > getAllServices() {
        return ResponseEntity.ok(serviceOfferingServiceImplement.getAllServices());
    }

    @PostMapping
    public ResponseEntity<ServiceOfferingDTO> createService(@Valid @RequestBody CreateServiceOfferingDTO createServiceOfferingDTO) {
        ServiceOfferingDTO newService = serviceOfferingServiceImplement.createServiceOffering(createServiceOfferingDTO);
        return ResponseEntity.status( HttpStatus.CREATED).body(newService);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceOfferingDTO> getServiceById(@PathVariable Long id) {
        ServiceOfferingDTO service = serviceOfferingServiceImplement.getServiceById(id);
        return ResponseEntity.ok(service);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceOfferingDTO> updateService(@PathVariable Long id, @Valid @RequestBody UpdateServiceOfferingDTO updateDTO) {
        ServiceOfferingDTO updatedService = serviceOfferingServiceImplement.updateServiceOffering(id, updateDTO);
        return ResponseEntity.ok(updatedService);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceOfferingServiceImplement.deleteServiceOffering(id);
        return ResponseEntity.noContent().build();
    }
}