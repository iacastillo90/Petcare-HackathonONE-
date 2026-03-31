# Delta for Test Fixes

## Purpose

This spec documents the issues found in the test suite and defines the success criteria to achieve 100% pass rate for the affected test classes.

## ADDED Requirements

### Requirement: SitterProfileControllerTest - Authorization Fix

The system SHALL ensure all authorization tests in `SitterProfileControllerTest` pass correctly.

#### Scenario: GET /api/sitter-profiles returns 403 for CLIENT role

- GIVEN a user with role CLIENT is authenticated
- WHEN client sends GET /api/sitter-profiles
- THEN response status is 403 Forbidden

#### Scenario: GET /api/sitter-profiles returns 403 for SITTER role

- GIVEN a user with role SITTER is authenticated
- WHEN sitter sends GET /api/sitter-profiles
- THEN response status is 403 Forbidden

#### Scenario: GET /api/sitter-profiles/{userId} returns 200 for own profile

- GIVEN a user with role SITTER owns profile with userId=5
- WHEN sitter sends GET /api/sitter-profiles/5
- THEN response status is 200 OK

#### Scenario: GET /api/sitter-profiles/{userId} returns 403 for other user's profile

- GIVEN a user with role SITTER is authenticated
- WHEN sitter sends GET /api/sitter-profiles/999 (other user's profile)
- THEN response status is 403 Forbidden

#### Scenario: POST /api/sitter-profiles returns 403 for CLIENT role

- GIVEN a user with role CLIENT is authenticated
- WHEN client sends POST /api/sitter-profiles
- THEN response status is 403 Forbidden

#### Scenario: PUT /api/sitter-profiles/{userId} returns 403 for CLIENT role

- GIVEN a user with role CLIENT is authenticated
- WHEN client sends PUT /api/sitter-profiles/5
- THEN response status is 403 Forbidden

#### Scenario: PUT /api/sitter-profiles/{userId} returns 403 for other SITTER

- GIVEN a SITTER with userId=1 is authenticated
- WHEN sitter sends PUT /api/sitter-profiles/999
- THEN response status is 403 Forbidden

### Requirement: SitterProfileControllerTest - Validation and 404 Fix

#### Scenario: POST /api/sitter-profiles returns 409 for existing profile

- GIVEN a Sitter already has a profile
- WHEN client sends POST /api/sitter-profiles
- THEN response status is 409 Conflict

#### Scenario: PUT /api/sitter-profiles/{userId} returns 400 for invalid DTO

- GIVEN a Sitter sends PUT request with invalid DTO data
- WHEN client sends PUT /api/sitter-profiles/5
- THEN response status is 400 Bad Request

#### Scenario: GET /api/sitter-profiles/{userId} returns 404 for non-existent profile

- GIVEN a User exists but has no SitterProfile
- WHEN client sends GET /api/sitter-profiles/{userId}
- THEN response status is 404 Not Found

### Requirement: SitterWorkExperienceControllerTest - Authorization Fix

The system SHALL ensure all authorization tests in `SitterWorkExperienceControllerTest` pass correctly.

#### Scenario: POST /api/sitter-work-experience returns 403 for CLIENT role

- GIVEN a user with role CLIENT is authenticated
- WHEN client sends POST /api/sitter-work-experience
- THEN response status is 403 Forbidden

#### Scenario: POST /api/sitter-work-experience returns 201 for SITTER owner

- GIVEN a SITTER owns a SitterProfile
- WHEN sitter sends POST /api/sitter-work-experience
- THEN response status is 201 Created

#### Scenario: POST /api/sitter-work-experience returns 201 for ADMIN

- GIVEN a user with role ADMIN is authenticated
- WHEN admin sends POST /api/sitter-work-experience
- THEN response status is 201 Created

#### Scenario: POST /api/sitter-work-experience returns 404 for non-existent SitterProfileId

- GIVEN no SitterProfile exists with id=99999
- WHEN client sends POST /api/sitter-work-experience with sitterProfileId=99999
- THEN response status is 404 Not Found

#### Scenario: POST /api/sitter-work-experience returns 400 for invalid DTO

- GIVEN a client sends POST with invalid work experience data
- WHEN client sends POST /api/sitter-work-experience
- THEN response status is 400 Bad Request

### Requirement: UserControllerTest - Authorization Fix

The system SHALL ensure all authorization tests in `UserControllerTest` pass correctly.

#### Scenario: GET /api/users returns 403 for CLIENT role

- GIVEN a user with role CLIENT is authenticated
- WHEN client sends GET /api/users
- THEN response status is 403 Forbidden

#### Scenario: GET /api/users returns 200 for ADMIN role

- GIVEN a user with role ADMIN is authenticated
- WHEN admin sends GET /api/users
- THEN response status is 200 OK

#### Scenario: GET /api/users/{id} returns 403 for CLIENT accessing other user

- GIVEN a user with role CLIENT is authenticated
- WHEN client sends GET /api/users/999
- THEN response status is 403 Forbidden

#### Scenario: GET /api/users/{id} returns 200 for ADMIN

- GIVEN a user with role ADMIN is authenticated
- WHEN admin sends GET /api/users/5
- THEN response status is 200 OK

#### Scenario: PUT /api/users/{id} returns 403 for CLIENT

- GIVEN a user with role CLIENT is authenticated
- WHEN client sends PUT /api/users/5
- THEN response status is 403 Forbidden

#### Scenario: PUT /api/users/{id} returns 200 for ADMIN

- GIVEN a user with role ADMIN is authenticated
- WHEN admin sends PUT /api/users/5
- THEN response status is 200 OK

#### Scenario: DELETE /api/users/{id} returns 200 for ADMIN

- GIVEN a user with role ADMIN is authenticated
- WHEN admin sends DELETE /api/users/5
- THEN response status is 200 OK

#### Scenario: POST /admin returns 403 for CLIENT

- GIVEN a user with role CLIENT is authenticated
- WHEN client sends POST /api/admin
- THEN response status is 403 Forbidden

#### Scenario: POST /admin returns 201 for ADMIN

- GIVEN a user with role ADMIN is authenticated
- WHEN admin sends POST /api/admin
- THEN response status is 201 Created

#### Scenario: PATCH /api/users/{id}/verify-email returns 403 for CLIENT

- GIVEN a user with role CLIENT is authenticated
- WHEN client sends PATCH /api/users/5/verify-email
- THEN response status is 403 Forbidden

#### Scenario: PATCH /api/users/{id}/verify-email returns 200 for ADMIN

- GIVEN a user with role ADMIN is authenticated
- WHEN admin sends PATCH /api/users/5/verify-email
- THEN response status is 200 OK

#### Scenario: PATCH /api/users/{id}/toggle-active returns 403 for CLIENT

- GIVEN a user with role CLIENT is authenticated
- WHEN client sends PATCH /api/users/5/toggle-active
- THEN response status is 403 Forbidden

#### Scenario: GET /api/users/unverified returns 403 for CLIENT

- GIVEN a user with role CLIENT is authenticated
- WHEN client sends GET /api/users/unverified
- THEN response status is 403 Forbidden

#### Scenario: GET /api/users/role/{role} returns 403 for CLIENT

- GIVEN a user with role CLIENT is authenticated
- WHEN client sends GET /api/users/role/SITTER
- THEN response status is 403 Forbidden

#### Scenario: GET /api/users/role/{role} returns 200 for ADMIN

- GIVEN a user with role ADMIN is authenticated
- WHEN admin sends GET /api/users/role/SITTER
- THEN response status is 200 OK

#### Scenario: GET /api/users/active returns 403 for CLIENT

- GIVEN a user with role CLIENT is authenticated
- WHEN client sends GET /api/users/active
- THEN response status is 403 Forbidden

#### Scenario: GET /api/users/active returns 200 for ADMIN

- GIVEN a user with role ADMIN is authenticated
- WHEN admin sends GET /api/users/active
- THEN response status is 200 OK

#### Scenario: GET /api/users/stats returns 403 for CLIENT

- GIVEN a user with role CLIENT is authenticated
- WHEN client sends GET /api/users/stats
- THEN response status is 403 Forbidden

#### Scenario: GET /summary returns 200 for ADMIN

- GIVEN a user with role ADMIN is authenticated
- WHEN admin sends GET /api/users/summary
- THEN response status is 200 OK

#### Scenario: GET /api/users/email/{email} returns 200 for ADMIN

- GIVEN a user with role ADMIN is authenticated
- WHEN admin sends GET /api/users/email/test@example.com
- THEN response status is 200 OK

### Requirement: GlobalExceptionHandler Coverage Verification

The system SHALL ensure `GlobalExceptionHandler` handles all exceptions defined in the error-handling spec.

#### Scenario: All NotFoundException subclasses return 404

- GIVEN a request for a non-existent resource
- WHEN the controller throws any NotFoundException subclass
- THEN response status is 404
- AND response body follows ErrorResponseDTO format

#### Scenario: All validation errors return 400

- GIVEN a request with invalid data
- WHEN validation fails
- THEN response status is 400
- AND response body contains field-level errors

#### Scenario: All authorization errors return 403

- GIVEN an authenticated user without permission
- WHEN user attempts restricted action
- THEN response status is 403
- AND response body contains access denied message

---

## MODIFIED Requirements

### Requirement: TestSecurityConfig Consistency

The test security configuration MUST align with production security behavior.

(Previously: Tests expected different HTTP status codes than what Spring Security returns)

---

## Error Response Format

All error responses SHALL follow this format (per error-handling spec):

```json
{
  "status": 403,
  "message": "Acceso denegado: no tienes permiso para este recurso",
  "timestamp": "2025-01-15T10:30:00",
  "validationErrors": null
}
```

---

## Success Criteria Summary

| Test Class | Target Pass Rate | Current Failing |
|------------|------------------|-----------------|
| SitterProfileControllerTest | 100% (24/24) | 16 tests |
| SitterWorkExperienceControllerTest | 100% (10/10) | 5 tests |
| UserControllerTest | 100% (37/37) | 21 tests |
| **Total** | **100% (71/71)** | **42 tests** |

---

## Test Execution Requirements

### Phase 1: Run Individual Test Classes
```bash
./gradlew test --tests "com.Petcare.Petcare.Controllers.SitterProfileControllerTest"
./gradlew test --tests "com.Petcare.Petcare.Controllers.SitterWorkExperienceControllerTest"
./gradlew test --tests "com.Petcare.Petcare.Controllers.UserControllerTest"
```

### Phase 2: Run Full Test Suite
```bash
./gradlew test
```

### Phase 3: Verify No Regression
All existing passing tests MUST continue to pass after fixes.

---

## Root Cause Categories Identified

| Category | Affected Tests | Likely Cause |
|----------|---------------|--------------|
| Authorization (403 vs other) | ~35 tests | TestSecurityConfig not mocking correct roles |
| Validation (400) | ~3 tests | DTO validation not working in tests |
| Not Found (404) | ~4 tests | Test data setup incorrect |

---

*Spec Version: 1.0*
*Status: Ready for Design Phase*
