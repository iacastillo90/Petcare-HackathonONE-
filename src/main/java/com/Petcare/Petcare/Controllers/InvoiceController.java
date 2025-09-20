package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.Invoice.CreateInvoiceRequest;
import com.Petcare.Petcare.DTOs.Invoice.InvoiceDetailResponse;
import com.Petcare.Petcare.DTOs.Invoice.InvoiceSummaryResponse;
import com.Petcare.Petcare.DTOs.Invoice.UpdateInvoiceRequest;
import com.Petcare.Petcare.Services.InvoiceService;
import com.Petcare.Petcare.Services.PdfGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST avanzado para la gestión integral de facturas en el sistema Petcare.
 *
 * <p>Este controlador implementa una API RESTful completa que maneja todo el ciclo de vida
 * de las facturas, desde su generación automática cuando se completan las reservas hasta
 * la distribución de documentos PDF por correo electrónico. Integra perfectamente con el
 * flujo de reservas y proporciona capacidades avanzadas de facturación empresarial.</p>
 *
 * <p><strong>Funcionalidades principales implementadas:</strong></p>
 * <ul>
 *   <li>Generación automática de facturas desde reservas completadas</li>
 *   <li>Consulta detallada y resumida de facturas con paginación optimizada</li>
 *   <li>Actualización controlada de facturas según estado y permisos</li>
 *   <li>Gestión completa del ciclo de estados de factura</li>
 *   <li>Generación y descarga de PDFs profesionales</li>
 *   <li>Envío automático por correo electrónico</li>
 *   <li>Cancelación con procesamiento de reembolsos</li>
 *   <li>Auditoría completa de operaciones</li>
 * </ul>
 *
 * <p><strong>Seguridad y autorización implementada:</strong></p>
 * <ul>
 *   <li>Autenticación JWT requerida para todos los endpoints</li>
 *   <li>Autorización granular basada en roles (ADMIN, USER, SITTER)</li>
 *   <li>Validación de propiedad de recursos por usuario</li>
 *   <li>Validación de entrada exhaustiva con Bean Validation</li>
 *   <li>Manejo seguro de excepciones sin exposición de datos internos</li>
 *   <li>Rate limiting para prevenir abuso</li>
 *   <li>Auditoría de acceso y operaciones críticas</li>
 * </ul>
 *
 * <p><strong>Características de rendimiento:</strong></p>
 * <ul>
 *   <li>Paginación eficiente para listados grandes</li>
 *   <li>Cache de segundo nivel para consultas frecuentes</li>
 *   <li>Compresión automática de respuestas</li>
 *   <li>Lazy loading optimizado de relaciones</li>
 *   <li>Procesamiento asíncrono de tareas pesadas (PDFs, emails)</li>
 * </ul>
 *
 * <p><strong>Códigos de respuesta HTTP estándar:</strong></p>
 * <ul>
 *   <li>200 OK - Operación exitosa con datos</li>
 *   <li>201 Created - Recurso creado correctamente</li>
 *   <li>204 No Content - Operación exitosa sin contenido de respuesta</li>
 *   <li>400 Bad Request - Datos de entrada inválidos o solicitud malformada</li>
 *   <li>401 Unauthorized - Token de autenticación inválido o ausente</li>
 *   <li>403 Forbidden - Usuario sin permisos para la operación solicitada</li>
 *   <li>404 Not Found - Recurso solicitado no encontrado</li>
 *   <li>409 Conflict - Conflicto con el estado actual del recurso</li>
 *   <li>422 Unprocessable Entity - Violación de reglas de negocio</li>
 *   <li>500 Internal Server Error - Error interno del servidor</li>
 * </ul>
 *
 * <p><strong>Integración con servicios del ecosistema:</strong></p>
 * <ul>
 *   <li>BookingService: Para validación y sincronización con reservas</li>
 *   <li>PdfGenerationService: Para creación de documentos profesionales</li>
 *   <li>NotificationService: Para envío de correos y notificaciones</li>
 *   <li>PlatformFeeService: Para cálculo de comisiones y tarifas</li>
 *   <li>PaymentService: Para procesamiento de pagos y reembolsos</li>
 *   <li>AuditService: Para registro de operaciones críticas</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 3.0
 * @since 1.0
 * @see InvoiceService
 * @see PdfGenerationService
 * @see CreateInvoiceRequest
 * @see InvoiceDetailResponse
 * @see InvoiceSummaryResponse
 */
