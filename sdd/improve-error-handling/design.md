# Design: improve-error-handling

## Technical Approach

Reemplazar todas las excepciones genéricas (`IllegalArgumentException`, `IllegalStateException`, `RuntimeException`) con excepciones de negocio tipadas que:
1. Extienden `RuntimeException` con `@ResponseStatus` para mapeo HTTP automático
2. Incluyen logging estructurado con `@Slf4j`
3. Proporcionan mensajes descriptivos en español
4. Permiten trazabilidad y debugging preciso

## Architecture Decisions

### Decision: Exception Class Structure

**Choice**: Excepciones con `@ResponseStatus` + logging interno
**Alternatives considered**: Handler centralizado genérico
**Rationale**: 
- `@ResponseStatus` mapea directamente HTTP status sin handler adicional
- Logging interno facilita debugging sin código duplicado en handlers
- Pattern existente (`UserNotFoundException`) ya usa este approach

### Decision: Naming Convention

**Choice**: `{Entity}{Reason}Exception` (e.g., `BookingNotFoundException`)
**Alternatives considered**: `NotFound{Entity}Exception`
**Rationale**: Sigue el patrón existente en el codebase (`UserNotFoundException`, `SitterProfileNotFoundException`)

### Decision: Handler Strategy

**Choice**: Mantener `GlobalExceptionHandler` pero usar `@ExceptionHandler` específicos por tipo
**Alternatives considered**: Eliminar handler y usar solo `@ResponseStatus`
**Rationale**: 
- Handler existente ya tiene logging y respuestas estructuradas
- Permite personalizar respuesta sin perder стандартизацию
- `ErrorResponseDTO` ya existe con el formato correcto

## Data Flow

```
Service Layer
     │
     ▼
┌─────────────────────────────┐
│  Business Exception         │
│  (extends RuntimeException)  │
│  + @ResponseStatus           │
└─────────────────────────────┘
     │
     ▼
GlobalExceptionHandler
     │
     ▼
ErrorResponseDTO (record)
     │
     ▼
HTTP Response (JSON)
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/main/java/com/Petcare/Petcare/Exception/Business/BookingNotFoundException.java` | Create | Excepción para reservas no encontradas |
| `src/main/java/com/Petcare/Petcare/Exception/Business/PetNotFoundException.java` | Create | Excepción para mascotas no encontradas |
| `src/main/java/com/Petcare/Petcare/Exception/Business/AccountNotFoundException.java` | Create | Excepción para cuentas no encontradas |
| `src/main/java/com/Petcare/Petcare/Exception/Business/ServiceOfferingNotFoundException.java` | Create | Excepción para ofertas de servicio no encontradas |
| `src/main/java/com/Petcare/Petcare/Exception/Business/InvoiceNotFoundException.java` | Create | Excepción para facturas no encontradas |
| `src/main/java/com/Petcare/Petcare/Exception/Business/BookingStateException.java` | Create | Excepción para transiciones de estado inválidas |
| `src/main/java/com/Petcare/Petcare/Exception/Business/BookingConflictException.java` | Create | Excepción para conflictos de horario |
| `src/main/java/com/Petcare/Petcare/Exception/Business/InactiveAccountException.java` | Create | Excepción para cuentas inactivas |
| `src/main/java/com/Petcare/Petcare/Exception/Business/PetAlreadyExistsException.java` | Create | Excepción para mascotas duplicadas |
| `src/main/java/com/Petcare/Petcare/Exception/Business/SitterNotFoundException.java` | Create | Excepción para cuidadores no encontrados |
| `src/main/java/com/Petcare/Petcare/Exception/Business/SitterInactiveException.java` | Create | Excepción para cuidadores inactivos |
| `src/main/java/com/Petcare/Petcare/Exception/Business/SitterRoleRequiredException.java` | Create | Excepción para usuarios sin rol SITTER |
| `src/main/java/com/Petcare/Petcare/Exception/Business/ServiceOfferingInactiveException.java` | Create | Excepción para ofertas de servicio inactivas |
| `src/main/java/com/Petcare/Petcare/Exception/Business/MaxPendingBookingsExceededException.java` | Create | Excepción para límite de reservas |
| `src/main/java/com/Petcare/Petcare/Exception/Business/InvoiceStateException.java` | Create | Excepción para estados de factura inválidos |
| `src/main/java/com/Petcare/Petcare/Exception/Business/InvoiceAlreadyExistsException.java` | Create | Excepción para facturas duplicadas |
| `src/main/java/com/Petcare/Petcare/Exception/Business/InvalidAmountException.java` | Create | Excepción para montos inválidos |
| `src/main/java/com/Petcare/Petcare/Exception/Business/CancellationReasonRequiredException.java` | Create | Excepción para motivo de cancelación faltante |
| `src/main/java/com/Petcare/Petcare/Exception/Business/InsufficientTimeException.java` | Create | Excepción para reservas con poco tiempo |
| `src/main/java/com/Petcare/Petcare/Exception/Business/UnauthorizedPetAccessException.java` | Create | Excepción para acceso no autorizado a mascotas |
| `src/main/java/com/Petcare/Petcare/Exception/GlobalExceptionHandler.java` | Modify | Agregar handlers específicos para nuevas excepciones |
| `src/main/java/com/Petcare/Petcare/Services/Implement/BookingServiceImplement.java` | Modify | Reemplazar IllegalArgument/State con excepciones tipadas |
| `src/main/java/com/Petcare/Petcare/Services/Implement/PetServiceImplement.java` | Modify | Reemplazar IllegalArgument/State con excepciones tipadas |
| `src/main/java/com/Petcare/Petcare/Services/Implement/InvoiceServiceImplement.java` | Modify | Reemplazar IllegalArgument/State con excepciones tipadas |
| `src/main/java/com/Petcare/Petcare/Services/Implement/AccountServiceImplement.java` | Modify | Reemplazar IllegalArgument/State con excepciones tipadas |
| `src/main/java/com/Petcare/Petcare/Services/Implement/AppliedCouponServiceImplement.java` | Modify | Reemplazar RuntimeException con excepciones tipadas |

