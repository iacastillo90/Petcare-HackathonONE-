package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.DTOs.Auth.Request.LoginRequest;
import com.Petcare.Petcare.DTOs.Auth.Respone.AuthResponse;
import com.Petcare.Petcare.DTOs.User.*;
import com.Petcare.Petcare.Models.User.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de servicio para la gestión de usuarios en el sistema Petcare.
 *
 * <p>Define las operaciones principales para el manejo de usuarios, incluyendo
 * autenticación, registro, operaciones CRUD y funcionalidades administrativas.
 * Proporciona métodos optimizados que retornan DTOs específicos según el contexto.</p>
 *
 * <p><strong>Responsabilidades principales:</strong></p>
 * <ul>
 *   <li>Autenticación y registro de usuarios</li>
 *   <li>Gestión completa del ciclo de vida de usuarios</li>
 *   <li>Operaciones administrativas con control de roles</li>
 *   <li>Consultas optimizadas para diferentes contextos UI</li>
 *   <li>Generación de reportes y estadísticas</li>
 * </ul>
 *
 * <p><strong>DTOs utilizados:</strong></p>
 * <ul>
 *   <li>{@link CreateUserRequest}: Para creación de usuarios</li>
 *   <li>{@link UserResponse}: Para respuestas completas de API</li>
 *   <li>{@link UserSummaryResponse}: Para listados optimizados</li>
 *   <li>{@link UserStatsResponse}: Para estadísticas del sistema</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see com.Petcare.Petcare.Models.User.User
 * @see com.Petcare.Petcare.Services.Implement.UserServiceImplement
 */
public interface UserService {

    // ========== MÉTODOS DE AUTENTICACIÓN ==========

    /**
     * Autentica un usuario en el sistema.
     *
     * <p>Valida las credenciales del usuario y genera un token JWT válido
     * para acceso a recursos protegidos. Actualiza el timestamp de último login.</p>
     *
     * @param request datos de autenticación (email y contraseña)
     * @return respuesta con token JWT y información del rol
     * @throws IllegalStateException si el usuario no existe después de autenticación
     * @throws org.springframework.security.authentication.BadCredentialsException si las credenciales son inválidas
     */
    AuthResponse login(LoginRequest request);

    /**
     * Registra un nuevo usuario cliente en el sistema.
     *
     * <p>Crea una cuenta nueva con rol CLIENT por defecto. El usuario
     * queda autenticado automáticamente tras el registro exitoso.</p>
     *
     * @param request datos del nuevo usuario
     * @return respuesta con token JWT para el usuario recién creado
     * @throws IllegalArgumentException si el email ya existe
     */
    AuthResponse registerUser(CreateUserRequest request);

    AuthResponse registerUserSitter(CreateUserRequest request);

    /**
     * Verifica el email de un usuario usando un token de verificación.
     * @param token El token JWT de verificación.
     * @return Un mensaje de éxito.
     * @throws RuntimeException si el token es inválido o el usuario no se encuentra.
     */
    String verifyEmailToken(String token);

    // ========== MÉTODOS DE CONSULTA ==========

    /**
     * Obtiene todos los usuarios del sistema con información completa.
     *
     * <p>Método para uso administrativo. Retorna información detallada
     * de todos los usuarios registrados sin paginación.</p>
     *
     * @return lista de todos los usuarios con datos completos
     */
    List<UserResponse> getAllUsers();

    /**
     * Obtiene todos los usuarios en formato resumido con paginación.
     *
     * <p>Optimizado para listados en UI con gran cantidad de usuarios.
     * Incluye solo información esencial para reducir payload.</p>
     *
     * @param pageable configuración de paginación y ordenamiento
     * @return página de usuarios en formato resumido
     */
    Page<UserSummaryResponse> getAllUsersSummary(Pageable pageable);

    /**
     * Busca un usuario por ID con información completa.
     *
     * @param id identificador único del usuario
     * @return respuesta completa del usuario o vacío si no existe
     */
    Optional<UserResponse> getUserById(Long id);

    /**
     * Busca un usuario por email con información completa.
     *
     * @param email dirección de correo electrónico
     * @return respuesta completa del usuario o vacío si no existe
     */
    Optional<UserResponse> getUserByEmail(String email);

