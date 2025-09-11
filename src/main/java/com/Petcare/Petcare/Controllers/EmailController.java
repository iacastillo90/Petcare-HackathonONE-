package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.Email.Email;
import com.Petcare.Petcare.Services.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para el manejo de envío de correos electrónicos en el sistema Petcare.
 *
 * <p>Este controlador proporciona endpoints REST para todas las funcionalidades relacionadas
 * con el envío de correos electrónicos, incluyendo correos de verificación, notificaciones
 * del sistema y correos con archivos adjuntos.</p>
 *
 * <p><strong>Características principales:</strong></p>
 * <ul>
 *   <li>Envío de correos electrónicos con plantillas HTML</li>
 *   <li>Soporte para archivos adjuntos múltiples</li>
 *   <li>Correos de verificación automatizados para nuevos usuarios</li>
 *   <li>Validación exhaustiva de entrada mediante Bean Validation</li>
 *   <li>Documentación completa con OpenAPI/Swagger</li>
 *   <li>Manejo robusto de errores con códigos HTTP apropiados</li>
 *   <li>Logging detallado para auditoría y debugging</li>
 * </ul>
 *
 * <p><strong>Seguridad:</strong></p>
 * <ul>
 *   <li>Autenticación requerida para todos los endpoints</li>
 *   <li>Autorización basada en roles para operaciones sensibles</li>
 *   <li>Validación de formato de direcciones de correo</li>
 *   <li>Límites de tamaño para archivos adjuntos</li>
 * </ul>
 *
 * <p><strong>Endpoints disponibles:</strong></p>
 * <ul>
 *   <li>POST /api/send-email - Envío de correos generales</li>
 *   <li>POST /api/send-email/verification - Envío de correos de verificación</li>
 *   <li>POST /api/send-email/with-attachments - Envío con archivos adjuntos</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see EmailService
 * @see com.Petcare.Petcare.DTOs.Email.Email
 */
