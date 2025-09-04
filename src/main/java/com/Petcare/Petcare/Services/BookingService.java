package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.DTOs.Booking.BookingDetailResponse;
import com.Petcare.Petcare.DTOs.Booking.BookingSummaryResponse;
import com.Petcare.Petcare.DTOs.Booking.CreateBookingRequest;
import com.Petcare.Petcare.DTOs.Booking.UpdateBookingRequest;
import com.Petcare.Petcare.Models.User.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

/**
 * Interfaz para el servicio de gestión de reservas (Bookings).
 * Define el contrato completo para las operaciones de negocio relacionadas con las reservas.
 *
 * <p><strong>Responsabilidades del servicio:</strong></p>
 * <ul>
 *   <li>Gestión completa del ciclo de vida de las reservas</li>
 *   <li>Validación de reglas de negocio complejas</li>
 *   <li>Cálculos automáticos de precios y tiempos</li>
 *   <li>Control de disponibilidad y prevención de conflictos</li>
 *   <li>Integración con servicios de notificaciones y pagos</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 2.0
 * @since 1.0
 */
public interface BookingService {

    /**
     * Crea una nueva reserva en el sistema con validaciones completas.
     *
     * <p>Realiza todas las validaciones necesarias, cálculos automáticos
     * y coordinación con otros servicios del sistema.</p>
     *
     * @param createBookingRequest DTO con los datos de entrada para la nueva reserva
     * @param currentUser El usuario autenticado que está realizando la reserva
     *
     * @return BookingDetailResponse con los detalles de la reserva recién creada
     *
     * @throws IllegalArgumentException si los datos de entrada son inválidos
     * @throws IllegalStateException si existe conflicto de disponibilidad
     * @throws SecurityException si el usuario no tiene permisos
     */
    BookingDetailResponse createBooking(CreateBookingRequest createBookingRequest, Authentication authentication);

    /**
     * Obtiene todas las reservas con paginación y información resumida.
     *
     * @param pageable Configuración de paginación y ordenamiento
     *
     * @return Page con reservas resumidas y metadatos de paginación
     */
    Page<BookingSummaryResponse> getAllBookings(Pageable pageable);

    /**
     * Obtiene una reserva específica por su ID con información completa.
     *
     * @param id Identificador único de la reserva
     *
     * @return BookingDetailResponse con información detallada
     *
     * @throws IllegalArgumentException si no existe la reserva
     */
    BookingDetailResponse getBookingById(Long id);

    /**
     * Actualiza los campos modificables de una reserva existente.
     *
     * @param id Identificador de la reserva a actualizar
     * @param updateRequest DTO con los nuevos datos
     *
     * @return BookingDetailResponse con la reserva actualizada
     *
     * @throws IllegalArgumentException si la reserva no existe o datos inválidos
     * @throws IllegalStateException si no se puede modificar en el estado actual
     */
    BookingDetailResponse updateBooking(Long id, UpdateBookingRequest updateRequest);

    /**
     * Elimina una reserva del sistema siguiendo las reglas de negocio.
     *
     * @param id Identificador de la reserva a eliminar
     *
     * @throws IllegalArgumentException si no existe la reserva
     * @throws IllegalStateException si no se puede eliminar en el estado actual
     */
    void deleteBooking(Long id);

    /**
     * Obtiene reservas filtradas por usuario, rol y estado.
     *
     * @param userId ID del usuario para filtrar
     * @param role Rol desde el cual consultar ("CLIENT" o "SITTER")
     * @param status Estado opcional para filtrar
     * @param pageable Configuración de paginación
     *
     * @return Page con reservas filtradas
     */
    Page<BookingSummaryResponse> getBookingsByUser(Long userId, String role, String status, Pageable pageable);

    /**
     * Actualiza el estado de una reserva siguiendo el workflow definido.
     *
     * @param id ID de la reserva
     * @param newStatus Nuevo estado solicitado
     * @param reason Motivo del cambio (obligatorio para cancelaciones)
     *
     * @return BookingDetailResponse con la reserva actualizada
     *
     * @throws IllegalStateException si la transición no es válida
     */
    BookingDetailResponse updateBookingStatus(Long id, String newStatus, String reason);
}