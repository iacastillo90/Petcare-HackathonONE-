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

/**
 * Controlador REST para la gestión de ofertas de servicios de cuidado de mascotas.
 * 
 * <p>Este controlador maneja todas las operaciones CRUD relacionadas con los servicios
 * que ofrecen los cuidadores (sitters) en la plataforma Petcare. Incluye funcionalidades
 * para crear, consultar, actualizar y eliminar servicios como paseos, cuidado diurno,
 * y otros servicios especializados.</p>
 * 
 * <p><strong>Endpoints principales:</strong></p>
 * <ul>
 *   <li>GET /api/services - Lista todos los servicios disponibles</li>
 *   <li>POST /api/services/create/{id} - Crea un nuevo servicio para un sitter</li>
 *   <li>GET /api/services/{id} - Obtiene un servicio específico</li>
 *   <li>GET /api/services/all/{id} - Lista servicios de un sitter específico</li>
 *   <li>PATCH /api/services/{id} - Actualiza un servicio existente</li>
 *   <li>DELETE /api/services/{id} - Elimina un servicio</li>
 * </ul>
 * 
 * <p><strong>Seguridad:</strong></p>
 * <p>La mayoría de endpoints requieren autenticación y rol SITTER, excepto la consulta
 * general de servicios que está disponible públicamente para que los clientes puedan
 * buscar servicios disponibles.</p>
 * 
 * @author Equipo Petcare
 * @version 1.0
 * @since 1.0
 */
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