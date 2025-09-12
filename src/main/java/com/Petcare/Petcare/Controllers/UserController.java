package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.Auth.Request.LoginRequest;
import com.Petcare.Petcare.DTOs.Auth.Respone.AuthResponse;
import com.Petcare.Petcare.DTOs.User.*;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para la gestión de usuarios en el sistema Petcare.
 *
 * <p>Proporciona endpoints para todas las operaciones relacionadas con usuarios,
 * incluyendo registro público, operaciones administrativas, consultas especializadas
 * y generación de estadísticas. Mantiene separación clara entre endpoints públicos
 * y protegidos con autorizaciones apropiadas.</p>
 *
 * <p><strong>Categorías de endpoints:</strong></p>
 * <ul>
 *   <li>Públicos: Registro de usuarios clientes</li>
 *   <li>Administrativos: Gestión completa de usuarios (ADMIN only)</li>
 *   <li>Consultas: Búsquedas y listados con diferentes formatos</li>
 *   <li>Estadísticas: Métricas agregadas para dashboards</li>
 * </ul>
 *
 * <p><strong>DTOs utilizados:</strong></p>
 * <ul>
 *   <li>{@link CreateUserRequest}: Input para creación/actualización</li>
 *   <li>{@link UserResponse}: Output completo para detalles</li>
 *   <li>{@link UserSummaryResponse}: Output resumido para listados</li>
 *   <li>{@link UserStatsResponse}: Output para estadísticas</li>
 * </ul>
 *
 * <p><strong>Seguridad implementada:</strong></p>
 * <ul>
 *   <li>Endpoints públicos sin autenticación</li>
 *   <li>Endpoints administrativos requieren rol ADMIN</li>
 *   <li>Acceso a datos propios para usuarios autenticados</li>
 *   <li>Validación de entrada con Bean Validation</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see UserService
 * @see com.Petcare.Petcare.Models.User.User
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${petcare.frontend.base-url:http://localhost:8080}")
    private String frontendBaseUrl;


    // ========== ENDPOINTS PÚBLICOS ==========

    /**
     * Autentica un usuario en el sistema.
     *
     * <p>Endpoint público que permite autenticar usuarios registrados
     * usando email y contraseña. Retorna un token JWT válido para acceso
     * a recursos protegidos y actualiza el timestamp de último login.</p>
     *
     * <p><strong>Proceso de autenticación:</strong></p>
     * <ul>
     *   <li>Valida las credenciales usando Spring Security</li>
     *   <li>Actualiza el timestamp de último login</li>
     *   <li>Genera token JWT con información del rol</li>
     *   <li>Retorna token y rol para autorización del frontend</li>
     * </ul>
     *
     * @param request datos de autenticación (email y contraseña)
     * @param sessionId identificador de sesión opcional para auditoría
     * @return ResponseEntity con token JWT y rol del usuario
     * @throws org.springframework.security.authentication.BadCredentialsException si las credenciales son inválidas
     * @throws IllegalStateException si el usuario no existe después de autenticación exitosa
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        log.info("Solicitud de login para email: {} [Session: {}]", request.getEmail(), sessionId);

        AuthResponse authResponse = userService.login(request);

        log.info("Login exitoso para email: {} [Session: {}]", request.getEmail(), sessionId);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Registra un nuevo usuario cliente en el sistema.
     *
     * <p>Endpoint público que permite a cualquier persona crear una cuenta
     * con rol CLIENT. El usuario queda automáticamente autenticado tras
     * el registro exitoso.</p>
     *
     * <p><strong>Proceso de registro:</strong></p>
     * <ul>
     *   <li>Valida la unicidad del email</li>
     *   <li>Cifra la contraseña proporcionada</li>
     *   <li>Asigna rol CLIENT automáticamente</li>
     *   <li>Genera token JWT para login inmediato</li>
     * </ul>
     *
     * @param request datos de registro del usuario
     * @return ResponseEntity con token JWT y rol del usuario registrado
     * @throws IllegalArgumentException si el email ya está registrado
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Solicitud de registro público para email: {}", request.getEmail());

        AuthResponse authResponse = userService.registerUser(request);

        log.info("Usuario registrado exitosamente para email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/register-sitter")
    public ResponseEntity<AuthResponse> registerUserSitter(@Valid @RequestBody CreateUserRequest request) {
        log.info("Solicitud de registro público para email: {}", request.getEmail());

        AuthResponse authResponse = userService.registerUserSitter(request);

        log.info("Cuidadora registrado exitosamente para email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    // ========== ENDPOINTS ADMINISTRATIVOS ==========

    /**
     * Crea un nuevo usuario con rol específico (operación administrativa).
     *
     * <p>Permite a los administradores crear usuarios con cualquier rol del sistema.
     * A diferencia del registro público, este endpoint no autentica automáticamente
     * al usuario creado.</p>
     *
     * <p><strong>Casos de uso:</strong></p>
     * <ul>
     *   <li>Creación de usuarios administradores</li>
     *   <li>Registro de cuidadores por parte del staff</li>
     *   <li>Cuentas corporativas o especiales</li>
     * </ul>
     *
     * @param request datos del usuario a crear
     * @param role rol específico a asignar (opcional, por defecto CLIENT)
     * @return ResponseEntity con datos del usuario creado
     * @throws IllegalArgumentException si el email ya existe
     */
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUserByAdmin(
            @Valid @RequestBody CreateUserRequest request,
            @RequestParam(defaultValue = "CLIENT") Role role) {

        log.info("Creación administrativa de usuario con rol {} para email: {}", role, request.getEmail());

        UserResponse userResponse = userService.createUserByAdmin(request, role);

        log.info("Usuario creado administrativamente con ID: {} y rol: {}", userResponse.getId(), role);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    /**
     * Actualiza un usuario existente.
     *
     * <p>Permite modificar todos los campos del usuario excepto el rol y
     * metadatos del sistema. Si se cambia el email, se resetea la verificación.</p>
     *
     * @param id identificador del usuario a actualizar
     * @param request datos actualizados
     * @return ResponseEntity con datos del usuario actualizado
     * @throws RuntimeException si el usuario no existe
     * @throws IllegalArgumentException si el nuevo email ya está en uso
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("Actualizando usuario ID: {}", id);

        UserResponse updatedUser = userService.updateUser(id, request);

        log.info("Usuario ID: {} actualizado exitosamente", id);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Cambia el estado activo/inactivo de un usuario.
     *
     * <p>Permite habilitar o deshabilitar cuentas sin eliminar datos.
     * Los usuarios inactivos no pueden autenticarse en el sistema.</p>
     *
     * @param id identificador del usuario
     * @param active nuevo estado de activación
     * @return ResponseEntity con datos del usuario actualizado
     * @throws RuntimeException si el usuario no existe
     */
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> toggleUserActive(
            @PathVariable Long id,
            @RequestParam boolean active) {

        log.info("Cambiando estado activo del usuario ID: {} a: {}", id, active);

        UserResponse updatedUser = userService.toggleUserActive(id, active);

        log.info("Estado del usuario ID: {} cambiado a: {}", id, active ? "activo" : "inactivo");
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Marca el email de un usuario como verificado.
     *
     * <p>Establece el timestamp de verificación de email. Típicamente
     * utilizado por procesos de verificación automáticos.</p>
     *
     * @param id identificador del usuario
     * @return ResponseEntity con datos del usuario actualizado
     * @throws RuntimeException si el usuario no existe
     */
    @PatchMapping("/{id}/verify-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> markEmailAsVerified(@PathVariable Long id) {
        log.info("Verificando email del usuario ID: {}", id);

        UserResponse updatedUser = userService.markEmailAsVerified(id);

        log.info("Email verificado para usuario ID: {}", id);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Elimina un usuario del sistema (eliminación física).
     *
     * <p><strong>Operación destructiva</strong> que elimina permanentemente
     * todos los datos del usuario. Debe usarse con precaución extrema.</p>
     *
     * @param id identificador del usuario a eliminar
     * @return ResponseEntity vacío con código 204
     * @throws RuntimeException si el usuario no existe
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.warn("Eliminando permanentemente usuario ID: {}", id);

        userService.deleteUser(id);

        log.warn("Usuario ID: {} eliminado permanentemente del sistema", id);
        return ResponseEntity.noContent().build();
    }

    // ========== ENDPOINTS DE CONSULTA ==========

    /**
     * Obtiene todos los usuarios del sistema en formato completo.
     *
     * <p>Retorna información detallada de todos los usuarios sin paginación.
     * Endpoint para uso administrativo donde se requiere información completa.</p>
     *
     * @return ResponseEntity con lista de todos los usuarios
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.debug("Obteniendo todos los usuarios del sistema");

        List<UserResponse> users = userService.getAllUsers();

        log.debug("Retornando {} usuarios del sistema", users.size());
        return ResponseEntity.ok(users);
    }

    /**
     * Obtiene todos los usuarios en formato resumido con paginación.
     *
     * <p>Optimizado para interfaces de usuario con listados grandes.
     * Incluye solo información esencial para reducir el payload.</p>
     *
     * @param page número de página (base 0, por defecto 0)
     * @param size tamaño de página (por defecto 20)
     * @param sortBy campo de ordenamiento (por defecto createdAt)
     * @param sortDir dirección de ordenamiento (asc/desc, por defecto desc)
     * @return ResponseEntity con página de usuarios resumidos
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserSummaryResponse>> getAllUsersSummary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.debug("Obteniendo resumen paginado - Página: {}, Tamaño: {}", page, size);

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserSummaryResponse> userPage = userService.getAllUsersSummary(pageable);

        log.debug("Retornando página {}/{} con {} usuarios",
                page + 1, userPage.getTotalPages(), userPage.getNumberOfElements());
        return ResponseEntity.ok(userPage);
    }

    /**
     * Obtiene un usuario específico por su ID.
     *
     * <p>Los administradores pueden acceder a cualquier usuario.
     * Los usuarios autenticados solo pueden acceder a su propio perfil.</p>
     *
     * @param id identificador del usuario
     * @param authentication información del usuario autenticado
     * @return ResponseEntity con datos completos del usuario
     * @throws RuntimeException si el usuario no existe
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id, Authentication authentication) {
        log.debug("Obteniendo usuario por ID: {}", id);

        Optional<UserResponse> userResponse = userService.getUserById(id);

        if (userResponse.isEmpty()) {
            log.warn("Usuario no encontrado con ID: {}", id);
            throw new RuntimeException("Usuario no encontrado con id " + id);
        }

        log.debug("Usuario encontrado: {}", userResponse.get().getEmail());
        return ResponseEntity.ok(userResponse.get());
    }

    /**
     * Obtiene un usuario por su dirección de email.
     *
     * <p>Endpoint administrativo para búsquedas por email.
     * Útil para verificaciones y operaciones de soporte.</p>
     *
     * @param email dirección de correo electrónico
     * @return ResponseEntity con datos completos del usuario
     * @throws RuntimeException si el usuario no existe
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        log.debug("Obteniendo usuario por email: {}", email);

        Optional<UserResponse> userResponse = userService.getUserByEmail(email);

        if (userResponse.isEmpty()) {
            log.warn("Usuario no encontrado con email: {}", email);
            throw new RuntimeException("Usuario no encontrado con email " + email);
        }

        log.debug("Usuario encontrado por email: {}", email);
        return ResponseEntity.ok(userResponse.get());
    }

    /**
     * Obtiene usuarios filtrados por rol específico.
     *
     * <p>Retorna usuarios en formato resumido filtrados por el rol especificado.
     * Útil para selección de cuidadores, clientes o administradores.</p>
     *
     * @param role rol a filtrar (CLIENT, SITTER, ADMIN)
     * @return ResponseEntity con lista de usuarios del rol especificado
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSummaryResponse>> getUsersByRole(@PathVariable Role role) {
        log.debug("Obteniendo usuarios con rol: {}", role);

        List<UserSummaryResponse> users = userService.getUsersByRole(role);

        log.debug("Encontrados {} usuarios con rol: {}", users.size(), role);
        return ResponseEntity.ok(users);
    }

    /**
     * Obtiene usuarios activos en formato resumido con paginación.
     *
     * <p>Filtra solo usuarios con cuenta activa. Optimizado para operaciones
     * que requieren usuarios disponibles para servicios.</p>
     *
     * @param page número de página (base 0, por defecto 0)
     * @param size tamaño de página (por defecto 20)
     * @return ResponseEntity con página de usuarios activos
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserSummaryResponse>> getActiveUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("Obteniendo usuarios activos paginados");

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserSummaryResponse> activeUsers = userService.getActiveUsers(pageable);

        log.debug("Retornando {} usuarios activos", activeUsers.getNumberOfElements());
        return ResponseEntity.ok(activeUsers);
    }

    /**
     * Obtiene usuarios con email sin verificar.
     *
     * <p>Lista usuarios que requieren verificación de email.
     * Útil para campañas de activación y reportes de engagement.</p>
     *
     * @return ResponseEntity con lista de usuarios sin verificar
     */
    @GetMapping("/unverified")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSummaryResponse>> getUnverifiedUsers() {
        log.debug("Obteniendo usuarios con email sin verificar");

        List<UserSummaryResponse> unverifiedUsers = userService.getUnverifiedUsers();

        log.debug("Encontrados {} usuarios sin verificar email", unverifiedUsers.size());
        return ResponseEntity.ok(unverifiedUsers);
    }

    // ========== ENDPOINTS DE ESTADÍSTICAS ==========

    /**
     * Obtiene estadísticas generales de usuarios del sistema.
     *
     * <p>Retorna métricas agregadas incluyendo conteos por rol,
     * usuarios activos, verificados y análisis de distribución.
     * Esencial para dashboards administrativos.</p>
     *
     * <p><strong>Métricas incluidas:</strong></p>
     * <ul>
     *   <li>Conteo total y por estado de usuarios</li>
     *   <li>Distribución por roles del sistema</li>
     *   <li>Porcentajes de verificación y activación</li>
     *   <li>Recomendaciones basadas en métricas</li>
     * </ul>
     *
     * @return ResponseEntity con estadísticas completas
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserStatsResponse> getUserStats() {
        log.debug("Generando estadísticas de usuarios");

        UserStatsResponse stats = userService.getUserStats();

        log.info("Estadísticas generadas: {}", stats.getExecutiveSummary());
        return ResponseEntity.ok(stats);
    }

    // ========== ENDPOINTS DE UTILIDAD ==========

    /**
     * Verifica si un email está disponible para registro.
     *
     * <p>Endpoint de utilidad para validaciones en tiempo real
     * durante procesos de registro.</p>
     *
     * @param email dirección de email a verificar
     * @return ResponseEntity con boolean indicando disponibilidad
     */
    @GetMapping("/email-available")
    public ResponseEntity<Boolean> isEmailAvailable(@RequestParam String email) {
        log.debug("Verificando disponibilidad de email: {}", email);

        Optional<UserResponse> existingUser = userService.getUserByEmail(email);
        boolean available = existingUser.isEmpty();

        log.debug("Email {} está {}", email, available ? "disponible" : "en uso");
        return ResponseEntity.ok(available);
    }

    /**
     * Endpoint de salud para verificar el estado del servicio de usuarios.
     *
     * <p>Retorna información básica sobre el estado del servicio
     * sin información sensible.</p>
     *
     * @return ResponseEntity con información de salud del servicio
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.debug("Verificación de salud del servicio de usuarios");

        try {
            // Verificación básica contando usuarios
            long userCount = userService.getUserStats().getTotalUsers();
            return ResponseEntity.ok("UserService está operativo. Total usuarios: " + userCount);
        } catch (Exception e) {
            log.error("Error en verificación de salud: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("UserService presenta problemas: " + e.getMessage());
        }
    }

    @GetMapping("/verify")
    @Operation(summary = "Verifica el email de un usuario a través de un token")
    public RedirectView verifyEmail(@RequestParam("token") String token) {
        try {
            String message = userService.verifyEmailToken(token);
            // Redirige a una página de éxito en tu frontend
            //return new RedirectView(frontendBaseUrl + "/verification-success?message=" + message);
            return new RedirectView("/verification-success.html");
        } catch (RuntimeException e) {
            // Redirige a una página de error en tu frontend
            return new RedirectView(frontendBaseUrl + "/verification-error?message=" + e.getMessage());
        }
    }

}
