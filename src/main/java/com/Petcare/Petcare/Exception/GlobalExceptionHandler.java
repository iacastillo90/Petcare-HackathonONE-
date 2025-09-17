package com.Petcare.Petcare.Exception;

import com.Petcare.Petcare.DTOs.GlobalException.ErrorResponseDTO;
import com.Petcare.Petcare.Exception.Business.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for REST controllers in the Petcare application.
 * This class provides standardized responses for different types of exceptions,
 * ensuring a consistent error format for the API clients.
 *
 * @version 2.0
 * @since 2023-10-27
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles exceptions related to DTO validation failures (when @Valid fails).
     * Returns a 400 Bad Request with a map of fields and their corresponding error messages.
     *
     * @param ex The MethodArgumentNotValidException that was thrown.
     * @return A ResponseEntity with a standardized error DTO and 400 status.
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
        logger.warn("Validation failed for request: {}", errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles business logic exceptions that indicate a bad request from the client.
     * This includes invalid arguments or state that cannot be processed.
     *
     * @param ex The runtime exception indicating a business logic failure.
     * @return A ResponseEntity with a standardized error DTO and 400 status.
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
     * Centralized handler for all business exceptions that signify a resource was not found.
     * This ensures a consistent 404 Not Found response across the application.
     *
     * @param ex A subclass of RuntimeException indicating a missing resource
     * (e.g., UserNotFoundException, SitterProfileNotFoundException).
     * @return A ResponseEntity with a standardized error DTO and 404 status.
     */
    @ExceptionHandler({
            UserNotFoundException.class,
            SitterProfileNotFoundException.class,
            WorkExperienceNotFoundException.class,
            ServiceOfferingNotFoundException.class,
            PetNotFoundException.class,
            ReviewNotFoundException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundExceptions(RuntimeException ex) {
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
     * Centralized handler for exceptions related to data conflicts, such as unique constraint violations.
     * This returns a 409 Conflict response.
     *
     * @param ex A subclass of RuntimeException indicating a data conflict
     * (e.g., EmailAlreadyExistsException, SitterProfileAlreadyExistsException).
     * @return A ResponseEntity with a standardized error DTO and 409 status.
     */
    @ExceptionHandler({
            EmailAlreadyExistsException.class,
            SitterProfileAlreadyExistsException.class,
            WorkExperienceConflictException.class,
            ReviewAlreadyExistsException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleConflictExceptions(RuntimeException ex) {
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
     * Handles database integrity violation exceptions, typically from foreign key constraints.
     * Provides a user-friendly message suggesting alternative actions.
     *
     * @param ex The DataIntegrityViolationException thrown by the persistence layer.
     * @return A ResponseEntity with a standardized error DTO and 409 status.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Cannot delete resource: dependent records exist (e.g., invoices or bookings). Consider deactivating the account instead.",
                LocalDateTime.now(),
                null
        );
        logger.warn("Data integrity violation: {}. Root cause: {}", ex.getMessage(), ex.getRootCause() != null ? ex.getRootCause().getMessage() : "N/A");
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles authentication failures, such as incorrect credentials.
     * Returns a 401 Unauthorized error.
     *
     * @param ex The AuthenticationException thrown by Spring Security.
     * @return A ResponseEntity with a standardized error DTO and 401 status.
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
     * Handles authorization failures, where a user attempts to access a resource without sufficient permissions.
     * Returns a 403 Forbidden error.
     *
     * @param ex The AccessDeniedException thrown by Spring Security.
     * @return A ResponseEntity with a standardized error DTO and 403 status.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.FORBIDDEN.value(),
                "Access Denied: You do not have permission to perform this action.",
                LocalDateTime.now(),
                null
        );
        logger.warn("Access denied: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * A catch-all handler for any other unhandled exceptions.
     * This prevents exposing stack traces to the client and provides a generic 500 Internal Server Error.
     *
     * @param ex The generic Exception that was thrown.
     * @return A ResponseEntity with a standardized error DTO and 500 status.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected internal error occurred. Please contact support.",
                LocalDateTime.now(),
                null
        );
        logger.error("An unexpected error occurred", ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ServiceOfferingConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handleServiceOfferingConflict(ServiceOfferingConflictException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Conflicto: " + ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        logger.warn("Conflicto al crear oferta de servicio: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles business logic exceptions where the request is well-formed
     * but cannot be processed due to a business rule violation.
     *
     * @param ex The exception indicating a business rule failure.
     * @return A ResponseEntity with a standardized error DTO and 422 status.
     */
    @ExceptionHandler(InvalidReviewException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnprocessableEntityExceptions(RuntimeException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        logger.warn("Unprocessable entity error: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}