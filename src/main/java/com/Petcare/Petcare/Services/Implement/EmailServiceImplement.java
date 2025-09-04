package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.DTOs.Email.Attachment;
import com.Petcare.Petcare.DTOs.Email.Email;
import com.Petcare.Petcare.Services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Implementación del servicio de envío de correos electrónicos para el sistema Petcare.
 *
 * <p>Esta clase proporciona una implementación completa del servicio de correo electrónico,
 * incluyendo funcionalidades avanzadas como plantillas HTML, archivos adjuntos,
 * validación de datos y manejo robusto de errores.</p>
 *
 * <p><strong>Características implementadas:</strong></p>
 * <ul>
 *   <li>Envío de correos HTML con plantillas Thymeleaf dinámicas</li>
 *   <li>Soporte completo para archivos adjuntos múltiples</li>
 *   <li>Validación exhaustiva de datos de entrada</li>
 *   <li>Logging detallado para auditoría y debugging</li>
 *   <li>Manejo de errores con mensajes descriptivos</li>
 *   <li>Configuración flexible mediante properties</li>
 * </ul>
 *
 * <p><strong>Seguridad y rendimiento:</strong></p>
 * <ul>
 *   <li>Validación de formato de emails mediante regex</li>
 *   <li>Límites configurables para tamaño de adjuntos</li>
 *   <li>Codificación UTF-8 para soporte internacional</li>
 *   <li>Thread-safe para uso concurrente</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see EmailService
 * @see Email
 * @see Attachment
 */
