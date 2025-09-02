package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de persistencia de la entidad User.
 *
 * <p>Extiende JpaRepository para proporcionar operaciones CRUD básicas
 * e incluye métodos de consulta personalizados para casos de uso específicos
 * del sistema Petcare.</p>
 *
 * <p><strong>Consultas incluidas:</strong></p>
 * <ul>
 *   <li>Búsquedas por email (único identificador)</li>
 *   <li>Filtros por rol y estado de activación</li>
 *   <li>Consultas de verificación de email</li>
 *   <li>Búsquedas de texto libre para UI</li>
 *   <li>Contadores para estadísticas del sistema</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see User
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ========== BÚSQUEDAS BÁSICAS ==========

    /**
     * Busca un usuario por su dirección de email.
     *
     * <p>El email es único en el sistema y se usa como identificador
     * principal para autenticación.</p>
     *
     * @param email dirección de correo electrónico
     * @return Optional con el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el email especificado.
     *
     * @param email dirección de correo electrónico
     * @return true si existe un usuario con ese email
     */
    boolean existsByEmail(String email);

    // ========== CONSULTAS POR ROL ==========

    /**
     * Obtiene todos los usuarios con un rol específico.
     *
     * @param role rol a filtrar
     * @return lista de usuarios con el rol especificado
     */
    List<User> findAllByRole(Role role);

    /**
     * Obtiene usuarios por rol con paginación.
     *
     * @param role rol a filtrar
     * @param pageable configuración de paginación
     * @return página de usuarios con el rol especificado
     */
    Page<User> findByRole(Role role, Pageable pageable);

    /**
     * Cuenta usuarios por rol específico.
     *
     * @param role rol a contar
     * @return número de usuarios con ese rol
     */
    long countByRole(Role role);

    // ========== CONSULTAS POR ESTADO ==========

    /**
     * Obtiene todos los usuarios activos.
     *
     * @return lista de usuarios activos
     */
    List<User> findAllByIsActiveTrue();

    /**
     * Obtiene usuarios activos con paginación.
     *
     * @param pageable configuración de paginación
     * @return página de usuarios activos
     */
    Page<User> findByIsActiveTrue(Pageable pageable);

    /**
     * Cuenta usuarios activos.
     *
     * @return número de usuarios activos
     */
    long countByIsActiveTrue();

    /**
     * Obtiene usuarios por estado de activación.
     *
     * @param isActive estado a filtrar
     * @param pageable configuración de paginación
     * @return página de usuarios con el estado especificado
     */
    Page<User> findByIsActive(boolean isActive, Pageable pageable);

    // ========== CONSULTAS POR VERIFICACIÓN DE EMAIL ==========

    /**
     * Obtiene usuarios con email sin verificar.
     *
     * @return lista de usuarios sin verificar email
     */
    List<User> findAllByEmailVerifiedAtIsNull();

    /**
     * Obtiene usuarios con email sin verificar con paginación.
     *
     * @param pageable configuración de paginación
     * @return página de usuarios sin verificar email
     */
    Page<User> findByEmailVerifiedAtIsNull(Pageable pageable);

    /**
     * Cuenta usuarios con email verificado.
     *
     * @return número de usuarios con email verificado
     */
    long countByEmailVerifiedAtIsNotNull();

    /**
     * Cuenta usuarios con email sin verificar.
     *
     * @return número de usuarios sin verificar email
     */
    long countByEmailVerifiedAtIsNull();

    // ========== CONSULTAS COMBINADAS ==========

    /**
     * Obtiene usuarios activos con un rol específico.
     *
     * @param role rol a filtrar
     * @param pageable configuración de paginación
     * @return página de usuarios activos con el rol especificado
     */
    Page<User> findByRoleAndIsActiveTrue(Role role, Pageable pageable);

    /**
     * Obtiene cuidadores disponibles (activos y con email verificado).
     *
     * <p>Busca usuarios con rol SITTER o ADMIN que estén activos
     * y tengan el email verificado.</p>
     *
     * @param pageable configuración de paginación
     * @return página de cuidadores disponibles
     */
    @Query("SELECT u FROM User u WHERE u.role IN (:roles) AND u.isActive = true AND u.emailVerifiedAt IS NOT NULL")
    Page<User> findAvailableSitters(@Param("roles") List<Role> roles, Pageable pageable);

    // ========== BÚSQUEDAS DE TEXTO ==========

    /**
     * Busca usuarios por término libre en nombre y email.
     *
     * <p>Búsqueda case-insensitive que incluye firstName, lastName y email.
     * Útil para funcionalidades de búsqueda en interfaces de usuario.</p>
     *
     * @param searchTerm término de búsqueda
     * @param pageable configuración de paginación
     * @return página de usuarios que coinciden con la búsqueda
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Busca usuarios activos por término libre.
     *
     * @param searchTerm término de búsqueda
     * @param pageable configuración de paginación
     * @return página de usuarios activos que coinciden con la búsqueda
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND (" +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> findActiveUsersBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // ========== CONSULTAS DE ESTADÍSTICAS ==========

    /**
     * Cuenta usuarios por rol y estado activo.
     *
     * @param role rol a contar
     * @param isActive estado a filtrar
     * @return número de usuarios activos con el rol especificado
     */
    long countByRoleAndIsActive(Role role, boolean isActive);

    /**
     * Obtiene el número total de usuarios registrados en los últimos N días.
     *
     * @param sinceDate fecha desde la cual contar usuarios
     * @return número de usuarios registrados en el período
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :sinceDate")
    long countUsersRegisteredSince(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Obtiene usuarios por rol específico ordenados por fecha de creación.
     *
     * @param role rol a filtrar
     * @param pageable configuración de paginación y ordenamiento
     * @return página de usuarios ordenados
     */
    Page<User> findByRoleOrderByCreatedAtDesc(Role role, Pageable pageable);
}