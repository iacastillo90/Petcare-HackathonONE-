package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.Pet.*;
import com.Petcare.Petcare.Models.Pet;
import com.Petcare.Petcare.Services.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de mascotas (Pets).
 *
 * <p>Expone endpoints REST para realizar operaciones CRUD completas sobre las entidades
 * de mascotas, aplicando las políticas de seguridad, validación y control de acceso
 * correspondientes. Este controlador sigue el patrón establecido en el modelo de Usuario
 * para mantener consistencia arquitectónica.</p>
 *
 * <p><strong>Características del controlador:</strong></p>
 * <ul>
 * <li>Endpoints RESTful completos con códigos HTTP apropiados</li>
 * <li>Validación automática con Bean Validation</li>
 * <li>Control de acceso granular con Spring Security</li>
 * <li>Logging detallado para auditoría y debugging</li>
 * <li>Manejo de errores centralizado</li>
 * <li>Documentación completa con Javadoc en español</li>
 * </ul>
 *
 * <p><strong>Endpoints públicos:</strong></p>
 * <ul>
 * <li>GET /health - Verificación de salud del servicio</li>
 * </ul>
 *
 * <p><strong>Endpoints autenticados:</strong></p>
 * <ul>
 * <li>POST / - Crear nueva mascota</li>
 * <li>GET /{id} - Obtener mascota por ID</li>
 * <li>PUT /{id} - Actualizar mascota existente</li>
 * <li>DELETE /{id} - Eliminar mascota</li>
 * <li>GET /account/{accountId} - Mascotas de una cuenta</li>
 * <li>GET /species/{species} - Mascotas por especie</li>
 * <li>GET /search - Búsqueda de texto</li>
 * </ul>
 *
 * <p><strong>Endpoints administrativos:</strong></p>
 * <ul>
 * <li>GET / - Todas las mascotas del sistema</li>
 * <li>GET /summary - Resumen optimizado</li>
 * <li>GET /inactive - Mascotas inactivas</li>
 * <li>GET /stats - Estadísticas del sistema</li>
 * <li>PUT /{id}/toggle-active - Cambiar estado</li>
 * </ul>
 *
 * @author Equipo Petcare
 * @version 1.0
 * @since 1.0
 * @see PetService
 * @see Pet
 * @see PetResponse
 * @see CreatePetRequest
 */
