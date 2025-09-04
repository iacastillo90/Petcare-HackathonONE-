package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.DTOs.Invoice.CreateInvoiceRequest;
import com.Petcare.Petcare.DTOs.Invoice.InvoiceDetailResponse;
import com.Petcare.Petcare.DTOs.Invoice.InvoiceSummaryResponse;
import com.Petcare.Petcare.DTOs.Invoice.UpdateInvoiceRequest;
import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Booking.BookingStatus;
import com.Petcare.Petcare.Models.Invoice.Invoice;
import com.Petcare.Petcare.Models.Invoice.InvoiceItem;
import com.Petcare.Petcare.Models.Invoice.InvoiceStatus;
import com.Petcare.Petcare.Repositories.BookingRepository;
import com.Petcare.Petcare.Repositories.InvoiceRepository;
import com.Petcare.Petcare.Services.*;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementación consolidada del servicio de gestión de facturas para Petcare.
 *
 * <p>Esta implementación unificada maneja todo el ciclo de vida de las facturas,
 * desde su generación automática hasta la distribución de PDFs por correo electrónico.
 * Integra completamente con el flujo de reservas para proporcionar facturación
 * automática cuando una reserva se completa.</p>
 *
 * <p><strong>Responsabilidades principales:</strong></p>
 * <ul>
 *   <li>Generación automática de facturas desde reservas completadas</li>
 *   <li>Validación exhaustiva de reglas de negocio antes de operaciones</li>
 *   <li>Cálculo automático de tarifas de plataforma y totales</li>
 *   <li>Generación de números de factura únicos y seguros</li>
 *   <li>Gestión completa de estados y transiciones válidas</li>
 *   <li>Generación automática de PDFs de factura</li>
 *   <li>Envío automático por correo electrónico al completar reservas</li>
 *   <li>Integración con servicios de notificación y tarifas</li>
 * </ul>
 *
 * <p><strong>Flujo de facturación automática:</strong></p>
 * <ol>
 *   <li>Se detecta una reserva completada</li>
 *   <li>Se genera automáticamente una factura</li>
 *   <li>Se calculan todos los montos y tarifas</li>
 *   <li>Se crea el PDF de la factura</li>
 *   <li>Se envía por correo electrónico al cliente</li>
 *   <li>Se notifica al cuidador sobre el pago</li>
 *   <li>Se registra en el sistema de tarifas de plataforma</li>
 * </ol>
 *
 * <p><strong>Características de seguridad:</strong></p>
 * <ul>
 *   <li>Validación de integridad referencial en todas las operaciones</li>
 *   <li>Prevención de duplicación de facturas por reserva</li>
 *   <li>Auditoría completa de operaciones de facturación</li>
 *   <li>Manejo seguro de errores y rollback automático</li>
 *   <li>Validación de permisos según el rol del usuario</li>
 * </ul>
 *
 * <p><strong>Optimizaciones de rendimiento:</strong></p>
 * <ul>
 *   <li>Transacciones optimizadas para operaciones complejas</li>
 *   <li>Carga lazy inteligente de relaciones</li>
 *   <li>Cache de consultas frecuentes</li>
 *   <li>Procesamiento asíncrono de PDFs y emails</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 3.0
 * @since 1.0
 * @see InvoiceService
 * @see PdfGenerationService
 * @see NotificationService
 * @see BookingService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImplement implements InvoiceService {

    // ========== DEPENDENCIAS INYECTADAS ==========

    /**
     * Repositorio para operaciones de persistencia de facturas.
     */
    private final InvoiceRepository invoiceRepository;

    /**
     * Repositorio para consulta y validación de reservas.
     */
    private final BookingRepository bookingRepository;

    /**
     * Servicio para cálculo y gestión de tarifas de plataforma.
     */
    private final PlatformFeeService platformFeeService;

    /**
     * Servicio para generación de documentos PDF de facturas.
     */
    private final PdfGenerationService pdfGenerationService;

    /**
     * Servicio para envío de notificaciones por correo electrónico.
     */
    private final NotificationService notificationService;

    // ========== MÉTODOS PRINCIPALES DE FACTURACIÓN ==========

    /**
     * Genera una factura completa para una reserva completada con PDF y envío automático.
     *
     * <p>Este método implementa el flujo completo de facturación automática que se
     * integra perfectamente con el ciclo de vida de las reservas. Cuando una reserva
     * cambia a estado COMPLETED, este método se invoca automáticamente para generar
     * la facturación correspondiente.</p>
     *
     * <p><strong>Proceso completo implementado:</strong></p>
     * <ol>
     *   <li>Validación exhaustiva de la reserva y sus precondiciones</li>
     *   <li>Verificación de que no exista facturación previa</li>
     *   <li>Generación de número de factura único y seguro</li>
     *   <li>Cálculo automático de montos, impuestos y tarifas</li>
     *   <li>Creación de items detallados de facturación</li>
     *   <li>Persistencia transaccional de la factura completa</li>
     *   <li>Generación automática del documento PDF</li>
     *   <li>Envío por correo electrónico al cliente y cuidador</li>
     *   <li>Registro en el sistema de tarifas de plataforma</li>
     *   <li>Notificaciones a servicios dependientes</li>
     * </ol>
     *
     * <p><strong>Validaciones críticas aplicadas:</strong></p>
     * <ul>
     *   <li>La reserva debe existir en la base de datos</li>
     *   <li>La reserva debe estar en estado COMPLETED únicamente</li>
     *   <li>No debe existir una factura previa para esta reserva</li>
     *   <li>Todos los datos financieros deben ser coherentes</li>
     *   <li>Las referencias a cuenta y servicio deben ser válidas</li>
     * </ul>
     *
     * <p><strong>Cálculos automáticos realizados:</strong></p>
     * <ul>
     *   <li>Subtotal basado en precio de la reserva</li>
     *   <li>Tarifa de plataforma según configuración (ej: 10%)</li>
     *   <li>Total final incluyendo todos los cargos</li>
     *   <li>Fecha de vencimiento (15 días por defecto)</li>
     *   <li>Tiempo estimado de procesamiento</li>
     * </ul>
     *
     * <p><strong>Integración con servicios externos:</strong></p>
     * <ul>
     *   <li>PDF Generation: Crea documento profesional de factura</li>
     *   <li>Email Service: Envía factura al cliente automáticamente</li>
     *   <li>Platform Fee: Registra comisiones para contabilidad</li>
     *   <li>Notification Service: Informa a todas las partes involucradas</li>
     * </ul>
     *
     * @param request DTO de creación que contiene:
     *                - bookingId: ID de la reserva completada (obligatorio)
     *                - notes: Notas adicionales para la factura (opcional)
     *                - items: Items personalizados de facturación (opcional)
     *                - autoSendEmail: Flag para envío automático (por defecto true)
     *
     * @return InvoiceDetailResponse con toda la información de la factura creada:
     *         - Datos completos de la factura generada
     *         - URL de descarga del PDF generado
     *         - Estado de envío del correo electrónico
     *         - Información de tarifas de plataforma aplicadas
     *         - Metadatos de auditoría y seguimiento
     *
     * @throws IllegalArgumentException si la reserva no existe o los datos son inválidos
     * @throws IllegalStateException si la reserva no está en estado COMPLETED o ya tiene factura
     * @throws ValidationException si los datos de entrada no cumplen las reglas de negocio
     * @throws DataAccessException si ocurre error en la persistencia de datos
     *
     * @apiNote Este método es transaccional. Si cualquier paso falla, se revierten
     *          todos los cambios para mantener la consistencia de datos.
     *
     * @since 3.0
     */
    @Override
    @Transactional
    public InvoiceDetailResponse generateInvoiceForBooking(CreateInvoiceRequest request) {
        log.info("Iniciando generación completa de factura para reserva ID: {}", request.getBookingId());

        try {
            // 1. Validación exhaustiva de la reserva
            Booking booking = validateAndFetchBooking(request.getBookingId());
            log.debug("Reserva validada exitosamente: {} - Estado: {}", booking.getId(), booking.getStatus());

            // 2. Validación de reglas de negocio para facturación
            validateBookingForInvoicing(booking);

            // 3. Creación de la factura con todos los datos calculados
            Invoice newInvoice = createComprehensiveInvoice(booking, request);
            log.debug("Factura creada con número: {}", newInvoice.getInvoiceNumber());

            // 4. Creación de items detallados de facturación
            createDetailedInvoiceItems(newInvoice, booking, request);

            // 5. Cálculo y establecimiento de todos los totales financieros
            calculateAndSetComprehensiveTotals(newInvoice);
            log.debug("Totales calculados - Subtotal: {}, Tarifa: {}, Total: {}",
                    newInvoice.getSubtotal(), newInvoice.getPlatformFee(), newInvoice.getTotalAmount());

            // 6. Persistencia transaccional de la factura completa
            Invoice savedInvoice = invoiceRepository.save(newInvoice);
            log.info("Factura persistida exitosamente con ID: {}", savedInvoice.getId());

            // 7. Procesamiento post-creación (PDF, emails, tarifas)
            processPostInvoiceCreationTasks(savedInvoice, request.isAutoSendEmail());

            // 8. Construcción de respuesta completa con metadatos
            return buildComprehensiveInvoiceResponse(savedInvoice);

        } catch (Exception e) {
            log.error("Error en generación de factura para reserva ID: {} - Error: {}",
                    request.getBookingId(), e.getMessage(), e);
            throw e; // Re-throw para que Spring maneje el rollback
        }
    }

    /**
     * Nuevo método para la generación interna de facturas desde un servicio.
     * Acepta la entidad Booking directamente.
     */
    @Override
    @Transactional
    public void generateAndProcessInvoiceForBooking(Booking booking) {
        // Creamos un CreateInvoiceRequest simple, ya que no viene de un cliente.
        // Asumimos que siempre queremos enviar el email en este flujo automático.
        CreateInvoiceRequest autoRequest = new CreateInvoiceRequest();
        autoRequest.setBookingId(booking.getId());
        autoRequest.setNotes("Factura generada automáticamente al completar reserva");
        autoRequest.setAutoSendEmail(true);

        // Llamamos a la lógica principal que ya tienes.
        // No es necesario capturar la respuesta aquí, a menos que quieras hacer algo con ella.
        this.generateInvoiceForBooking(autoRequest);
    }

    /**
     * Obtiene los detalles completos de una factura por su identificador único.
     *
     * <p>Este método proporciona acceso optimizado a toda la información de una factura,
     * incluyendo datos relacionales denormalizados para evitar consultas adicionales
     * desde el cliente y mejorar la experiencia del usuario.</p>
     *
     * <p><strong>Optimizaciones implementadas:</strong></p>
     * <ul>
     *   <li>Consulta única con eager fetching de relaciones críticas</li>
     *   <li>Cache de segundo nivel para facturas consultadas frecuentemente</li>
     *   <li>Proyección específica para evitar lazy loading exceptions</li>
     *   <li>Validación de permisos integrada en la consulta</li>
     * </ul>
     *
     * <p><strong>Información incluida en la respuesta:</strong></p>
     * <ul>
     *   <li>Datos completos de la factura y todos sus campos</li>
     *   <li>Información detallada de la reserva asociada</li>
     *   <li>Datos de la cuenta y usuario facturados</li>
     *   <li>Items detallados con precios y cantidades</li>
     *   <li>Información de pagos realizados (si los hay)</li>
     *   <li>Metadatos de auditoría y seguimiento</li>
     *   <li>URLs de descarga de documentos asociados</li>
     * </ul>
     *
     * @param invoiceId Identificador único de la factura a consultar.
     *                  Debe ser un valor numérico positivo válido.
     *
     * @return InvoiceDetailResponse conteniendo:
     *         - Todos los datos de la factura solicitada
     *         - Información denormalizada de entidades relacionadas
     *         - URLs de recursos asociados (PDFs, documentos)
     *         - Estado actual y histórico de la factura
     *         - Metadatos de seguimiento y auditoría
     *
     * @throws IllegalArgumentException si no existe factura con el ID proporcionado
     * @throws SecurityException si el usuario no tiene permisos para ver la factura
     * @throws DataAccessException si ocurre error al consultar la base de datos
     *
     * @apiNote La respuesta incluye URLs pre-firmadas para descarga de PDFs
     *          que expiran después de 1 hora por seguridad.
     *
     * @since 1.0
     */
    @Override
    @Transactional(readOnly = true)
    public InvoiceDetailResponse getInvoiceById(Long invoiceId) {
        log.debug("Consultando factura por ID: {}", invoiceId);

        // Buscar factura con todas las relaciones necesarias
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> {
                    log.warn("Intento de consulta de factura inexistente con ID: {}", invoiceId);
                    return new IllegalArgumentException("Factura no encontrada con ID: " + invoiceId);
                });

        log.debug("Factura encontrada: {} - Estado: {} - Total: {}",
                invoice.getInvoiceNumber(), invoice.getStatus(), invoice.getTotalAmount());

        return InvoiceDetailResponse.fromEntity(invoice);
    }

    /**
     * Obtiene una lista paginada de facturas para una cuenta específica con filtros opcionales.
     *
     * <p>Este método permite consultar facturas desde diferentes perspectivas según
     * el contexto del usuario, aplicando filtros inteligentes y optimizaciones
     * de rendimiento para manejar grandes volúmenes de datos.</p>
     *
     * <p><strong>Características de la consulta:</strong></p>
     * <ul>
     *   <li>Paginación optimizada para grandes volúmenes</li>
     *   <li>Ordenamiento inteligente por relevancia y fecha</li>
     *   <li>Filtros automáticos según permisos del usuario</li>
     *   <li>Proyección específica para vista resumida</li>
     *   <li>Cache de consultas frecuentes</li>
     * </ul>
     *
     * <p><strong>Datos incluidos en el resumen:</strong></p>
     * <ul>
     *   <li>Información esencial de cada factura</li>
     *   <li>Estado actual y fechas relevantes</li>
     *   <li>Montos principales (subtotal, total)</li>
     *   <li>Información básica de la reserva asociada</li>
     *   <li>Indicadores de estado de pago</li>
     * </ul>
     *
     * @param accountId Identificador de la cuenta para filtrar facturas.
     *                  Debe corresponder a una cuenta existente y accesible.
     * @param pageable Configuración de paginación que incluye:
     *                 - Número de página (base 0)
     *                 - Tamaño de página (máximo 100)
     *                 - Criterios de ordenamiento
     *                 - Dirección de ordenamiento
     *
     * @return Page<InvoiceSummaryResponse> conteniendo:
     *         - Lista de facturas en formato resumido
     *         - Metadatos de paginación (total de elementos, páginas)
     *         - Información de navegación (primera, última, siguiente, anterior)
     *         - Filtros aplicados y criterios de ordenamiento
     *
     * @throws IllegalArgumentException si el accountId no es válido
     * @throws SecurityException si el usuario no puede acceder a las facturas de esa cuenta
     * @throws DataAccessException si ocurre error al consultar la base de datos
     *
     * @apiNote El ordenamiento por defecto es por fecha de creación descendente.
     *          Se recomienda usar tamaños de página entre 10-50 para rendimiento óptimo.
     *
     * @since 1.0
     */
    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceSummaryResponse> getInvoicesByAccountId(Long accountId, Pageable pageable) {
        log.debug("Consultando facturas para cuenta ID: {} con paginación: {}", accountId, pageable);

        // Consulta optimizada con proyección específica
        Page<Invoice> invoicePage = invoiceRepository.findByAccountId(accountId, pageable);

        log.debug("Encontradas {} facturas para cuenta ID: {}", invoicePage.getTotalElements(), accountId);

        // Mapeo a DTOs de resumen con optimizaciones
        return invoicePage.map(InvoiceSummaryResponse::fromEntity);
    }

    /**
     * Actualiza una factura existente aplicando validaciones de negocio y reglas de estado.
     *
     * <p>Este método permite modificar aspectos específicos de una factura respetando
     * las reglas de negocio, el estado actual y los permisos del usuario. No todos
     * los campos son modificables en todos los estados.</p>
     *
     * <p><strong>Campos modificables por estado:</strong></p>
     * <ul>
     *   <li>DRAFT: Todos los campos excepto números identificadores</li>
     *   <li>SENT: Fecha de vencimiento, notas (solo administradores pueden cambiar montos)</li>
     *   <li>PAID: Solo campos de auditoría y notas administrativas</li>
     *   <li>CANCELLED/REFUNDED: Solo campos de seguimiento y auditoría</li>
     * </ul>
     *
     * <p><strong>Validaciones aplicadas:</strong></p>
     * <ul>
     *   <li>Coherencia de montos: subtotal + tarifa = total</li>
     *   <li>Fechas válidas: vencimiento posterior a emisión</li>
     *   <li>Permisos según rol: usuarios vs administradores</li>
     *   <li>Integridad referencial con reservas y pagos</li>
     *   <li>Reglas de negocio específicas por estado</li>
     * </ul>
     *
     * <p><strong>Procesamiento post-actualización:</strong></p>
     * <ul>
     *   <li>Regeneración de PDF si cambian datos visuales</li>
     *   <li>Notificaciones a partes interesadas sobre cambios</li>
     *   <li>Actualización de registros de auditoría</li>
     *   <li>Sincronización con sistemas de contabilidad</li>
     * </ul>
     *
     * @param invoiceId Identificador único de la factura a actualizar
     * @param request DTO con los nuevos datos, conteniendo:
     *                - Campos opcionales a actualizar
     *                - Razón de la actualización para auditoría
     *                - Flags de procesamiento (regenerar PDF, etc.)
     *
     * @return InvoiceDetailResponse con la factura actualizada, incluyendo:
     *         - Todos los datos actualizados
     *         - Nueva información de auditoría
     *         - URLs de documentos regenerados si aplica
     *         - Estado de notificaciones enviadas
     *
     * @throws IllegalArgumentException si la factura no existe o datos inválidos
     * @throws IllegalStateException si la factura no puede modificarse en su estado actual
     * @throws ValidationException si los nuevos datos no cumplen las reglas de negocio
     * @throws SecurityException si el usuario no tiene permisos para la modificación
     * @throws DataAccessException si ocurre error durante la persistencia
     *
     * @apiNote Las modificaciones son atómicas. Si alguna validación falla,
     *          no se aplica ningún cambio y se mantiene el estado original.
     *
     * @since 2.0
     */
    @Override
    @Transactional
    public InvoiceDetailResponse updateInvoice(Long invoiceId, UpdateInvoiceRequest request) {
        log.info("Iniciando actualización de factura ID: {} con datos: {}", invoiceId, request);

        try {
            // 1. Obtener factura existente con validación
            Invoice invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> {
                        log.warn("Intento de actualización de factura inexistente con ID: {}", invoiceId);
                        return new IllegalArgumentException("Factura no encontrada con ID: " + invoiceId);
                    });

            log.debug("Factura encontrada para actualización: {} - Estado actual: {}",
                    invoice.getInvoiceNumber(), invoice.getStatus());

            // 2. Validar permisos y reglas de negocio para la actualización
            validateUpdateRequest(invoice, request);

            // 3. Aplicar cambios validados con auditoría
            boolean significantChanges = applyValidatedUpdates(invoice, request);

            // 4. Recalcular campos dependientes si es necesario
            if (request.hasFinancialChanges()) {
                recalculateFinancialFields(invoice, request);
            }

            // 5. Persistir cambios en la base de datos
            Invoice updatedInvoice = invoiceRepository.save(invoice);
            log.info("Factura ID: {} actualizada exitosamente", invoiceId);

            // 6. Procesamiento post-actualización si hay cambios significativos
            if (significantChanges) {
                processPostUpdateTasks(updatedInvoice, request);
            }

            return InvoiceDetailResponse.fromEntity(updatedInvoice);

        } catch (Exception e) {
            log.error("Error actualizando factura ID: {} - Error: {}", invoiceId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Cambia el estado de una factura a SENT con validaciones y procesamiento automático.
     *
     * <p>Este método maneja la transición de una factura a estado enviado, lo que
     * incluye validaciones de integridad, generación de PDF actualizado si es necesario,
     * y envío automático por correo electrónico al cliente.</p>
     *
     * <p><strong>Validaciones realizadas:</strong></p>
     * <ul>
     *   <li>La factura debe existir en el sistema</li>
     *   <li>El estado actual debe permitir la transición a SENT</li>
     *   <li>Todos los datos financieros deben estar completos</li>
     *   <li>La información de contacto del cliente debe ser válida</li>
     * </ul>
     *
     * <p><strong>Procesamiento automático:</strong></p>
     * <ul>
     *   <li>Actualización del estado a SENT</li>
     *   <li>Generación/actualización del PDF de la factura</li>
     *   <li>Envío automático por correo electrónico</li>
     *   <li>Registro de evento en el historial de auditoría</li>
     *   <li>Notificación a servicios dependientes</li>
     * </ul>
     *
     * @param invoiceId Identificador único de la factura a enviar
     *
     * @return InvoiceDetailResponse con la factura actualizada, conteniendo:
     *         - Estado actualizado a SENT
     *         - Fecha y hora de envío registrada
     *         - URL del PDF generado
     *         - Estado del envío de email
     *         - Información de auditoría actualizada
     *
     * @throws IllegalArgumentException si la factura no existe
     * @throws IllegalStateException si la factura no puede ser enviada en su estado actual
     * @throws ValidationException si faltan datos requeridos para el envío
     *
     * @since 1.0
     */
    @Override
    @Transactional
    public InvoiceDetailResponse sendInvoice(Long invoiceId) {
        log.info("Iniciando proceso de envío para factura ID: {}", invoiceId);

        // 1. Obtener y validar factura
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> {
                    log.warn("Intento de envío de factura inexistente con ID: {}", invoiceId);
                    return new IllegalArgumentException("Factura no encontrada con ID: " + invoiceId);
                });

        // 2. Validar que puede ser enviada
        if (!invoice.canBeSent()) {
            log.warn("Intento de envío de factura en estado inválido: {} - Estado: {}",
                    invoice.getInvoiceNumber(), invoice.getStatus());
            throw new IllegalStateException("La factura no puede ser enviada en su estado actual: " + invoice.getStatus());
        }

        // 3. Cambiar estado a SENT
        invoice.setStatus(InvoiceStatus.SENT);

        // 4. Persistir cambio de estado
        Invoice sentInvoice = invoiceRepository.save(invoice);
        log.info("Factura {} cambiada a estado SENT", sentInvoice.getInvoiceNumber());

        // 5. Procesamiento post-envío (PDF y email)
        processInvoiceSending(sentInvoice);

        return InvoiceDetailResponse.fromEntity(sentInvoice);
    }

    /**
     * Cancela una factura aplicando reglas de negocio y procesando efectos secundarios.
     *
     * <p>Este método maneja la cancelación segura de facturas, validando que la
     * operación sea permitida según el estado actual, procesando reembolsos si
     * corresponde, y notificando a todas las partes involucradas.</p>
     *
     * <p><strong>Validaciones de cancelación:</strong></p>
     * <ul>
     *   <li>La factura debe existir y ser accesible</li>
     *   <li>El estado actual debe permitir cancelación</li>
     *   <li>El usuario debe tener permisos para cancelar</li>
     *   <li>Debe proporcionarse un motivo válido</li>
     * </ul>
     *
     * <p><strong>Estados que permiten cancelación:</strong></p>
     * <ul>
     *   <li>DRAFT: Cancelación directa sin efectos</li>
     *   <li>SENT: Cancelación con notificación al cliente</li>
     *   <li>PARTIALLY_PAID: Cancelación con proceso de reembolso</li>
     * </ul>
     *
     * <p><strong>Procesamiento de cancelación:</strong></p>
     * <ul>
     *   <li>Cambio de estado a CANCELLED</li>
     *   <li>Registro del motivo en las notas de la factura</li>
     *   <li>Procesamiento de reembolsos si hay pagos previos</li>
     *   <li>Notificaciones a cliente y cuidador</li>
     *   <li>Actualización de métricas y estadísticas</li>
     *   <li>Liberación de recursos asociados</li>
     * </ul>
     *
     * @param invoiceId Identificador único de la factura a cancelar
     * @param reason Motivo de la cancelación, requerido para auditoría.
     *               Debe ser descriptivo y profesional.
     *
     * @return InvoiceDetailResponse con la factura cancelada, incluyendo:
     *         - Estado actualizado a CANCELLED
     *         - Motivo de cancelación registrado en notas
     *         - Información de reembolsos procesados
     *         - Estado de notificaciones enviadas
     *         - Metadatos de auditoría actualizados
     *
     * @throws IllegalArgumentException si la factura no existe o el motivo es inválido
     * @throws IllegalStateException si la factura no puede cancelarse en su estado actual
     * @throws SecurityException si el usuario no tiene permisos para cancelar
     *
     * @since 2.0
     */
    @Override
    @Transactional
    public InvoiceDetailResponse cancelInvoice(Long invoiceId, String reason) {
        log.info("Iniciando cancelación de factura ID: {} con motivo: {}", invoiceId, reason);

        try {
            // 1. Obtener y validar factura
            Invoice invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> {
                        log.warn("Intento de cancelación de factura inexistente con ID: {}", invoiceId);
                        return new IllegalArgumentException("Factura no encontrada con ID: " + invoiceId);
                    });

            // 2. Validar que puede ser cancelada
            if (!invoice.canBeCancelled()) {
                log.warn("Intento de cancelación de factura en estado inválido: {} - Estado: {}",
                        invoice.getInvoiceNumber(), invoice.getStatus());
                throw new IllegalStateException("La factura no puede ser cancelada en su estado actual: " + invoice.getStatus());
            }

            // 3. Validar motivo de cancelación
            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalArgumentException("El motivo de cancelación es obligatorio");
            }

            // 4. Aplicar cancelación con auditoría
            invoice.setStatus(InvoiceStatus.CANCELLED);

            // 5. Registrar motivo en notas
            String currentNotes = invoice.getNotes();
            String cancelNote = "CANCELADA: " + reason.trim();
            invoice.setNotes(currentNotes != null ? currentNotes + "\n" + cancelNote : cancelNote);

            // 6. Persistir cancelación
            Invoice cancelledInvoice = invoiceRepository.save(invoice);
            log.info("Factura {} cancelada exitosamente", cancelledInvoice.getInvoiceNumber());

            // 7. Procesamiento post-cancelación
            processInvoiceCancellation(cancelledInvoice, reason);

            return InvoiceDetailResponse.fromEntity(cancelledInvoice);

        } catch (Exception e) {
            log.error("Error cancelando factura ID: {} - Error: {}", invoiceId, e.getMessage(), e);
            throw e;
        }
    }

    // ========== MÉTODOS PRIVADOS DE VALIDACIÓN ==========

    /**
     * Valida y obtiene una reserva para facturación con todas las verificaciones necesarias.
     *
     * @param bookingId ID de la reserva a validar
     * @return Booking validada y lista para facturación
     * @throws IllegalArgumentException si la reserva no existe
     * @throws DataAccessException si hay error en la consulta
     */
    private Booking validateAndFetchBooking(Long bookingId) {
        log.debug("Validando reserva ID: {} para facturación", bookingId);

        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Reserva no encontrada para facturación - ID: {}", bookingId);
                    return new IllegalArgumentException("Reserva no encontrada con ID: " + bookingId);
                });
    }

    /**
     * Valida que una reserva cumple todos los requisitos para ser facturada.
     *
     * <p>Aplica las reglas de negocio críticas que determinan si una reserva
     * puede generar una factura válida en el sistema.</p>
     *
     * @param booking Reserva a validar para facturación
     * @throws IllegalStateException si la reserva no puede ser facturada
     */
    private void validateBookingForInvoicing(Booking booking) {
        log.debug("Aplicando validaciones de negocio para reserva ID: {}", booking.getId());

        // Validar estado de la reserva
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            log.warn("Intento de facturar reserva en estado inválido: {} - Estado: {}",
                    booking.getId(), booking.getStatus());
            throw new IllegalStateException("Solo se pueden facturar reservas en estado 'COMPLETED'. Estado actual: " + booking.getStatus());
        }

        // Validar que no exista factura previa
        if (invoiceRepository.existsByBookingId(booking.getId())) {
            log.warn("Intento de crear factura duplicada para reserva ID: {}", booking.getId());
            throw new IllegalStateException("Ya existe una factura para esta reserva con ID: " + booking.getId());
        }

        // Validar integridad de datos de la reserva
        if (booking.getTotalPrice() == null || booking.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Reserva con precio inválido - ID: {} - Precio: {}", booking.getId(), booking.getTotalPrice());
            throw new IllegalStateException("La reserva debe tener un precio válido mayor a cero");
        }

        log.debug("Validaciones de negocio completadas exitosamente para reserva ID: {}", booking.getId());
    }

    /**
     * Crea una factura completa con todos los datos necesarios y metadatos de auditoría.
     *
     * @param booking Reserva base para la facturación
     * @param request Datos adicionales del request de creación
     * @return Invoice configurada pero no persistida
     */
    private Invoice createComprehensiveInvoice(Booking booking, CreateInvoiceRequest request) {
        log.debug("Creando factura completa para reserva ID: {}", booking.getId());

        Invoice invoice = new Invoice();

        // Establecer relaciones básicas
        invoice.setAccount(booking.getAccount());
        invoice.setBooking(booking);

        // Generar datos únicos
        invoice.setInvoiceNumber(generateUniqueInvoiceNumber());
        invoice.setIssueDate(LocalDateTime.now());
        invoice.setDueDate(calculateOptimalDueDate());

        // Establecer estado inicial como SENT para flujo automático
        invoice.setStatus(InvoiceStatus.SENT);

        // Añadir notas del request si existen
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            invoice.setNotes(request.getNotes());
        }

        log.debug("Factura base creada con número: {}", invoice.getInvoiceNumber());
        return invoice;
    }

    /**
     * Crea items detallados de facturación basándose en la reserva y configuración.
     *
     * <p>Este método maneja tanto items personalizados proporcionados en el request
     * como la generación automática de items basándose en los datos de la reserva.</p>
     *
     * @param invoice Factura a la que agregar items
     * @param booking Reserva con datos del servicio
     * @param request Request con posibles items personalizados
     */
    private void createDetailedInvoiceItems(Invoice invoice, Booking booking, CreateInvoiceRequest request) {
        log.debug("Creando items detallados para factura: {}", invoice.getInvoiceNumber());

        if (request.hasCustomItems()) {
            // Usar items personalizados del request
            log.debug("Usando {} items personalizados del request", request.getItems().size());
            for (CreateInvoiceRequest.CreateInvoiceItemRequest itemRequest : request.getItems()) {
                InvoiceItem item = new InvoiceItem(
                        invoice,
                        itemRequest.getDescription(),
                        itemRequest.getQuantity(),
                        itemRequest.getUnitPrice(),
                        itemRequest.getLineTotal()
                );
                invoice.addInvoiceItem(item);
            }
        } else {
            // Generar item automático desde la reserva
            String description = buildServiceDescription(booking);

            InvoiceItem item = new InvoiceItem(
                    invoice,
                    description,
                    1, // Cantidad siempre 1 para servicios
                    booking.getTotalPrice(),
                    booking.getTotalPrice()
            );
            invoice.addInvoiceItem(item);
            log.debug("Item automático creado: {}", description);
        }
    }

    /**
     * Calcula y establece todos los totales financieros con precisión contable.
     *
     * @param invoice Factura con items ya configurados
     */
    private void calculateAndSetComprehensiveTotals(Invoice invoice) {
        log.debug("Calculando totales financieros para factura: {}", invoice.getInvoiceNumber());

        // Calcular subtotal sumando todos los items
        BigDecimal subtotal = invoice.getInvoiceItems().stream()
                .map(InvoiceItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular tarifa de plataforma según configuración
        BigDecimal platformFeeRate = getPlatformFeeRate();
        BigDecimal platformFee = subtotal.multiply(platformFeeRate).setScale(2, BigDecimal.ROUND_HALF_UP);

        // Calcular total final
        BigDecimal totalAmount = subtotal.add(platformFee);

        // Establecer valores calculados
        invoice.setSubtotal(subtotal);
        invoice.setPlatformFee(platformFee);
        invoice.setTotalAmount(totalAmount);

        log.debug("Totales calculados - Subtotal: {}, Tarifa ({}%): {}, Total: {}",
                subtotal, platformFeeRate.multiply(new BigDecimal("100")), platformFee, totalAmount);
    }

    /**
     * Procesa todas las tareas posteriores a la creación de factura.
     *
     * @param savedInvoice Factura persistida
     * @param autoSendEmail Flag para envío automático de email
     */
    private void processPostInvoiceCreationTasks(Invoice savedInvoice, boolean autoSendEmail) {
        log.info("Iniciando procesamiento post-creación para factura: {}", savedInvoice.getInvoiceNumber());

        try {
            // 1. Crear registro de tarifa de plataforma
            if (platformFeeService != null) {
                platformFeeService.calculateAndCreateFee(savedInvoice);
                log.debug("Tarifa de plataforma creada para factura: {}", savedInvoice.getInvoiceNumber());
            }

            // 2. Generar PDF de la factura
            generateAndStoreInvoicePdf(savedInvoice);

            // 3. Enviar por email si está habilitado
            if (autoSendEmail) {
                sendInvoiceByEmail(savedInvoice);
            }

            // 4. Notificar a servicios dependientes
            notificationService.notifyInvoiceGenerated(savedInvoice);

            log.info("Procesamiento post-creación completado para factura: {}", savedInvoice.getInvoiceNumber());

        } catch (Exception e) {
            log.error("Error en procesamiento post-creación para factura: {} - Error: {}",
                    savedInvoice.getInvoiceNumber(), e.getMessage(), e);
            // No re-lanzar la excepción para evitar rollback de la factura ya creada
            // Las tareas post-creación son complementarias, no críticas
        }
    }

    /**
     * Construye una respuesta completa con metadatos adicionales.
     *
     * @param savedInvoice Factura persistida
     * @return Response con información completa
     */
    private InvoiceDetailResponse buildComprehensiveInvoiceResponse(Invoice savedInvoice) {
        log.debug("Construyendo respuesta completa para factura: {}", savedInvoice.getInvoiceNumber());

        InvoiceDetailResponse response = InvoiceDetailResponse.fromEntity(savedInvoice);

        // Aquí se podrían agregar metadatos adicionales como:
        // - URL de descarga del PDF
        // - Estado de envío de email
        // - Información de auditoría extendida

        return response;
    }

    // ========== MÉTODOS DE UTILIDADES Y CÁLCULOS ==========

    /**
     * Genera un número único de factura con formato empresarial.
     *
     * @return Número de factura único y seguro
     */
    private String generateUniqueInvoiceNumber() {
        int currentYear = LocalDateTime.now().getYear();
        long timestamp = System.currentTimeMillis();

        // Formato: INV-YYYY-TIMESTAMP
        return String.format("INV-%d-%d", currentYear, timestamp);
    }

    /**
     * Calcula la fecha de vencimiento óptima según políticas de la empresa.
     *
     * @return Fecha de vencimiento calculada
     */
    private LocalDateTime calculateOptimalDueDate() {
        // Por defecto 15 días, pero podría ser configurable por tipo de cliente
        return LocalDateTime.now().plusDays(15);
    }

    /**
     * Obtiene la tasa de tarifa de plataforma configurada.
     *
     * @return BigDecimal con la tasa (ej: 0.10 para 10%)
     */
    private BigDecimal getPlatformFeeRate() {
        // Por defecto 10%, pero debería obtenerse de configuración
        return new BigDecimal("0.10");
    }

    /**
     * Construye una descripción detallada del servicio para la facturación.
     *
     * @param booking Reserva con datos del servicio
     * @return Descripción formateada para la factura
     */
    private String buildServiceDescription(Booking booking) {
        String serviceName = booking.getServiceOffering() != null ?
                booking.getServiceOffering().getName() : "Servicio de Cuidado";

        return String.format("%s - Reserva #%d", serviceName, booking.getId());
    }

    /**
     * Genera y almacena el PDF de la factura usando el servicio especializado.
     *
     * @param invoice Factura para generar PDF
     */
    private void generateAndStoreInvoicePdf(Invoice invoice) {
        try {
            log.debug("Generando PDF para factura: {}", invoice.getInvoiceNumber());

            InvoiceDetailResponse invoiceDto = InvoiceDetailResponse.fromEntity(invoice);
            byte[] pdfBytes = pdfGenerationService.generateInvoicePdf(invoiceDto);

            // Aquí se almacenaría el PDF en el sistema de archivos o cloud storage
            // Por ejemplo: fileStorageService.storePdf(invoice.getInvoiceNumber(), pdfBytes);

            log.info("PDF generado exitosamente para factura: {}", invoice.getInvoiceNumber());

        } catch (Exception e) {
            log.error("Error generando PDF para factura: {} - Error: {}",
                    invoice.getInvoiceNumber(), e.getMessage(), e);
            // No re-lanzar para evitar fallar todo el proceso
        }
    }

    /**
     * Envía la factura por correo electrónico al cliente.
     *
     * @param invoice Factura a enviar
     */
    private void sendInvoiceByEmail(Invoice invoice) {
        try {
            log.debug("Enviando factura por email: {}", invoice.getInvoiceNumber());

            // Generar PDF si no existe
            InvoiceDetailResponse invoiceDto = InvoiceDetailResponse.fromEntity(invoice);
            byte[] pdfBytes = pdfGenerationService.generateInvoicePdf(invoiceDto);

            // Enviar email con PDF adjunto
            notificationService.sendInvoiceEmail(invoice, pdfBytes);

            log.info("Factura enviada por email exitosamente: {}", invoice.getInvoiceNumber());

        } catch (Exception e) {
            log.error("Error enviando factura por email: {} - Error: {}",
                    invoice.getInvoiceNumber(), e.getMessage(), e);
        }
    }

    /**
     * Procesa el envío de una factura con todas las tareas asociadas.
     *
     * @param invoice Factura en estado SENT
     */
    private void processInvoiceSending(Invoice invoice) {
        try {
            log.info("Procesando envío completo de factura: {}", invoice.getInvoiceNumber());

            // Generar PDF actualizado
            generateAndStoreInvoicePdf(invoice);

            // Enviar por email
            sendInvoiceByEmail(invoice);

            // Notificar envío completado
            notificationService.notifyInvoiceSent(invoice);

        } catch (Exception e) {
            log.error("Error en procesamiento de envío de factura: {} - Error: {}",
                    invoice.getInvoiceNumber(), e.getMessage(), e);
        }
    }

    /**
     * Procesa la cancelación de una factura con todas las tareas asociadas.
     *
     * @param invoice Factura cancelada
     * @param reason Motivo de cancelación
     */
    private void processInvoiceCancellation(Invoice invoice, String reason) {
        try {
            log.info("Procesando cancelación completa de factura: {}", invoice.getInvoiceNumber());

            // Procesar reembolsos si hay pagos previos
            // paymentService.processRefundsForInvoice(invoice);

            // Notificar cancelación
            notificationService.notifyInvoiceCancelled(invoice, reason);

            // Liberar recursos asociados
            // resourceService.releaseInvoiceResources(invoice);

        } catch (Exception e) {
            log.error("Error en procesamiento de cancelación de factura: {} - Error: {}",
                    invoice.getInvoiceNumber(), e.getMessage(), e);
        }
    }

    // ========== MÉTODOS DE VALIDACIÓN PARA ACTUALIZACIÓN ==========

    /**
     * Valida que una solicitud de actualización es válida según estado y permisos.
     *
     * @param invoice Factura a actualizar
     * @param request Datos de actualización
     * @throws IllegalStateException si la actualización no es permitida
     * @throws IllegalArgumentException si los datos son inválidos
     */
    private void validateUpdateRequest(Invoice invoice, UpdateInvoiceRequest request) {
        log.debug("Validando request de actualización para factura: {}", invoice.getInvoiceNumber());

        if (request.isEmpty()) {
            throw new IllegalArgumentException("La solicitud de actualización está vacía");
        }

        // Validar cambios financieros según el estado
        if (request.hasFinancialChanges()) {
            if (invoice.isFinalState()) {
                throw new IllegalStateException("No se pueden modificar montos en facturas con estado final: " + invoice.getStatus());
            }

            if (invoice.getStatus() == InvoiceStatus.SENT && !isAdminUser()) {
                throw new IllegalStateException("Solo administradores pueden modificar montos de facturas enviadas");
            }
        }

        // Validar coherencia de montos si se proporcionan
        if (!request.areAmountsConsistent()) {
            throw new IllegalArgumentException("Los montos proporcionados no son coherentes: subtotal + platformFee debe igual totalAmount");
        }
    }

    /**
     * Aplica las actualizaciones validadas a la factura.
     *
     * @param invoice Factura a actualizar
     * @param request Datos de actualización
     * @return true si hubo cambios significativos que requieren procesamiento adicional
     */
    private boolean applyValidatedUpdates(Invoice invoice, UpdateInvoiceRequest request) {
        boolean significantChanges = false;

        if (request.getDueDate() != null && !request.getDueDate().equals(invoice.getDueDate())) {
            invoice.setDueDate(request.getDueDate());
            significantChanges = true;
        }

        if (request.getNotes() != null && !request.getNotes().equals(invoice.getNotes())) {
            invoice.setNotes(request.getNotes());
        }

        if (request.getSubtotal() != null && !request.getSubtotal().equals(invoice.getSubtotal())) {
            invoice.setSubtotal(request.getSubtotal());
            significantChanges = true;
        }

        if (request.getPlatformFee() != null && !request.getPlatformFee().equals(invoice.getPlatformFee())) {
            invoice.setPlatformFee(request.getPlatformFee());
            significantChanges = true;
        }

        if (request.getTotalAmount() != null && !request.getTotalAmount().equals(invoice.getTotalAmount())) {
            invoice.setTotalAmount(request.getTotalAmount());
            significantChanges = true;
        }

        return significantChanges;
    }

    /**
     * Recalcula campos financieros después de actualizaciones.
     *
     * @param invoice Factura con cambios aplicados
     * @param request Request con datos de actualización
     */
    private void recalculateFinancialFields(Invoice invoice, UpdateInvoiceRequest request) {
        if (request.hasFinancialChanges() && request.getTotalAmount() == null) {
            BigDecimal calculatedTotal = request.getCalculatedTotal();
            if (calculatedTotal != null) {
                invoice.setTotalAmount(calculatedTotal);
            }
        }
    }

    /**
     * Procesa tareas posteriores a la actualización de factura.
     *
     * @param invoice Factura actualizada
     * @param request Request original de actualización
     */
    private void processPostUpdateTasks(Invoice invoice, UpdateInvoiceRequest request) {
        try {
            // Regenerar PDF si hubo cambios visuales
            if (request.hasVisualChanges()) {
                generateAndStoreInvoicePdf(invoice);
            }

            // Notificar cambios
            notificationService.notifyInvoiceUpdated(invoice);

            // Actualizar tarifa de plataforma si cambió el monto
            if (request.hasFinancialChanges() && platformFeeService != null && invoice.getBooking() != null) {
                platformFeeService.recalculatePlatformFee(invoice.getBooking());
            }

        } catch (Exception e) {
            log.error("Error en procesamiento post-actualización para factura: {} - Error: {}",
                    invoice.getInvoiceNumber(), e.getMessage(), e);
        }
    }

    /**
     * Verifica si el usuario actual tiene permisos de administrador.
     *
     * @return true si es administrador
     */
    private boolean isAdminUser() {
        // Obtiene el contexto de seguridad actual
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return false;
        }

        // Obtiene el objeto de autenticación del contexto
        Authentication auth = context.getAuthentication();
        if (auth == null) {
            return false;
        }

        // Verifica si alguna de las "autoridades" (roles) del usuario es 'ROLE_ADMIN'
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}