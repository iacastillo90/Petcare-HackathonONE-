package com.Petcare.Petcare.DTOs.GlobalException;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Respuesta de error estandarizada de la API")
public record ErrorResponseDTO(
        @Schema(description = "Código de estado HTTP", example = "400")
        int status,
        @Schema(description = "Mensaje de error descriptivo", example = "El email no es válido.")
        String message,
        @Schema(description = "Marca de tiempo del error", example = "2025-09-12T23:22:29.479Z")
        LocalDateTime timestamp,
        @Schema(description = "Mapa de errores de validación de campos")
        Map<String, String> validationErrors
) {

}