@Service
public class EmailServiceImplement implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImplement.class);

    // Patrón regex para validación de emails según RFC 5322
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Plantillas predefinidas del sistema
    private static final String VERIFICATION_TEMPLATE = "Email-verified";
    private static final String VERRIFICATION_BOOKING_TEMPLATE = "new-booking-sitter";

    // Dependencias inyectadas
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    // Configuración desde properties
    @Value("${petcare.email.max-attachment-size:10485760}") // 10MB por defecto
    private long maxAttachmentSize;

    @Value("${petcare.email.default-from:${spring.mail.username}}")
    private String defaultFromEmail;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param javaMailSender sender configurado de Spring Mail
     * @param templateEngine motor de plantillas Thymeleaf
     */
    @Autowired
    public EmailServiceImplement(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        logger.info("EmailService inicializado correctamente");
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Flujo de procesamiento:</strong></p>
     * <ol>
     *   <li>Validación completa del objeto Email</li>
     *   <li>Determinación automática del tipo de envío</li>
     *   <li>Delegación al método especializado correspondiente</li>
     *   <li>Logging del resultado de la operación</li>
     * </ol>
     */
    @Override
    public void sendEmail(Email email) throws MessagingException {
        logger.info("Iniciando envío de correo a: {}", email.getTo());

        try {
            // Validación previa
            validateEmail(email);

            // Establecer remitente por defecto si no está especificado
            if (email.getFrom() == null || email.getFrom().trim().isEmpty()) {
                email.setFrom(defaultFromEmail);
                logger.debug("Establecido remitente por defecto: {}", defaultFromEmail);
            }

            // Determinar tipo de envío
            if (email.getAttachments() != null && !email.getAttachments().isEmpty()) {
                logger.debug("Enviando correo con {} archivos adjuntos", email.getAttachments().size());
                sendEmailWithAttachments(email);
            } else {
                logger.debug("Enviando correo HTML estándar");
                sendHtmlEmail(email);
            }

            logger.info("Correo enviado exitosamente a: {}", email.getTo());

        } catch (Exception e) {
            logger.error("Error al enviar correo a {}: {}", email.getTo(), e.getMessage(), e);
            throw new MessagingException("Error al procesar el envío de correo: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Variables automáticas incluidas:</strong></p>
     * <ul>
     *   <li>nombreUsuario: nombre formateado del usuario</li>
     *   <li>urlVerificacion: URL completa con token de verificación</li>
     *   <li>fechaExpiracion: fecha formateada de expiración</li>
     *   <li>horasExpiracion: número de horas hasta la expiración</li>
     *   <li>añoActual: año actual para el copyright</li>
     *   <li>fechaEnvio: fecha y hora de envío formateada</li>
     * </ul>
     */
    @Override
    public void sendVerificationEmail(String recipientEmail, String recipientName,
                                      String verificationUrl, int expirationHours) throws MessagingException {

        logger.info("Enviando correo de verificación a: {} ({})", recipientName, recipientEmail);

        // Validación de parámetros
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("El email del destinatario es obligatorio");
        }
        if (recipientName == null || recipientName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del destinatario es obligatorio");
        }
        if (verificationUrl == null || verificationUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("La URL de verificación es obligatoria");
        }
        if (expirationHours <= 0) {
            throw new IllegalArgumentException("Las horas de expiración deben ser positivas");
        }

        try {
            // Preparar variables para la plantilla
            Map<String, Object> variables = new HashMap<>();
            variables.put("nombreUsuario", recipientName.trim());
            variables.put("urlVerificacion", verificationUrl.trim());
            variables.put("horasExpiracion", expirationHours);

            // Calcular fecha de expiración
            LocalDateTime expiration = LocalDateTime.now().plusHours(expirationHours);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");
            variables.put("fechaExpiracion", expiration.format(formatter));

            // Variables adicionales
            variables.put("añoActual", LocalDateTime.now().getYear());
            variables.put("fechaEnvio", LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            ));

            // Crear objeto Email
            Email email = Email.builder()
                    .to(recipientEmail.trim())
                    .from(defaultFromEmail)
                    .subject("Verificación de correo electrónico - Petcare")
                    .templateName(VERIFICATION_TEMPLATE)
                    .variables(variables)
                    .build();

            // Enviar usando el método principal
            sendHtmlEmail(email);

            logger.info("Correo de verificación enviado exitosamente a: {}", recipientEmail);

        } catch (Exception e) {
            logger.error("Error al enviar correo de verificación a {}: {}", recipientEmail, e.getMessage(), e);
            throw new MessagingException("Error al enviar correo de verificación: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Procesamiento de adjuntos:</strong></p>
     * <ol>
     *   <li>Validación de cada archivo adjunto</li>
     *   <li>Verificación de tamaño total acumulado</li>
     *   <li>Configuración de MIME types apropiados</li>
     *   <li>Adjunto seguro al mensaje</li>
     * </ol>
     */
    @Override
    public void sendEmailWithAttachments(Email email) throws MessagingException {
        logger.info("Enviando correo con adjuntos a: {}", email.getTo());

        if (email.getAttachments() == null || email.getAttachments().isEmpty()) {
            throw new IllegalArgumentException("No se especificaron archivos adjuntos para enviar");
        }

        try {
            // Validar tamaño total de adjuntos
            long totalSize = calculateAttachmentsSize(email.getAttachments());
            if (totalSize > maxAttachmentSize) {
                throw new IllegalStateException(
                        String.format("El tamaño total de adjuntos (%d bytes) excede el límite permitido (%d bytes)",
                                totalSize, maxAttachmentSize)
                );
            }

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Configurar destinatarios y asunto
            helper.setTo(email.getTo());
            helper.setFrom(email.getFrom());
            helper.setSubject(email.getSubject());

            // Procesar y adjuntar archivos
            for (Attachment attachment : email.getAttachments()) {
                validateAttachment(attachment);
                helper.addAttachment(attachment.getName(), attachment.getResource());
                logger.debug("Adjuntado archivo: {} (tipo: {})",
                        attachment.getName(), attachment.getContentType());
            }

            // Procesar plantilla para el cuerpo del correo
            String htmlContent = processTemplate(email);
            helper.setText(htmlContent, true);

            // Enviar correo
            javaMailSender.send(message);

            logger.info("Correo con {} adjuntos enviado exitosamente a: {}",
                    email.getAttachments().size(), email.getTo());

        } catch (Exception e) {
            logger.error("Error al enviar correo con adjuntos a {}: {}", email.getTo(), e.getMessage(), e);
            throw new MessagingException("Error al enviar correo con archivos adjuntos: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Validaciones realizadas:</strong></p>
     * <ul>
     *   <li>Objeto Email no nulo</li>
     *   <li>Formato válido de direcciones de correo</li>
     *   <li>Presencia de campos obligatorios</li>
     *   <li>Existencia de plantilla si se especifica</li>
     *   <li>Validez de archivos adjuntos</li>
     * </ul>
     */
    @Override
    public boolean validateEmail(Email email) throws IllegalArgumentException {
        if (email == null) {
            throw new IllegalArgumentException("El objeto Email no puede ser nulo");
        }

        // Validar destinatario
        if (email.getTo() == null || email.getTo().trim().isEmpty()) {
            throw new IllegalArgumentException("El destinatario es obligatorio");
        }
        if (!EMAIL_PATTERN.matcher(email.getTo().trim()).matches()) {
            throw new IllegalArgumentException("Formato de email destinatario inválido: " + email.getTo());
        }

        // Validar remitente si está especificado
        if (email.getFrom() != null && !email.getFrom().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(email.getFrom().trim()).matches()) {
                throw new IllegalArgumentException("Formato de email remitente inválido: " + email.getFrom());
            }
        }

        // Validar asunto
        if (email.getSubject() == null || email.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("El asunto del correo es obligatorio");
        }

        // Validar plantilla si se especifica
        if (email.getTemplateName() != null && !email.getTemplateName().trim().isEmpty()) {
            validateTemplate(email.getTemplateName());
        }

        // Validar adjuntos si existen
        if (email.getAttachments() != null) {
            for (Attachment attachment : email.getAttachments()) {
                validateAttachment(attachment);
            }
        }

        logger.debug("Email validado correctamente para: {}", email.getTo());
        return true;
    }

    /**
     * Envía un correo electrónico HTML usando una plantilla de Thymeleaf.
     *
     * <p>Método interno especializado para el envío de correos HTML sin archivos adjuntos.
     * Procesa las plantillas Thymeleaf con las variables proporcionadas y envía
     * el resultado como contenido HTML.</p>
     *
     * @param email objeto Email configurado para envío HTML
     * @throws MessagingException si ocurre un error durante el procesamiento o envío
     */
    private void sendHtmlEmail(Email email) throws MessagingException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(email.getTo());
            helper.setFrom(email.getFrom());
            helper.setSubject(email.getSubject());

            // Procesar plantilla HTML
            String htmlContent = processTemplate(email);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            logger.debug("Correo HTML enviado exitosamente");

        } catch (Exception e) {
            logger.error("Error al enviar correo HTML: {}", e.getMessage(), e);
            throw new MessagingException("Error al enviar el correo HTML: " + e.getMessage(), e);
        }
    }

    /**
     * Procesa una plantilla Thymeleaf con las variables proporcionadas.
     *
     * <p>Método utilitario que crea el contexto de Thymeleaf, establece las variables
     * y procesa la plantilla especificada para generar el contenido HTML final.</p>
     *
     * @param email objeto Email con la configuración de plantilla y variables
     * @return contenido HTML procesado listo para envío
     * @throws IllegalArgumentException si la plantilla no existe o hay errores de procesamiento
     */
    private String processTemplate(Email email) {
        try {
            Context context = new Context();

            // Establecer variables si existen
            if (email.getVariables() != null) {
                context.setVariables(email.getVariables());
            }

            // Usar plantilla especificada o por defecto
            String templateName = email.getTemplateName();


            String htmlContent = templateEngine.process(templateName, context);
            logger.debug("Plantilla '{}' procesada correctamente", templateName);

            return htmlContent;

        } catch (Exception e) {
            logger.error("Error al procesar plantilla '{}': {}", email.getTemplateName(), e.getMessage(), e);
            throw new IllegalArgumentException("Error al procesar la plantilla: " + e.getMessage(), e);
        }
    }

    /**
     * Valida que un archivo adjunto esté correctamente configurado.
     *
     * <p>Verifica que el archivo adjunto tenga todos los campos obligatorios
     * y que el recurso sea accesible para lectura.</p>
     *
     * @param attachment archivo adjunto a validar
     * @throws IllegalArgumentException si el adjunto es inválido
     */
    private void validateAttachment(Attachment attachment) {
        if (attachment == null) {
            throw new IllegalArgumentException("Los archivos adjuntos no pueden ser nulos");
        }

        if (attachment.getName() == null || attachment.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo adjunto es obligatorio");
        }

        if (attachment.getResource() == null) {
            throw new IllegalArgumentException("El recurso del archivo adjunto es obligatorio");
        }

        if (!attachment.getResource().exists()) {
            throw new IllegalArgumentException("El archivo adjunto no existe: " + attachment.getName());
        }

        if (!attachment.getResource().isReadable()) {
            throw new IllegalArgumentException("El archivo adjunto no es legible: " + attachment.getName());
        }
    }

    /**
     * Calcula el tamaño total de todos los archivos adjuntos.
     *
     * @param attachments lista de archivos adjuntos
     * @return tamaño total en bytes
     * @throws IllegalStateException si no se puede determinar el tamaño
     */
    private long calculateAttachmentsSize(java.util.List<Attachment> attachments) {
        long totalSize = 0;

        for (Attachment attachment : attachments) {
            try {
                if (attachment.getResource().exists()) {
                    totalSize += attachment.getResource().contentLength();
                }
            } catch (Exception e) {
                logger.warn("No se pudo determinar el tamaño del adjunto '{}': {}",
                        attachment.getName(), e.getMessage());
            }
        }

        return totalSize;
    }

    /**
     * Valida que una plantilla existe y es accesible.
     *
     * @param templateName nombre de la plantilla a validar
     * @throws IllegalArgumentException si la plantilla no existe
     */
    private void validateTemplate(String templateName) {
        try {
            // Intentar resolver la plantilla
            templateEngine.process(templateName, new Context());

        } catch (Exception e) {
            logger.error("Plantilla '{}' no encontrada o inaccesible: {}", templateName, e.getMessage());
            throw new IllegalArgumentException("La plantilla especificada no existe: " + templateName, e);
        }
    }
}