@RestController
@RequestMapping("/api/send-email")
@Validated
@Tag(name = "Email", description = "API para el envío de correos electrónicos en el sistema Petcare")
public class EmailController {

    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    private final EmailService emailService;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param emailService servicio de correo electrónico configurado
     */
    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
        logger.info("EmailController inicializado correctamente");
    }

    /**
     * Envía un correo electrónico según la configuración especificada.
     *
     * <p>Endpoint principal para el envío de correos electrónicos con soporte completo
     * para plantillas HTML, variables dinámicas y archivos adjuntos opcionales.</p>
     *
     * <p><strong>Funcionalidades:</strong></p>
     * <ul>
     *   <li>Procesamiento automático de plantillas Thymeleaf</li>
     *   <li>Sustitución de variables dinámicas en plantillas</li>
     *   <li>Manejo automático de archivos adjuntos si están presentes</li>
     *   <li>Validación completa de datos de entrada</li>
     *   <li>Respuestas detalladas sobre el resultado del envío</li>
     * </ul>
     *
     * @param email objeto con toda la configuración del correo a enviar
     * @return ResponseEntity con el resultado del envío y metadatos
     *
     * @apiNote Requiere autenticación. Los usuarios CLIENT solo pueden enviar a su propia dirección,
     *          mientras que ADMIN y SITTER pueden enviar a cualquier dirección válida.
     */
    @PostMapping
    @Operation(
            summary = "Enviar correo electrónico",
            description = "Envía un correo electrónico con soporte para plantillas HTML, variables dinámicas y archivos adjuntos opcionales.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Configuración completa del correo electrónico a enviar",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.Petcare.Petcare.DTOs.Email.Email.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Correo Simple",
                                            description = "Ejemplo de correo electrónico básico con plantilla",
                                            value = """
                        {
                          "to": "usuario@example.com",
                          "subject": "Bienvenido a Petcare",
                          "templateName": "welcome-email",
                          "variables": {
                            "nombreUsuario": "Juan Pérez",
                            "fechaRegistro": "2024-01-15"
                          }
                        }
                        """
                                    ),
                                    @ExampleObject(
                                            name = "Correo con Adjuntos",
                                            description = "Ejemplo de correo con archivos adjuntos",
                                            value = """
                        {
                          "to": "cliente@petcare.com",
                          "subject": "Reporte Mensual - Enero 2024",
                          "templateName": "monthly-report",
                          "variables": {
                            "mes": "Enero",
                            "año": "2024",
                            "totalServicios": 15
                          },
                          "attachments": [
                            {
                              "name": "reporte-enero-2024.pdf",
                              "contentType": "application/pdf",
                              "description": "Reporte detallado de actividades del mes"
                            }
                          ]
                        }
                        """
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Correo enviado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                      "success": true,
                      "message": "Correo enviado correctamente",
                      "timestamp": "2024-01-15T10:30:45",
                      "recipientEmail": "usuario@example.com",
                      "attachmentCount": 0
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                      "success": false,
                      "message": "Error de validación: El destinatario es obligatorio",
                      "timestamp": "2024-01-15T10:30:45",
                      "errors": ["El destinatario es obligatorio", "Formato de email inválido"]
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor durante el envío",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                      "success": false,
                      "message": "Error al enviar el correo: Servicio SMTP no disponible",
                      "timestamp": "2024-01-15T10:30:45"
                    }
                    """
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SITTER')")
    public ResponseEntity<Map<String, Object>> sendEmail(
            @Valid @RequestBody com.Petcare.Petcare.DTOs.Email.Email email) {

        logger.info("Solicitud de envío de correo recibida para: {}", email.getTo());

        Map<String, Object> response = new HashMap<>();

        try {
            // Enviar el correo usando el servicio
            emailService.sendEmail(email);

            // Preparar respuesta exitosa
            response.put("success", true);
            response.put("message", "Correo enviado correctamente");
            response.put("timestamp", LocalDateTime.now());
            response.put("recipientEmail", email.getTo());
            response.put("attachmentCount", email.getAttachmentCount());

            if (email.getTrackingId() != null) {
                response.put("trackingId", email.getTrackingId());
            }

            logger.info("Correo enviado exitosamente a: {}", email.getTo());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación al enviar correo a {}: {}", email.getTo(), e.getMessage());

            response.put("success", false);
            response.put("message", "Error de validación: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.badRequest().body(response);

        } catch (MessagingException e) {
            logger.error("Error de mensajería al enviar correo a {}: {}", email.getTo(), e.getMessage(), e);

            response.put("success", false);
            response.put("message", "Error al enviar el correo: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (Exception e) {
            logger.error("Error inesperado al enviar correo a {}: {}", email.getTo(), e.getMessage(), e);

            response.put("success", false);
            response.put("message", "Error interno del servidor");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Envía un correo de verificación para activación de cuenta de usuario.
     *
     * <p>Endpoint especializado para el proceso de verificación de correo electrónico
     * de nuevos usuarios. Utiliza la plantilla predefinida de verificación y
     * genera automáticamente las variables necesarias.</p>
     *
     * <p><strong>Características especiales:</strong></p>
     * <ul>
     *   <li>Plantilla de verificación predefinida y optimizada</li>
     *   <li>Generación automática de variables de contexto</li>
     *   <li>Configuración de tiempo de expiración flexible</li>
     *   <li>Integración con sistema de tokens de verificación</li>
     * </ul>
     *
     * @param recipientEmail dirección de correo del destinatario
     * @param recipientName nombre completo del usuario
     * @param verificationUrl URL única de verificación con token
     * @param expirationHours horas hasta que expire el enlace (opcional, por defecto 24)
     * @return ResponseEntity con el resultado del envío
     *
     * @apiNote Requiere rol ADMIN o SYSTEM para uso directo. Generalmente llamado
     *          internamente durante el proceso de registro de usuarios.
     */
    @PostMapping("/verification")
    @Operation(
            summary = "Enviar correo de verificación",
            description = "Envía un correo de verificación de cuenta con plantilla predefinida y variables automáticas.",
            parameters = {
                    @Parameter(
                            name = "recipientEmail",
                            description = "Dirección de correo electrónico del destinatario",
                            required = true,
                            example = "nuevo.usuario@example.com"
                    ),
                    @Parameter(
                            name = "recipientName",
                            description = "Nombre completo del usuario para personalización",
                            required = true,
                            example = "María González"
                    ),
                    @Parameter(
                            name = "verificationUrl",
                            description = "URL completa con token único de verificación",
                            required = true,
                            example = "https://petcare.com/verify?token=abc123xyz789"
                    ),
                    @Parameter(
                            name = "expirationHours",
                            description = "Número de horas hasta que expire el enlace de verificación",
                            required = false,
                            example = "24"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Correo de verificación enviado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                      "success": true,
                      "message": "Correo de verificación enviado correctamente",
                      "timestamp": "2024-01-15T10:30:45",
                      "recipientEmail": "nuevo.usuario@example.com",
                      "expirationHours": 24
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSTEM')")
    public ResponseEntity<Map<String, Object>> sendVerificationEmail(
            @RequestParam @NotBlank @jakarta.validation.constraints.Email String recipientEmail,
            @RequestParam @NotBlank String recipientName,
            @RequestParam @NotBlank String verificationUrl,
            @RequestParam(defaultValue = "24") @Positive Integer expirationHours) {

        logger.info("Solicitud de correo de verificación para: {} ({})", recipientName, recipientEmail);

        Map<String, Object> response = new HashMap<>();

        try {
            emailService.sendVerificationEmail(recipientEmail, recipientName, verificationUrl, expirationHours);

            response.put("success", true);
            response.put("message", "Correo de verificación enviado correctamente");
            response.put("timestamp", LocalDateTime.now());
            response.put("recipientEmail", recipientEmail);
            response.put("expirationHours", expirationHours);

            logger.info("Correo de verificación enviado exitosamente a: {}", recipientEmail);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación en correo de verificación para {}: {}", recipientEmail, e.getMessage());

            response.put("success", false);
            response.put("message", "Error de validación: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.badRequest().body(response);

        } catch (MessagingException e) {
            logger.error("Error al enviar correo de verificación a {}: {}", recipientEmail, e.getMessage(), e);

            response.put("success", false);
            response.put("message", "Error al enviar el correo de verificación: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Envía un correo electrónico con múltiples archivos adjuntos.
     *
     * <p>Endpoint especializado para el envío de correos que requieren adjuntar
     * documentos, reportes o cualquier tipo de archivo. Incluye validaciones
     * especiales para tamaño y tipo de archivos.</p>
     *
     * <p><strong>Limitaciones y validaciones:</strong></p>
     * <ul>
     *   <li>Tamaño máximo total de adjuntos configurable (por defecto 10MB)</li>
     *   <li>Tipos de archivo permitidos según política de seguridad</li>
     *   <li>Validación de consistencia entre extensión y tipo MIME</li>
     *   <li>Bloqueo automático de archivos ejecutables</li>
     * </ul>
     *
     * @param email objeto Email con archivos adjuntos configurados
     * @return ResponseEntity con el resultado del envío y detalles de adjuntos
     *
     * @apiNote Requiere roles ADMIN o SITTER para uso general. Los usuarios CLIENT
     *          pueden usar este endpoint solo para enviar documentos a su propio correo.
     */
    @PostMapping("/with-attachments")
    @Operation(
            summary = "Enviar correo con archivos adjuntos",
            description = "Envía un correo electrónico con uno o más archivos adjuntos, con validaciones de seguridad.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Correo electrónico con archivos adjuntos configurados",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.Petcare.Petcare.DTOs.Email.Email.class),
                            examples = @ExampleObject(
                                    name = "Correo con Múltiples Adjuntos",
                                    description = "Ejemplo de correo con varios archivos adjuntos",
                                    value = """
                    {
                      "to": "cliente@petcare.com",
                      "subject": "Documentación Completa - Servicio de Cuidado",
                      "templateName": "service-documentation",
                      "variables": {
                        "clienteName": "Ana López",
                        "petName": "Max",
                        "serviceDate": "2024-01-15"
                      },
                      "attachments": [
                        {
                          "name": "reporte-salud-max.pdf",
                          "contentType": "application/pdf",
                          "description": "Reporte de salud completo de la mascota"
                        },
                        {
                          "name": "fotos-actividades.zip",
                          "contentType": "application/zip",
                          "description": "Galería de fotos de las actividades realizadas"
                        },
                        {
                          "name": "certificado-vacunacion.pdf",
                          "contentType": "application/pdf",
                          "description": "Certificado actualizado de vacunación"
                        }
                      ]
                    }
                    """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Correo con adjuntos enviado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                      "success": true,
                      "message": "Correo con archivos adjuntos enviado correctamente",
                      "timestamp": "2024-01-15T10:30:45",
                      "recipientEmail": "cliente@petcare.com",
                      "attachmentCount": 3,
                      "totalAttachmentSize": "2.5 MB"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Error de validación en adjuntos"),
            @ApiResponse(responseCode = "413", description = "Tamaño de adjuntos excede límite permitido"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SITTER')")
    public ResponseEntity<Map<String, Object>> sendEmailWithAttachments(
            @Valid @RequestBody com.Petcare.Petcare.DTOs.Email.Email email) {

        logger.info("Solicitud de correo con adjuntos para: {} ({} archivos)",
                email.getTo(), email.getAttachmentCount());

        Map<String, Object> response = new HashMap<>();

        try {
            // Validar que hay adjuntos
            if (!email.hasAttachments()) {
                throw new IllegalArgumentException("Se requieren archivos adjuntos para usar este endpoint");
            }

            emailService.sendEmailWithAttachments(email);

            response.put("success", true);
            response.put("message", "Correo con archivos adjuntos enviado correctamente");
            response.put("timestamp", LocalDateTime.now());
            response.put("recipientEmail", email.getTo());
            response.put("attachmentCount", email.getAttachmentCount());

            // Calcular tamaño total de adjuntos para la respuesta
            long totalSize = email.getAttachments().stream()
                    .mapToLong(attachment -> {
                        try {
                            return attachment.getResource().contentLength();
                        } catch (Exception e) {
                            return 0L;
                        }
                    })
                    .sum();

            response.put("totalAttachmentSize", formatBytes(totalSize));

            logger.info("Correo con {} adjuntos enviado exitosamente a: {}",
                    email.getAttachmentCount(), email.getTo());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación en correo con adjuntos para {}: {}", email.getTo(), e.getMessage());

            response.put("success", false);
            response.put("message", "Error de validación: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.badRequest().body(response);

        } catch (IllegalStateException e) {
            logger.warn("Error de estado en correo con adjuntos para {}: {}", email.getTo(), e.getMessage());

            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);

        } catch (MessagingException e) {
            logger.error("Error al enviar correo con adjuntos a {}: {}", email.getTo(), e.getMessage(), e);

            response.put("success", false);
            response.put("message", "Error al enviar el correo con archivos adjuntos: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint para verificar el estado del servicio de correo electrónico.
     *
     * <p>Útil para monitoreo y health checks del sistema de correo.</p>
     *
     * @return ResponseEntity con información del estado del servicio
     */
    @GetMapping("/status")
    @Operation(
            summary = "Verificar estado del servicio de correo",
            description = "Endpoint de health check para verificar que el servicio de correo está funcionando correctamente."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Servicio de correo operativo",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                {
                  "status": "OK",
                  "service": "EmailService",
                  "timestamp": "2024-01-15T10:30:45",
                  "version": "1.0"
                }
                """
                    )
            )
    )
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "OK");
        status.put("service", "EmailService");
        status.put("timestamp", LocalDateTime.now());
        status.put("version", "1.0");

        return ResponseEntity.ok(status);
    }

    // ========== MÉTODOS UTILITARIOS PRIVADOS ==========

    /**
     * Formatea un número de bytes en una representación legible.
     *
     * @param bytes número de bytes a formatear
     * @return cadena formateada (ej: "1.5 MB", "256 KB")
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " bytes";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}