## Exception Class Template

```java
package com.Petcare.Petcare.Exception.Business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando un recurso no existe en el sistema.
 * Mapea automáticamente a HTTP 404 Not Found.
 *
 * @author Equipo Petcare 10
 * @version 1.0
 */
@Slf4j
@ResponseStatus(HttpStatus.NOT_FOUND)
public class BookingNotFoundException extends RuntimeException {
    
    public BookingNotFoundException(Long id) {
        super("Reserva no encontrada con ID: " + id);
        log.warn("Booking not found with ID: {}", id);
    }
}
```

## GlobalExceptionHandler Updates

```java
/**
 * Handler para excepciones de estado de negocio inválido.
 * Ejemplo: intentar modificar una reserva ya completada.
 */
@ExceptionHandler(BookingStateException.class)
public ResponseEntity<ErrorResponseDTO> handleBookingStateException(BookingStateException ex) {
    log.warn("Booking state violation: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponseDTO.of(HttpStatus.CONFLICT, ex.getMessage()));
}

/**
 * Handler para excepciones de conflictos de horario.
 */
@ExceptionHandler(BookingConflictException.class)
public ResponseEntity<ErrorResponseDTO> handleBookingConflictException(BookingConflictException ex) {
    log.warn("Booking conflict: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponseDTO.of(HttpStatus.CONFLICT, ex.getMessage()));
}

/**
 * Handler para excepciones de recurso no encontrado.
 */
@ExceptionHandler({
        BookingNotFoundException.class,
        PetNotFoundException.class,
        AccountNotFoundException.class,
        ServiceOfferingNotFoundException.class,
        InvoiceNotFoundException.class,
        SitterNotFoundException.class
})
public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundExceptions(RuntimeException ex) {
    log.warn("Resource not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponseDTO.of(HttpStatus.NOT_FOUND, ex.getMessage()));
}

/**
 * Handler para excepciones de cuenta inactiva.
 */
@ExceptionHandler({
        InactiveAccountException.class,
        SitterInactiveException.class,
        ServiceOfferingInactiveException.class
})
public ResponseEntity<ErrorResponseDTO> handleInactiveResourceExceptions(RuntimeException ex) {
    log.warn("Inactive resource: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponseDTO.of(HttpStatus.BAD_REQUEST, ex.getMessage()));
}

/**
 * Handler para excepciones de validación.
 */
@ExceptionHandler({
        CancellationReasonRequiredException.class,
        InsufficientTimeException.class,
        InvalidAmountException.class,
        SitterRoleRequiredException.class
})
public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(RuntimeException ex) {
    log.warn("Validation error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponseDTO.of(HttpStatus.BAD_REQUEST, ex.getMessage()));
}
```

## Migration Map

