package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.DTOs.Booking.BookingDetailResponse;
import com.Petcare.Petcare.DTOs.Booking.BookingSummaryResponse;
import com.Petcare.Petcare.DTOs.Booking.CreateBookingRequest;
import com.Petcare.Petcare.DTOs.Booking.UpdateBookingRequest;
import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Booking.BookingStatus;
import com.Petcare.Petcare.Models.Pet;
import com.Petcare.Petcare.Models.ServiceOffering.ServiceOffering;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.*;
import com.Petcare.Petcare.Services.BookingService;
import com.Petcare.Petcare.Services.NotificationService;
import com.Petcare.Petcare.Services.PlatformFeeService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Implementación concreta del servicio de gestión de reservas de cuidado de mascotas.
 *
 * <p>Esta clase proporciona la lógica de negocio completa para todas las operaciones
 * relacionadas con reservas, incluyendo validaciones, cálculos automáticos,
 * gestión de estados y coordinación con otros servicios del sistema.</p>
 *
 * <p><strong>Responsabilidades principales:</strong></p>
 * <ul>
 *   <li>Validación integral de datos de entrada y reglas de negocio</li>
 *   <li>Gestión del ciclo de vida completo de las reservas</li>
 *   <li>Cálculos automáticos de precios, tiempos y tarifas</li>
 *   <li>Coordinación con servicios de notificaciones y pagos</li>
 *   <li>Control de disponibilidad y prevención de conflictos</li>
 *   <li>Auditoría y trazabilidad de operaciones</li>
 * </ul>
 *
 * <p><strong>Patrones implementados:</strong></p>
 * <ul>
 *   <li>Service Layer: Encapsula lógica de negocio compleja</li>
 *   <li>Transaction Script: Operaciones transaccionales atómicas</li>
 *   <li>Domain Events: Notificaciones de cambios de estado</li>
 *   <li>Specification: Validaciones complejas reutilizables</li>
 * </ul>
 *
 * <p><strong>Consideraciones de rendimiento:</strong></p>
 * <ul>
 *   <li>Carga lazy optimizada para evitar N+1 queries</li>
 *   <li>Caché de consultas frecuentes</li>
 *   <li>Paginación en listados grandes</li>
 *   <li>Proyecciones específicas para diferentes vistas</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 2.0
 * @since 1.0
 * @see BookingService
 * @see Booking
 * @see BookingDetailResponse
 * @see BookingSummaryResponse
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImplement implements BookingService {

    // ========== DEPENDENCIAS INYECTADAS ==========

    /**
     * Repositorio para operaciones de persistencia de reservas.
     */
    private final BookingRepository bookingRepository;

    /**
     * Repositorio para validación y consulta de mascotas.
     */
    private final PetRepository petRepository;

    /**
     * Repositorio para validación y consulta de usuarios.
     */
    private final UserRepository userRepository;

    /**
     * Repositorio para consulta de ofertas de servicios.
     */
    private final ServiceOfferingRepository serviceOfferingRepository;

    /**
     * Servicio para cálculo y gestión de tarifas de plataforma.
     */
    private final PlatformFeeService platformFeeService;

    /**
     * Servicio para envío de notificaciones a usuarios.
     */
    private final NotificationService notificationService;

    /**
     * Repositorio para validación y consulta de cuentas de usuarios.
     */
    private final AccountUserRepository accountUserRepository;


    // ========== IMPLEMENTACIÓN DE MÉTODOS DE SERVICIO ==========

    /**
     * {@inheritDoc}
     *
     * <p><strong>Proceso de creación implementado:</strong></p>
     * <ol>
     *   <li>Validación exhaustiva de parámetros de entrada</li>
     *   <li>Verificación de existencia de entidades relacionadas</li>
     *   <li>Validación de reglas de negocio específicas</li>
     *   <li>Verificación de disponibilidad del cuidador</li>
     *   <li>Cálculo automático de campos derivados</li>
     *   <li>Persistencia transaccional de la reserva</li>
     *   <li>Cálculo y persistencia de tarifas de plataforma</li>
     *   <li>Envío de notificaciones a partes interesadas</li>
     *   <li>Construcción y retorno del DTO de respuesta</li>
     * </ol>
     *
     * <p><strong>Validaciones específicas aplicadas:</strong></p>
     * <ul>
     *   <li>La mascota debe existir y pertenecer al usuario o estar autorizada</li>
     *   <li>El cuidador debe existir, estar activo y tener el rol SITTER</li>
     *   <li>La oferta de servicio debe existir, estar activa y pertenecer al cuidador</li>
     *   <li>La fecha de inicio debe ser futura y dentro del horario del cuidador</li>
     *   <li>No debe existir conflicto de horarios con otras reservas</li>
     *   <li>El usuario no debe tener reservas pendientes excesivas</li>
     * </ul>
     *
     * <p><strong>Cálculos automáticos realizados:</strong></p>
     * <ul>
     *   <li>Fecha de finalización = fecha inicio + duración del servicio</li>
     *   <li>Precio base = precio de la oferta × duración en horas</li>
     *   <li>Tarifas de plataforma según estructura de comisiones</li>
     *   <li>Precio total final incluyendo todos los cargos</li>
     * </ul>
     *
     * @param createBookingRequest DTO validado con datos de entrada
     * @param currentUser Usuario autenticado obtenido del contexto de seguridad
     *
     * @return BookingDetailResponse con todos los datos de la reserva creada
     *
     * @throws IllegalArgumentException si alguna entidad referenciada no existe
     * @throws IllegalStateException si existen conflictos de horario o disponibilidad
     * @throws ValidationException si los datos no cumplen las reglas de negocio
     * @throws DataAccessException si ocurre error en la persistencia
     *
     * @since 1.0
     */
    @Override
    @Transactional
    public BookingDetailResponse createBooking(CreateBookingRequest createBookingRequest, User currentUser) {
        log.info("Iniciando creación de reserva para usuario ID: {} y mascota ID: {}",
                currentUser.getId(), createBookingRequest.getPetId());

        // 1. Validación inicial de parámetros
        validateCreateBookingRequest(createBookingRequest, currentUser);

        // 2. Obtención y validación de entidades relacionadas
        Pet pet = findAndValidatePet(createBookingRequest.getPetId(), currentUser);
        User sitter = findAndValidateSitter(createBookingRequest.getSitterId());
        ServiceOffering serviceOffering = findAndValidateServiceOffering(
                createBookingRequest.getServiceOfferingId(), sitter);

        // 3. Validación de reglas de negocio específicas
        validateBusinessRules(createBookingRequest, pet, sitter, serviceOffering, currentUser);

        // 4. Cálculo de campos derivados
        BookingCalculations calculations = calculateBookingDetails(
                createBookingRequest.getStartTime(), serviceOffering);

        // 5. Creación y configuración de la entidad Booking
        Booking newBooking = createBookingEntity(
                pet, sitter, serviceOffering, currentUser,
                createBookingRequest, calculations);

        // 6. Persistencia transaccional
        Booking savedBooking = bookingRepository.save(newBooking);
        log.info("Reserva creada exitosamente con ID: {}", savedBooking.getId());

        // 7. Procesamiento post-creación
        processPostCreationTasks(savedBooking);

        // 8. Construcción y retorno de la respuesta
        return BookingDetailResponse.fromEntity(savedBooking);
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Optimizaciones implementadas:</strong></p>
     * <ul>
     *   <li>Proyección específica para datos resumidos</li>
     *   <li>Consulta paginada para mejorar rendimiento</li>
     *   <li>Ordenamiento por defecto por fecha de creación descendente</li>
     *   <li>Eager fetching selectivo de relaciones necesarias</li>
     * </ul>
     *
     * <p>La paginación permite manejar eficientemente grandes volúmenes
     * de reservas sin impacto en el rendimiento del sistema.</p>
     *
     * @param pageable Parámetros de paginación y ordenamiento configurados desde el controlador
     *
     * @return Un {@link Page} de {@link BookingSummaryResponse} que contiene
     * reservas resumidas y metadatos de paginación.
     *
     * @since 1.0
     */
    @Override
    public Page<BookingSummaryResponse> getAllBookings(Pageable pageable) {
        log.debug("Consultando todas las reservas con paginación: {}", pageable);

        Page<Booking> bookingsPage = bookingRepository.findAllWithBasicInfo(pageable);

        return bookingsPage.map(BookingSummaryResponse::fromEntity);
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Optimización de consulta:</strong></p>
     * <ul>
     *   <li>Eager fetching de relaciones necesarias para el DTO completo</li>
     *   <li>Una sola consulta SQL para evitar lazy loading exceptions</li>
     *   <li>Validación de existencia antes del mapeo</li>
     * </ul>
     *
     * @param id Identificador único de la reserva solicitada
     *
     * @return BookingDetailResponse con información completa de la reserva
     *
     * @throws IllegalArgumentException si no existe reserva con el ID proporcionado
     *
     * @since 1.0
     */
    @Override
    public BookingDetailResponse getBookingById(Long id) {
        log.debug("Consultando reserva por ID: {}", id);

        Booking booking = bookingRepository.findByIdWithAllRelations(id)
                .orElseThrow(() -> {
                    log.warn("Intento de consulta de reserva inexistente con ID: {}", id);
                    return new IllegalArgumentException("Reserva no encontrada con ID: " + id);
                });

        return BookingDetailResponse.fromEntity(booking);
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Proceso de actualización implementado:</strong></p>
     * <ol>
     *   <li>Validación de existencia de la reserva</li>
     *   <li>Verificación de permisos de modificación</li>
     *   <li>Validación de transiciones de estado permitidas</li>
     *   <li>Aplicación de cambios con validación de reglas de negocio</li>
     *   <li>Recálculo de campos dependientes si es necesario</li>
     *   <li>Persistencia transaccional de los cambios</li>
     *   <li>Notificación de cambios a partes interesadas</li>
     *   <li>Construcción de respuesta con datos actualizados</li>
     * </ol>
     *
     * <p><strong>Campos actualizables por estado:</strong></p>
     * <ul>
     *   <li>PENDING: startTime, endTime, notes, sitter (con validaciones)</li>
     *   <li>CONFIRMED: notes, actualStartTime (al iniciar servicio)</li>
     *   <li>IN_PROGRESS: actualEndTime, notes</li>
     *   <li>COMPLETED/CANCELLED: Solo campos de auditoría por administradores</li>
     * </ul>
     *
     * <p><strong>Validaciones específicas:</strong></p>
     * <ul>
     *   <li>No modificar reservas en estados finales sin permisos admin</li>
     *   <li>Cambios de horario deben verificar disponibilidad</li>
     *   <li>Cambios de cuidador requieren confirmación del nuevo sitter</li>
     *   <li>Modificaciones de precio requieren autorización especial</li>
     * </ul>
     *
     * @param id Identificador de la reserva a actualizar
     * @param updateRequest DTO con los nuevos datos validados
     *
     * @return BookingDetailResponse con la reserva actualizada
     *
     * @throws IllegalArgumentException si la reserva no existe o datos inválidos
     * @throws IllegalStateException si la reserva no puede modificarse en su estado actual
     * @throws SecurityException si el usuario no tiene permisos para la modificación
     * @throws ValidationException si los nuevos datos violan reglas de negocio
     *
     * @since 2.0
     */
    @Override
    @Transactional
    public BookingDetailResponse updateBooking(Long id, UpdateBookingRequest updateRequest) {
        log.info("Iniciando actualización de reserva ID: {}", id);

        // 1. Obtener reserva existente con todas las relaciones
        Booking existingBooking = bookingRepository.findByIdWithAllRelations(id)
                .orElseThrow(() -> {
                    log.warn("Intento de actualización de reserva inexistente con ID: {}", id);
                    return new IllegalArgumentException("Reserva no encontrada con ID: " + id);
                });

        // 2. Validar que la reserva puede ser modificada
        validateUpdatePermissions(existingBooking, updateRequest);

        // 3. Aplicar cambios validados
        applyValidatedUpdates(existingBooking, updateRequest);

        // 4. Recalcular campos dependientes si es necesario
        recalculateDerivedFields(existingBooking, updateRequest);

        // 5. Persistir cambios
        Booking updatedBooking = bookingRepository.save(existingBooking);

        // 6. Procesamiento post-actualización
        processPostUpdateTasks(updatedBooking, updateRequest);

        log.info("Reserva ID: {} actualizada exitosamente", id);
        return BookingDetailResponse.fromEntity(updatedBooking);
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Proceso de eliminación implementado:</strong></p>
     * <ol>
     *   <li>Validación de existencia de la reserva</li>
     *   <li>Verificación de permisos de eliminación</li>
     *   <li>Evaluación del tipo de eliminación según estado</li>
     *   <li>Procesamiento de efectos secundarios (pagos, notificaciones)</li>
     *   <li>Eliminación física o lógica según reglas de negocio</li>
     *   <li>Limpieza de datos relacionados</li>
     *   <li>Auditoría de la operación</li>
     * </ol>
     *
     * <p><strong>Tipos de eliminación por estado:</strong></p>
     * <ul>
     *   <li>PENDING: Eliminación física directa</li>
     *   <li>CONFIRMED: Cancelación automática antes de eliminar</li>
     *   <li>IN_PROGRESS: Error - debe completarse primero</li>
     *   <li>COMPLETED: Eliminación lógica para preservar historial</li>
     *   <li>CANCELLED: Eliminación física después del período de gracia</li>
     * </ul>
     *
     * <p><strong>Efectos secundarios gestionados:</strong></p>
     * <ul>
     *   <li>Liberación de slots de disponibilidad del cuidador</li>
     *   <li>Procesamiento de reembolsos si corresponde</li>
     *   <li>Notificaciones de cancelación a partes involucradas</li>
     *   <li>Actualización de métricas y estadísticas</li>
     *   <li>Limpieza de datos temporales relacionados</li>
     * </ul>
     *
     * @param id Identificador único de la reserva a eliminar
     *
     * @throws IllegalArgumentException si no existe reserva con el ID especificado
     * @throws IllegalStateException si la reserva no puede eliminarse en su estado actual
     * @throws SecurityException si el usuario no tiene permisos para eliminar la reserva
     * @throws DataAccessException si ocurre error durante la eliminación
     *
     * @since 1.0
     */
    @Override
    @Transactional
    public void deleteBooking(Long id) {
        log.info("Iniciando eliminación de reserva ID: {}", id);

        // 1. Obtener reserva existente
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Intento de eliminación de reserva inexistente con ID: {}", id);
                    return new IllegalArgumentException("Reserva no encontrada con ID: " + id);
                });

        // 2. Validar que puede ser eliminada
        validateDeletionRules(booking);

        // 3. Procesar efectos secundarios antes de eliminar
        processPreDeletionTasks(booking);

        // 4. Realizar eliminación según el tipo determinado
        performDeletion(booking);

        log.info("Reserva ID: {} eliminada exitosamente", id);
    }

    /**
     * Obtiene reservas filtradas por usuario, rol y estado con paginación.
     *
     * <p>Este método permite consultar reservas desde diferentes perspectivas
     * según el rol del usuario consultante, aplicando filtros dinámicos.</p>
     *
     * @param userId ID del usuario para filtrar
     * @param role Rol desde el cual consultar ("CLIENT" o "SITTER")
     * @param status Estado opcional para filtrar
     * @param pageable Configuración de paginación
     *
     * @return Un {@link Page} de {@link BookingSummaryResponse} que contiene
     * reservas filtradas por usuario.
     *
     * @since 2.0
     */
    @Override
    public Page<BookingSummaryResponse> getBookingsByUser(Long userId, String role, String status, Pageable pageable) {
        log.debug("Consultando reservas para usuario ID: {}, rol: {}, estado: {}", userId, role, status);

        Page<Booking> bookingsPage;

        if ("SITTER".equalsIgnoreCase(role)) {
            bookingsPage = status != null ?
                    bookingRepository.findBySitterIdAndStatus(userId, BookingStatus.valueOf(status.toUpperCase()), pageable) :
                    bookingRepository.findBySitterId(userId, pageable);
        } else {
            bookingsPage = status != null ?
                    bookingRepository.findByBookedByUserIdAndStatus(userId, BookingStatus.valueOf(status.toUpperCase()), pageable) :
                    bookingRepository.findByBookedByUserId(userId, pageable);
        }

        return bookingsPage.map(BookingSummaryResponse::fromEntity);
    }

    /**
     * Actualiza el estado de una reserva siguiendo las reglas de transición del workflow.
     *
     * <p>Gestiona los cambios de estado validando transiciones permitidas,
     * permisos de usuario y requisitos específicos de cada transición.</p>
     *
     * @param id ID de la reserva
     * @param newStatus Nuevo estado solicitado
     * @param reason Motivo del cambio (obligatorio para cancelaciones)
     *
     * @return BookingDetailResponse con la reserva actualizada
     *
     * @since 2.0
     */
    @Override
    @Transactional
    public BookingDetailResponse updateBookingStatus(Long id, String newStatus, String reason) {
        log.info("Actualizando estado de reserva ID: {} a estado: {}", id, newStatus);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + id));

        BookingStatus targetStatus = BookingStatus.valueOf(newStatus.toUpperCase());

        // Validar transición
        validateStatusTransition(booking.getStatus(), targetStatus);

        // Aplicar cambio de estado
        booking.setStatus(targetStatus);

        // Manejar campos específicos según el nuevo estado
        handleStatusSpecificFields(booking, targetStatus, reason);

        Booking updatedBooking = bookingRepository.save(booking);

        // Notificar cambio de estado
        notificationService.notifyStatusChange(updatedBooking);

        return BookingDetailResponse.fromEntity(updatedBooking);
    }

    // ========== MÉTODOS PRIVADOS DE VALIDACIÓN ==========

    /**
     * Valida los parámetros básicos del request de creación.
     *
     * @param request DTO con datos de entrada
     * @param currentUser Usuario autenticado
     *
     * @throws IllegalArgumentException si algún parámetro es inválido
     */
    private void validateCreateBookingRequest(CreateBookingRequest request, User currentUser) {
        Objects.requireNonNull(request, "El request de creación no puede ser null");
        Objects.requireNonNull(currentUser, "El usuario actual no puede ser null");

        if (request.getStartTime().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new IllegalArgumentException("La reserva debe programarse con al menos 1 hora de anticipación");
        }
    }

    /**
     * Encuentra y valida que la mascota existe y pertenece al usuario autorizado.
     *
     * @param petId ID de la mascota
     * @param currentUser Usuario que crea la reserva
     *
     * @return Pet validada
     *
     * @throws IllegalArgumentException si la mascota no existe o no está autorizada
     */
    private Pet findAndValidatePet(Long petId, User currentUser) {
        // 1. Encontrar la mascota
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada con ID: " + petId));

        // 2. Obtener el ID de la cuenta de la mascota
        Long petAccountId = pet.getAccount().getId();

        // 3. Verificar si el usuario actual es miembro de esa cuenta
        boolean isUserMemberOfAccount = accountUserRepository
                .existsByAccountIdAndUserId(petAccountId, currentUser.getId());

        if (!isUserMemberOfAccount) {
            throw new SecurityException("No tiene autorización para crear reservas para esta mascota.");
        }

        return pet;
    }

    /**
     * Encuentra y valida que el cuidador existe, está activo y tiene el rol correcto.
     *
     * @param sitterId ID del cuidador
     *
     * @return User cuidador validado
     *
     * @throws IllegalArgumentException si el cuidador no existe o no está disponible
     */
    private User findAndValidateSitter(Long sitterId) {
        User sitter = userRepository.findById(sitterId)
                .orElseThrow(() -> new IllegalArgumentException("Cuidador no encontrado con ID: " + sitterId));

        // Comprobación directa del rol (más clara y explícita)
        if (sitter.getRole() != Role.SITTER) {
            throw new IllegalArgumentException("El usuario con ID " + sitterId + " no tiene el rol de cuidador.");
        }

        if (!sitter.isActive()) {
            throw new IllegalArgumentException("El cuidador no está activo y no puede recibir nuevas reservas.");
        }

        return sitter;
    }

    /**
     * Encuentra y valida la oferta de servicio y su relación con el cuidador.
     *
     * @param serviceOfferingId ID de la oferta de servicio
     * @param sitter Cuidador validado
     *
     * @return ServiceOffering validada
     *
     * @throws IllegalArgumentException si la oferta no existe o no pertenece al cuidador
     */
    private ServiceOffering findAndValidateServiceOffering(Long serviceOfferingId, User sitter) {
        ServiceOffering serviceOffering = serviceOfferingRepository.findById(serviceOfferingId)
                .orElseThrow(() -> new IllegalArgumentException("Oferta de servicio no encontrada con ID: " + serviceOfferingId));

        if (!serviceOffering.getSitterId().equals(sitter.getId())) {
            throw new IllegalArgumentException("La oferta de servicio no pertenece al cuidador especificado");
        }

        if (!serviceOffering.isActive()) {
            throw new IllegalArgumentException("La oferta de servicio no está disponible");
        }

        return serviceOffering;
    }

    /**
     * Valida reglas de negocio complejas para la creación de reservas.
     *
     * @param request Datos de la nueva reserva
     * @param pet Mascota validada
     * @param sitter Cuidador validado
     * @param serviceOffering Servicio validado
     * @param currentUser Usuario que crea la reserva
     *
     * @throws IllegalStateException si se violan reglas de negocio
     */
    private void validateBusinessRules(CreateBookingRequest request, Pet pet, User sitter,
                                       ServiceOffering serviceOffering, User currentUser) {
        // Verificar disponibilidad del cuidador
        if (hasScheduleConflict(sitter, request.getStartTime(), serviceOffering)) {
            throw new IllegalStateException("El cuidador no tiene disponibilidad en el horario solicitado");
        }

        // Verificar límites del usuario
        long pendingBookings = bookingRepository.countByBookedByUserAndStatus(currentUser, BookingStatus.PENDING);
        if (pendingBookings >= 5) {
            throw new IllegalStateException("Ha alcanzado el límite máximo de reservas pendientes");
        }
    }

    /**
     * Verifica si existe conflicto de horario para el cuidador.
     *
     * @param sitter Cuidador a verificar
     * @param startTime Hora de inicio solicitada
     * @param serviceOffering Servicio que define la duración
     *
     * @return true si existe conflicto
     */
    private boolean hasScheduleConflict(User sitter, LocalDateTime startTime, ServiceOffering serviceOffering) {
        LocalDateTime endTime = startTime.plusMinutes(serviceOffering.getDurationInMinutes());

        return bookingRepository.existsConflictingBooking(sitter.getId(), startTime, endTime);
    }

    /**
     * Calcula todos los campos derivados para una nueva reserva.
     *
     * @param startTime Hora de inicio
     * @param serviceOffering Servicio contratado
     *
     * @return Objeto con todos los cálculos
     */
    private BookingCalculations calculateBookingDetails(LocalDateTime startTime, ServiceOffering serviceOffering) {
        LocalDateTime endTime = startTime.plusMinutes(serviceOffering.getDurationInMinutes());
        BigDecimal totalPrice = serviceOffering.getPrice();

        return new BookingCalculations(endTime, totalPrice);
    }

    /**
     * Crea la entidad Booking con todos los datos calculados.
     *
     * @param pet Mascota del servicio
     * @param sitter Cuidador asignado
     * @param serviceOffering Servicio contratado
     * @param currentUser Usuario que crea la reserva
     * @param request Datos de entrada
     * @param calculations Campos calculados
     *
     * @return Entidad Booking configurada
     */
    private Booking createBookingEntity(Pet pet, User sitter, ServiceOffering serviceOffering,
                                        User currentUser, CreateBookingRequest request,
                                        BookingCalculations calculations) {
        return new Booking(
                pet,
                sitter,
                serviceOffering,
                currentUser,
                request.getStartTime(),
                calculations.getEndTime(),
                calculations.getTotalPrice(),
                request.getNotes()
        );
    }

    /**
     * Procesa tareas posteriores a la creación de la reserva.
     *
     * @param savedBooking Reserva persistida
     */
    private void processPostCreationTasks(Booking savedBooking) {
        platformFeeService.calculateAndCreatePlatformFee(savedBooking);

        notificationService.notifyNewBookingCreated(savedBooking);

    }

    // ========== MÉTODOS DE VALIDACIÓN PARA ACTUALIZACIÓN ==========

    /**
     * Valida permisos y reglas para actualizar una reserva.
     */
    private void validateUpdatePermissions(Booking booking, UpdateBookingRequest request) {
        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("No se pueden modificar reservas en estado " + booking.getStatus());
        }

    }

    /**
     * Aplica los cambios validados a la entidad booking.
     */
    private void applyValidatedUpdates(Booking booking, UpdateBookingRequest request) {
        if (request.getStartTime() != null && !request.getStartTime().equals(booking.getStartTime())) {
            booking.setStartTime(request.getStartTime());
        }

        if (request.getNotes() != null) {
            booking.setNotes(request.getNotes());
        }

    }

    /**
     * Recalcula campos que dependen de otros campos modificados.
     */
    private void recalculateDerivedFields(Booking booking, UpdateBookingRequest request) {
        if (request.getStartTime() != null) {
            // Recalcular endTime si cambió startTime
            Integer durationMinutes = booking.getServiceOffering().getDurationInMinutes();
            booking.setEndTime(booking.getStartTime().plusMinutes(durationMinutes));
        }
    }

    /**
     * Procesa tareas posteriores a la actualización.
     */
    private void processPostUpdateTasks(Booking booking, UpdateBookingRequest request) {
        notificationService.notifyBookingUpdated(booking);

        if (request.getTotalPrice() != null) {
            platformFeeService.recalculatePlatformFee(booking);
        }
    }

    // ========== MÉTODOS DE VALIDACIÓN PARA ELIMINACIÓN ==========

    /**
     * Valida que una reserva puede ser eliminada según las reglas de negocio.
     */
    private void validateDeletionRules(Booking booking) {
        if (booking.getStatus() == BookingStatus.IN_PROGRESS) {
            throw new IllegalStateException("No se puede eliminar una reserva en progreso");
        }

    }

    /**
     * Procesa efectos secundarios antes de eliminar la reserva.
     */
    private void processPreDeletionTasks(Booking booking) {

        notificationService.notifyBookingCancelled(booking);
    }

    /**
     * Realiza la eliminación física o lógica según las reglas.
     */
    private void performDeletion(Booking booking) {
        if (shouldUseLogicalDeletion(booking)) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancellationReason("Eliminada por usuario");
            bookingRepository.save(booking);
        } else {
            bookingRepository.delete(booking);
        }
    }

    /**
     * Determina si debe usarse eliminación lógica en lugar de física.
     */
    private boolean shouldUseLogicalDeletion(Booking booking) {
        return booking.getStatus() == BookingStatus.COMPLETED ||
                booking.getCreatedAt().isBefore(LocalDateTime.now().minusDays(30));
    }

    /**
     * Valida que una transición de estado es permitida.
     */
    private void validateStatusTransition(BookingStatus currentStatus, BookingStatus newStatus) {

        boolean validTransition = switch (currentStatus) {
            case PENDING -> newStatus == BookingStatus.CONFIRMED || newStatus == BookingStatus.CANCELLED;
            case CONFIRMED -> newStatus == BookingStatus.IN_PROGRESS || newStatus == BookingStatus.CANCELLED;
            case IN_PROGRESS -> newStatus == BookingStatus.COMPLETED || newStatus == BookingStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false; // Estados finales
        };

        if (!validTransition) {
            throw new IllegalStateException(
                    String.format("Transición no válida de %s a %s", currentStatus, newStatus));
        }
    }

    /**
     * Maneja campos específicos según el nuevo estado.
     */
    private void handleStatusSpecificFields(Booking booking, BookingStatus newStatus, String reason) {
        switch (newStatus) {
            case CANCELLED:
                if (reason == null || reason.trim().isEmpty()) {
                    throw new IllegalArgumentException("El motivo de cancelación es obligatorio");
                }
                booking.setCancellationReason(reason);
                break;
            case IN_PROGRESS:
                booking.setActualStartTime(LocalDateTime.now());
                break;
            case COMPLETED:
                booking.setActualEndTime(LocalDateTime.now());
                break;
        }
    }

    // ========== CLASE INTERNA PARA CÁLCULOS ==========

    /**
     * Clase interna para encapsular los resultados de cálculos de reserva.
     */
    private static class BookingCalculations {
        private final LocalDateTime endTime;
        private final BigDecimal totalPrice;

        public BookingCalculations(LocalDateTime endTime, BigDecimal totalPrice) {
            this.endTime = endTime;
            this.totalPrice = totalPrice;
        }

        public LocalDateTime getEndTime() { return endTime; }
        public BigDecimal getTotalPrice() { return totalPrice; }
    }
}