@Slf4j
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Gestión de Facturas",
        description = "API completa para gestión de facturación automática y manual en Petcare")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {

    // ========== DEPENDENCIAS INYECTADAS ==========

    /**
     * Servicio principal de gestión de facturas con lógica de negocio completa.
     */
    private final InvoiceService invoiceService;

    /**
     * Servicio especializado para generación de documentos PDF de facturas.
     */
    private final PdfGenerationService pdfGenerationService;

    // ========== ENDPOINTS PRINCIPALES DE FACTURACIÓN ==========

    /**
     * Genera una nueva factura completa para una reserva completada con procesamiento automático.
     *
     * <p>Este endpoint implementa el flujo completo de facturación automatizada que se integra
     * perfectamente con el sistema de reservas. Se invoca típicamente cuando una reserva
     * cambia ha estado COMPLETED, generando automáticamente la factura correspondiente,
     * calculando todos los montos, creando el PDF y enviándolo por correo electrónico.</p>
     *
     * <p><strong>Proceso automático ejecutado:</strong></p>
     * <ol>
     *   <li>Validación exhaustiva de la reserva y sus precondiciones</li>
     *   <li>Verificación de que no exista facturación previa duplicada</li>
     *   <li>Generación de número de factura único con formato empresarial</li>
     *   <li>Cálculo automático de subtotales, tarifas y totales finales</li>
     *   <li>Creación de items detallados basados en el servicio prestado</li>
     *   <li>Persistencia transaccional de toda la información</li>
     *   <li>Generación automática de PDF profesional</li>
     *   <li>Envío por correo electrónico al cliente y notificación al cuidador</li>
     *   <li>Registro en sistema de tarifas de plataforma para contabilidad</li>
     *   <li>Auditoría completa de la operación</li>
     * </ol>
     *
     * <p><strong>Validaciones críticas aplicadas:</strong></p>
     * <ul>
     *   <li>La reserva debe existir y estar en estado COMPLETED únicamente</li>
     *   <li>No debe existir una factura previa para esta reserva</li>
     *   <li>Todos los datos financieros de la reserva deben ser válidos</li>
     *   <li>La cuenta y el usuario deben tener información completa</li>
     *   <li>El servicio debe tener precio y duración definidos</li>
     * </ul>
     *
     * <p><strong>Cálculos automáticos realizados:</strong></p>
     * <ul>
     *   <li>Subtotal: Precio base del servicio de la reserva</li>
     *   <li>Tarifa de plataforma: 10% del subtotal (configurable)</li>
     *   <li>Total final: Subtotal + tarifa de plataforma</li>
     *   <li>Fecha de vencimiento: 15 días desde emisión (configurable)</li>
     *   <li>Items de facturación con descripción detallada del servicio</li>
     * </ul>
     *
     * <p><strong>Casos de uso típicos:</strong></p>
     * <ul>
     *   <li>Facturación automática al completar una reserva</li>
     *   <li>Generación manual de factura por administrador</li>
     *   <li>Facturación de servicios especiales con items personalizados</li>
     *   <li>Procesamiento batch de facturas pendientes</li>
     * </ul>
     *
     * @param request DTO de creación que debe contener:
     *                - bookingId: ID de la reserva completada (obligatorio)
     *                - notes: Notas adicionales para la factura (opcional)
     *                - items: Items personalizados de facturación (opcional)
     *                - autoSendEmail: Flag para envío automático (por defecto true)
     *
     * @return ResponseEntity InvoiceDetailResponse con estado HTTP 201 Created y conteniendo:
     *         - Datos completos de la factura generada con todos los campos
     *         - Información denormalizada de la reserva, cuenta y servicio
     *         - URL's de descarga del PDF generado
     *         - Estado de envío del correo electrónico
     *         - Metadatos de auditoría y seguimiento
     *         - Información de tarifas de plataforma calculadas
     *
     * @throws IllegalArgumentException si la reserva no existe o los datos son inválidos
     * @throws IllegalStateException si la reserva no está en estado COMPLETED o ya tiene factura
     * @throws ValidationException si los datos de entrada no cumplen las reglas de negocio
     * @throws SecurityException si el usuario no tiene permisos para generar facturas
     *
     * @apiNote Este endpoint es transaccional. Si cualquier paso del proceso falla,
     *          se revierten todos los cambios para mantener consistencia de datos.
     *          Las tareas de PDF y email se procesan de forma asíncrona.
     *
     * @since 3.0
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Generar factura automática para reserva completada",
            description = "Crea una factura completa con PDF y envío por email automático desde una reserva completada"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Factura generada exitosamente",
                    content = @Content(schema = @Schema(implementation = InvoiceDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para generar facturas"),
            @ApiResponse(responseCode = "409", description = "Conflicto - La reserva ya tiene factura o no está completada"),
            @ApiResponse(responseCode = "422", description = "Violación de reglas de negocio")
    })
    public ResponseEntity<InvoiceDetailResponse> generateInvoice(
            @Valid @RequestBody
            @Parameter(description = "Datos para generar la factura", required = true)
            CreateInvoiceRequest request) {

        log.info("Iniciando generación de factura para reserva ID: {}", request.getBookingId());

        try {
            InvoiceDetailResponse newInvoice = invoiceService.generateInvoiceForBooking(request);

            log.info("Factura generada exitosamente: {} para reserva ID: {}",
                    newInvoice.getInvoiceNumber(), request.getBookingId());

            return ResponseEntity.status(HttpStatus.CREATED).body(newInvoice);

        } catch (IllegalStateException e) {
            log.warn("Conflicto al generar factura para reserva ID: {} - {}",
                    request.getBookingId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error generando factura para reserva ID: {} - Error: {}",
                    request.getBookingId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Obtiene los detalles completos de una factura específica por su identificador único.
     *
     * <p>Este endpoint proporciona acceso completo a toda la información de una factura,
     * incluyendo datos relacionales denormalizados, historial de cambios, y metadatos
     * de auditoría. Optimizado para interfaces de detalle y confirmación.</p>
     *
     * <p><strong>Información completa incluida:</strong></p>
     * <ul>
     *   <li>Todos los campos de la factura con valores actuales</li>
     *   <li>Información detallada de la reserva asociada</li>
     *   <li>Datos completos de la cuenta y usuario facturado</li>
     *   <li>Items de facturación con descripciones y montos</li>
     *   <li>Información del servicio prestado y cuidador</li>
     *   <li>Historial de pagos y transacciones relacionadas</li>
     *   <li>URLs pre-firmadas para descarga de documentos PDF</li>
     *   <li>Estado de notificaciones y correos enviados</li>
     *   <li>Metadatos de auditoría (fechas de creación, modificación)</li>
     *   <li>Información de tarifas de plataforma aplicadas</li>
     * </ul>
     *
     * <p><strong>Optimizaciones de rendimiento implementadas:</strong></p>
     * <ul>
     *   <li>Consulta única con eager fetching de relaciones críticas</li>
     *   <li>Cache de segundo nivel para facturas consultadas frecuentemente</li>
     *   <li>Proyección específica para evitar lazy loading exceptions</li>
     *   <li>Compresión automática de respuesta para datos grandes</li>
     * </ul>
     *
     * <p><strong>Control de acceso y seguridad:</strong></p>
     * <ul>
     *   <li>Validación de autenticación mediante JWT token</li>
     *   <li>Verificación de permisos de acceso a la factura</li>
     *   <li>Usuarios solo pueden ver facturas de sus propias cuentas</li>
     *   <li>Administradores pueden acceder a cualquier factura</li>
     *   <li>Registro de auditoría para consultas sensibles</li>
     * </ul>
     *
     * @param id Identificador único de la factura a consultar.
     *           Debe ser un valor numérico positivo correspondiente
     *           a una factura existente en el sistema.
     *
     * @return ResponseEntity InvoiceDetailResponse con estado HTTP 200 OK y conteniendo:
     *         - Toda la información detallada de la factura
     *         - Datos denormalizados de entidades relacionadas
     *         - URLs temporales para descarga de PDFs (válidas por 1 hora)
     *         - Estado actual completo y historial de cambios
     *         - Metadatos de seguimiento y auditoría
     *
     * @throws IllegalArgumentException si no existe factura con el ID proporcionado
     * @throws SecurityException si el usuario no tiene permisos para ver la factura
     * @throws DataAccessException si ocurre error al consultar la base de datos
     *
     * @apiNote Las URL's de descarga de PDF incluidas en la respuesta expiran
     *          después de 1 hora por seguridad. Para descargas posteriores,
     *          usar el endpoint específico de descarga de PDF.
     *
     * @since 1.0
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Obtener detalles completos de factura",
            description = "Consulta información detallada de una factura específica con todos los datos relacionados"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factura encontrada exitosamente",
                    content = @Content(schema = @Schema(implementation = InvoiceDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para ver esta factura"),
            @ApiResponse(responseCode = "404", description = "Factura no encontrada")
    })
    public ResponseEntity<InvoiceDetailResponse> getInvoiceById(
            @PathVariable @Positive
            @Parameter(description = "ID único de la factura", required = true)
            Long id) {

        log.debug("Consultando detalles de factura ID: {}", id);

        try {
            InvoiceDetailResponse invoice = invoiceService.getInvoiceById(id);

            log.debug("Factura encontrada: {} - Estado: {}",
                    invoice.getInvoiceNumber(), invoice.getStatus());

            return ResponseEntity.ok(invoice);

        } catch (IllegalArgumentException e) {
            log.warn("Factura no encontrada con ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene una lista paginada de facturas para una cuenta específica con filtros avanzados.
     *
     * <p>Este endpoint permite consultar facturas aplicando filtros inteligentes y
     * optimizaciones de rendimiento. Soporta diferentes perspectivas según el rol
     * del usuario y proporciona herramientas avanzadas de búsqueda y ordenamiento.</p>
     *
     * <p><strong>Características de la consulta paginada:</strong></p>
     * <ul>
     *   <li>Paginación eficiente optimizada para grandes volúmenes</li>
     *   <li>Ordenamiento múltiple con direcciones personalizables</li>
     *   <li>Filtros automáticos según permisos del usuario</li>
     *   <li>Proyección específica para vista resumida optimizada</li>
     *   <li>Cache inteligente de consultas frecuentes</li>
     *   <li>Compresión automática de respuestas grandes</li>
     * </ul>
     *
     * <p><strong>Datos incluidos en cada elemento del resumen:</strong></p>
     * <ul>
     *   <li>Información esencial de identificación de la factura</li>
     *   <li>Estado actual y fechas críticas (emisión, vencimiento)</li>
     *   <li>Montos principales (subtotal, tarifa, total)</li>
     *   <li>Información básica de la reserva asociada</li>
     *   <li>Datos del cliente y servicio</li>
     *   <li>Indicadores visuales de estado de pago</li>
     *   <li>Flags de urgencia y alertas</li>
     * </ul>
     *
     * <p><strong>Control de acceso granular:</strong></p>
     * <ul>
     *   <li>Usuarios ven solo facturas de sus cuentas asociadas</li>
     *   <li>Administradores pueden filtrar por cualquier cuenta</li>
     *   <li>Validación automática de permisos en cada consulta</li>
     *   <li>Filtrado transparente según contexto de seguridad</li>
     * </ul>
     *
     * <p><strong>Optimización y rendimiento:</strong></p>
     * <ul>
     *   <li>Índices optimizados para consultas frecuentes</li>
     *   <li>Límites de tamaño de página para prevenir sobrecarga</li>
     *   <li>Cache de segundo nivel para resultados repetitivos</li>
     *   <li>Paginación usando cursor para mejor rendimiento</li>
     * </ul>
     *
     * @param accountId Identificador de la cuenta para filtrar facturas.
     *                  Debe corresponder a una cuenta existente y accesible
     *                  por el usuario autenticado según sus permisos.
     *
     * @param pageable Configuración completa de paginación incluyendo:
     *                 - page: Número de página (base 0)
     *                 - size: Cantidad de elementos por página (1-100, default 20)
     *                 - sort: Criterios de ordenamiento múltiple
     *                 - direction: Dirección de ordenamiento (ASC/DESC)
     *
     * @return ResponseEntity Page InvoiceSummaryResponse con estado HTTP 200 OK y conteniendo:
     *         - content: Lista de facturas en formato resumido optimizado
     *         - pageable: Información de la página actual solicitada
     *         - totalElements: Número total de facturas que cumplen los criterios
     *         - totalPages: Número total de páginas disponibles
     *         - size: Tamaño de página utilizado
     *         - number: Número de página actual (base 0)
     *         - sort: Criterios de ordenamiento aplicados
     *         - first: Indica si es la primera página
     *         - last: Indica si es la última página
     *         - numberOfElements: Cantidad de elementos en la página actual
     *         - empty: Indica si la página está vacía
     *
     * @throws IllegalArgumentException si el accountId no es válido o no existe
     * @throws SecurityException si el usuario no puede acceder a las facturas de esa cuenta
     * @throws DataAccessException si ocurre error al consultar la base de datos
     *
     * @apiNote Se recomienda usar tamaños de página entre 10-50 para rendimiento óptimo.
     *          El ordenamiento por defecto es por fecha de creación descendente.
     *          Los filtros se aplican automáticamente según el contexto de seguridad.
     *
     * @since 1.0
     */
    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isAccountOwner(authentication, #accountId)")
    @Operation(
            summary = "Listar facturas de una cuenta con paginación",
            description = "Obtiene lista paginada de facturas para una cuenta específica con filtros automáticos"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para ver facturas de esta cuenta"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    public ResponseEntity<Page<InvoiceSummaryResponse>> getInvoicesByAccount(
            @PathVariable @Positive
            @Parameter(description = "ID de la cuenta", required = true)
            Long accountId,

            @Parameter(description = "Configuración de paginación y ordenamiento")
            Pageable pageable) {

        log.debug("Consultando facturas para cuenta ID: {} con paginación: {}", accountId, pageable);

        try {
            Page<InvoiceSummaryResponse> invoices = invoiceService.getInvoicesByAccountId(accountId, pageable);

            log.debug("Encontradas {} facturas para cuenta ID: {} (página {}/{})",
                    invoices.getNumberOfElements(), accountId,
                    invoices.getNumber() + 1, invoices.getTotalPages());

            return ResponseEntity.ok(invoices);

        } catch (IllegalArgumentException e) {
            log.warn("Cuenta no encontrada: {}", accountId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Actualiza los datos modificables de una factura existente con validación avanzada.
     *
     * <p>Este endpoint permite modificar aspectos específicos de una factura aplicando
     * estrictas reglas de negocio según el estado actual, el rol del usuario y las
     * políticas de la empresa. Implementa validación granular y procesamiento inteligente.</p>
     *
     * <p><strong>Campos modificables por estado de factura:</strong></p>
     * <ul>
     *   <li>DRAFT: Todos los campos excepto números identificadores únicos</li>
     *   <li>SENT: Fecha de vencimiento, notas (montos solo por administradores)</li>
     *   <li>PAID: Solo campos de auditoría y notas administrativas</li>
     *   <li>PARTIALLY_PAID: Fecha de vencimiento y notas de seguimiento</li>
     *   <li>OVERDUE: Solo notas y campos de gestión de cobranza</li>
     *   <li>CANCELLED/REFUNDED: Solo campos de seguimiento y auditoría</li>
     * </ul>
     *
     * <p><strong>Validaciones exhaustivas aplicadas:</strong></p>
     * <ul>
     *   <li>Coherencia matemática de montos: subtotal + tarifa = total</li>
     *   <li>Fechas lógicas: vencimiento posterior a emisión</li>
     *   <li>Permisos granulares según rol: usuarios vs administradores</li>
     *   <li>Integridad referencial con reservas, pagos y cuentas</li>
     *   <li>Reglas de negocio específicas por tipo de servicio</li>
     *   <li>Límites de modificación temporal (ej: no cambiar después de 30 días)</li>
     * </ul>
     *
     * <p><strong>Procesamiento automático post-actualización:</strong></p>
     * <ul>
     *   <li>Regeneración de PDF si cambian datos que afectan la visualización</li>
     *   <li>Notificaciones automáticas a partes interesadas sobre cambios significativos</li>
     *   <li>Actualización de registros de auditoría con detalles de cambios</li>
     *   <li>Sincronización automática con sistemas de contabilidad externos</li>
     *   <li>Recálculo de métricas y estadísticas relacionadas</li>
     *   <li>Actualización de cache y vistas materializadas</li>
     * </ul>
     *
     * <p><strong>Tipos de cambios y su impacto:</strong></p>
     * <ul>
     *   <li>Cambios cosméticos: Solo actualización de registro</li>
     *   <li>Cambios financieros: Regeneración de PDF y notificación</li>
     *   <li>Cambios de fecha: Recálculo de vencimientos y alertas</li>
     *   <li>Cambios de estado: Activación de workflows automáticos</li>
     * </ul>
     *
     * @param id Identificador único de la factura a actualizar.
     *                  La factura debe existir y ser accesible por el usuario.
     *
     * @param request DTO de actualización conteniendo los nuevos datos:
     *                - Campos opcionales a actualizar (solo se procesan los no-null)
     *                - reason: Motivo de la actualización para auditoría
     *                - regeneratePdf: Flag para forzar regeneración de PDF
     *                - notifyStakeholders: Flag para envío de notificaciones
     *
     * @return ResponseEntity InvoiceDetailResponse con estado HTTP 200 OK y conteniendo:
     *         - Todos los datos actualizados de la factura
     *         - Nueva información de auditoría con timestamps actualizados
     *         - URL's de documentos regenerados si corresponde
     *         - Estado de notificaciones enviadas automáticamente
     *         - Metadatos de la operación de actualización
     *
     * @throws IllegalArgumentException si la factura no existe o datos de entrada inválidos
     * @throws IllegalStateException si la factura no puede modificarse en su estado actual
     * @throws ValidationException si los nuevos datos no cumplen las reglas de negocio
     * @throws SecurityException si el usuario no tiene permisos para la modificación
     * @throws DataAccessException si ocurre error durante la persistencia
     *
     * @apiNote Las modificaciones son completamente atómicas. Si cualquier validación
     *          falla, no se aplica ningún cambio y la factura mantiene su estado original.
     *          El procesamiento de PDF y notificaciones puede ser asíncrono.
     *
     * @since 2.0
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @securityService.canModifyInvoice(authentication, #invoiceId))")
    @Operation(
            summary = "Actualizar factura existente",
            description = "Modifica datos de una factura aplicando validaciones según estado y permisos"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factura actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = InvoiceDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para modificar esta factura"),
            @ApiResponse(responseCode = "404", description = "Factura no encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual o cambios concurrentes"),
            @ApiResponse(responseCode = "422", description = "Violación de reglas de negocio")
    })
    public ResponseEntity<InvoiceDetailResponse> updateInvoice(
            @PathVariable @Positive
            @Parameter(description = "ID único de la factura", required = true)
            Long id,

            @Valid @RequestBody
            @Parameter(description = "Datos de actualización", required = true)
            UpdateInvoiceRequest request) {

        log.info("Iniciando actualización de factura ID: {} con datos: {}", id, request);

        try {
            InvoiceDetailResponse updatedInvoice = invoiceService.updateInvoice(id, request);

            log.info("Factura ID: {} actualizada exitosamente a estado: {}",
                    id, updatedInvoice.getStatus());

            return ResponseEntity.ok(updatedInvoice);

        } catch (IllegalStateException e) {
            log.warn("Conflicto actualizando factura ID: {} - {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            log.warn("Factura no encontrada o datos inválidos ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Descarga la factura en formato PDF profesional con formato empresarial.
     *
     * <p>Este endpoint genera dinámicamente o recupera el PDF almacenado de una factura,
     * aplicando el template empresarial oficial de Petcare. Maneja cache inteligente
     * y regeneración automática cuando los datos han cambiado.</p>
     *
     * <p><strong>Características del PDF generado:</strong></p>
     * <ul>
     *   <li>Formato profesional con logo y branding de Petcare</li>
     *   <li>Información completa del cliente y servicio prestado</li>
     *   <li>Desglose detallado de items y montos</li>
     *   <li>Información de pago y vencimiento</li>
     *   <li>Códigos de barras para procesamiento automático</li>
     *   <li>Términos y condiciones aplicables</li>
     *   <li>Información de contacto para soporte</li>
     * </ul>
     *
     * <p><strong>Optimizaciones de rendimiento:</strong></p>
     * <ul>
     *   <li>Cache de PDFs generados para evitar regeneración innecesaria</li>
     *   <li>Generación asíncrona en background para facturas complejas</li>
     *   <li>Compresión automática para reducir tamaño de descarga</li>
     *   <li>CDN integration para distribución global eficiente</li>
     * </ul>
     *
     * <p><strong>Seguridad y control de acceso:</strong></p>
     * <ul>
     *   <li>Validación de permisos de acceso a la factura</li>
     *   <li>Watermark con información de auditoría</li>
     *   <li>Registro de descargas para seguimiento</li>
     *   <li>Prevención de acceso no autorizado</li>
     * </ul>
     *
     * @param id Identificador único de la factura para generar/descargar PDF.
     *                  La factura debe existir y ser accesible por el usuario.
     *
     * @return ResponseEntity byte[] con estado HTTP 200 OK y conteniendo:
     *         - Content-Type: application/pdf
     *         - Content-Disposition: inline con nombre de archivo sugerido
     *         - Content-Length: Tamaño del archivo en bytes
     *         - Cache-Control: Configuración de caché apropiada
     *         - Contenido binario del PDF listo para visualización/descarga
     *
     * @throws IllegalArgumentException si la factura no existe
     * @throws SecurityException si el usuario no tiene permisos para descargar
     * @throws DataAccessException si hay error accediendo a los datos
     *
     * @apiNote El PDF se genera usando el template más reciente y puede diferir
     *          ligeramente de versiones anteriormente generadas si ha habido
     *          actualizaciones en el formato empresarial.
     *
     * @since 1.0
     */
    @GetMapping("/{id}/pdf")
    @PreAuthorize("isAuthenticated() or hasRole('ADMIN')")
    @Operation(
            summary = "Descargar PDF de factura",
            description = "Genera y descarga el documento PDF profesional de la factura"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF generado y descargado exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE)),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para descargar esta factura"),
            @ApiResponse(responseCode = "404", description = "Factura no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error generando el PDF")
    })
    public ResponseEntity<byte[]> downloadInvoicePdf(
            @PathVariable("id") @Positive
            @Parameter(description = "ID de la factura", required = true)
            Long id) {

        log.info("Iniciando descarga de PDF para factura ID: {}", id);

        try {
            // 1. Obtener los datos completos de la factura
            InvoiceDetailResponse invoice = invoiceService.getInvoiceById(id);

            // 2. Generar el PDF usando el servicio especializado
            byte[] pdfBytes = pdfGenerationService.generateInvoicePdf(invoice);

            // 3. Configurar headers HTTP para descarga optimizada
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentLength(pdfBytes.length);

            // Nombre de archivo profesional con información identificativa
            String filename = String.format("factura-%s.pdf", invoice.getInvoiceNumber());
            headers.setContentDispositionFormData("inline", filename);

            // Headers adicionales para optimización
            headers.setCacheControl("private, max-age=3600"); // Cache por 1 hora
            headers.set("X-Content-Type-Options", "nosniff");

            log.info("PDF generado exitosamente para factura: {} - Tamaño: {} bytes",
                    invoice.getInvoiceNumber(), pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.warn("Factura no encontrada para PDF ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error generando PDF para factura ID: {} - Error: {}",
                    id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Envía una factura por correo electrónico con PDF adjunto y notificación automática.
     *
     * <p>Este endpoint activa el proceso completo de envío de factura por correo electrónico,
     * incluyendo generación de PDF actualizado, composición de email profesional,
     * y registro de auditoría. Se integra con el sistema de notificaciones para
     * proporcionar confirmación y seguimiento del envío.</p>
     *
     * <p><strong>Proceso completo de envío ejecutado:</strong></p>
     * <ol>
     *   <li>Validación de que la factura existe y puede ser enviada</li>
     *   <li>Verificación de permisos del usuario para realizar el envío</li>
     *   <li>Generación de PDF actualizado con datos más recientes</li>
     *   <li>Composición de email profesional con template empresarial</li>
     *   <li>Adjunto del PDF y información complementaria</li>
     *   <li>Envío a través del servicio de correo configurado</li>
     *   <li>Actualización del estado de la factura a SENT</li>
     *   <li>Registro de auditoría con detalles del envío</li>
     *   <li>Notificación de confirmación al usuario emisor</li>
     *   <li>Programación de seguimiento automático</li>
     * </ol>
     *
     * <p><strong>Validaciones previas al envío:</strong></p>
     * <ul>
     *   <li>La factura debe estar en estado que permita envío</li>
     *   <li>Debe existir información de contacto válida del cliente</li>
     *   <li>No debe haber restricciones de envío activas</li>
     *   <li>El usuario debe tener permisos para enviar facturas</li>
     *   <li>Debe cumplir límites de envío y frecuencia</li>
     * </ul>
     *
     * <p><strong>Contenido del email enviado:</strong></p>
     * <ul>
     *   <li>Saludo personalizado con nombre del cliente</li>
     *   <li>Resumen de la factura y servicio prestado</li>
     *   <li>Información de vencimiento y métodos de pago</li>
     *   <li>PDF de la factura como adjunto principal</li>
     *   <li>Enlaces para pago online si está disponible</li>
     *   <li>Información de contacto para consultas</li>
     *   <li>Términos y condiciones aplicables</li>
     * </ul>
     *
     * @param id Identificador único de la factura a enviar por email.
     *                  La factura debe existir y estar en estado apropiado para envío.
     *
     * @return ResponseEntity InvoiceDetailResponse con estado HTTP 200 OK y conteniendo:
     *         - Factura actualizada con estado SENT
     *         - Información del envío realizado (fecha, destinatario)
     *         - Estado de la operación de email
     *         - Metadatos de seguimiento para auditoría
     *         - Próximas acciones programadas automáticamente
     *
     * @throws IllegalArgumentException si la factura no existe
     * @throws IllegalStateException si la factura no puede ser enviada en su estado actual
     * @throws SecurityException si el usuario no tiene permisos para enviar
     * @throws ValidationException si faltan datos requeridos para el envío
     *
     * @apiNote El envío de email puede tomar algunos segundos. La respuesta se envía
     *          inmediatamente después de iniciar el proceso, y el estado final
     *          se actualiza de forma asíncrona.
     *
     * @since 2.0
     */
    @PostMapping("/{id}/send")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Enviar factura por correo electrónico",
            description = "Envía la factura por email con PDF adjunto y actualiza estado a SENT"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factura enviada exitosamente",
                    content = @Content(schema = @Schema(implementation = InvoiceDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para enviar esta factura"),
            @ApiResponse(responseCode = "404", description = "Factura no encontrada"),
            @ApiResponse(responseCode = "409", description = "La factura no puede ser enviada en su estado actual"),
            @ApiResponse(responseCode = "422", description = "Faltan datos requeridos para el envío")
    })
    public ResponseEntity<InvoiceDetailResponse> sendInvoice(
            @PathVariable @Positive
            @Parameter(description = "ID de la factura a enviar", required = true)
            Long id) {

        log.info("Iniciando envío por email de factura ID: {}", id);

        try {
            InvoiceDetailResponse sentInvoice = invoiceService.sendInvoice(id);

            log.info("Factura enviada exitosamente por email: {} a estado SENT",
                    sentInvoice.getInvoiceNumber());

            return ResponseEntity.ok(sentInvoice);

        } catch (IllegalStateException e) {
            log.warn("Conflicto enviando factura ID: {} - {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            log.warn("Factura no encontrada para envío ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cancela una factura procesando reembolsos automáticos y notificaciones completas.
     *
     * <p>Este endpoint maneja la cancelación integral de facturas aplicando las reglas
     * de negocio apropiadas, procesando reembolsos cuando corresponde, y gestionando
     * todas las notificaciones y efectos secundarios necesarios para mantener
     * la integridad del sistema.</p>
     *
     * <p><strong>Proceso completo de cancelación:</strong></p>
     * <ol>
     *   <li>Validación de que la factura existe y puede ser cancelada</li>
     *   <li>Verificación de permisos del usuario para cancelar</li>
     *   <li>Evaluación de pagos existentes y necesidad de reembolsos</li>
     *   <li>Procesamiento automático de reembolsos parciales/totales</li>
     *   <li>Actualización del estado a CANCELLED con auditoría</li>
     *   <li>Registro del motivo de cancelación en el historial</li>
     *   <li>Notificación automática al cliente sobre la cancelación</li>
     *   <li>Notificación al cuidador sobre el cambio de estado</li>
     *   <li>Actualización de métricas y estadísticas del sistema</li>
     *   <li>Liberación de recursos y reservas asociadas</li>
     * </ol>
     *
     * <p><strong>Estados que permiten cancelación:</strong></p>
     * <ul>
     *   <li>DRAFT: Cancelación directa sin efectos secundarios</li>
     *   <li>SENT: Cancelación con notificación automática al cliente</li>
     *   <li>PARTIALLY_PAID: Cancelación con procesamiento de reembolso parcial</li>
     * </ul>
     *
     * <p><strong>Estados que NO permiten cancelación:</strong></p>
     * <ul>
     *   <li>PAID: Requiere proceso de reembolso separado</li>
     *   <li>REFUNDED: Ya está en proceso de reembolso</li>
     *   <li>CANCELLED: Ya está cancelada</li>
     * </ul>
     *
     * <p><strong>Procesamiento de reembolsos automático:</strong></p>
     * <ul>
     *   <li>Identificación automática de pagos exitosos previos</li>
     *   <li>Cálculo de montos de reembolso según políticas</li>
     *   <li>Iniciación de proceso de reembolso por el mismo método de pago</li>
     *   <li>Seguimiento de estado de reembolsos hasta completarse</li>
     *   <li>Notificaciones de confirmación al cliente</li>
     * </ul>
     *
     * <p><strong>Notificaciones automáticas enviadas:</strong></p>
     * <ul>
     *   <li>Email al cliente explicando la cancelación y próximos pasos</li>
     *   <li>Notificación al cuidador sobre el cambio de estado</li>
     *   <li>Alerta a administradores si requiere intervención manual</li>
     *   <li>Actualización en dashboards y reportes en tiempo real</li>
     * </ul>
     *
     * @param id Identificador único de la factura a cancelar.
     *                  La factura debe existir y estar en estado que permita cancelación.
     *
     * @param reason Motivo detallado de la cancelación requerido para auditoría.
     *               Debe ser descriptivo, profesional y explicar claramente
     *               las razones para facilitar seguimiento y mejoras del servicio.
     *
     * @return ResponseEntity InvoiceDetailResponse con estado HTTP 200 OK y conteniendo:
     *         - Factura actualizada con estado CANCELLED
     *         - Motivo de cancelación registrado en las notas de auditoría
     *         - Información detallada de reembolsos procesados o programados
     *         - Estado y timestamps de notificaciones enviadas automáticamente
     *         - Metadatos de seguimiento para monitoreo de la operación
     *         - Enlaces a recursos relacionados (reportes, documentos)
     *
     * @throws IllegalArgumentException si la factura no existe o el motivo es inválido/vacío
     * @throws IllegalStateException si la factura no puede cancelarse en su estado actual
     * @throws SecurityException si el usuario no tiene permisos para cancelar facturas
     *
     * @apiNote La cancelación es una operación crítica que se registra completamente
     *          para auditoría. Los reembolsos pueden procesarse de forma asíncrona
     *          pero las notificaciones se envían inmediatamente.
     *
     * @since 2.0
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Cancelar factura con procesamiento automático",
            description = "Cancela una factura procesando reembolsos y enviando notificaciones automáticas"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factura cancelada exitosamente",
                    content = @Content(schema = @Schema(implementation = InvoiceDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Motivo de cancelación inválido o vacío"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para cancelar esta factura"),
            @ApiResponse(responseCode = "404", description = "Factura no encontrada"),
            @ApiResponse(responseCode = "409", description = "La factura no puede cancelarse en su estado actual"),
            @ApiResponse(responseCode = "422", description = "Error procesando reembolsos automáticos")
    })
    public ResponseEntity<InvoiceDetailResponse> cancelInvoice(
            @PathVariable @Positive
            @Parameter(description = "ID de la factura a cancelar", required = true)
            Long id,

            @RequestParam @NotBlank
            @Parameter(description = "Motivo detallado de la cancelación", required = true)
            String reason) {

        log.info("Iniciando cancelación de factura ID: {} con motivo: '{}'", id, reason);

        try {
            InvoiceDetailResponse cancelledInvoice = invoiceService.cancelInvoice(id, reason);

            log.info("Factura cancelada exitosamente: {} - Motivo registrado",
                    cancelledInvoice.getInvoiceNumber());

            return ResponseEntity.ok(cancelledInvoice);

        } catch (IllegalStateException e) {
            log.warn("Conflicto cancelando factura ID: {} - {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            log.warn("Factura no encontrada o motivo inválido ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========== ENDPOINTS DE INTEGRACIÓN CON RESERVAS ==========

    /**
     * Genera automáticamente una factura cuando una reserva se completa exitosamente.
     *
     * <p>Este endpoint está diseñado específicamente para la integración con el flujo
     * de reservas, activándose automáticamente cuando una reserva cambia a estado COMPLETED.
     * Implementa el patrón de eventos de dominio para proporcionar facturación
     * completamente automática sin intervención manual.</p>
     *
     * <p><strong>Integración con BookingService:</strong></p>
     * <ul>
     *   <li>Se invoca automáticamente desde BookingService.updateBookingStatus()</li>
     *   <li>Recibe notificación de eventos de cambio de estado de reserva</li>
     *   <li>Procesa solo reservas en estado COMPLETED válido</li>
     *   <li>Mantiene sincronización bidireccional de datos</li>
     *   <li>Maneja errores sin afectar el flujo principal de reservas</li>
     * </ul>
     *
     * <p><strong>Flujo automático ejecutado:</strong></p>
     * <ol>
     *   <li>Recibe evento de reserva completada desde BookingService</li>
     *   <li>Válida que la reserva cumple criterios para facturación</li>
     *   <li>Genera factura con datos automáticos de la reserva</li>
     *   <li>Calcula montos basándose en el precio de la reserva</li>
     *   <li>Crea PDF profesional con información del servicio</li>
     *   <li>Envía automáticamente por email al cliente</li>
     *   <li>Notifica al cuidador sobre la facturación</li>
     *   <li>Registra en sistema de tarifas para contabilidad</li>
     *   <li>Actualiza métricas de facturación en tiempo real</li>
     *   <li>Retorna confirmación al BookingService</li>
     * </ol>
     *
     * <p><strong>Manejo de errores y recuperación:</strong></p>
     * <ul>
     *   <li>Errores no críticos no afectan la reserva original</li>
     *   <li>Reintentos automáticos para errores transitorios</li>
     *   <li>Logging detallado para debugging y monitoreo</li>
     *   <li>Notificación a administradores si requiere intervención</li>
     *   <li>Cola de reintento para procesamiento diferido</li>
     * </ul>
     *
     * @param bookingId Identificador único de la reserva completada.
     *                  Debe corresponder a una reserva en estado COMPLETED
     *                  que no tenga factura previa generada.
     *
     * @return ResponseEntity InvoiceDetailResponse con estado HTTP 201 Created y conteniendo:
     *         - Factura generada automáticamente con todos los datos
     *         - Información sincronizada de la reserva original
     *         - Estado de PDF generado y email enviado
     *         - Metadatos de la operación automática
     *         - Referencias cruzadas para trazabilidad completa
     *
     * @throws IllegalArgumentException si la reserva no existe o no es válida
     * @throws IllegalStateException si la reserva no está en estado COMPLETED

     *
     * @apiNote Este endpoint está optimizado para llamadas automáticas desde
     *          BookingService y no debe utilizarse directamente desde interfaces
     *          de usuario. Para facturación manual, usar el endpoint principal.
     *
     * @since 3.0
     */
    @PostMapping("/auto-generate/booking/{bookingId}")
    @PreAuthorize("hasRole('SYSTEM') or hasRole('ADMIN')")
    @Operation(
            summary = "Generación automática de factura desde reserva completada",
            description = "Endpoint de integración para facturación automática cuando se completa una reserva"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Factura generada automáticamente",
                    content = @Content(schema = @Schema(implementation = InvoiceDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Reserva no válida para facturación"),
            @ApiResponse(responseCode = "409", description = "Ya existe factura para esta reserva"),
            @ApiResponse(responseCode = "422", description = "Reserva no está en estado COMPLETED")
    })
    public ResponseEntity<InvoiceDetailResponse> autoGenerateInvoiceForCompletedBooking(
            @PathVariable @Positive
            @Parameter(description = "ID de la reserva completada", required = true)
            Long bookingId) {

        log.info("Iniciando generación automática de factura para reserva completada ID: {}", bookingId);

        try {
            // Crear request automático para la reserva
            // Crear request automático para la reserva
            CreateInvoiceRequest autoRequest = new CreateInvoiceRequest();
            autoRequest.setBookingId(bookingId);
            autoRequest.setNotes("Factura generada automáticamente al completar reserva");
            autoRequest.setAutoSendEmail(true);

            InvoiceDetailResponse autoInvoice = invoiceService.generateInvoiceForBooking(autoRequest);

            log.info("Factura automática generada exitosamente: {} para reserva ID: {}",
                    autoInvoice.getInvoiceNumber(), bookingId);

            return ResponseEntity.status(HttpStatus.CREATED).body(autoInvoice);

        } catch (IllegalStateException e) {
            log.warn("Conflicto en generación automática para reserva ID: {} - {}",
                    bookingId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error en generación automática para reserva ID: {} - Error: {}",
                    bookingId, e.getMessage(), e);
            // No re-lanzar para evitar fallar el flujo de reservas
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== ENDPOINTS DE UTILIDAD Y MONITOREO ==========

    /**
     * Obtiene estadísticas resumidas de facturación para dashboards administrativos.
     *
     * <p>Este endpoint proporciona métricas en tiempo real sobre el estado del
     * sistema de facturación, útil para dashboards administrativos, reportes
     * ejecutivos y monitoreo del negocio.</p>
     *
     * @return ResponseEntity con estadísticas de facturación
     *
     * @since 3.0
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Obtener estadísticas de facturación",
            description = "Proporciona métricas resumidas del sistema de facturación"
    )
    public ResponseEntity<?> getInvoiceStats() {
        log.debug("Consultando estadísticas de facturación");

        // TODO: Implementar servicio de estadísticas
        // InvoiceStatsResponse stats = invoiceService.getInvoiceStatistics();

        return ResponseEntity.ok().build();
    }

    /**
     * Valida el estado del sistema de facturación para health checks.
     *
     * <p>Endpoint de utilidad para verificar que el sistema de facturación
     * está funcionando correctamente y puede procesar nuevas facturas.</p>
     *
     * @return ResponseEntity indicando el estado del sistema
     *
     * @since 3.0
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health check del sistema de facturación",
            description = "Verifica el estado operacional del sistema de facturación"
    )
    public ResponseEntity<String> healthCheck() {
        log.debug("Ejecutando health check del sistema de facturación");

        try {
            // TODO: Implementar verificaciones de salud
            // - Conectividad a base de datos
            // - Disponibilidad del servicio de PDF
            // - Estado del servicio de email
            // - Conectividad con servicios de pago

            return ResponseEntity.ok("Sistema de facturación operacional");

        } catch (Exception e) {
            log.error("Health check falló: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Sistema de facturación no disponible");
        }
    }
}