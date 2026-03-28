# Tasks: improve-error-handling

## Overview
Replace all generic exceptions (`IllegalArgumentException`, `IllegalStateException`, `RuntimeException`) with typed business exceptions following the existing `UserNotFoundException` pattern.

## Phase 1: Exception Classes - Not Found (5 tasks)

- [ ] 1.1 **Create BookingNotFoundException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/BookingNotFoundException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.NOT_FOUND)` and constructor accepting `Long id`
  - Verification: `./gradlew test --tests "*BookingService*" 2>&1 | grep -i "booking"`

- [ ] 1.2 **Create PetNotFoundException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/PetNotFoundException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.NOT_FOUND)` and constructor accepting `Long id`
  - Verification: `./gradlew test --tests "*PetService*" 2>&1 | grep -i "pet"`

- [ ] 1.3 **Create AccountNotFoundException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/AccountNotFoundException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.NOT_FOUND)` and constructor accepting `Long id`
  - Verification: `./gradlew test --tests "*AccountService*" 2>&1 | grep -i "account"`

- [ ] 1.4 **Create ServiceOfferingNotFoundException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/ServiceOfferingNotFoundException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.NOT_FOUND)` and constructor accepting `Long id`
  - Verification: `./gradlew test --tests "*BookingService*" 2>&1 | grep -i "service"`

- [ ] 1.5 **Create InvoiceNotFoundException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/InvoiceNotFoundException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.NOT_FOUND)` and constructor accepting `Long id`
  - Verification: `./gradlew test --tests "*InvoiceService*" 2>&1 | grep -i "invoice"`

## Phase 2: Exception Classes - Conflict/State (8 tasks)

- [ ] 2.1 **Create BookingStateException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/BookingStateException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.CONFLICT)` and message parameter
  - Verification: Check imports in BookingServiceImplement

- [ ] 2.2 **Create BookingConflictException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/BookingConflictException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.CONFLICT)` for schedule conflicts
  - Verification: Check imports in BookingServiceImplement

- [ ] 2.3 **Create InvoiceStateException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/InvoiceStateException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.CONFLICT)` and message parameter
  - Verification: Check imports in InvoiceServiceImplement

- [ ] 2.4 **Create InvoiceAlreadyExistsException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/InvoiceAlreadyExistsException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.CONFLICT)` and booking ID parameter
  - Verification: Check imports in InvoiceServiceImplement

- [ ] 2.5 **Create PetAlreadyExistsException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/PetAlreadyExistsException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.CONFLICT)` and name/account parameters
  - Verification: Check imports in PetServiceImplement

- [ ] 2.6 **Create InactiveAccountException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/InactiveAccountException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.BAD_REQUEST)` and account ID parameter
  - Verification: Check imports in services using inactive accounts

- [ ] 2.7 **Create ServiceOfferingInactiveException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/ServiceOfferingInactiveException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.BAD_REQUEST)` and offering ID parameter
  - Verification: Check imports in BookingServiceImplement

- [ ] 2.8 **Create SitterInactiveException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/SitterInactiveException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.BAD_REQUEST)` and sitter email parameter
  - Verification: Check imports in BookingServiceImplement

## Phase 3: Exception Classes - Validation (7 tasks)

- [ ] 3.1 **Create InvalidAmountException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/InvalidAmountException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.BAD_REQUEST)` and amount parameter
  - Verification: Check imports in InvoiceServiceImplement

- [ ] 3.2 **Create CancellationReasonRequiredException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/CancellationReasonRequiredException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.BAD_REQUEST)`
  - Verification: Check imports in BookingServiceImplement and InvoiceServiceImplement

- [ ] 3.3 **Create InsufficientTimeException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/InsufficientTimeException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.BAD_REQUEST)` and hours parameter
  - Verification: Check imports in BookingServiceImplement

