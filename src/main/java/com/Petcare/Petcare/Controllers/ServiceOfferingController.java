package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.ServiceOffering.CreateServiceOfferingDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.ServiceOfferingDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.UpdateServiceOfferingDTO;
import com.Petcare.Petcare.Services.ServiceOffering.ServiceOfferingServiceImplement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceOfferingController {
    private final ServiceOfferingServiceImplement serviceOfferingServiceImplement;

    /**
     * Endpoint de prueba para verificar el funcionamiento del controlador.
     * 
     * @return ResponseEntity con mensaje de confirmación
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("controller works!");
    }

    /**
     * Obtiene todos los servicios disponibles en la plataforma.
     * 
     * <p>Este endpoint está disponible públicamente para que los clientes
     * puedan explorar todos los servicios ofrecidos por los cuidadores.</p>
     * 
     * @return ResponseEntity con lista de todos los servicios disponibles
     */
    @GetMapping
    public ResponseEntity<List<ServiceOfferingDTO>> getAllServices() {
        return ResponseEntity.ok(serviceOfferingServiceImplement.getAllServices());
    }

    /**
     * Crea un nuevo servicio para un cuidador específico.
     * 
     * <p>Permite a los cuidadores registrar nuevos servicios que ofrecen,
     * incluyendo detalles como tipo de servicio, duración, precio y descripción.</p>
     * 
     * @param createServiceOfferingDTO datos del servicio a crear
     * @param id identificador del cuidador
     * @return ResponseEntity con el servicio creado
     * @throws IllegalArgumentException si los datos son inválidos o el cuidador no existe
     */
    @PostMapping("/create/{id}")
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<ServiceOfferingDTO> createService(
            @Valid @RequestBody CreateServiceOfferingDTO createServiceOfferingDTO, @PathVariable Long id) {
        ServiceOfferingDTO newService = serviceOfferingServiceImplement.createServiceOffering(createServiceOfferingDTO, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(newService);
    }

    /**
     * Obtiene un servicio específico por su identificador.
     * 
     * @param id identificador único del servicio
     * @return ResponseEntity con los detalles del servicio
     * @throws IllegalArgumentException si el servicio no existe
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<ServiceOfferingDTO> getServiceById(@PathVariable Long id) {
        ServiceOfferingDTO service = serviceOfferingServiceImplement.getServiceById(id);
        return ResponseEntity.ok(service);
    }

    /**
     * Obtiene todos los servicios ofrecidos por un cuidador específico.
     * 
     * <p>Permite a los cuidadores ver todos sus servicios registrados
     * y a los administradores consultar servicios por cuidador.</p>
     * 
     * @param id identificador del cuidador
     * @return ResponseEntity con lista de servicios del cuidador
     * @throws IllegalArgumentException si el cuidador no existe
     */
    @GetMapping("/all/{id}")
    @PreAuthorize ( "hasRole('SITTER')" )
    public ResponseEntity<List<ServiceOfferingDTO>> getAllServicesByUserId(@PathVariable Long id) {
        List<ServiceOfferingDTO> service = serviceOfferingServiceImplement.getAllSetvicesByUserId(id);
        return ResponseEntity.ok(service);
    }

    /**
     * Actualiza un servicio existente.
     * 
     * <p>Permite a los cuidadores modificar los detalles de sus servicios,
     * como precio, duración, descripción o disponibilidad.</p>
     * 
     * @param id identificador del servicio a actualizar
     * @param updateDTO datos actualizados del servicio
     * @return ResponseEntity con el servicio actualizado
     * @throws IllegalArgumentException si el servicio no existe
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<ServiceOfferingDTO> updateService(@PathVariable Long id, @Valid @RequestBody UpdateServiceOfferingDTO updateDTO) {
        ServiceOfferingDTO updatedService = serviceOfferingServiceImplement.updateServiceOffering(id, updateDTO);
        return ResponseEntity.ok(updatedService);
    }

    /**
     * Elimina un servicio de la plataforma.
     * 
     * <p>Marca el servicio como inactivo en lugar de eliminarlo físicamente
     * para mantener la integridad referencial con reservas existentes.</p>
     * 
     * @param id identificador del servicio a eliminar
     * @return ResponseEntity vacío con status 204 No Content
     * @throws IllegalArgumentException si el servicio no existe
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceOfferingServiceImplement.deleteServiceOffering(id);
        return ResponseEntity.noContent().build();
    }
}