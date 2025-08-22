package com.Petcare.Petcare.DTOs.GlobalException;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponseDTO(
        int status,
        String message,
        LocalDateTime timestamp,
        Map<String, String> validationErrors
) {

}