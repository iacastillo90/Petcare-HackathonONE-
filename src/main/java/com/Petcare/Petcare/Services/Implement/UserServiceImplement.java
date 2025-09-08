package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.Configurations.Security.Jwt.JwtService;
import com.Petcare.Petcare.DTOs.Account.CreateAccountRequest;
import com.Petcare.Petcare.DTOs.Auth.Request.LoginRequest;
import com.Petcare.Petcare.DTOs.Auth.Respone.AuthResponse;
import com.Petcare.Petcare.DTOs.Booking.BookingSummaryResponse;
import com.Petcare.Petcare.DTOs.Pet.PetSummaryResponse;
import com.Petcare.Petcare.DTOs.User.*;
import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.Account.AccountUser;
import com.Petcare.Petcare.Models.Booking.BookingStatus;
import com.Petcare.Petcare.Models.Pet;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.*;
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
import java.util.concurrent.ThreadLocalRandom;
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
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;
    private final BookingRepository bookingRepository;
    private final SitterProfileRepository sitterProfileRepository;
    private final PetRepository petRepository;

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

        // . Crea el DTO del perfil del usuario
        UserProfileDTO userProfile = new UserProfileDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name(),
                String.format("%c%c", user.getFirstName().charAt(0), user.getLastName().charAt(0)).toUpperCase()
        );

        // 4. Generar el token JWT
        String token = jwtService.getToken(user);

        log.info("Login exitoso para usuario: {} con rol: {}", user.getEmail(), user.getRole());

        // 5. Devolver la respuesta con el token y el rol
        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .userProfile(userProfile)
                .build();
    }

    @Override
    public AuthResponse registerUserSitter(CreateUserRequest request) {
        log.info("Intento de registro para email: {}", request.getEmail());

        // 1. Validar si el email ya existe
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Intento de registro con email duplicado: {}", request.getEmail());
            throw new IllegalArgumentException("El email ya está registrado: " + request.getEmail());
        }

        // 2. Crear la entidad User
        User newUser = createUserEntity(request, Role.SITTER);
        User savedUser = userRepository.save(newUser);

        // =================================================================
        // PASO 2: LÓGICA AÑADIDA PARA CREAR LA CUENTA AUTOMÁTICAMENTE
        // =================================================================

        // 2a. Generar el número de cuenta único
        String accountNumber = generateUniqueAccountNumber();

        // 2b. Generar el nombre de la cuenta por defecto
        String accountName = "Cuidadora - " + savedUser.getLastName();

        // 2c. Crear y guardar la nueva entidad Account
        Account newAccount = new Account(savedUser, accountName, accountNumber);
        Account savedAccount = accountRepository.save(newAccount);

        // 2d. Crear y guardar la relación en AccountUser
        AccountUser accountUserLink = new AccountUser(savedAccount, savedUser, Role.SITTER);
        accountUserRepository.save(accountUserLink);

        log.info("Creada cuenta automática {} para para la cuidadora {}", savedAccount.getAccountNumber(), savedUser.getEmail());

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

        // =================================================================
        // PASO 2: LÓGICA AÑADIDA PARA CREAR LA CUENTA AUTOMÁTICAMENTE
        // =================================================================

        // 2a. Generar el número de cuenta único
        String accountNumber = generateUniqueAccountNumber();

        // 2b. Generar el nombre de la cuenta por defecto
        String accountName = "Cuenta de Familia - " + savedUser.getLastName();

        // 2c. Crear y guardar la nueva entidad Account
        Account newAccount = new Account(savedUser, accountName, accountNumber);
        Account savedAccount = accountRepository.save(newAccount);

        // 2d. Crear y guardar la relación en AccountUser
        AccountUser accountUserLink = new AccountUser(savedAccount, savedUser, Role.CLIENT);
        accountUserRepository.save(accountUserLink);

        log.info("Creada cuenta automática {} para el usuario {}", savedAccount.getAccountNumber(), savedUser.getEmail());

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

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            // Genera un número aleatorio de 8 dígitos
            long randomNumber = ThreadLocalRandom.current().nextLong(10_000_000L, 100_000_000L);
            accountNumber = "ACC-" + randomNumber;
        } while (accountRepository.existsByAccountNumber(accountNumber)); // Verifica en la BD

        return accountNumber;
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


    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStatsForUser(Long userId) {
        log.debug("Generando estadísticas de dashboard para usuario ID: {}", userId);

        try {
            // 1. Validación inicial y obtención de cuenta
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("Usuario no encontrado con ID: {}", userId);
                        return new IllegalArgumentException("Usuario no encontrado con ID: " + userId);
                    });

            Account account = accountRepository.findByOwnerUser(user)
                    .orElseThrow(() -> {
                        log.error("No se encontró cuenta para el usuario ID: {}", userId);
                        return new IllegalStateException("No se encontró una cuenta para el usuario con ID: " + userId);
                    });

            Long accountId = account.getId();
            log.debug("Calculando estadísticas para cuenta ID: {}", accountId);

            // 2. Cálculo de mascotas activas
            long activePetsCount = petRepository.countByAccountIdAndIsActiveTrue(accountId);
            log.debug("Mascotas activas encontradas: {}", activePetsCount);

            // 3. Cálculo de citas programadas (futuras y no canceladas/completadas)
            LocalDateTime now = LocalDateTime.now();
            long scheduledAppointmentsCount = bookingRepository.countByPetAccountIdAndStartTimeAfterAndStatusNotIn(
                    accountId,
                    now,
                    List.of(BookingStatus.CANCELLED, BookingStatus.COMPLETED)
            );
            log.debug("Citas programadas encontradas: {}", scheduledAppointmentsCount);

            // 4. Cálculo de vacunas (lógica simplificada - ajustar según tu modelo de datos)
            List<Pet> userPets = petRepository.findByAccountIdAndIsActiveTrue(accountId);
            String vaccinesStatus = calculateVaccinesStatus(userPets);

            // 5. Cálculo de recordatorios pendientes
            // Consideramos como recordatorios: citas confirmadas próximas (próximas 7 días)
            LocalDateTime nextWeek = now.plusDays(7);
            long pendingRemindersCount = bookingRepository.countByPet_Account_IdAndStartTimeBetweenAndStatus(
                    accountId,
                    now,
                    nextWeek,
                    BookingStatus.CONFIRMED
            );
            log.debug("Recordatorios pendientes encontrados: {}", pendingRemindersCount);

            // 6. Construir y retornar el DTO
            DashboardStatsDTO stats = new DashboardStatsDTO(
                    (int) activePetsCount,
                    generateChangeText("mascotas", activePetsCount),
                    (int) scheduledAppointmentsCount,
                    generateChangeText("citas", scheduledAppointmentsCount),
                    vaccinesStatus,
                    calculateVaccinesChangeText(userPets),
                    (int) pendingRemindersCount,
                    generateChangeText("recordatorios", pendingRemindersCount)
            );

            log.info("Estadísticas de dashboard generadas exitosamente para usuario ID: {} - " +
                            "Mascotas: {}, Citas: {}, Recordatorios: {}",
                    userId, activePetsCount, scheduledAppointmentsCount, pendingRemindersCount);

            return stats;

        } catch (Exception e) {
            log.error("Error al generar estadísticas de dashboard para usuario ID {}: {}", userId, e.getMessage());
            throw e;
        }
    }


    /**
     * Calcula el estado de vacunación de las mascotas.
     * Nota: Esta implementación es básica - ajustar según tu modelo de vacunas real.
     */
    private String calculateVaccinesStatus(List<Pet> pets) {
        if (pets.isEmpty()) {
            return "0/0";
        }

        // Lógica simplificada - ajustar según tu modelo de Vaccine/VaccinationRecord
        long totalPetsRequiringVaccines = pets.size();

        // Por ahora, asumimos que todas las mascotas activas necesitan vacunas
        // Puedes implementar lógica más compleja aquí:
        // - Consultar tabla de vacunas por mascota
        // - Verificar fechas de vencimiento
        // - Considerar tipo de mascota y vacunas requeridas

        long vaccinesUpToDateCount = pets.stream()
                .mapToLong(pet -> {
                    // Ejemplo: verificar si la mascota tiene vacunas registradas y al día
                    // return vaccineRepository.countValidVaccinesForPet(pet.getId(), LocalDate.now());
                    // Por ahora, lógica placeholder:
                    return pet.getSpecialNotes() != null &&
                            pet.getSpecialNotes().toLowerCase().contains("vacunado") ? 1 : 0;
                })
                .sum();

        return String.format("%d/%d", vaccinesUpToDateCount, totalPetsRequiringVaccines);
    }

    /**
     * Genera texto de cambio para vacunas.
     */
    private String calculateVaccinesChangeText(List<Pet> pets) {
        if (pets.isEmpty()) {
            return "Sin mascotas registradas";
        }

        // Lógica simplificada para calcular vacunas pendientes
        long pendingVaccines = pets.stream()
                .mapToLong(pet -> {
                    // Lógica placeholder - implementar según tu modelo real
                    return pet.getSpecialNotes() == null ||
                            !pet.getSpecialNotes().toLowerCase().contains("vacunado") ? 1 : 0;
                })
                .sum();

        if (pendingVaccines == 0) {
            return "Todas al día";
        }

        return String.format("%d pendiente(s)", pendingVaccines);
    }

    /**
     * Genera texto descriptivo para los cambios en las estadísticas.
     */
    private String generateChangeText(String type, long count) {
        if (count == 0) {
            return "Sin " + type + " registradas";
        }

        return switch (type) {
            case "mascotas" -> count == 1 ? "Total de mascotas activas" : "Total de mascotas activas";
            case "citas" -> count == 1 ? "Próxima cita programada" : "Próximas citas";
            case "recordatorios" -> count == 1 ? "Evento próximo" : "Eventos próximos";
            default -> "Elementos registrados";
        };
    }

    /*@Override
    public MainDashboardDTO getMainDashboardData(Long userId) {
        // Busca el usuario principal. Si no existe, lanza una excepción.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        // Busca la cuenta asociada al usuario.
        Account account = accountRepository.findByOwnerUser(user)
                .orElseThrow(() -> new RuntimeException("No se encontró cuenta para el usuario ID: " + userId));
        Long accountId = account.getId();

        // --- Lógica para cada parte del Dashboard ---

        // 1. Obtener el perfil del usuario
        UserProfileDTO userProfile = new UserProfileDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name(),
                String.format("%c%c", user.getFirstName().charAt(0), user.getLastName().charAt(0)).toUpperCase()
        );

        // 2. Obtener las estadísticas (usando el método que ya creaste)
        DashboardStatsDTO stats = getDashboardStatsForUser(userId);

        // 3. Obtener la lista de mascotas y mapearlas usando tu DTO existente
        List<PetSummaryResponse> petSummaries = petRepository.findByAccountId(accountId)
                .stream()
                .map(PetSummaryResponse::fromEntity)
                .collect(Collectors.toList());

        // 4. Obtener la próxima cita y mapearla usando tu DTO existente
        BookingSummaryResponse nextAppointment = bookingRepository
                .findFirstByAccountIdAndStartTimeAfterOrderByStartTimeAsc(accountId, LocalDateTime.now())
                .map(BookingSummaryResponse::fromEntity) // Usa tu método de fábrica
                .orElse(null);

        // 5. Obtener los cuidadores recientes (lógica de ejemplo)
        // Necesitarás una consulta más específica aquí, por ahora listamos algunos
        List<SitterSummaryDTO> recentSitters = sitterProfileRepository.findTop3ByOrderByLastServiceDateDesc() // Método de ejemplo
                .stream()
                .map(sitterProfile -> new SitterSummaryDTO(
                        sitterProfile.getUser().getId(),
                        sitterProfile.getUser().getFirstName() + " " + sitterProfile.getUser().getLastName(),
                        "Especialidad aquí", // Deberás añadir este campo a tu SitterProfile
                        sitterProfile.getProfileImageUrl(),
                        sitterProfile.getAverageRating() != null ? sitterProfile.getAverageRating().doubleValue() : 0.0
                ))
                .collect(Collectors.toList());

        // 6. Ensamblar y devolver el DTO principal
        return new MainDashboardDTO(userProfile, nextAppointment, petSummaries, recentSitters, stats);
    }*/
}