package com.Petcare.Petcare.Exception;

import com.Petcare.Petcare.DTOs.GlobalException.ErrorResponseDTO;
import com.Petcare.Petcare.Exception.Business.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


/**
 * Clase de manejo de excepciones global para controladores REST en la aplicación Petcare.
 * Proporciona respuestas estandarizadas para diferentes tipos de excepciones.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja las excepciones de validación de DTOs (cuando falla @Valid).
     * Devuelve un error 400 Bad Request con un mapa de los campos y sus errores.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                LocalDateTime.now(),
                errors
        );
        logger.warn("Validation failed: {}", errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja las excepciones de lógica de negocio (ej: email ya existe).
     * Devuelve un error 400 Bad Request.
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponseDTO> handleBusinessLogicExceptions(RuntimeException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        logger.warn("Business logic error: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja las excepciones de autenticación (ej: credenciales incorrectas).
     * Devuelve un error 401 Unauthorized.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(AuthenticationException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication failed: Bad credentials",
                LocalDateTime.now(),
                null
        );
        logger.warn("Authentication failed: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Maneja las excepciones de autorización (ej: un CLIENT intenta acceder a un recurso de ADMIN).
     * Devuelve un error 403 Forbidden.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.FORBIDDEN.value(),
                "Access Denied: You do not have permission to access this resource.",
                LocalDateTime.now(),
                null
        );
        logger.warn("Access denied: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Captura todas las demás excepciones no controladas.
     * Devuelve un error genérico 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred. Please try again later.",
                LocalDateTime.now(),
                null
        );

        logger.error("An unexpected error occurred", ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Maneja la excepción cuando un recurso solicitado no se encuentra.
     * Devuelve un error 404 Not Found.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        logger.warn("Resource not found: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja la excepción cuando se intenta crear o actualizar un recurso con un
     * atributo que debe ser único y ya existe (ej. email duplicado).
     * Devuelve un error 409 Conflict.
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        logger.warn("Data conflict: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Maneja excepciones de violación de integridad de la base de datos.
     * Ocurre cuando una operación (ej. DELETE) viola una restricción de clave foránea.
     * Devuelve un error 409 Conflict con un mensaje amigable.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "No se puede eliminar el recurso: existen registros dependientes (ej. facturas o reservas). Considere desactivar la cuenta en su lugar.",
                LocalDateTime.now(),
                null
        );
        logger.warn("Data integrity violation: {}. Causa: {}", ex.getMessage(), ex.getRootCause() != null ? ex.getRootCause().getMessage() : "N/A");
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Maneja la excepción específica cuando un perfil de cuidador no se encuentra.
     * Devuelve un código de estado 404 Not Found, que es el estándar REST para
     * recursos no encontrados.
     *
     * @param ex La excepción SitterProfileNotFoundException capturada.
     * @return Un ResponseEntity con el código 404 y un mensaje de error claro.
     */
    @ExceptionHandler({
            UserNotFoundException.class,
            SitterProfileNotFoundException.class,
            WorkExperienceNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponseDTO> handleSitterProfileNotFoundException(SitterProfileNotFoundException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(), // El mensaje viene de la excepción que lanzamos
                LocalDateTime.now(),
                null
        );
        logger.warn("Recurso no encontrado: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja excepciones cuando se intenta crear un recurso que ya existe.
     * Devuelve un error 409 Conflict.
     * @param ex La excepción de conflicto, como SitterProfileAlreadyExistsException.
     * @return ResponseEntity con el DTO de error y estado 409.
     */
    @ExceptionHandler(SitterProfileAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflictException(RuntimeException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Conflicto: " + ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        logger.warn("Conflict error: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Maneja la excepción cuando se intenta crear una experiencia laboral que ya existe
     * para el mismo perfil de cuidador, evitando duplicados.
     * Devuelve un error 409 Conflict.
     *
     * @param ex La excepción WorkExperienceConflictException capturada.
     * @return ResponseEntity con el DTO de error y estado 409.
     */
    @ExceptionHandler(WorkExperienceConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handleWorkExperienceConflictException(WorkExperienceConflictException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Conflicto: " + ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        logger.warn("Data conflict: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Maneja excepciones de negocio cuando un recurso específico no se encuentra.
     * <p>
     * Este método centraliza el manejo para todas las excepciones que heredan de
     * {@link RuntimeException} y están anotadas con {@code @ResponseStatus(HttpStatus.NOT_FOUND)}.
     * Devuelve una respuesta HTTP 404 Not Found estandarizada.
     * </p>
     * <b>Excepciones Cubiertas:</b>
     * <ul>
     * <li>{@link UserNotFoundException}</li>
     * <li>{@link SitterProfileNotFoundException}</li>
     * <li>{@link WorkExperienceNotFoundException}</li>
     * <li>Cualquier otra excepción de negocio marcada como NOT_FOUND.</li>
     * </ul>
     *
     * @param ex La excepción de negocio capturada.
     * @return Un {@link ResponseEntity} con el DTO de error y el estado 404.
     */
    @ExceptionHandler({
            UserNotFoundException.class,
            SitterProfileNotFoundException.class,
            WorkExperienceNotFoundException.class
            // Puedes añadir aquí cualquier otra excepción futura que deba devolver un 404
    })
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(RuntimeException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        logger.warn("Recurso no encontrado: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

}