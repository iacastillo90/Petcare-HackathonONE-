# Delta for Error Handling

## ADDED Requirements

### Requirement: BookingNotFoundException (404)

The system SHALL provide a `BookingNotFoundException` class that extends `RuntimeException` with `@ResponseStatus(HttpStatus.NOT_FOUND)`. It SHALL be thrown when a booking is requested by a non-existent ID.

#### Scenario: GET booking with non-existent ID returns 404

- GIVEN a booking with ID 99999 does not exist in the database
- WHEN a client sends GET /api/v1/bookings/99999
- THEN response status is 404
- AND response body contains message "Reserva no encontrada con ID: 99999"
- AND response body contains timestamp

### Requirement: PetNotFoundException (404)

The system SHALL provide a `PetNotFoundException` class that extends `RuntimeException` with `@ResponseStatus(HttpStatus.NOT_FOUND)`.

#### Scenario: GET pet with non-existent ID returns 404

- GIVEN a pet with ID 88888 does not exist in the database
- WHEN a client sends GET /api/v1/pets/88888
- THEN response status is 404
- AND response body contains message "Mascota no encontrada con ID: 88888"

### Requirement: AccountNotFoundException (404)

The system SHALL provide a `AccountNotFoundException` class that extends `RuntimeException` with `@ResponseStatus(HttpStatus.NOT_FOUND)`.

#### Scenario: GET account with non-existent ID returns 404

- GIVEN an account with ID 77777 does not exist in the database
- WHEN a client sends GET /api/v1/accounts/77777
- THEN response status is 404
- AND response body contains message "Cuenta no encontrada con ID: 77777"

### Requirement: ServiceOfferingNotFoundException (404)

The system SHALL provide a `ServiceOfferingNotFoundException` class that extends `RuntimeException` with `@ResponseStatus(HttpStatus.NOT_FOUND)`.

### Requirement: InvoiceNotFoundException (404)

The system SHALL provide a `InvoiceNotFoundException` class that extends `RuntimeException` with `@ResponseStatus(HttpStatus.NOT_FOUND)`.

#### Scenario: GET invoice with non-existent ID returns 404

- GIVEN an invoice with ID 66666 does not exist in the database
- WHEN a client sends GET /api/v1/invoices/66666
- THEN response status is 404
- AND response body contains message "Factura no encontrada con ID: 66666"

### Requirement: ReviewNotFoundException (404)

The system SHALL provide a `ReviewNotFoundException` class that extends `RuntimeException` with `@ResponseStatus(HttpStatus.NOT_FOUND)`.

### Requirement: DiscountCouponNotFoundException (404)

The system SHALL provide a `DiscountCouponNotFoundException` class that extends `RuntimeException` with `@ResponseStatus(HttpStatus.NOT_FOUND)`. It SHALL be thrown when a discount coupon is requested by a non-existent ID or code.

#### Scenario: GET discount coupon with invalid code returns 404

- GIVEN a discount coupon with code "INVALID" does not exist
- WHEN a client sends GET /api/v1/discount-coupons/INVALID
- THEN response status is 404
- AND response body contains message "Cupón de descuento no encontrado con código: INVALID"

### Requirement: InvalidBookingStateException (400)

The system SHALL provide a `InvalidBookingStateException` class that extends `RuntimeException` with `@ResponseStatus(HttpStatus.BAD_REQUEST)`. It SHALL be thrown when a booking operation is invalid due to the current state of the booking.

#### Scenario: Invalid booking state transition returns 400

- GIVEN a booking with status "COMPLETED" exists
- WHEN a client sends PATCH /api/v1/bookings/{id}/cancel
- THEN response status is 400
- AND response body contains message about invalid state transition

### Requirement: UnauthorizedAccessException (403)

The system SHALL provide a `UnauthorizedAccessException` class that extends `RuntimeException` with `@ResponseStatus(HttpStatus.FORBIDDEN)`. It SHALL be thrown when a user attempts to access a resource they do not have permission to access.

#### Scenario: Access without permission returns 403

- GIVEN a client is authenticated but lacks ROLE_ADMIN
- WHEN client sends GET /api/v1/users (admin-only endpoint)
- THEN response status is 403
- AND response body contains message about access denied

### Requirement: ResourceAlreadyExistsException (409)

The system SHALL provide a `ResourceAlreadyExistsException` class that extends `RuntimeException` with `@ResponseStatus(HttpStatus.CONFLICT)`. It SHALL be thrown when a duplicate resource creation is attempted.

#### Scenario: Create duplicate resource returns 409

- GIVEN a resource of type 'Account' with identifier '123' already exists
- WHEN a client attempts to create it again
- THEN response status is 409
- AND response body contains message "El recurso 'Account' ya existe: 123"

### Requirement: ConstraintViolationException Handler (400)

The system SHALL handle `ConstraintViolationException` in `GlobalExceptionHandler` and return HTTP 400 with field-level validation errors. This covers `@Validated` path variable and query parameter validation.

#### Scenario: Invalid enum value returns 400

- GIVEN a request with invalid enum value "INVALID_STATUS"
- WHEN a client sends a request with BookingStatus "INVALID_STATUS"
- THEN response status is 400
- AND response body contains message about invalid value

### Requirement: MissingServletRequestParameterException Handler (400)

The system SHALL handle `MissingServletRequestParameterException` in `GlobalExceptionHandler` and return HTTP 400 with a message indicating the missing required parameter.

#### Scenario: Missing required query parameter returns 400

