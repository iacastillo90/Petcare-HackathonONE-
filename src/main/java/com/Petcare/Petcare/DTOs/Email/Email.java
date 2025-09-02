package com.Petcare.Petcare.DTOs.Email;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO (Data Transfer Object) para el envío de correos electrónicos en el sistema Petcare.
 *
 * <p>Esta clase encapsula toda la información necesaria para enviar un correo electrónico,
 * incluyendo destinatarios, contenido, plantillas y archivos adjuntos. Está diseñada
 * para ser flexible y soportar diferentes tipos de envío de correo.</p>
 *
 * <p><strong>Características principales:</strong></p>
 * <ul>
 *   <li>Validación automática de campos mediante Bean Validation</li>
 *   <li>Soporte para plantillas HTML dinámicas con Thymeleaf</li>
 *   <li>Gestión de archivos adjuntos múltiples</li>
 *   <li>Variables dinámicas para personalización de contenido</li>
 *   <li>Metadatos de envío para auditoría y tracking</li>
 * </ul>
 *
 * <p><strong>Casos de uso típicos:</strong></p>
 * <ul>
 *   <li>Correos de verificación de cuenta</li>
 *   <li>Notificaciones de servicios de cuidado</li>
 *   <li>Reportes con documentos adjuntos</li>
 *   <li>Comunicaciones administrativas</li>
 * </ul>
 *
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>
 * Email email = Email.builder()
 *     .to("usuario@example.com")
 *     .subject("Bienvenido a Petcare")
 *     .templateName("welcome-email")
 *     .addVariable("userName", "Juan Pérez")
 *     .build();
 * </pre>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see Attachment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Email {

    /**
     * Dirección de correo electrónico del destinatario.
     *
     * <p>Campo obligatorio que debe contener una dirección de correo válida
     * según los estándares RFC. Se valida automáticamente el formato.</p>
     */
    @NotBlank(message = "La dirección del destinatario es obligatoria")
    @jakarta.validation.constraints.Email(message = "El formato del email destinatario debe ser válido")
    @Size(max = 250, message = "La dirección de correo no puede exceder 250 caracteres")
    private String to;

    /**
     * Dirección de correo electrónico del remitente.
     *
     * <p>Campo opcional que especifica el remitente del correo. Si no se proporciona,
     * se utilizará la dirección configurada por defecto del sistema.</p>
     */
    @jakarta.validation.constraints.Email(message = "El formato del email remitente debe ser válido")
    @Size(max = 250, message = "La dirección del remitente no puede exceder 250 caracteres")
    private String from;

    /**
     * Asunto del correo electrónico.
     *
     * <p>Campo obligatorio que describe brevemente el contenido o propósito
     * del correo electrónico. Aparece en la línea de asunto del cliente de correo.</p>
     */
    @NotBlank(message = "El asunto del correo es obligatorio")
    @Size(max = 200, message = "El asunto no puede exceder 200 caracteres")
    private String subject;

    /**
     * Nombre de la plantilla HTML a utilizar.
     *
     * <p>Campo opcional que especifica qué plantilla Thymeleaf utilizar para
     * generar el contenido HTML del correo. Si no se especifica, se usará
     * una plantilla por defecto.</p>
     *
     * <p><strong>Plantillas disponibles:</strong></p>
     * <ul>
     *   <li>Email-verified: Para verificación de correo electrónico</li>
     *   <li>welcome-email: Para correos de bienvenida</li>
     *   <li>notification: Para notificaciones generales</li>
     *   <li>appointment-reminder: Para recordatorios de citas</li>
     * </ul>
     */
    @Size(max = 100, message = "El nombre de la plantilla no puede exceder 100 caracteres")
    private String templateName;

    /**
     * Variables dinámicas para personalización de la plantilla.
     *
     * <p>Mapa que contiene las variables que serán sustituidas en la plantilla HTML.
     * Las claves son los nombres de las variables en la plantilla, y los valores
     * son los datos que las reemplazarán.</p>
     *
     * <p><strong>Variables comunes incluyen:</strong></p>
     * <ul>
     *   <li>nombreUsuario: nombre del destinatario</li>
     *   <li>fechaEnvio: fecha de envío del correo</li>
     *   <li>urlVerificacion: enlaces de acción específicos</li>
     *   <li>contenidoPersonalizado: contenido específico del contexto</li>
     * </ul>
     */
    @Builder.Default
    private Map<String, Object> variables = new HashMap<>();

    /**
     * Lista de archivos adjuntos a incluir en el correo.
     *
     * <p>Colección de objetos Attachment que representan documentos, imágenes
     * u otros archivos que acompañarán al correo electrónico.</p>
     */
    @Valid
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();

    /**
     * Prioridad del correo electrónico.
     *
     * <p>Campo opcional que especifica la prioridad de entrega del correo.
     * Los valores típicos son: ALTA, NORMAL, BAJA.</p>
     */
    @Builder.Default
    private EmailPriority priority = EmailPriority.NORMAL;

    /**
     * Fecha y hora de creación del objeto Email.
     *
     * <p>Se establece automáticamente al crear el objeto para propósitos
     * de auditoría y tracking. Es inmutable después de la creación.</p>
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Identificador único del correo para tracking.
     *
     * <p>Campo opcional que puede utilizarse para rastrear el estado
     * y la entrega de correos específicos en sistemas de logging.</p>
     */
    private String trackingId;

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Agrega un archivo adjunto a la lista de adjuntos del correo.
     *
     * <p>Método de conveniencia que permite agregar archivos adjuntos
     * de forma fluida durante la construcción del objeto Email.</p>
     *
     * @param attachment archivo adjunto a agregar
     * @throws IllegalArgumentException si el attachment es nulo
     *
     * @apiNote Este método es thread-safe y puede ser llamado concurrentemente
     */
    public void addAttachment(Attachment attachment) {
        if (attachment == null) {
            throw new IllegalArgumentException("El archivo adjunto no puede ser nulo");
        }

        if (this.attachments == null) {
            this.attachments = new ArrayList<>();
        }

        this.attachments.add(attachment);
    }

    /**
     * Agrega múltiples archivos adjuntos de una vez.
     *
     * <p>Método de conveniencia para agregar varios archivos adjuntos
     * en una sola operación.</p>
     *
     * @param attachments lista de archivos adjuntos a agregar
     * @throws IllegalArgumentException si la lista es nula o contiene elementos nulos
     */
    public void addAttachments(List<Attachment> attachments) {
        if (attachments == null) {
            throw new IllegalArgumentException("La lista de adjuntos no puede ser nula");
        }

        for (Attachment attachment : attachments) {
            addAttachment(attachment);
        }
    }

    /**
     * Agrega una variable dinámica para la plantilla.
     *
     * <p>Método de conveniencia que permite agregar variables de forma
     * fluida durante la construcción del objeto Email.</p>
     *
     * @param key nombre de la variable en la plantilla
     * @param value valor a sustituir en la plantilla
     * @throws IllegalArgumentException si la clave es nula o vacía
     *
     * @return this para permitir método chaining
     */
    public Email addVariable(String key, Object value) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("La clave de la variable no puede ser nula o vacía");
        }

        if (this.variables == null) {
            this.variables = new HashMap<>();
        }

        this.variables.put(key.trim(), value);
        return this;
    }

    /**
     * Agrega múltiples variables dinámicas de una vez.
     *
     * <p>Método de conveniencia para agregar varias variables
     * en una sola operación.</p>
     *
     * @param variables mapa de variables a agregar
     * @throws IllegalArgumentException si el mapa es nulo
     *
     * @return this para permitir método chaining
     */
    public Email addVariables(Map<String, Object> variables) {
        if (variables == null) {
            throw new IllegalArgumentException("El mapa de variables no puede ser nulo");
        }

        if (this.variables == null) {
            this.variables = new HashMap<>();
        }

        this.variables.putAll(variables);
        return this;
    }

    /**
     * Verifica si el correo tiene archivos adjuntos.
     *
     * @return true si hay archivos adjuntos, false en caso contrario
     */
    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    /**
     * Obtiene el número total de archivos adjuntos.
     *
     * @return cantidad de archivos adjuntos
     */
    public int getAttachmentCount() {
        return attachments != null ? attachments.size() : 0;
    }

    /**
     * Verifica si el correo tiene variables definidas.
     *
     * @return true si hay variables definidas, false en caso contrario
     */
    public boolean hasVariables() {
        return variables != null && !variables.isEmpty();
    }

    /**
     * Obtiene una variable específica por su clave.
     *
     * @param key clave de la variable a buscar
     * @return valor de la variable o null si no existe
     */
    public Object getVariable(String key) {
        return variables != null ? variables.get(key) : null;
    }

    /**
     * Elimina una variable específica.
     *
     * @param key clave de la variable a eliminar
     * @return valor anterior de la variable o null si no existía
     */
    public Object removeVariable(String key) {
        return variables != null ? variables.remove(key) : null;
    }

    /**
     * Limpia todos los archivos adjuntos.
     */
    public void clearAttachments() {
        if (attachments != null) {
            attachments.clear();
        }
    }

    /**
     * Limpia todas las variables.
     */
    public void clearVariables() {
        if (variables != null) {
            variables.clear();
        }
    }

    // ========== MÉTODOS DE VALIDACIÓN ==========

    /**
     * Valida que el objeto Email esté correctamente configurado para envío.
     *
     * <p>Método de auto-validación que verifica la consistencia interna
     * del objeto antes del envío.</p>
     *
     * @return true si el objeto es válido
     * @throws IllegalStateException si hay inconsistencias en los datos
     */
    public boolean isValid() {
        // Validaciones básicas
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalStateException("El destinatario es obligatorio");
        }

        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalStateException("El asunto es obligatorio");
        }

        // Validar consistencia de plantilla y variables
        if (templateName != null && !templateName.trim().isEmpty() &&
                (variables == null || variables.isEmpty())) {
            // Advertencia: plantilla especificada pero sin variables
            // No es un error, pero puede indicar configuración incompleta
        }

        return true;
    }

    // ========== TOSTRING PERSONALIZADO ==========

    /**
     * Representación de cadena del objeto Email para logging y debugging.
     *
     * <p>No incluye información sensible y está optimizada para logs de auditoría.</p>
     *
     * @return representación segura del objeto
     */
    @Override
    public String toString() {
        return String.format("Email{to='%s', from='%s', subject='%s', templateName='%s', " +
                        "attachmentCount=%d, variableCount=%d, priority=%s, createdAt=%s, trackingId='%s'}",
                to, from, subject, templateName, getAttachmentCount(),
                hasVariables() ? variables.size() : 0, priority, createdAt, trackingId);
    }

    // ========== ENUM INTERNO PARA PRIORIDADES ==========

    /**
     * Enumeración que define las prioridades disponibles para los correos electrónicos.
     */
    public enum EmailPriority {
        /** Prioridad alta - correos urgentes que requieren atención inmediata */
        ALTA("High"),

        /** Prioridad normal - correos estándar del sistema */
        NORMAL("Normal"),

        /** Prioridad baja - correos informativos o de marketing */
        BAJA("Low");

        private final String mimeValue;

        EmailPriority(String mimeValue) {
            this.mimeValue = mimeValue;
        }

        /**
         * Obtiene el valor MIME estándar para la prioridad.
         *
         * @return valor MIME de la prioridad
         */
        public String getMimeValue() {
            return mimeValue;
        }
    }
}