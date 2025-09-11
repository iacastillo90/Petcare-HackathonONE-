package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.DTOs.Email.Email;
import jakarta.mail.MessagingException;

/**
 * Servicio para el envío de correos electrónicos del sistema Petcare.
 *
 * <p>Proporciona funcionalidades completas para el envío de correos electrónicos
 * con soporte para plantillas HTML, archivos adjuntos y variables dinámicas.</p>
 *
 * <p><strong>Características principales:</strong></p>
 * <ul>
 *   <li>Envío de correos HTML con plantillas Thymeleaf</li>
 *   <li>Soporte para archivos adjuntos múltiples</li>
 *   <li>Variables dinámicas para personalización</li>
 *   <li>Integración con Spring Mail</li>
 *   <li>Manejo de errores robusto</li>
 * </ul>
 *
 * <p><strong>Casos de uso principales:</strong></p>
 * <ul>
 *   <li>Verificación de correo electrónico para nuevos usuarios</li>
 *   <li>Notificaciones de servicios de cuidado de mascotas</li>
 *   <li>Reportes y documentos con archivos adjuntos</li>
 *   <li>Comunicaciones administrativas</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see Email
 */
public interface EmailService {

    /**
     * Envía un correo electrónico según la configuración especificada.
     *
     * <p>Método principal que determina automáticamente el tipo de envío basado
     * en el contenido del objeto Email. Si contiene archivos adjuntos, utiliza
     * el flujo de envío con adjuntos; de lo contrario, envía un correo HTML estándar.</p>
     *
     * <p><strong>Comportamiento:</strong></p>
     * <ul>
     *   <li>Detecta automáticamente si hay archivos adjuntos</li>
     *   <li>Procesa plantillas Thymeleaf para contenido HTML</li>
     *   <li>Sustituye variables dinámicas en las plantillas</li>
     *   <li>Maneja codificación UTF-8 para caracteres especiales</li>
     * </ul>
     *
     * @param email objeto con toda la información del correo a enviar
     * @throws MessagingException si ocurre un error durante el envío del correo
     * @throws IllegalArgumentException si los datos del email son inválidos
     *
     * @apiNote Este método es thread-safe y puede ser llamado concurrentemente
     */
    void sendEmail(Email email) throws MessagingException;

    /**
     * Envía un correo de verificación para activación de cuenta de usuario.
     *
     * <p>Método especializado para el proceso de verificación de correo electrónico
     * de nuevos usuarios. Utiliza la plantilla predefinida de verificación y
     * genera automáticamente las variables necesarias.</p>
     *
     * <p><strong>Variables incluidas automáticamente:</strong></p>
     * <ul>
     *   <li>nombreUsuario: nombre completo del usuario</li>
     *   <li>urlVerificacion: enlace de verificación único</li>
     *   <li>fechaExpiracion: fecha límite para verificar</li>
     *   <li>añoActual: año actual para el footer</li>
     * </ul>
     *
     * @param recipientEmail dirección de correo del destinatario
     * @param recipientName nombre completo del usuario
     * @param verificationUrl URL única de verificación con token
     * @param expirationHours horas hasta que expire el enlace de verificación
     * @throws MessagingException si ocurre un error durante el envío
     * @throws IllegalArgumentException si algún parámetro es nulo o vacío
     *
     * @apiNote Este método está optimizado para el flujo de registro de usuarios
     */
    void sendVerificationEmail(String recipientEmail, String recipientName,
                               String verificationUrl, int expirationHours) throws MessagingException;

    /**
     * Envía un correo con múltiples archivos adjuntos.
     *
     * <p>Método especializado para envío de correos con documentos, reportes
     * o cualquier tipo de archivo adjunto. Optimizado para manejar múltiples
     * archivos de diferentes tipos y tamaños.</p>
     *
     * <p><strong>Tipos de archivos soportados:</strong></p>
     * <ul>
     *   <li>Documentos: PDF, DOC, DOCX, TXT</li>
     *   <li>Imágenes: JPG, PNG, GIF, SVG</li>
     *   <li>Hojas de cálculo: XLS, XLSX, CSV</li>
     *   <li>Otros: cualquier tipo MIME válido</li>
     * </ul>
     *
     * @param email objeto Email con archivos adjuntos configurados
     * @throws MessagingException si falla el envío o procesamiento de adjuntos
     * @throws IllegalArgumentException si no hay archivos adjuntos válidos
     * @throws IllegalStateException si el tamaño total excede límites del servidor
     *
     * @apiNote Verifica el tamaño total de adjuntos antes del envío
     */
    void sendEmailWithAttachments(Email email) throws MessagingException;

    /**
     * Valida que un objeto Email esté correctamente configurado para envío.
     *
     * <p>Verifica todos los campos obligatorios y validaciones de negocio
     * antes del envío real del correo electrónico.</p>
     *
     * <p><strong>Validaciones incluidas:</strong></p>
     * <ul>
     *   <li>Formato válido de direcciones de correo</li>
     *   <li>Presencia de asunto y destinatario</li>
     *   <li>Existencia de la plantilla especificada</li>
     *   <li>Validez de archivos adjuntos si están presentes</li>
     *   <li>Tamaño total de adjuntos dentro de límites</li>
     * </ul>
     *
     * @param email objeto Email a validar
     * @return true si el email es válido para envío
     * @throws IllegalArgumentException con detalles específicos del error de validación
     *
     * @apiNote Este método se llama automáticamente antes de cada envío
     */
    boolean validateEmail(Email email) throws IllegalArgumentException;
}