| Service | Exception Type | File to Create |
|---------|---------------|----------------|
| `BookingServiceImplement` | `BookingNotFoundException` | `BookingNotFoundException.java` |
| `BookingServiceImplement` | `PetNotFoundException` | `PetNotFoundException.java` |
| `BookingServiceImplement` | `AccountNotFoundException` | `AccountNotFoundException.java` |
| `BookingServiceImplement` | `ServiceOfferingNotFoundException` | `ServiceOfferingNotFoundException.java` |
| `BookingServiceImplement` | `SitterNotFoundException` | `SitterNotFoundException.java` |
| `BookingServiceImplement` | `InactiveAccountException` | `InactiveAccountException.java` |
| `BookingServiceImplement` | `SitterInactiveException` | `SitterInactiveException.java` |
| `BookingServiceImplement` | `SitterRoleRequiredException` | `SitterRoleRequiredException.java` |
| `BookingServiceImplement` | `ServiceOfferingInactiveException` | `ServiceOfferingInactiveException.java` |
| `BookingServiceImplement` | `BookingConflictException` | `BookingConflictException.java` |
| `BookingServiceImplement` | `BookingStateException` | `BookingStateException.java` |
| `BookingServiceImplement` | `MaxPendingBookingsExceededException` | `MaxPendingBookingsExceededException.java` |
| `BookingServiceImplement` | `CancellationReasonRequiredException` | `CancellationReasonRequiredException.java` |
| `BookingServiceImplement` | `InsufficientTimeException` | `InsufficientTimeException.java` |
| `BookingServiceImplement` | `UnauthorizedPetAccessException` | `UnauthorizedPetAccessException.java` |
| `PetServiceImplement` | `PetNotFoundException` | `PetNotFoundException.java` |
| `PetServiceImplement` | `AccountNotFoundException` | `AccountNotFoundException.java` |
| `PetServiceImplement` | `InactiveAccountException` | `InactiveAccountException.java` |
| `PetServiceImplement` | `PetAlreadyExistsException` | `PetAlreadyExistsException.java` |
| `InvoiceServiceImplement` | `InvoiceNotFoundException` | `InvoiceNotFoundException.java` |
| `InvoiceServiceImplement` | `InvoiceStateException` | `InvoiceStateException.java` |
| `InvoiceServiceImplement` | `InvoiceAlreadyExistsException` | `InvoiceAlreadyExistsException.java` |
| `InvoiceServiceImplement` | `InvalidAmountException` | `InvalidAmountException.java` |
| `InvoiceServiceImplement` | `CancellationReasonRequiredException` | `CancellationReasonRequiredException.java` |
| `InvoiceServiceImplement` | `BookingStateException` | `BookingStateException.java` |
| `AppliedCouponServiceImplement` | `CouponNotFoundException` | `CouponNotFoundException.java` |
| `AppliedCouponServiceImplement` | `CouponExpiredException` | `CouponExpiredException.java` |

## Implementation Order

1. **Phase 1: Create all exception classes**
   - Create 20+ exception classes following the template
   - Each with appropriate `@ResponseStatus` and logging

2. **Phase 2: Update GlobalExceptionHandler**
   - Add handlers for new exception groups
   - Group exceptions by HTTP status semantically

3. **Phase 3: Migrate BookingServiceImplement**
   - Replace all 20+ IllegalArgument/State/Runtime exceptions
   - Verify no breaking changes in behavior

4. **Phase 4: Migrate PetServiceImplement**
   - Replace 6 IllegalState exceptions

5. **Phase 5: Migrate InvoiceServiceImplement**
   - Replace 10 IllegalArgument/State exceptions

6. **Phase 6: Migrate AppliedCouponServiceImplement**
   - Replace 2 RuntimeException

7. **Phase 7: Verify and test**
   - Run existing tests
   - Manual API testing with Swagger

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | Exception constructors | Verify message format and logging |
| Unit | Service exception throwing | Mock repos, verify correct exception type |
| Integration | API error responses | Verify HTTP status and response body |
| E2E | Error scenarios | Swagger UI manual testing |

## Open Questions

- [x] Should EmailService exceptions also be migrated? (Sí, pero de menor prioridad)
- [x] Should PdfGenerationService RuntimeException be migrated? (Sí, como `PdfGenerationException`)
- [ ] UserService has duplicate email checks - should be `EmailAlreadyExistsException` (Ya existe)

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Breaking existing error messages | Medium | High | Mantener mensajes idénticos |
| Missing exception handler | Low | Medium | Agregar handlers genéricos por grupo |
| Tests failing after migration | Medium | Medium | Ejecutar tests antes y después de cada servicio |