@Slf4j
@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    // ========== ENDPOINTS PÚBLICOS ==========

    /**
     * Endpoint de verificación de salud del servicio de mascotas.
     *
     * <p>Proporciona información básica sobre el estado del servicio,
     * útil para monitoreo y verificaciones de conectividad.</p>
     *
     * @return ResponseEntity con mensaje de estado del servicio
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.debug("Verificación de salud del controlador de mascotas");

        try {
            String healthStatus = petService.healthCheck();
            return ResponseEntity.ok(healthStatus);
        } catch (Exception e) {
            log.error("Error en verificación de salud: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Servicio temporalmente no disponible");
        }
    }

    // ========== OPERACIONES CRUD BÁSICAS ==========

    /**
     * Crea una nueva mascota en el sistema.
     *
     * <p>Permite a usuarios autenticados registrar nuevas mascotas en sus cuentas.
     * Valida automáticamente los datos de entrada y aplica reglas de negocio
     * como verificación de duplicados y validación de cuenta activa.</p>
     *
     * @param petRequest DTO con los datos de la nueva mascota
     * @return ResponseEntity con el DTO de la mascota creada y estado 201 Created
     * @throws IllegalArgumentException si la cuenta no existe o está inactiva
     * @throws IllegalStateException si se detecta un duplicado
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PetResponse> createPet(@Valid @RequestBody CreatePetRequest petRequest) {
        log.info("Solicitud de creación de mascota recibida para cuenta: {}", petRequest.getAccountId());

        try {
            PetResponse createdPet = petService.createPet(petRequest);
            log.info("Mascota creada exitosamente con ID: {}", createdPet.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdPet);

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Error de validación al crear mascota: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error interno al crear mascota: {}", e.getMessage());
            throw new RuntimeException("Error interno del servidor", e);
        }
    }

    /**
     * Obtiene una mascota específica por su ID.
     *
     * <p>Retorna información completa de la mascota incluyendo datos de la cuenta
     * propietaria. Valida automáticamente permisos de acceso según el usuario autenticado.</p>
     *
     * @param id El identificador único de la mascota
     * @return ResponseEntity con el DTO de la mascota encontrada
     * @throws IllegalArgumentException si la mascota no existe
     * @throws SecurityException si el usuario no tiene permisos
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PetResponse> getPetById(@PathVariable Long id) {
        log.debug("Solicitud de consulta de mascota ID: {}", id);

        try {
            PetResponse pet = petService.getPetById(id);
            return ResponseEntity.ok(pet);

        } catch (IllegalArgumentException e) {
            log.warn("Mascota no encontrada con ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            log.warn("Acceso denegado a mascota ID: {} - {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Obtiene una lista de todas las mascotas del sistema.
     *
     * <p>Para usuarios regulares, retorna solo las mascotas de sus cuentas.
     * Para administradores, retorna todas las mascotas del sistema con información completa.</p>
     *
     * @return ResponseEntity con lista de DTOs de mascotas
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PetResponse>> getAllPets() {
        log.debug("Solicitud de listado completo de mascotas");

        try {
            List<PetResponse> pets = petService.getAllPets();
            log.debug("Retornando {} mascotas", pets.size());

            return ResponseEntity.ok(pets);

        } catch (Exception e) {
            log.error("Error al obtener listado de mascotas: {}", e.getMessage());
            throw new RuntimeException("Error interno del servidor", e);
        }
    }

    /**
     * Obtiene una lista resumida de mascotas para listados optimizados.
     *
     * <p>Versión optimizada que retorna solo campos esenciales, ideal para
     * interfaces de usuario con muchas mascotas o APIs con limitaciones de ancho de banda.</p>
     *
     * @return ResponseEntity con lista de DTOs resumidos
     */
    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PetSummaryResponse>> getAllPetsSummary() {
        log.debug("Solicitud de resumen de mascotas");

        try {
            List<PetSummaryResponse> petsSummary = petService.getAllPetsSummary();
            log.debug("Retornando resumen de {} mascotas", petsSummary.size());

            return ResponseEntity.ok(petsSummary);

        } catch (Exception e) {
            log.error("Error al obtener resumen de mascotas: {}", e.getMessage());
            throw new RuntimeException("Error interno del servidor", e);
        }
    }

    /**
     * Actualiza la información de una mascota existente.
     *
     * <p>Permite modificar todos los campos editables de una mascota.
     * Valida permisos de acceso y aplica reglas de negocio como verificación
     * de nombres duplicados en la misma cuenta.</p>
     *
     * @param id El identificador único de la mascota a actualizar
     * @param petRequest DTO con los nuevos datos para la mascota
     * @return ResponseEntity con el DTO de la mascota actualizada
     * @throws IllegalArgumentException si la mascota no existe
     * @throws SecurityException si no tiene permisos
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PetResponse> updatePet(@PathVariable Long id,
                                                 @Valid @RequestBody CreatePetRequest petRequest) {
        log.info("Solicitud de actualización de mascota ID: {}", id);

        try {
            PetResponse updatedPet = petService.updatePet(id, petRequest);
            log.info("Mascota ID: {} actualizada exitosamente", id);

            return ResponseEntity.ok(updatedPet);

        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al actualizar mascota ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            log.warn("Acceso denegado para actualizar mascota ID: {} - {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalStateException e) {
            log.warn("Error de estado al actualizar mascota ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Elimina una mascota del sistema por su ID.
     *
     * <p>Realiza eliminación lógica marcando la mascota como inactiva.
     * Valida permisos y dependencias antes de proceder con la eliminación.</p>
     *
     * @param id El identificador único de la mascota a eliminar
     * @return ResponseEntity vacío con estado 204 No Content
     * @throws IllegalArgumentException si la mascota no existe
     * @throws SecurityException si no tiene permisos
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        log.info("Solicitud de eliminación de mascota ID: {}", id);

        try {
            petService.deletePet(id);
            log.info("Mascota ID: {} eliminada exitosamente", id);

            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("Mascota no encontrada para eliminar ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            log.warn("Acceso denegado para eliminar mascota ID: {} - {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalStateException e) {
            log.warn("No se puede eliminar mascota ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // ========== CONSULTAS ESPECIALIZADAS ==========

    /**
     * Obtiene todas las mascotas de una cuenta específica.
     *
     * <p>Retorna todas las mascotas (activas e inactivas) asociadas a la cuenta especificada.
     * Valida permisos de acceso a la cuenta antes de retornar los datos.</p>
     *
     * @param accountId ID de la cuenta propietaria
     * @return ResponseEntity con lista de mascotas de la cuenta
     */
    @GetMapping("/account/{accountId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PetResponse>> getPetsByAccountId(@PathVariable Long accountId) {
        log.debug("Solicitud de mascotas para cuenta ID: {}", accountId);

        try {
            List<PetResponse> pets = petService.getPetsByAccountId(accountId);
            log.debug("Retornando {} mascotas para cuenta {}", pets.size(), accountId);

            return ResponseEntity.ok(pets);

        } catch (SecurityException e) {
            log.warn("Acceso denegado a mascotas de cuenta {}: {}", accountId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error al obtener mascotas de cuenta {}: {}", accountId, e.getMessage());
            throw new RuntimeException("Error interno del servidor", e);
        }
    }

    /**
     * Obtiene solo las mascotas activas de una cuenta específica.
     *
     * @param accountId ID de la cuenta propietaria
     * @return ResponseEntity con lista de mascotas activas de la cuenta
     */
    @GetMapping("/account/{accountId}/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PetResponse>> getActivePetsByAccountId(@PathVariable Long accountId) {
        log.debug("Solicitud de mascotas activas para cuenta ID: {}", accountId);

        try {
            List<PetResponse> activePets = petService.getActivePetsByAccountId(accountId);
            return ResponseEntity.ok(activePets);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Obtiene mascotas filtradas por especie.
     *
     * @param species Especie a buscar
     * @return ResponseEntity con lista de mascotas de la especie especificada
     */
    @GetMapping("/species/{species}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PetResponse>> getPetsBySpecies(@PathVariable String species) {
        log.debug("Solicitud de mascotas por especie: {}", species);

        try {
            List<PetResponse> pets = petService.getPetsBySpecies(species);
            log.debug("Retornando {} mascotas de especie '{}'", pets.size(), species);

            return ResponseEntity.ok(pets);

        } catch (Exception e) {
            log.error("Error al obtener mascotas por especie '{}': {}", species, e.getMessage());
            throw new RuntimeException("Error interno del servidor", e);
        }
    }

    /**
     * Obtiene mascotas filtradas por raza.
     *
     * @param breed Raza a buscar
     * @return ResponseEntity con lista de mascotas de la raza especificada
     */
    @GetMapping("/breed/{breed}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PetResponse>> getPetsByBreed(@PathVariable String breed) {
        log.debug("Solicitud de mascotas por raza: {}", breed);

        try {
            List<PetResponse> pets = petService.getPetsByBreed(breed);
            return ResponseEntity.ok(pets);

        } catch (Exception e) {
            log.error("Error al obtener mascotas por raza '{}': {}", breed, e.getMessage());
            throw new RuntimeException("Error interno del servidor", e);
        }
    }

    /**
     * Obtiene todas las mascotas activas del sistema.
     *
     * <p>Para usuarios regulares retorna sus mascotas activas, para administradores
     * retorna todas las mascotas activas del sistema.</p>
     *
     * @return ResponseEntity con lista de mascotas activas
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PetResponse>> getActivePets() {
        log.debug("Solicitud de todas las mascotas activas");

        try {
            List<PetResponse> activePets = petService.getActivePets();
            log.debug("Retornando {} mascotas activas", activePets.size());

            return ResponseEntity.ok(activePets);

        } catch (Exception e) {
            log.error("Error al obtener mascotas activas: {}", e.getMessage());
            throw new RuntimeException("Error interno del servidor", e);
        }
    }

    /**
     * Obtiene todas las mascotas inactivas del sistema.
     *
     * <p>Endpoint administrativo que permite ver mascotas que han sido marcadas
     * como inactivas para propósitos de auditoría y gestión.</p>
     *
     * @return ResponseEntity con lista de mascotas inactivas
     * @throws SecurityException si no tiene permisos administrativos
     */
    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PetResponse>> getInactivePets() {
        log.info("Admin solicitando mascotas inactivas");

        try {
            List<PetResponse> inactivePets = petService.getInactivePets();
            log.info("Admin consultando {} mascotas inactivas", inactivePets.size());

            return ResponseEntity.ok(inactivePets);

        } catch (Exception e) {
            log.error("Error al obtener mascotas inactivas: {}", e.getMessage());
            throw new RuntimeException("Error interno del servidor", e);
        }
    }

    // ========== BÚSQUEDAS Y FILTROS ==========

    /**
     * Realiza búsqueda de texto en campos relevantes de mascotas.
     *
     * <p>Busca en nombre, especie, raza y color de las mascotas.
     * Aplica filtros de seguridad según el contexto del usuario.</p>
     *
     * @param searchTerm Término de búsqueda
     * @param activeOnly Si true, busca solo en mascotas activas (opcional, default: false)
     * @return ResponseEntity con lista de mascotas que coinciden con el término
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PetResponse>> searchPets(
            @RequestParam("q") String searchTerm,
            @RequestParam(value = "activeOnly", defaultValue = "false") boolean activeOnly) {

        log.debug("Solicitud de búsqueda con término: '{}', solo activas: {}", searchTerm, activeOnly);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            log.warn("Término de búsqueda vacío recibido");
            return ResponseEntity.badRequest().build();
        }

        try {
            List<PetResponse> results;

            if (activeOnly) {
                results = petService.searchActivePets(searchTerm);
            } else {
                results = petService.searchPets(searchTerm);
            }

            log.debug("Búsqueda retornó {} resultados para término '{}'", results.size(), searchTerm);
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            log.error("Error en búsqueda con término '{}': {}", searchTerm, e.getMessage());
            throw new RuntimeException("Error interno del servidor", e);
        }
    }

    // ========== OPERACIONES ADMINISTRATIVAS ==========

    /**
     * Cambia el estado activo/inactivo de una mascota.
     *
     * <p>Permite a administradores o propietarios activar/desactivar mascotas.
     * Útil para gestión de disponibilidad sin eliminar permanentemente.</p>
     *
     * @param id ID de la mascota
     * @return ResponseEntity con el DTO de la mascota con nuevo estado
     */
    @PutMapping("/{id}/toggle-active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PetResponse> togglePetActive(@PathVariable Long id) {
        log.info("Solicitud de cambio de estado para mascota ID: {}", id);

        try {
            PetResponse updatedPet = petService.togglePetActive(id);
            log.info("Estado de mascota ID {} cambiado a: {}", id, updatedPet.isActive() ? "activa" : "inactiva");

            return ResponseEntity.ok(updatedPet);

        } catch (IllegalArgumentException e) {
            log.warn("Mascota no encontrada para cambio de estado ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            log.warn("Acceso denegado para cambiar estado de mascota ID: {}", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Obtiene estadísticas completas del sistema de mascotas.
     *
     * <p>Endpoint administrativo que proporciona métricas detalladas sobre
     * las mascotas registradas, distribuciones por características,
     * y estadísticas temporales para dashboards ejecutivos.</p>
     *
     * @return ResponseEntity con objeto de estadísticas completas
     * @throws SecurityException si no tiene permisos administrativos
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PetStatsResponse> getPetStats() {
        log.info("Admin solicitando estadísticas de mascotas");

        try {
            PetStatsResponse stats = petService.getPetStats();
            log.info("Estadísticas generadas: {} mascotas totales, {} activas",
                    stats.getTotalPets(), stats.getActivePets());

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error al generar estadísticas de mascotas: {}", e.getMessage());
            throw new RuntimeException("Error interno del servidor", e);
        }
    }

    // ========== ENDPOINTS DE UTILIDAD ==========

    /**
     * Verifica si un nombre de mascota está disponible en una cuenta.
     *
     * <p>Útil para validaciones del frontend antes de enviar formularios
     * de creación o actualización de mascotas.</p>
     *
     * @param name Nombre a verificar
     * @param accountId ID de la cuenta
     * @return ResponseEntity con boolean indicando disponibilidad
     */
    @GetMapping("/name-available")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> isPetNameAvailable(
            @RequestParam("name") String name,
            @RequestParam("accountId") Long accountId) {

        log.debug("Verificando disponibilidad del nombre '{}' en cuenta {}", name, accountId);

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            boolean isAvailable = petService.isPetNameAvailable(name.trim(), accountId);
            return ResponseEntity.ok(isAvailable);

        } catch (Exception e) {
            log.error("Error al verificar disponibilidad de nombre: {}", e.getMessage());
            throw new RuntimeException("Error interno del servidor", e);
        }
    }

    /**
     * Obtiene mascotas que requieren atención especial.
     *
     * <p>Retorna mascotas que tienen medicamentos o alergias registradas,
     * útil para servicios de cuidado especializados.</p>
     *
     * @return ResponseEntity con lista de mascotas con necesidades especiales
     */
    @GetMapping("/special-needs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PetResponse>> getPetsWithSpecialNeeds() {
        log.debug("Solicitud de mascotas con necesidades especiales");

        try {
            List<PetResponse> petsWithSpecialNeeds = petService.getPetsWithSpecialNeeds();
            log.debug("Retornando {} mascotas con necesidades especiales", petsWithSpecialNeeds.size());

            return ResponseEntity.ok(petsWithSpecialNeeds);

        } catch (Exception e) {
            log.error("Error al obtener mascotas con necesidades especiales: {}", e.getMessage());
            throw new RuntimeException("Error interno del servidor", e);
        }
    }

    /**
     * Obtiene mascotas disponibles para servicios de una cuenta.
     *
     * <p>Retorna mascotas activas de cuentas activas, listas para servicios de cuidado.</p>
     *
     * @param accountId ID de la cuenta
     * @return ResponseEntity con lista de mascotas disponibles
     */
    @GetMapping("/account/{accountId}/available")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PetResponse>> getAvailablePetsByAccountId(@PathVariable Long accountId) {
        log.debug("Solicitud de mascotas disponibles para servicios en cuenta: {}", accountId);

        try {
            List<PetResponse> availablePets = petService.getAvailablePetsByAccountId(accountId);
            return ResponseEntity.ok(availablePets);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error al obtener mascotas disponibles para cuenta {}: {}", accountId, e.getMessage());
            throw new RuntimeException("Error interno del servidor", e);
        }
    }

    /**
     * Obtiene la mascota más reciente registrada en una cuenta.
     *
     * @param accountId ID de la cuenta
     * @return ResponseEntity con la mascota más reciente, o 204 si no hay mascotas
     */
    @GetMapping("/account/{accountId}/newest")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PetResponse> getNewestPetByAccountId(@PathVariable Long accountId) {
        log.debug("Solicitud de mascota más reciente para cuenta: {}", accountId);

        try {
            PetResponse newestPet = petService.getNewestPetByAccountId(accountId);

            if (newestPet != null) {
                return ResponseEntity.ok(newestPet);
            } else {
                return ResponseEntity.noContent().build();
            }

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error al obtener mascota más reciente para cuenta {}: {}", accountId, e.getMessage());
            throw new RuntimeException("Error interno del servidor", e);
        }
    }
}