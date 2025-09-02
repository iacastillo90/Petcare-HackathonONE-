package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.Configurations.Security.Jwt.JwtService;
import com.Petcare.Petcare.DTOs.Auth.Request.LoginRequest;
import com.Petcare.Petcare.DTOs.Auth.Respone.AuthResponse;
import com.Petcare.Petcare.DTOs.User.*;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.UserRepository;
import com.Petcare.Petcare.Services.EmailService;
import com.Petcare.Petcare.Services.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de usuarios para el sistema Petcare.
 *
 * <p>Esta clase proporciona la lógica de negocio completa para la gestión de usuarios,
 * incluyendo autenticación, registro, operaciones CRUD y consultas especializadas.
 * Implementa todas las mejores prácticas de Spring Boot y mantiene consistencia
 * con el patrón DTO establecido en el proyecto.</p>
 *
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li>Autenticación y autorización de usuarios</li>
 *   <li>Registro de usuarios con diferentes roles</li>
 *   <li>Operaciones CRUD completas con validaciones</li>
 *   <li>Consultas especializadas y reportes de estadísticas</li>
 *   <li>Gestión de estados de cuenta y verificación</li>
 * </ul>
 *
 * <p><strong>Patrones implementados:</strong></p>
 * <ul>
 *   <li>DTO pattern para transferencia de datos</li>
 *   <li>Factory methods para creación de DTOs</li>
 *   <li>Transactional boundaries apropiados</li>
 *   <li>Logging estructurado para auditoria</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see UserService
 * @see User
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImplement implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    @Value("${petcare.api.base-url:http://localhost:8088}")
    private String apiBaseUrl;

    // ========== MÉTODOS DE AUTENTICACIÓN ==========

    /**
     * Autentica un usuario y genera un token JWT.
     *
     * <p>Realiza la autenticación usando Spring Security y actualiza
     * el timestamp de último login del usuario.</p>
     *
     * @param request datos de login (email y contraseña)
     * @return respuesta de autenticación con token y rol
     * @throws IllegalStateException si el usuario no existe después de autenticación exitosa
     */
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Intento de login para email: {}", request.getEmail());

        // 1. Autenticar al usuario con el AuthenticationManager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. Si la autenticación es exitosa, buscar al usuario
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado después de autenticación exitosa: {}", request.getEmail());
                    return new IllegalStateException("Usuario no encontrado después de la autenticación.");
                });

        // 3. Actualizar último login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 4. Generar el token JWT
        String token = jwtService.getToken(user);

        log.info("Login exitoso para usuario: {} con rol: {}", user.getEmail(), user.getRole());

        // 5. Devolver la respuesta con el token y el rol
        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .build();
    }

    /**
     * Registra un nuevo usuario como CLIENT y lo autentica automáticamente.
     *
     * <p>Valida la unicidad del email, cifra la contraseña y asigna el rol CLIENT
     * por defecto. Genera un token JWT para login automático.</p>
     *
     * @param request datos de registro del usuario
     * @return respuesta de autenticación con token
     * @throws IllegalArgumentException si el email ya existe
     */
    @Override
    @Transactional
    public AuthResponse registerUser(CreateUserRequest request) {
        log.info("Intento de registro para email: {}", request.getEmail());

        // 1. Validar si el email ya existe
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Intento de registro con email duplicado: {}", request.getEmail());
            throw new IllegalArgumentException("El email ya está registrado: " + request.getEmail());
        }

        // 2. Crear la entidad User
        User newUser = createUserEntity(request, Role.CLIENT);
        User savedUser = userRepository.save(newUser);

        try {
            // 1. Generar el token de verificación
            String verificationToken = jwtService.generateVerificationToken(savedUser);

            // 2. Construir la URL completa
            String verificationUrl = apiBaseUrl + "/api/users/verify?token=" + verificationToken;

            // 3. Enviar el correo de forma asíncrona
            emailService.sendVerificationEmail(
                    savedUser.getEmail(),
                    savedUser.getFullName(),
                    verificationUrl,
                    24 // Horas de expiración (para mostrar en el correo)
            );
            log.info("Correo de verificación encolado para: {}", savedUser.getEmail());
        } catch (Exception e) {
            log.error("Error al intentar enviar el correo de verificación para {}: {}", savedUser.getEmail(), e.getMessage());
        }


        // 3. Generar el token JWT para el nuevo usuario
        String token = jwtService.getToken(savedUser);

        log.info("Usuario registrado exitosamente: {} con ID: {}", savedUser.getEmail(), savedUser.getId());

        // 4. Devolver la respuesta de autenticación
        return AuthResponse.builder()
                .token(token)
                .role(savedUser.getRole().name())
                .build();
    }

    // ========== OPERACIONES CRUD ==========

    /**
     * Obtiene todos los usuarios del sistema como DTOs completos.
     *
     * @return lista de UserResponse DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Obteniendo todos los usuarios del sistema");
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los usuarios del sistema en formato resumido, con paginación.
     *
     * @param pageable configuración de paginación
     * @return página de UserSummaryResponse DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> getAllUsersSummary(Pageable pageable) {
        log.debug("Obteniendo resumen paginado de usuarios - Página: {}, Tamaño: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<User> userPage = userRepository.findAll(pageable);
        List<UserSummaryResponse> summaryList = userPage.getContent()
                .stream()
                .map(UserSummaryResponse::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(summaryList, pageable, userPage.getTotalElements());
    }

    /**
     * Busca un usuario por su ID y devuelve un DTO completo.
     *
     * @param id identificador del usuario
     * @return Optional con UserResponse si existe
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserById(Long id) {
        log.debug("Buscando usuario por ID: {}", id);
        return userRepository.findById(id)
                .map(UserResponse::fromEntity);
    }

    /**
     * Busca un usuario por su email y devuelve un DTO completo.
     *
     * @param email dirección de correo electrónico
     * @return Optional con UserResponse si existe
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserByEmail(String email) {
        log.debug("Buscando usuario por email: {}", email);
        return userRepository.findByEmail(email)
                .map(UserResponse::fromEntity);
    }

    /**
     * Actualiza un usuario existente con los datos proporcionados.
     *
     * <p>Valida la unicidad del email si se cambia y permite actualización
     * opcional de la contraseña.</p>
     *
     * @param id identificador del usuario a actualizar
     * @param request datos de actualización
     * @return DTO del usuario actualizado
     * @throws RuntimeException si el usuario no existe
     * @throws IllegalArgumentException si el nuevo email ya existe
     */
    @Override
    @Transactional
    public UserResponse updateUser(Long id, CreateUserRequest request) {
        log.info("Actualizando usuario con ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado para actualizar con ID: {}", id);
                    return new RuntimeException("Usuario no encontrado con id " + id);
                });

        // Actualizar campos básicos
        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());
        existingUser.setAddress(request.getAddress());
        existingUser.setPhoneNumber(request.getPhoneNumber());

        // Validar y actualizar email si cambió
        if (!request.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                log.warn("Intento de actualizar a email duplicado: {}", request.getEmail());
                throw new IllegalArgumentException("El nuevo email ya está registrado: " + request.getEmail());
            }
            existingUser.setEmail(request.getEmail());
            // Reset verificación de email si cambió
            existingUser.setEmailVerifiedAt(null);
        }

        // Actualizar contraseña si se proporcionó
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        existingUser.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(existingUser);

        log.info("Usuario actualizado exitosamente: {}", updatedUser.getEmail());
        return UserResponse.fromEntity(updatedUser);
    }

    /**
     * Elimina un usuario del sistema.
     *
     * @param id identificador del usuario a eliminar
     * @throws RuntimeException si el usuario no existe
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Eliminando usuario con ID: {}", id);

        if (!userRepository.existsById(id)) {
            log.error("Usuario no encontrado para eliminar con ID: {}", id);
            throw new RuntimeException("Usuario no encontrado con id " + id);
        }

        userRepository.deleteById(id);
        log.info("Usuario eliminado exitosamente con ID: {}", id);
    }

    // ========== OPERACIONES ADMINISTRATIVAS ==========

    /**
     * Crea un usuario con rol específico (para uso administrativo).
     *
     * @param request datos del usuario
     * @param role rol a asignar
     * @return DTO del usuario creado
     * @throws IllegalArgumentException si el email ya existe
     */
    @Override
    @Transactional
    public UserResponse createUserByAdmin(CreateUserRequest request, Role role) {
        log.info("Creando usuario por admin con rol: {} para email: {}", role, request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Intento de crear usuario con email duplicado: {}", request.getEmail());
            throw new IllegalArgumentException("El email ya está registrado: " + request.getEmail());
        }

        User newUser = createUserEntity(request, role);
        User savedUser = userRepository.save(newUser);

        log.info("Usuario creado exitosamente por admin: {} con rol: {}", savedUser.getEmail(), role);
        return UserResponse.fromEntity(savedUser);
    }

    /**
     * Cambia el estado activo/inactivo de un usuario.
     *
     * @param id identificador del usuario
     * @param active nuevo estado
     * @return DTO del usuario actualizado
     * @throws RuntimeException si el usuario no existe
     */
    @Override
    @Transactional
    public UserResponse toggleUserActive(Long id, boolean active) {
        log.info("Cambiando estado activo del usuario ID: {} a: {}", id, active);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado para cambiar estado con ID: {}", id);
                    return new RuntimeException("Usuario no encontrado con id " + id);
                });

        user.setActive(active);
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        log.info("Estado del usuario cambiado exitosamente: {} ahora está {}",
                user.getEmail(), active ? "activo" : "inactivo");
        return UserResponse.fromEntity(updatedUser);
    }

    /**
     * Marca el email de un usuario como verificado.
     *
     * @param id identificador del usuario
     * @return DTO del usuario actualizado
     * @throws RuntimeException si el usuario no existe
     */
    @Override
    @Transactional
    public UserResponse markEmailAsVerified(Long id) {
        log.info("Marcando email como verificado para usuario ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado para verificar email con ID: {}", id);
                    return new RuntimeException("Usuario no encontrado con id " + id);
                });

        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        log.info("Email verificado exitosamente para usuario: {}", user.getEmail());
        return UserResponse.fromEntity(updatedUser);
    }

    // ========== CONSULTAS ESPECIALIZADAS ==========

    /**
     * Obtiene usuarios filtrados por rol en formato resumido.
     *
     * @param role rol a filtrar
     * @return lista de UserSummaryResponse
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getUsersByRole(Role role) {
        log.debug("Obteniendo usuarios con rol: {}", role);
        return userRepository.findAllByRole(role)
                .stream()
                .map(UserSummaryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene usuarios activos paginados en formato resumido.
     *
     * @param pageable configuración de paginación
     * @return página de UserSummaryResponse
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> getActiveUsers(Pageable pageable) {
        log.debug("Obteniendo usuarios activos paginados");

        Page<User> userPage = userRepository.findByIsActiveTrue(pageable);
        List<UserSummaryResponse> summaryList = userPage.getContent()
                .stream()
                .map(UserSummaryResponse::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(summaryList, pageable, userPage.getTotalElements());
    }

    /**
     * Obtiene usuarios con email sin verificar en formato resumido.
     *
     * @return lista de UserSummaryResponse
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getUnverifiedUsers() {
        log.debug("Obteniendo usuarios con email sin verificar");
        return userRepository.findAllByEmailVerifiedAtIsNull()
                .stream()
                .map(UserSummaryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Genera estadísticas generales de usuarios del sistema.
     *
     * @return DTO con estadísticas completas
     */
    @Override
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats() {
        log.debug("Generando estadísticas de usuarios");

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActiveTrue();
        long clientCount = userRepository.countByRole(Role.CLIENT);
        long sitterCount = userRepository.countByRole(Role.SITTER);
        long adminCount = userRepository.countByRole(Role.ADMIN);
        long verifiedUsers = userRepository.countByEmailVerifiedAtIsNotNull();

        UserStatsResponse stats = UserStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .clientCount(clientCount)
                .sitterCount(sitterCount)
                .adminCount(adminCount)
                .verifiedUsers(verifiedUsers)
                .build();

        log.info("Estadísticas generadas: {}", stats.getExecutiveSummary());
        return stats;
    }

    // ========== MÉTODOS PRIVADOS DE UTILIDAD ==========

    /**
     * Método de utilidad para crear entidades User con datos comunes.
     *
     * @param request datos del usuario
     * @param role rol a asignar
     * @return nueva entidad User configurada
     */
    private User createUserEntity(CreateUserRequest request, Role role) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAddress(request.getAddress());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return user;
    }

    @Override
    @Transactional
    public String verifyEmailToken(String token) {
        log.info("Intentando verificar email con token.");
        try {
            // 1. Validar el token y extraer el email
            Claims claims = jwtService.getClaimsFromVerificationToken(token);
            String email = claims.getSubject();

            // 2. Buscar al usuario
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado para el token de verificación."));

            // 3. Verificar si ya está verificado
            if (user.isEmailVerified()) {
                log.warn("Intento de verificar un email ya verificado: {}", email);
                return "Tu email ya ha sido verificado anteriormente. Ya puedes iniciar sesión.";
            }

            // 4. Marcar como verificado y guardar
            markEmailAsVerified(user.getId());

            log.info("Email verificado exitosamente para: {}", email);
            return "¡Gracias por verificar tu correo electrónico! Tu cuenta ya está activa.";

        } catch (JwtException e) {
            log.error("Token de verificación inválido o expirado: {}", e.getMessage());
            throw new RuntimeException("El enlace de verificación es inválido o ha expirado. Por favor, solicita uno nuevo.");
        }
    }

}