    // ========== MÉTODOS DE ACTUALIZACIÓN ==========

    /**
     * Actualiza un usuario existente con información proporcionada.
     *
     * <p>Permite actualización de campos no sensibles. El email puede
     * cambiarse si no está en uso por otro usuario. La contraseña
     * se actualiza solo si se proporciona. Reset verificación si cambia email.</p>
     *
     * @param id identificador del usuario a actualizar
     * @param request datos actualizados del usuario
     * @return respuesta con información completa actualizada
     * @throws RuntimeException si el usuario no existe
     * @throws IllegalArgumentException si el nuevo email ya está en uso
     */
    UserResponse updateUser(Long id, CreateUserRequest request);

    /**
     * Activa o desactiva una cuenta de usuario.
     *
     * <p>Permite habilitar/deshabilitar cuentas sin eliminar datos.
     * Los usuarios inactivos no pueden autenticarse.</p>
     *
     * @param id identificador del usuario
     * @param active nuevo estado de activación
     * @return respuesta con información actualizada
     * @throws RuntimeException si el usuario no existe
     */
    UserResponse toggleUserActive(Long id, boolean active);

    /**
     * Marca el email de un usuario como verificado.
     *
     * <p>Establece la fecha de verificación del email. Típicamente
     * utilizado tras confirmar un enlace de verificación.</p>
     *
     * @param id identificador del usuario
     * @return respuesta con información actualizada
     * @throws RuntimeException si el usuario no existe
     */
    UserResponse markEmailAsVerified(Long id);

    // ========== MÉTODOS DE ELIMINACIÓN ==========

    /**
     * Elimina un usuario del sistema (eliminación física).
     *
     * <p><strong>Operación destructiva</strong> que elimina permanentemente
     * todos los datos del usuario. Debe usarse con precaución.</p>
     *
     * @param id identificador del usuario a eliminar
     * @throws RuntimeException si el usuario no existe
     */
    void deleteUser(Long id);

    // ========== MÉTODOS ADMINISTRATIVOS ==========

    /**
     * Crea un nuevo usuario con rol específico (operación administrativa).
     *
     * <p>Permite a administradores crear usuarios con cualquier rol.
     * A diferencia del registro público, no autentica automáticamente.</p>
     *
     * @param request datos del nuevo usuario
     * @param role rol específico a asignar
     * @return respuesta con información del usuario creado
     * @throws IllegalArgumentException si el email ya existe
     */
    UserResponse createUserByAdmin(CreateUserRequest request, Role role);

    /**
     * Obtiene estadísticas generales de usuarios.
     *
     * <p>Retorna contadores por rol, usuarios activos, verificados, etc.
     * Incluye cálculos de porcentajes y análisis. Útil para dashboards administrativos.</p>
     *
     * @return objeto con estadísticas completas del sistema
     */
    UserStatsResponse getUserStats();

    // ========== MÉTODOS DE CONSULTA ESPECIALIZADA ==========

    /**
     * Obtiene usuarios filtrados por rol en formato resumido.
     *
     * <p>Útil para seleccionar cuidadores o clientes específicos
     * en interfaces de usuario.</p>
     *
     * @param role rol a filtrar
     * @return lista de usuarios del rol especificado en formato resumido
     */
    List<UserSummaryResponse> getUsersByRole(Role role);

    /**
     * Obtiene usuarios activos con paginación en formato resumido.
     *
     * <p>Filtra solo usuarios con cuenta activa, optimizado para
     * operaciones que requieren usuarios disponibles.</p>
     *
     * @param pageable configuración de paginación
     * @return página de usuarios activos en formato resumido
     */
    Page<UserSummaryResponse> getActiveUsers(Pageable pageable);

    /**
     * Obtiene usuarios con email sin verificar.
     *
     * <p>Lista usuarios que requieren verificación de email.
     * Útil para campañas de activación y reportes de engagement.</p>
     *
     * @return lista de usuarios sin verificar email en formato resumido
     */
    List<UserSummaryResponse> getUnverifiedUsers();

    DashboardStatsDTO getDashboardStatsForUser(Long userId);

    //MainDashboardDTO getMainDashboardData(Long id);
}