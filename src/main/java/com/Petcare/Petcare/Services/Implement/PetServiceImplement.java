package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.DTOs.Pet.*;
import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.Pet;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.AccountRepository;
import com.Petcare.Petcare.Repositories.PetRepository;
import com.Petcare.Petcare.Repositories.UserRepository;
import com.Petcare.Petcare.Services.PetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de mascotas (Pets).
 *
 * <p>Proporciona la lógica de negocio completa para la gestión de mascotas,
 * incluyendo validaciones, control de acceso, transformaciones de datos y
 * operaciones transaccionales. Esta implementación sigue los patrones
 * establecidos en el modelo de Usuario para mantener consistencia.</p>
 *
 * <p><strong>Características de la implementación:</strong></p>
 * <ul>
 * <li>Logging completo con Slf4j para trazabilidad</li>
 * <li>Transacciones apropiadas con @Transactional</li>
 * <li>Control de acceso con Spring Security</li>
 * <li>Validaciones de negocio exhaustivas</li>
 * <li>Manejo de excepciones descriptivo</li>
 * <li>Optimizaciones de consulta y cache</li>
 * </ul>
 *
 * <p><strong>Patrones implementados:</strong></p>
 * <ul>
 * <li>Service Implementation con inyección por constructor</li>
 * <li>DTO Mapping con métodos de fábrica</li>
 * <li>Transaction Management declarativo</li>
 * <li>Security Context para control de acceso</li>
 * <li>Builder Pattern para objetos complejos</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see PetService
 * @see Pet
 * @see PetRepository
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PetServiceImplement implements PetService {

    private final PetRepository petRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    // ========== OPERACIONES CRUD BÁSICAS ==========

    @Override
    @Transactional
    public PetResponse createPet(CreatePetRequest petRequest) {
        log.info("Iniciando creación de mascota para cuenta ID: {}", petRequest.getAccountId());

        try {
            // 1. Validar que la cuenta existe y está activa
            Account account = accountRepository.findById(petRequest.getAccountId())
                    .orElseThrow(() -> {
                        log.error("Cuenta no encontrada con ID: {}", petRequest.getAccountId());
                        return new IllegalArgumentException("Cuenta no encontrada con ID: " + petRequest.getAccountId());
                    });

            if (!account.isActive()) {
                log.error("Intento de crear mascota en cuenta inactiva ID: {}", account.getId());
                throw new IllegalStateException("No se puede crear mascota en cuenta inactiva");
            }

            // 2. Validar duplicados opcionales (nombres únicos por cuenta)
            if (petRepository.existsByNameIgnoreCaseAndAccountId(petRequest.getName(), petRequest.getAccountId())) {
                log.warn("Intento de crear mascota con nombre duplicado '{}' en cuenta {}",
                        petRequest.getName(), petRequest.getAccountId());
                throw new IllegalStateException("Ya existe una mascota con el nombre '" + petRequest.getName() + "' en esta cuenta");
            }

            // 3. Crear la nueva mascota
            Pet newPet = new Pet();
            newPet.setAccount(account);
            mapRequestToEntity(petRequest, newPet);
            newPet.setActive(true);

            // 4. Guardar en base de datos
            Pet savedPet = petRepository.save(newPet);
            log.info("Mascota creada exitosamente con ID: {} para cuenta: {}", savedPet.getId(), account.getId());

            // 5. Retornar DTO de respuesta
            return PetResponse.fromEntity(savedPet);

        } catch (Exception e) {
            log.error("Error al crear mascota para cuenta {}: {}", petRequest.getAccountId(), e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PetResponse getPetById(Long id) {
        log.debug("Buscando mascota con ID: {}", id);

        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Mascota no encontrada con ID: {}", id);
                    return new IllegalArgumentException("Mascota no encontrada con ID: " + id);
                });

        // Validar permisos de acceso
        validateUserAccessToPet(pet);

        log.debug("Mascota encontrada: {} de la cuenta: {}", pet.getName(), pet.getAccount().getId());
        return PetResponse.fromEntity(pet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetResponse> getAllPets() {
        log.debug("Obteniendo todas las mascotas del sistema");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (isAdmin(auth)) {
            // Administradores ven todas las mascotas
            List<Pet> allPets = petRepository.findAll();
            log.info("Admin consultando {} mascotas totales", allPets.size());
            return allPets.stream()
                    .map(PetResponse::fromEntity)
                    .collect(Collectors.toList());
        } else {
            // Usuarios regulares solo ven sus mascotas
            Long userAccountId = getUserAccountId(auth);
            List<Pet> userPets = petRepository.findByAccountId(userAccountId);
            log.info("Usuario consultando {} mascotas de su cuenta {}", userPets.size(), userAccountId);
            return userPets.stream()
                    .map(PetResponse::fromEntity)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetSummaryResponse> getAllPetsSummary() {
        log.debug("Obteniendo resumen de todas las mascotas");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (isAdmin(auth)) {
            List<Pet> allPets = petRepository.findAll();
            log.debug("Admin consultando resumen de {} mascotas", allPets.size());
            return allPets.stream()
                    .map(PetSummaryResponse::fromEntity)
                    .collect(Collectors.toList());
        } else {
            Long userAccountId = getUserAccountId(auth);
            List<Pet> userPets = petRepository.findByAccountId(userAccountId);
            log.debug("Usuario consultando resumen de {} mascotas", userPets.size());
            return userPets.stream()
                    .map(PetSummaryResponse::fromEntity)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public PetResponse updatePet(Long id, CreatePetRequest petRequest) {
        log.info("Actualizando mascota con ID: {}", id);

        try {
            // 1. Buscar mascota existente
            Pet existingPet = petRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Mascota no encontrada para actualizar con ID: {}", id);
                        return new IllegalArgumentException("Mascota no encontrada con ID: " + id);
                    });

            // 2. Validar permisos de acceso
            validateUserAccessToPet(existingPet);

            // 3. Validar duplicados si se cambió el nombre
            if (!existingPet.getName().equalsIgnoreCase(petRequest.getName()) &&
                    petRepository.existsByNameIgnoreCaseAndAccountId(petRequest.getName(), existingPet.getAccount().getId())) {
                log.warn("Intento de actualizar con nombre duplicado '{}' en cuenta {}",
                        petRequest.getName(), existingPet.getAccount().getId());
                throw new IllegalStateException("Ya existe otra mascota con el nombre '" + petRequest.getName() + "' en esta cuenta");
            }

            // 4. Actualizar campos
            String oldName = existingPet.getName();
            mapRequestToEntity(petRequest, existingPet);

            // 5. Guardar cambios
            Pet updatedPet = petRepository.save(existingPet);
            log.info("Mascota actualizada exitosamente. ID: {}, nombre anterior: '{}', nuevo: '{}'",
                    id, oldName, updatedPet.getName());

            return PetResponse.fromEntity(updatedPet);

        } catch (Exception e) {
            log.error("Error al actualizar mascota con ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void deletePet(Long id) {
        log.info("Eliminando mascota con ID: {}", id);

        try {
            // 1. Verificar que la mascota existe
            Pet pet = petRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Mascota no encontrada para eliminar con ID: {}", id);
                        return new IllegalArgumentException("Mascota no encontrada con ID: " + id);
                    });

            // 2. Validar permisos de acceso
            validateUserAccessToPet(pet);

            // 3. Verificar dependencias (servicios activos, reservas, etc.)
            // TODO: Implementar validaciones de dependencias según reglas de negocio

            // 4. Realizar eliminación lógica (cambiar a inactiva)
            pet.setActive(false);
            petRepository.save(pet);

            log.info("Mascota '{}' marcada como inactiva exitosamente (ID: {})", pet.getName(), id);

            // Alternativamente, para eliminación física:
            // petRepository.deleteById(id);
            // log.info("Mascota eliminada físicamente del sistema (ID: {})", id);

        } catch (Exception e) {
            log.error("Error al eliminar mascota con ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    // ========== CONSULTAS POR CUENTA ==========

    @Override
    @Transactional(readOnly = true)
    public List<PetResponse> getPetsByAccountId(Long accountId) {
        log.debug("Obteniendo mascotas de cuenta ID: {}", accountId);

        // Validar acceso a la cuenta
        validateUserAccessToAccount(accountId);

        List<Pet> pets = petRepository.findByAccountId(accountId);
        log.debug("Encontradas {} mascotas para cuenta {}", pets.size(), accountId);

        return pets.stream()
                .map(PetResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetResponse> getActivePetsByAccountId(Long accountId) {
        log.debug("Obteniendo mascotas activas de cuenta ID: {}", accountId);

        validateUserAccessToAccount(accountId);

        List<Pet> activePets = petRepository.findByAccountIdAndIsActiveTrue(accountId);
        log.debug("Encontradas {} mascotas activas para cuenta {}", activePets.size(), accountId);

        return activePets.stream()
                .map(PetResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetSummaryResponse> getPetsSummaryByAccountId(Long accountId) {
        log.debug("Obteniendo resumen de mascotas de cuenta ID: {}", accountId);

        validateUserAccessToAccount(accountId);

        List<Pet> pets = petRepository.findByAccountId(accountId);
        return pets.stream()
                .map(PetSummaryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========== CONSULTAS POR CARACTERÍSTICAS ==========

    @Override
    @Transactional(readOnly = true)
    public List<PetResponse> getPetsBySpecies(String species) {
        log.debug("Obteniendo mascotas por especie: {}", species);

        List<Pet> pets = petRepository.findBySpeciesIgnoreCase(species);
        log.debug("Encontradas {} mascotas de especie '{}'", pets.size(), species);

        return pets.stream()
                .filter(this::userCanAccessPet)
                .map(PetResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetResponse> getPetsByBreed(String breed) {
        log.debug("Obteniendo mascotas por raza: {}", breed);

        List<Pet> pets = petRepository.findByBreedIgnoreCase(breed);
        log.debug("Encontradas {} mascotas de raza '{}'", pets.size(), breed);

        return pets.stream()
                .filter(this::userCanAccessPet)
                .map(PetResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetResponse> getPetsByGender(String gender) {
        log.debug("Obteniendo mascotas por género: {}", gender);

        List<Pet> pets = petRepository.findByGenderIgnoreCase(gender);
        log.debug("Encontradas {} mascotas de género '{}'", pets.size(), gender);

        return pets.stream()
                .filter(this::userCanAccessPet)
                .map(PetResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========== CONSULTAS POR ESTADO ==========

    @Override
    @Transactional(readOnly = true)
    public List<PetResponse> getActivePets() {
        log.debug("Obteniendo todas las mascotas activas");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (isAdmin(auth)) {
            List<Pet> activePets = petRepository.findByIsActiveTrue();
            log.debug("Admin consultando {} mascotas activas totales", activePets.size());
            return activePets.stream()
                    .map(PetResponse::fromEntity)
                    .collect(Collectors.toList());
        } else {
            Long userAccountId = getUserAccountId(auth);
            List<Pet> userActivePets = petRepository.findByAccountIdAndIsActiveTrue(userAccountId);
            log.debug("Usuario consultando {} mascotas activas de su cuenta", userActivePets.size());
            return userActivePets.stream()
                    .map(PetResponse::fromEntity)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<PetResponse> getInactivePets() {
        log.debug("Admin obteniendo todas las mascotas inactivas");

        List<Pet> inactivePets = petRepository.findByIsActiveFalse();
        log.info("Admin consultando {} mascotas inactivas totales", inactivePets.size());

        return inactivePets.stream()
                .map(PetResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetResponse> getAvailablePetsByAccountId(Long accountId) {
        log.debug("Obteniendo mascotas disponibles para servicios de cuenta: {}", accountId);

        validateUserAccessToAccount(accountId);

        List<Pet> availablePets = petRepository.findAvailablePetsByAccountId(accountId);
        log.debug("Encontradas {} mascotas disponibles para servicios en cuenta {}",
                availablePets.size(), accountId);

        return availablePets.stream()
                .map(PetResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========== OPERACIONES DE ESTADO ==========

    @Override
    @Transactional
    public PetResponse togglePetActive(Long id) {
        log.info("Cambiando estado activo de mascota ID: {}", id);

        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Mascota no encontrada para cambio de estado ID: {}", id);
                    return new IllegalArgumentException("Mascota no encontrada con ID: " + id);
                });

        validateUserAccessToPet(pet);

        boolean previousState = pet.isActive();
        pet.setActive(!previousState);
        Pet updatedPet = petRepository.save(pet);

        log.info("Estado de mascota '{}' cambiado de {} a {}",
                pet.getName(), previousState, updatedPet.isActive());

        return PetResponse.fromEntity(updatedPet);
    }

    // ========== BÚSQUEDAS DE TEXTO ==========

    @Override
    @Transactional(readOnly = true)
    public List<PetResponse> searchPets(String searchTerm) {
        log.debug("Realizando búsqueda de mascotas con término: '{}'", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            log.warn("Término de búsqueda vacío, retornando lista vacía");
            return List.of();
        }

        List<Pet> foundPets = petRepository.findBySearchTerm(searchTerm.trim());
        log.debug("Búsqueda encontró {} mascotas con término '{}'", foundPets.size(), searchTerm);

        return foundPets.stream()
                .filter(this::userCanAccessPet)
                .map(PetResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetResponse> searchActivePets(String searchTerm) {
        log.debug("Realizando búsqueda de mascotas activas con término: '{}'", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }

        List<Pet> foundPets = petRepository.findActivePetsBySearchTerm(searchTerm.trim());
        log.debug("Búsqueda encontró {} mascotas activas con término '{}'", foundPets.size(), searchTerm);

        return foundPets.stream()
                .filter(this::userCanAccessPet)
                .map(PetResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========== ESTADÍSTICAS Y MÉTRICAS ==========

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public PetStatsResponse getPetStats() {
        log.info("Generando estadísticas completas de mascotas (Admin)");

        try {
            PetStatsResponse stats = new PetStatsResponse();

            // Conteos básicos
            stats.setTotalPets(petRepository.count());
            stats.setActivePets(petRepository.countByIsActiveTrue());
            stats.setInactivePets(petRepository.countByIsActiveFalse());

            // Distribución por especie
            List<Object[]> speciesData = petRepository.countBySpecies();
            Map<String, Long> petsBySpecies = new HashMap<>();
            for (Object[] row : speciesData) {
                String species = (String) row[0];
                Long count = (Long) row[1];
                petsBySpecies.put(species != null ? species : "No especificada", count);
            }
            stats.setPetsBySpecies(petsBySpecies);

            // Distribución por género
            List<Object[]> genderData = petRepository.countByGender();
            Map<String, Long> petsByGender = new HashMap<>();
            for (Object[] row : genderData) {
                String gender = (String) row[0];
                Long count = (Long) row[1];
                petsByGender.put(gender != null ? gender : "No especificado", count);
            }
            stats.setPetsByGender(petsByGender);

            // Distribución por rango de edad
            List<Object[]> ageData = petRepository.countByAgeRange();
            Map<String, Long> petsByAgeRange = new HashMap<>();
            for (Object[] row : ageData) {
                String ageRange = (String) row[0];
                Long count = (Long) row[1];
                petsByAgeRange.put(ageRange, count);
            }
            stats.setPetsByAgeRange(petsByAgeRange);

            // Estadísticas de cuentas
            stats.setAccountsWithPets(petRepository.countDistinctAccounts());
            if (stats.getAccountsWithPets() > 0) {
                stats.setAveragePetsPerAccount((double) stats.getTotalPets() / stats.getAccountsWithPets());
            }

            // Registros recientes
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            stats.setPetsRegisteredLast30Days(petRepository.countByCreatedAtGreaterThanEqual(thirtyDaysAgo));
            stats.setPetsRegisteredLast7Days(petRepository.countByCreatedAtGreaterThanEqual(sevenDaysAgo));

            log.info("Estadísticas generadas: {} mascotas totales, {} activas, {} cuentas con mascotas",
                    stats.getTotalPets(), stats.getActivePets(), stats.getAccountsWithPets());

            return stats;

        } catch (Exception e) {
            log.error("Error al generar estadísticas de mascotas: {}", e.getMessage());
            throw new RuntimeException("Error interno al generar estadísticas", e);
        }
    }

    // ========== OPERACIONES DE VALIDACIÓN Y UTILIDAD ==========

    @Override
    @Transactional(readOnly = true)
    public boolean isPetNameAvailable(String name, Long accountId) {
        log.debug("Verificando disponibilidad del nombre '{}' en cuenta {}", name, accountId);

        boolean isAvailable = !petRepository.existsByNameIgnoreCaseAndAccountId(name, accountId);
        log.debug("Nombre '{}' en cuenta {} está {}", name, accountId, isAvailable ? "disponible" : "ocupado");

        return isAvailable;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetResponse> getPetsWithSpecialNeeds() {
        log.debug("Obteniendo mascotas con necesidades especiales");

        List<Pet> petsWithSpecialNeeds = petRepository.findPetsWithSpecialNeeds();
        log.debug("Encontradas {} mascotas con necesidades especiales", petsWithSpecialNeeds.size());

        return petsWithSpecialNeeds.stream()
                .filter(this::userCanAccessPet)
                .map(PetResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PetResponse getNewestPetByAccountId(Long accountId) {
        log.debug("Obteniendo mascota más reciente de cuenta: {}", accountId);

        validateUserAccessToAccount(accountId);

        return petRepository.findTopByAccountIdOrderByCreatedAtDesc(accountId)
                .map(PetResponse::fromEntity)
                .orElse(null);
    }

    @Override
    public String healthCheck() {
        log.debug("Verificación de salud del servicio de mascotas");

        try {
            long totalPets = petRepository.count();
            return String.format("PetService está operativo. Total de mascotas: %d", totalPets);
        } catch (Exception e) {
            log.error("Error en verificación de salud: {}", e.getMessage());
            return "PetService presenta problemas de conectividad";
        }
    }

    // ========== MÉTODOS AUXILIARES PRIVADOS ==========

    /**
     * Mapea los datos del DTO de request a la entidad Pet.
     */
    private void mapRequestToEntity(CreatePetRequest request, Pet pet) {
        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setAge(request.getAge());
        pet.setWeight(request.getWeight());
        pet.setGender(request.getGender());
        pet.setColor(request.getColor());
        pet.setPhysicalDescription(request.getPhysicalDescription());
        pet.setMedications(request.getMedications());
        pet.setAllergies(request.getAllergies());
        pet.setSpecialNotes(request.getSpecialNotes());
    }

    /**
     * Valida si el usuario actual tiene acceso a una mascota específica.
     */
    private void validateUserAccessToPet(Pet pet) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (isAdmin(auth)) {
            return; // Los admins tienen acceso a todas las mascotas
        }

        Long userAccountId = getUserAccountId(auth);
        if (!pet.belongsToAccount(userAccountId)) {
            log.error("Usuario sin permisos intentó acceder a mascota ID: {} de cuenta: {}",
                    pet.getId(), pet.getAccount().getId());
            throw new SecurityException("No tiene permisos para acceder a esta mascota");
        }
    }

    /**
     * Valida si el usuario actual tiene acceso a una cuenta específica.
     */
    private void validateUserAccessToAccount(Long accountId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (isAdmin(auth)) {
            return; // Los admins tienen acceso a todas las cuentas
        }

        Long userAccountId = getUserAccountId(auth);
        if (!userAccountId.equals(accountId)) {
            log.error("Usuario sin permisos intentó acceder a cuenta ID: {}", accountId);
            throw new SecurityException("No tiene permisos para acceder a esta cuenta");
        }
    }

    /**
     * Verifica si el usuario puede acceder a una mascota (para filtros).
     */
    private boolean userCanAccessPet(Pet pet) {
        try {
            validateUserAccessToPet(pet);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * Verifica si el usuario actual es administrador.
     */
    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Obtiene el ID de cuenta del usuario autenticado.
     */
    private Long getUserAccountId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new SecurityException("No se pudo determinar el usuario autenticado.");
        }

        // --- INICIO DE LA LÓGICA CORREGIDA ---
        String userEmail;
        Object principal = auth.getPrincipal();

        if (principal instanceof UserDetails) {
            userEmail = ((UserDetails) principal).getUsername();
        } else {
            userEmail = principal.toString();
        }

        log.debug("Buscando cuenta para el usuario autenticado: {}", userEmail);

        // Busca el usuario completo en la BD a partir de su email
        User authenticatedUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new SecurityException("Usuario autenticado no encontrado en la base de datos."));

        // Busca la cuenta asociada a ese usuario
        Account account = accountRepository.findByOwnerUser(authenticatedUser)
                .orElseThrow(() -> new IllegalStateException("El usuario autenticado no tiene una cuenta asociada."));


        return account.getId();
    }

}