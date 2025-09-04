package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.Booking.BookingDetailResponse;
import com.Petcare.Petcare.DTOs.Booking.BookingSummaryResponse;
import com.Petcare.Petcare.DTOs.Booking.CreateBookingRequest;

import com.Petcare.Petcare.DTOs.Booking.UpdateBookingRequest;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Services.BookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión completa de reservas de servicios de cuidado de mascotas.
 *
 * <p>Este controlador expone endpoints RESTful para realizar operaciones CRUD sobre reservas,
 * implementando las mejores prácticas de seguridad, validación y manejo de respuestas HTTP.</p>
 *
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li>Creación de nuevas reservas con validación completa</li>
 *   <li>Consulta de reservas con paginación y filtros</li>
 *   <li>Actualización de reservas existentes</li>
 *   <li>Eliminación segura de reservas</li>
 *   <li>Consultas específicas por usuario y estado</li>
 * </ul>
 *
 * <p><strong>Seguridad implementada:</strong></p>
 * <ul>
 *   <li>Autenticación requerida para todos los endpoints</li>
 *   <li>Autorización basada en roles y propiedad de recursos</li>
 *   <li>Validación de entrada usando Bean Validation</li>
 *   <li>Manejo seguro de excepciones</li>
 * </ul>
 *
 * <p><strong>Códigos de respuesta HTTP:</strong></p>
 * <ul>
 *   <li>200 OK - Operación exitosa</li>
 *   <li>201 Created - Reserva creada correctamente</li>
 *   <li>204 No Content - Eliminación exitosa</li>
 *   <li>400 Bad Request - Datos de entrada inválidos</li>
 *   <li>403 Forbidden - Sin permisos para la operación</li>
 *   <li>404 Not Found - Recurso no encontrado</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 2.0
 * @since 1.0
 * @see BookingService
 * @see BookingDetailResponse
 * @see BookingSummaryResponse
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Operaciones relacionadas con reservas")
public class BookingController {

    private final BookingService bookingService;