- [ ] 3.4 **Create SitterNotFoundException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/SitterNotFoundException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.NOT_FOUND)` and email parameter
  - Verification: Check imports in BookingServiceImplement

- [ ] 3.5 **Create SitterRoleRequiredException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/SitterRoleRequiredException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.BAD_REQUEST)`
  - Verification: Check imports in BookingServiceImplement

- [ ] 3.6 **Create MaxPendingBookingsExceededException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/MaxPendingBookingsExceededException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.BAD_REQUEST)` and max parameter
  - Verification: Check imports in BookingServiceImplement

- [ ] 3.7 **Create UnauthorizedPetAccessException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/UnauthorizedPetAccessException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.FORBIDDEN)` and pet/account IDs
  - Verification: Check imports in BookingServiceImplement

## Phase 4: Exception Classes - Coupon (2 tasks)

- [ ] 4.1 **Create CouponNotFoundException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/CouponNotFoundException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.NOT_FOUND)` and coupon ID parameter
  - Verification: Check imports in AppliedCouponServiceImplement

- [ ] 4.2 **Create CouponExpiredException.java**
  - File: `src/main/java/com/Petcare/Petcare/Exception/Business/CouponExpiredException.java`
  - Changes: Create class with `@ResponseStatus(HttpStatus.BAD_REQUEST)` and coupon code parameter
  - Verification: Check imports in AppliedCouponServiceImplement

## Phase 5: GlobalExceptionHandler Updates (1 task)

- [ ] 5.1 **Update GlobalExceptionHandler with grouped handlers**
  - File: `src/main/java/com/Petcare/Petcare/Exception/GlobalExceptionHandler.java`
  - Changes:
    - Add handler for state exceptions (BookingStateException, InvoiceStateException) → 409 Conflict
    - Add handler for conflict exceptions (BookingConflictException, InvoiceAlreadyExistsException, PetAlreadyExistsException) → 409 Conflict
    - Add handler for inactive resource exceptions (InactiveAccountException, SitterInactiveException, ServiceOfferingInactiveException) → 400 Bad Request
    - Add handler for validation exceptions (InvalidAmountException, CancellationReasonRequiredException, InsufficientTimeException, SitterRoleRequiredException, MaxPendingBookingsExceededException, UnauthorizedPetAccessException) → 400 Bad Request
    - Add handler for coupon exceptions (CouponNotFoundException, CouponExpiredException) → 404/400
    - Add new NotFound exceptions (BookingNotFoundException, PetNotFoundException, AccountNotFoundException, ServiceOfferingNotFoundException, InvoiceNotFoundException, SitterNotFoundException) to existing handler
  - Verification: `./gradlew test 2>&1 | tail -20`

## Phase 6: Service Migration - BookingServiceImplement (1 task)

- [ ] 6.1 **Migrate BookingServiceImplement to typed exceptions**
  - File: `src/main/java/com/Petcare/Petcare/Services/Implement/BookingServiceImplement.java`
  - Changes:
    - Replace all `IllegalArgumentException` with appropriate typed exceptions
    - Replace all `IllegalStateException` with BookingStateException
    - Add imports for new exception classes
    - Verify message format remains consistent with existing error messages
  - Verification: `./gradlew test --tests "*BookingService*" 2>&1`

## Phase 7: Service Migration - PetServiceImplement (1 task)

- [ ] 7.1 **Migrate PetServiceImplement to typed exceptions**
  - File: `src/main/java/com/Petcare/Petcare/Services/Implement/PetServiceImplement.java`
  - Changes:
    - Replace all `IllegalStateException` with appropriate typed exceptions
    - Replace all `IllegalArgumentException` with PetAlreadyExistsException or InactiveAccountException
    - Add imports for new exception classes
  - Verification: `./gradlew test --tests "*PetService*" 2>&1`

## Phase 8: Service Migration - InvoiceServiceImplement (1 task)

- [ ] 8.1 **Migrate InvoiceServiceImplement to typed exceptions**
  - File: `src/main/java/com/Petcare/Petcare/Services/Implement/InvoiceServiceImplement.java`
  - Changes:
    - Replace all `IllegalArgumentException` with InvoiceNotFoundException, InvalidAmountException, etc.
    - Replace all `IllegalStateException` with InvoiceStateException
    - Add imports for new exception classes
  - Verification: `./gradlew test --tests "*InvoiceService*" 2>&1`

## Phase 9: Service Migration - AppliedCouponServiceImplement (1 task)

- [ ] 9.1 **Migrate AppliedCouponServiceImplement to typed exceptions**
  - File: `src/main/java/com/Petcare/Petcare/Services/Implement/AppliedCouponServiceImplement.java`
  - Changes:
    - Replace `RuntimeException` with CouponNotFoundException and CouponExpiredException
    - Add imports for new exception classes
  - Verification: `./gradlew test --tests "*AppliedCoupon*" 2>&1`

## Phase 10: Verification & Testing (1 task)

- [ ] 10.1 **Run full test suite and verify error responses**
  - Files: All modified services and new exception classes
  - Changes:
    - Run `./gradlew test` to ensure no regressions
    - Run `./gradlew build` to ensure compilation
    - Manual verification via Swagger UI for error scenarios:
      - GET /api/v1/bookings/99999 → 404 with BookingNotFoundException message
      - POST /api/v1/bookings with invalid state → 409 with BookingStateException message
      - GET /api/v1/pets/99999 → 404 with PetNotFoundException message
      - GET /api/v1/invoices/99999 → 404 with InvoiceNotFoundException message
  - Verification: `./gradlew test && ./gradlew build`

---

## Task Summary

| Phase | Tasks | Focus |
|-------|-------|-------|
| Phase 1 | 5 | Not Found Exceptions |
| Phase 2 | 8 | Conflict/State Exceptions |
| Phase 3 | 7 | Validation Exceptions |
| Phase 4 | 2 | Coupon Exceptions |
| Phase 5 | 1 | GlobalExceptionHandler Updates |
| Phase 6 | 1 | BookingServiceImplement Migration |
| Phase 7 | 1 | PetServiceImplement Migration |
| Phase 8 | 1 | InvoiceServiceImplement Migration |
| Phase 9 | 1 | AppliedCouponServiceImplement Migration |
| Phase 10 | 1 | Verification & Testing |
| **Total** | **28** | |

## Implementation Order

1. **Phases 1-4 (Exception Classes)**: Create all 22 exception classes first - these are dependencies for all other phases
2. **Phase 5 (GlobalExceptionHandler)**: Add handlers after exception classes exist
3. **Phases 6-9 (Service Migration)**: Migrate services in parallel or any order - they don't depend on each other
4. **Phase 10 (Verification)**: Run full test suite last

## Exception Class Template Reference

```java
package com.Petcare.Petcare.Exception.Business;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)  // or CONFLICT, BAD_REQUEST, etc.
public class {Entity}{Reason}Exception extends RuntimeException {
    public {Entity}{Reason}Exception({params}) {
        super("Descriptive message in Spanish: " + params);
    }
}
```

## Key Files Reference

| File | Purpose |
|------|---------|
| `src/main/java/com/Petcare/Petcare/Exception/Business/*.java` | Business exception classes |
| `src/main/java/com/Petcare/Petcare/Exception/GlobalExceptionHandler.java` | Exception handler |
| `src/main/java/com/Petcare/Petcare/Services/Implement/BookingServiceImplement.java` | Main service to migrate |
| `src/main/java/com/Petcare/Petcare/Services/Implement/PetServiceImplement.java` | Service to migrate |
| `src/main/java/com/Petcare/Petcare/Services/Implement/InvoiceServiceImplement.java` | Service to migrate |
| `src/main/java/com/Petcare/Petcare/Services/Implement/AppliedCouponServiceImplement.java` | Service to migrate |
