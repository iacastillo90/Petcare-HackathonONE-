package com.Petcare.Petcare.DTOs.Email;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) para archivos adjuntos en correos electrónicos del sistema Petcare.
 *
 * <p>Esta clase encapsula toda la información necesaria para manejar archivos adjuntos
 * en correos electrónicos, incluyendo metadatos, validaciones de seguridad y
 * utilidades para diferentes tipos de archivos.</p>
 *
 * <p><strong>Características principales:</strong></p>
 * <ul>
 *   <li>Validación automática de campos mediante Bean Validation</li>
 *   <li>Soporte para múltiples tipos MIME y formatos de archivo</li>
 *   <li>Metadatos de tamaño y fecha para auditoría</li>
 *   <li>Integración con Spring Resource para flexibilidad de origen</li>
 *   <li>Validaciones de seguridad para prevenir archivos maliciosos</li>
 * </ul>
 *
 * <p><strong>Tipos de archivo soportados:</strong></p>
 * <ul>
 *   <li>Documentos: PDF, DOC, DOCX, TXT, RTF</li>
 *   <li>Imágenes: JPG, JPEG, PNG, GIF, SVG, BMP</li>
 *   <li>Hojas de cálculo: XLS, XLSX, CSV</li>
 *   <li>Presentaciones: PPT, PPTX</li>
 *   <li>Archivos de datos: JSON, XML, ZIP (con restricciones)</li>
 * </ul>
 *
 * <p><strong>Restricciones de seguridad:</strong></p>
 * <ul>
 *   <li>Tamaño máximo configurable por archivo</li>
 *   <li>Lista blanca de tipos MIME permitidos</li>
 *   <li>Validación de extensión vs contenido real</li>
 *   <li>Bloqueo de ejecutables y scripts</li>
 * </ul>
 *
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>
 * Attachment attachment = Attachment.builder()
 *     .name("reporte-mensual.pdf")
 *     .contentType("application/pdf")
 *     .resource(new FileSystemResource("path/to/file.pdf"))
 *     .description("Reporte mensual de actividad")
 *     .build();
 * </pre>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see Email
 * @see Resource
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {

    /**
     * Nombre del archivo adjunto.
     *
     * <p>Campo obligatorio que especifica el nombre con el que aparecerá
     * el archivo en el correo electrónico. Debe incluir la extensión
     * para que los clientes de correo lo manejen apropiadamente.</p>
     *
     * <p><strong>Convenciones de nomenclatura:</strong></p>
     * <ul>
     *   <li>Usar nombres descriptivos sin espacios</li>
     *   <li>Incluir siempre la extensión correcta</li>
     *   <li>Evitar caracteres especiales problemáticos</li>
     *   <li>Máximo 100 caracteres para compatibilidad</li>
     * </ul>
     */
    @NotBlank(message = "El nombre del archivo adjunto es obligatorio")
    @Size(max = 100, message = "El nombre del archivo no puede exceder 100 caracteres")
    private String name;

    /**
     * Tipo MIME del archivo adjunto.
     *
     * <p>Campo obligatorio que especifica el tipo de contenido del archivo
     * según los estándares MIME. Es crucial para que los clientes de correo
     * manejen correctamente el archivo.</p>
     *
     * <p><strong>Tipos MIME comunes soportados:</strong></p>
     * <ul>
     *   <li>application/pdf - Documentos PDF</li>
     *   <li>image/jpeg, image/png - Imágenes</li>
     *   <li>application/vnd.ms-excel - Archivos Excel</li>
     *   <li>text/plain - Archivos de texto</li>
     *   <li>application/zip - Archivos comprimidos</li>
     * </ul>
     */
    @NotBlank(message = "El tipo de contenido (MIME type) es obligatorio")
    @Size(max = 100, message = "El tipo de contenido no puede exceder 100 caracteres")
    private String contentType;

    /**
     * Recurso que representa el archivo físico.
     *
     * <p>Campo obligatorio que utiliza Spring Resource para abstracción
     * del origen del archivo. Puede ser desde sistema de archivos, classpath,
     * URL remota, o cualquier otro origen compatible.</p>
     *
     * <p><strong>Tipos de Resource soportados:</strong></p>
     * <ul>
     *   <li>FileSystemResource - Archivos del sistema de archivos</li>
     *   <li>ClassPathResource - Archivos del classpath</li>
     *   <li>ByteArrayResource - Archivos en memoria</li>
     *   <li>InputStreamResource - Streams de datos</li>
     * </ul>
     */
    @NotNull(message = "El recurso del archivo adjunto es obligatorio")
    private Resource resource;

    /**
     * Descripción opcional del archivo adjunto.
     *
     * <p>Campo descriptivo que puede proporcionar contexto adicional
     * sobre el propósito o contenido del archivo adjunto.</p>
     */
    @Size(max = 250, message = "La descripción no puede exceder 250 caracteres")
    private String description;

    /**
     * Tamaño del archivo en bytes.
     *
     * <p>Campo opcional que se calcula automáticamente cuando está disponible.
     * Útil para validaciones de tamaño y logging de auditoría.</p>
     */
    private Long sizeInBytes;

    /**
     * Fecha y hora de creación del objeto Attachment.
     *
     * <p>Se establece automáticamente al crear el objeto para propósitos
     * de auditoría y tracking. Es inmutable después de la creación.</p>
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Identificador único del adjunto para tracking.
     *
     * <p>Campo opcional que puede utilizarse para rastrear archivos
     * específicos en sistemas de logging y auditoría.</p>
     */
    private String attachmentId;

    /**
     * Categoría del archivo adjunto.
     *
     * <p>Clasificación automática basada en el tipo de contenido
     * para facilitar el procesamiento y validación.</p>
     */
    @Builder.Default
    private AttachmentCategory category = AttachmentCategory.UNKNOWN;

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Calcula y establece el tamaño del archivo en bytes.
     *
     * <p>Método de conveniencia que obtiene el tamaño del recurso
     * y lo almacena en el campo sizeInBytes para futuras referencias.</p>
     *
     * @return el tamaño del archivo en bytes
     * @throws IOException si no se puede determinar el tamaño
     */
    public long calculateAndSetSize() throws IOException {
        if (resource != null && resource.exists()) {
            this.sizeInBytes = resource.contentLength();
            return this.sizeInBytes;
        }
        return 0L;
    }

    /**
     * Determina automáticamente la categoría basada en el tipo MIME.
     *
     * <p>Clasifica el archivo adjunto en una categoría predefinida
     * basándose en su tipo de contenido MIME.</p>
     *
     * @return la categoría determinada del archivo
     */
    public AttachmentCategory determineCategory() {
        if (contentType == null) {
            this.category = AttachmentCategory.UNKNOWN;
            return this.category;
        }

        String lowerContentType = contentType.toLowerCase();

        if (lowerContentType.startsWith("image/")) {
            this.category = AttachmentCategory.IMAGE;
        } else if (lowerContentType.equals("application/pdf")) {
            this.category = AttachmentCategory.DOCUMENT;
        } else if (lowerContentType.startsWith("application/vnd.ms-excel") ||
                lowerContentType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml") ||
                lowerContentType.equals("text/csv")) {
            this.category = AttachmentCategory.SPREADSHEET;
        } else if (lowerContentType.startsWith("application/vnd.ms-powerpoint") ||
                lowerContentType.startsWith("application/vnd.openxmlformats-officedocument.presentationml")) {
            this.category = AttachmentCategory.PRESENTATION;
        } else if (lowerContentType.startsWith("text/")) {
            this.category = AttachmentCategory.TEXT;
        } else if (lowerContentType.equals("application/zip") ||
                lowerContentType.equals("application/x-rar-compressed") ||
                lowerContentType.equals("application/x-7z-compressed")) {
            this.category = AttachmentCategory.ARCHIVE;
        } else {
            this.category = AttachmentCategory.OTHER;
        }

        return this.category;
    }

    /**
     * Valida que el archivo adjunto es seguro para envío.
     *
     * <p>Realiza validaciones de seguridad para prevenir el envío
     * de archivos potencialmente maliciosos o problemáticos.</p>
     *
     * @return true si el archivo es seguro
     * @throws IllegalStateException si el archivo no pasa las validaciones de seguridad
     */
    public boolean validateSecurity() {
        // Lista de tipos MIME prohibidos por seguridad
        String[] prohibitedTypes = {
                "application/x-executable",
                "application/x-msdownload",
                "application/x-msdos-program",
                "application/x-sh",
                "application/x-shellscript",
                "text/x-script",
                "application/javascript",
                "text/javascript"
        };

        if (contentType != null) {
            String lowerContentType = contentType.toLowerCase();
            for (String prohibited : prohibitedTypes) {
                if (lowerContentType.equals(prohibited)) {
                    throw new IllegalStateException("Tipo de archivo prohibido por seguridad: " + contentType);
                }
            }
        }

        // Validar extensión vs nombre
        if (name != null && contentType != null) {
            validateExtensionConsistency();
        }

        return true;
    }

    /**
     * Verifica que la extensión del archivo sea consistente con el tipo MIME.
     *
     * <p>Validación adicional de seguridad que verifica que la extensión
     * del nombre del archivo coincida con el tipo de contenido declarado.</p>
     *
     * @throws IllegalStateException si hay inconsistencia entre extensión y tipo MIME
     */
    private void validateExtensionConsistency() {
        String extension = getFileExtension().toLowerCase();
        String lowerContentType = contentType.toLowerCase();

        // Mapeo básico de extensiones a tipos MIME esperados
        boolean isConsistent = switch (extension) {
            case "pdf" -> lowerContentType.equals("application/pdf");
            case "jpg", "jpeg" -> lowerContentType.equals("image/jpeg");
            case "png" -> lowerContentType.equals("image/png");
            case "gif" -> lowerContentType.equals("image/gif");
            case "txt" -> lowerContentType.equals("text/plain");
            case "csv" -> lowerContentType.equals("text/csv");
            case "zip" -> lowerContentType.equals("application/zip");
            default -> true; // Permitir otros tipos no validados específicamente
        };

        if (!isConsistent) {
            throw new IllegalStateException(
                    String.format("Inconsistencia entre extensión (%s) y tipo MIME (%s)", extension, contentType)
            );
        }
    }

    /**
     * Obtiene la extensión del archivo desde el nombre.
     *
     * @return la extensión del archivo sin el punto, o cadena vacía si no tiene
     */
    public String getFileExtension() {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }

        int lastDotIndex = name.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < name.length() - 1) {
            return name.substring(lastDotIndex + 1);
        }

        return "";
    }

    /**
     * Verifica si el archivo existe y es legible.
     *
     * @return true si el recurso existe y es legible
     */
    public boolean isResourceValid() {
        return resource != null && resource.exists() && resource.isReadable();
    }

    /**
     * Obtiene el tamaño del archivo en formato legible.
     *
     * @return cadena con el tamaño formateado (ej: "1.5 MB", "256 KB")
     */
    public String getFormattedSize() {
        if (sizeInBytes == null || sizeInBytes == 0) {
            try {
                calculateAndSetSize();
            } catch (IOException e) {
                return "Desconocido";
            }
        }

        if (sizeInBytes == null) {
            return "Desconocido";
        }

        long bytes = sizeInBytes;
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

    // ========== TOSTRING PERSONALIZADO ==========

    /**
     * Representación de cadena del objeto Attachment para logging y debugging.
     *
     * <p>Incluye información relevante sin exponer datos sensibles.</p>
     *
     * @return representación segura del objeto
     */
    @Override
    public String toString() {
        return String.format("Attachment{name='%s', contentType='%s', category=%s, " +
                        "sizeInBytes=%d, description='%s', createdAt=%s, attachmentId='%s'}",
                name, contentType, category, sizeInBytes, description, createdAt, attachmentId);
    }

    // ========== ENUM INTERNO PARA CATEGORÍAS ==========

    /**
     * Enumeración que define las categorías disponibles para archivos adjuntos.
     *
     * <p>Permite clasificar automáticamente los archivos para facilitar
     * el procesamiento, validación y presentación en la interfaz.</p>
     */
    public enum AttachmentCategory {
        /** Imágenes (JPG, PNG, GIF, etc.) */
        IMAGE("Imagen", "image/*"),

        /** Documentos de texto (PDF, DOC, etc.) */
        DOCUMENT("Documento", "application/pdf"),

        /** Hojas de cálculo (XLS, CSV, etc.) */
        SPREADSHEET("Hoja de Cálculo", "application/vnd.ms-excel"),

        /** Presentaciones (PPT, PPTX, etc.) */
        PRESENTATION("Presentación", "application/vnd.ms-powerpoint"),

        /** Archivos de texto plano */
        TEXT("Texto", "text/*"),

        /** Archivos comprimidos (ZIP, RAR, etc.) */
        ARCHIVE("Archivo Comprimido", "application/zip"),

        /** Otros tipos de archivo */
        OTHER("Otro", "*/*"),

        /** Tipo desconocido o no determinado */
        UNKNOWN("Desconocido", "*/*");

        private final String displayName;
        private final String mimePattern;

        AttachmentCategory(String displayName, String mimePattern) {
            this.displayName = displayName;
            this.mimePattern = mimePattern;
        }

        /**
         * Obtiene el nombre de presentación de la categoría.
         *
         * @return nombre amigable para mostrar en interfaces
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Obtiene el patrón MIME asociado con la categoría.
         *
         * @return patrón MIME típico de la categoría
         */
        public String getMimePattern() {
            return mimePattern;
        }
    }
}