- GIVEN endpoint requires query parameter "startDate"
- WHEN a client sends GET /api/v1/bookings without startDate
- THEN response status is 400
- AND response body contains message about missing parameter

### Requirement: HttpMessageNotReadableException Handler (400)

The system SHALL handle `HttpMessageNotReadableException` in `GlobalExceptionHandler` and return HTTP 400 with a message indicating malformed JSON or invalid request body format.

#### Scenario: Malformed JSON returns 400

- GIVEN a request body with malformed JSON
- WHEN a client sends POST /api/v1/users with invalid JSON
- THEN response status is 400
- AND response body contains message about malformed JSON

### Requirement: SecurityException Handler (403)

The system SHALL handle `SecurityException` in `GlobalExceptionHandler` and return HTTP 403 (not 500). This prevents leaking security implementation details.

#### Scenario: Security exception returns 403 (not 500)

- GIVEN a SecurityException is thrown during request processing
- WHEN the request is processed
- THEN response status is 403
- AND response body does NOT contain stack trace

### Requirement: MethodArgumentTypeMismatchException Handler (400)

The system SHALL handle `MethodArgumentTypeMismatchException` in `GlobalExceptionHandler` and return HTTP 400 with details about the invalid parameter type.

#### Scenario: Invalid UUID format returns 400

- GIVEN an invalid UUID format in path variable
- WHEN client sends GET /api/v1/bookings/invalid-uuid
- THEN response status is 400
- AND response body contains message about invalid parameter type

## MODIFIED Requirements

### Requirement: Update handleResourceNotFoundExceptions Handler

The system SHALL update the `handleResourceNotFoundExceptions` handler to include all new NotFoundException subclasses.

(Previously: Only handled `UserNotFoundException`, `SitterProfileNotFoundException`, `WorkExperienceNotFoundException`)

### Requirement: Update handleConflictExceptions Handler

The system SHALL update the `handleConflictExceptions` handler to include `InvalidBookingStateException` (for 400) and `ResourceAlreadyExistsException` (for 409).

(Previously: Only handled email and profile conflicts)

---

## Error Response Format

All error responses SHALL follow this format:

```json
{
  "status": 404,
  "message": "Reserva no encontrada con ID: 123",
  "timestamp": "2025-01-15T10:30:00",
  "validationErrors": null
}
```

### Field Specification

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `status` | int | HTTP status code | No |
| `message` | String | Human-readable error message in Spanish | No |
| `timestamp` | LocalDateTime | ISO-8601 timestamp | No |
| `validationErrors` | Map<String, String> | Field-specific errors | Yes |

---

## Error Codes Table

| Error Code | HTTP Status | Exception Class | Message Pattern |
|------------|-------------|-----------------|-----------------|
| ERR-001 | 400 | InvalidBookingStateException | "No se puede {acción} la reserva: estado actual es {estado}" |
| ERR-002 | 400 | ConstraintViolationException | "Validación fallida: {field} {message}" |
| ERR-003 | 400 | MissingServletRequestParameterException | "Parámetro requerido '{name}' no encontrado" |
| ERR-004 | 400 | HttpMessageNotReadableException | "Cuerpo de solicitud inválido o JSON malformado" |
| ERR-005 | 400 | MethodArgumentTypeMismatchException | "Valor '{value}' no válido para parámetro '{name}'" |
| ERR-006 | 403 | UnauthorizedAccessException | "Acceso denegado: no tienes permiso para este recurso" |
| ERR-007 | 403 | AccessDeniedException | "Acceso denegado: no tienes permiso para realizar esta acción" |
| ERR-008 | 403 | SecurityException | "Acceso denegado: condición de seguridad no cumplida" |
| ERR-009 | 404 | BookingNotFoundException | "Reserva no encontrada con ID: {id}" |
| ERR-010 | 404 | PetNotFoundException | "Mascota no encontrada con ID: {id}" |
| ERR-011 | 404 | AccountNotFoundException | "Cuenta no encontrada con ID: {id}" |
| ERR-012 | 404 | ServiceOfferingNotFoundException | "Servicio no encontrado con ID: {id}" |
| ERR-013 | 404 | InvoiceNotFoundException | "Factura no encontrada con ID: {id}" |
| ERR-014 | 404 | ReviewNotFoundException | "Reseña no encontrada con ID: {id}" |
| ERR-015 | 404 | DiscountCouponNotFoundException | "Cupón de descuento no encontrado con código: {code}" |
| ERR-016 | 409 | ResourceAlreadyExistsException | "El recurso '{type}' ya existe: {detail}" |
| ERR-017 | 409 | EmailAlreadyExistsException | "El email {email} ya está registrado" |
| ERR-018 | 409 | SitterProfileAlreadyExistsException | "El perfil de cuidador ya existe para este usuario" |
| ERR-019 | 409 | DataIntegrityViolationException | "No se puede eliminar: existen registros dependientes" |

---

## Edge Cases

| Edge Case | Expected Behavior |
|-----------|-------------------|
| Empty request body | Return 400 with "Validation failed" |
| Malformed JSON | Return 400 with generic message |
| Missing required parameter | Return 400 with parameter name |
| Invalid enum value | Return 400 with value and expected type |
| Expired JWT token | Return 401 with "Token has expired" |
| Access to deleted resource | Return 404 (not 403) |
| Concurrent duplicate creation | First succeeds (201), second returns 409 |
| Database connection failure | Return 500 with generic error |

---

*Spec Version: 1.0*
*Status: Ready for Design Phase*