    /**
     * Crea una nueva reserva de servicio de cuidado de mascotas.
     *
     * <p>Este endpoint permite a los usuarios autenticados crear una nueva reserva
     * proporcionando los datos necesarios del servicio, mascota y cuidador deseado.</p>
     *
     * <p><strong>Validaciones aplicadas:</strong></p>
     * <ul>
     *   <li>Usuario debe estar autenticado</li>
     *   <li>Todos los campos requeridos deben estar presentes</li>
     *   <li>La mascota debe pertenecer al usuario o estar autorizada</li>
     *   <li>El cuidador debe tener disponibilidad en las fechas solicitadas</li>
     *   <li>La oferta de servicio debe estar activa y pertenecer al cuidador</li>
     * </ul>
     *
     * <p><strong>Cálculos automáticos realizados:</strong></p>
     * <ul>
     *   <li>Fecha de finalización basada en la duración del servicio</li>
     *   <li>Precio total según tarifas de la oferta</li>
     *   <li>Asignación del usuario autenticado como creador</li>
     *   <li>Estado inicial PENDING</li>
     * </ul>
     *
     * @param request DTO con los datos necesarios para crear la reserva.
     *                Incluye petId, sitterId, serviceOfferingId, startTime y notes opcionales.
     * @param currentUser Usuario autenticado obtenido del contexto de seguridad.
     *                    Se inyecta automáticamente desde el token JWT.
     *
     * @return ResponseEntity con BookingDetailResponse conteniendo todos los detalles
     *         de la reserva creada, incluyendo IDs generados y campos calculados.
     *         Estado HTTP 201 Created.
     *
     * @throws IllegalArgumentException si alguna de las entidades referenciadas no existe
     * @throws IllegalStateException si el cuidador no tiene disponibilidad
     * @throws ValidationException si los datos de entrada no son válidos
     *
     * @apiNote El endpoint calcula automáticamente el endTime y totalPrice,
     *          por lo que no deben incluirse en el request.
     *
     * @since 1.0
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDetailResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication
    ) {
        BookingDetailResponse newBooking = bookingService.createBooking(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);
    }

    /**
     * Obtiene una lista paginada de todas las reservas con información resumida.
     *
     * <p>Este endpoint proporciona una vista optimizada para listados, mostrando
     * solo la información esencial de cada reserva para mejorar el rendimiento
     * y la experiencia de usuario en interfaces con muchas reservas.</p>
     *
     * <p><strong>Características de la paginación:</strong></p>
     * <ul>
     *   <li>Tamaño de página configurable (por defecto 20)</li>
     *   <li>Ordenamiento por fecha de creación descendente</li>
     *   <li>Metadatos de paginación incluidos en la respuesta</li>
     * </ul>
     *
     * <p><strong>Información incluida en el resumen:</strong></p>
     * <ul>
     *   <li>ID de la reserva</li>
     *   <li>Nombre de la mascota</li>
     *   <li>Nombre del cuidador</li>
     *   <li>Fecha y hora de inicio</li>
     *   <li>Estado actual</li>
     *   <li>Precio total</li>
     * </ul>
     *
     * @param pageable Parámetros de paginación y ordenamiento.
     *
     * @return Un {@link ResponseEntity} que contiene una {@link Page} de
     * {@link BookingSummaryResponse} con la lista de reservas en formato resumido.
     * Estado HTTP 200 OK.
     *
     * @apiNote Para obtener detalles completos de una reserva específica,
     * usar el endpoint GET /api/bookings/{id}
     *
     * @since 2.0
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<BookingSummaryResponse>> getAllBookings(Pageable pageable) {
        Page<BookingSummaryResponse> bookings = bookingService.getAllBookings(pageable);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Obtiene los detalles completos de una reserva específica por su identificador.
     *
     * <p>Este endpoint proporciona toda la información disponible sobre una reserva,
     * incluyendo datos relacionales denormalizados para optimizar la presentación
     * en interfaces de detalle.</p>
     *
     * <p><strong>Información completa incluida:</strong></p>
     * <ul>
     *   <li>Todos los campos de la reserva</li>
     *   <li>Información detallada de la mascota</li>
     *   <li>Datos completos del cuidador</li>
     *   <li>Detalles del servicio contratado</li>
     *   <li>Información del usuario que creó la reserva</li>
     *   <li>Tiempos programados y reales</li>
     *   <li>Tarifas de plataforma calculadas</li>
     *   <li>Metadatos de auditoría</li>
     * </ul>
     *
     * <p><strong>Casos de uso típicos:</strong></p>
     * <ul>
     *   <li>Vista de detalle de reserva</li>
     *   <li>Confirmación después de crear/actualizar</li>
     *   <li>Información para seguimiento del servicio</li>
     *   <li>Datos para facturación detallada</li>
     * </ul>
     *
     * @param id Identificador único de la reserva a consultar.
     *           Debe ser un valor numérico positivo.
     *
     * @return ResponseEntity con BookingDetailResponse conteniendo toda la información
     *         de la reserva solicitada. Estado HTTP 200 OK.
     *
     * @throws IllegalArgumentException si no existe una reserva con el ID especificado
     *
     * @apiNote La respuesta incluye datos denormalizados para evitar consultas adicionales
     *          desde el frontend, optimizando así la experiencia de usuario.
     *
     * @since 1.0
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDetailResponse> getBookingById(@PathVariable Long id) {
        BookingDetailResponse booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    /**
     * Actualiza los datos modificables de una reserva existente.
     *
     * <p>Este endpoint permite modificar ciertos aspectos de una reserva, respetando
     * las reglas de negocio y el estado actual de la misma. No todos los campos
     * son modificables en todos los estados.</p>
     *
     * <p><strong>Campos actualizables según el estado:</strong></p>
     * <ul>
     *   <li>PENDING: Todos los campos excepto IDs y datos calculados</li>
     *   <li>CONFIRMED: Solo notas y algunos campos administrativos</li>
     *   <li>IN_PROGRESS: Solo tiempos reales y notas</li>
     *   <li>COMPLETED/CANCELLED: Solo campos de auditoría por administradores</li>
     * </ul>
     *
     * <p><strong>Validaciones aplicadas:</strong></p>
     * <ul>
     *   <li>El usuario debe tener permisos para modificar la reserva</li>
     *   <li>La reserva debe estar en un estado que permita modificaciones</li>
     *   <li>Los nuevos valores deben cumplir las reglas de negocio</li>
     *   <li>No se pueden modificar referencias a entidades inexistentes</li>
     * </ul>
     *
     * <p><strong>Recálculos automáticos:</strong></p>
     * <ul>
     *   <li>Precio total si cambian datos que lo afectan</li>
     *   <li>Fecha de finalización si cambia la hora de inicio</li>
     *   <li>Estado si los cambios implican transiciones válidas</li>
     * </ul>
     *
     * @param id Identificador único de la reserva a actualizar.
     * @param request DTO con los nuevos datos para la reserva.
     *                Solo se procesarán los campos modificables según el estado.
     *
     * @return ResponseEntity con BookingDetailResponse conteniendo la reserva
     *         actualizada con todos sus detalles. Estado HTTP 200 OK.
     *
     * @throws IllegalArgumentException si no existe la reserva o los datos son inválidos
     * @throws IllegalStateException si la reserva no puede modificarse en su estado actual
     * @throws ValidationException si los nuevos datos no son válidos
     * @throws SecurityException si el usuario no tiene permisos para la modificación
     *
     * @apiNote Las modificaciones son atómicas. Si alguna validación falla,
     *          no se aplica ningún cambio.
     *
     * @since 2.0
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDetailResponse> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingRequest request
    ) {
        BookingDetailResponse updatedBooking = bookingService.updateBooking(id, request);
        return ResponseEntity.ok(updatedBooking);
    }

    /**
     * Elimina una reserva del sistema de forma segura.
     *
     * <p>Este endpoint implementa eliminación lógica o física según las reglas
     * de negocio y el estado de la reserva. Se considera la integridad referencial
     * y los requisitos de auditoría.</p>
     *
     * <p><strong>Reglas de eliminación por estado:</strong></p>
     * <ul>
     *   <li>PENDING: Eliminación física permitida</li>
     *   <li>CONFIRMED: Cambio a CANCELLED en lugar de eliminación</li>
     *   <li>IN_PROGRESS: No se permite eliminación, debe completarse</li>
     *   <li>COMPLETED: Solo eliminación lógica para auditoría</li>
     *   <li>CANCELLED: Eliminación física permitida después de período de gracia</li>
     * </ul>
     *
     * <p><strong>Validaciones de seguridad:</strong></p>
     * <ul>
     *   <li>Solo el creador o administradores pueden eliminar</li>
     *   <li>Se verifica que no existan dependencias críticas</li>
     *   <li>Se registra la acción para auditoría</li>
     * </ul>
     *
     * <p><strong>Efectos de la eliminación:</strong></p>
     * <ul>
     *   <li>Liberación de disponibilidad del cuidador</li>
     *   <li>Notificaciones a partes involucradas</li>
     *   <li>Reversión de cargos si aplica</li>
     *   <li>Limpieza de datos relacionados</li>
     * </ul>
     *
     * @param id Identificador único de la reserva a eliminar.
     *
     * @return ResponseEntity vacío con estado HTTP 204 No Content indicando
     *         que la operación se completó exitosamente.
     *
     * @throws IllegalArgumentException si no existe la reserva con el ID especificado
     * @throws IllegalStateException si la reserva no puede eliminarse en su estado actual
     * @throws SecurityException si el usuario no tiene permisos para eliminar la reserva
     *
     * @apiNote La eliminación puede ser asíncrona para operaciones complejas.
     *          En estos casos, se retorna 204 inmediatamente y el procesamiento
     *          continúa en segundo plano.
     *
     * @since 1.0
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene las reservas asociadas a un usuario específico con filtros opcionales.
     *
     * <p>Este endpoint permite consultar las reservas desde la perspectiva de
     * diferentes roles de usuario, aplicando filtros de estado y rango temporal.</p>
     *
     * <p><strong>Tipos de consulta soportados:</strong></p>
     * <ul>
     *   <li>Como cliente: reservas creadas por el usuario</li>
     *   <li>Como cuidador: reservas asignadas al usuario</li>
     *   <li>Por estado: filtrar por uno o más estados</li>
     *   <li>Por rango de fechas: servicios en período específico</li>
     * </ul>
     *
     * @param userId ID del usuario para filtrar reservas
     * @param role Rol desde el cual consultar ("CLIENT", "SITTER")
     * @param status Estado opcional para filtrar (PENDING, CONFIRMED, etc.)
     * @param pageable Parámetros de paginación
     *
     * @return Un {@link ResponseEntity} que contiene una {@link Page} de
     * {@link BookingSummaryResponse} con la lista de reservas filtradas.
     *
     * @since 2.0
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated() and (#userId == authentication.principal.id or hasRole('ADMIN'))")
    public ResponseEntity<Page<BookingSummaryResponse>> getBookingsByUser(
            @PathVariable Long userId,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) String status,
            Pageable pageable
    ) {
        Page<BookingSummaryResponse> bookings = bookingService.getBookingsByUser(userId, role, status, pageable);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Cambia el estado de una reserva siguiendo las transiciones válidas del workflow.
     *
     * <p>Este endpoint maneja los cambios de estado de las reservas asegurando
     * que se respeten las reglas de negocio y los permisos de cada rol.</p>
     *
     * <p><strong>Transiciones válidas:</strong></p>
     * <ul>
     *   <li>PENDING → CONFIRMED (por cuidador)</li>
     *   <li>CONFIRMED → IN_PROGRESS (por cuidador)</li>
     *   <li>IN_PROGRESS → COMPLETED (por cuidador)</li>
     *   <li>Cualquier estado → CANCELLED (por cliente o admin)</li>
     * </ul>
     *
     * @param id ID de la reserva
     * @param newStatus Nuevo estado solicitado
     * @param reason Motivo del cambio (requerido para cancelaciones)
     *
     * @return ResponseEntity con BookingDetailResponse actualizada
     *
     * @since 2.0
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDetailResponse> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam String newStatus,
            @RequestParam(required = false) String reason
    ) {
        BookingDetailResponse updatedBooking = bookingService.updateBookingStatus(id, newStatus, reason);
        return ResponseEntity.ok(updatedBooking);
    }
}