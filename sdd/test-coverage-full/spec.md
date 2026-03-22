# Test Coverage Specification — test-coverage-full

## Purpose

This specification defines comprehensive test coverage requirements for all untested services and controllers in the Petcare application. The goal is to achieve thorough unit and integration test coverage following existing patterns.

## ADDED Requirements

---

## Section 1: Service Test Requirements

### Requirement: BookingServiceImplement Test Suite

The system SHALL have a comprehensive unit test suite for `BookingServiceImplement` covering all business logic methods.

#### Scenario: Create booking with valid data

- GIVEN a valid `CreateBookingRequest` with client, pet, sitter, and service details
- AND the authenticated user exists and has a verified email
- WHEN `createBooking()` is invoked
- THEN a booking SHALL be created with status PENDING
- AND repository save SHALL be called with the booking entity
- AND notifications SHALL be triggered for sitter and client

#### Scenario: Create booking with invalid service offering

- GIVEN a `CreateBookingRequest` referencing an inactive service offering
- WHEN `createBooking()` is invoked
- THEN `ServiceOfferingInactiveException` SHALL be thrown
- AND no booking SHALL be persisted

#### Scenario: Create booking with conflicting dates

- GIVEN a `CreateBookingRequest` with dates that conflict with existing bookings for the same pet
- WHEN `createBooking()` is invoked
- THEN `IllegalStateException` SHALL be thrown with "conflicting dates" message
- AND no booking SHALL be persisted

#### Scenario: Get booking by ID - exists

- GIVEN a booking ID that exists in the repository
- WHEN `getBookingById()` is invoked
- THEN a `BookingDetailResponse` SHALL be returned with all booking details
- AND pet, client, sitter, and service information SHALL be included

#### Scenario: Get booking by ID - not exists

- GIVEN a booking ID that does not exist
- WHEN `getBookingById()` is invoked
- THEN `IllegalArgumentException` SHALL be thrown

#### Scenario: Update booking status - valid transition

- GIVEN an existing booking with status PENDING
- WHEN `updateBookingStatus()` is invoked with CONFIRMED
- THEN the booking status SHALL be updated to CONFIRMED
- AND `save()` SHALL be called on the repository
- AND confirmation notification SHALL be sent

#### Scenario: Update booking status - invalid transition

- GIVEN an existing booking with status COMPLETED
- WHEN `updateBookingStatus()` is invoked with CANCELLED
- THEN `IllegalStateException` SHALL be thrown
- AND the booking status SHALL remain COMPLETED

#### Scenario: Delete booking - pending status

- GIVEN a booking with status PENDING
- WHEN `deleteBooking()` is invoked
- THEN the booking SHALL be deleted from repository
- AND cancellation notification SHALL be sent

#### Scenario: Delete booking - completed status (not allowed)

- GIVEN a booking with status COMPLETED
- WHEN `deleteBooking()` is invoked
- THEN `IllegalStateException` SHALL be thrown with "cannot delete completed booking"

#### Scenario: Get bookings filtered by user and role

- GIVEN authenticated SITTER user with associated bookings
- WHEN `getBookingsByUser()` is invoked with sitter's userId and role SITTER
- THEN only bookings assigned to that sitter SHALL be returned
- AND results SHALL be paginated

#### Scenario: Get all bookings as ADMIN

- GIVEN authenticated ADMIN user
- WHEN `getAllBookings()` is invoked
- THEN all bookings SHALL be returned regardless of owner
- AND results SHALL be paginated

---

### Requirement: InvoiceServiceImplement Test Suite

The system SHALL have a comprehensive unit test suite for `InvoiceServiceImplement`.

#### Scenario: Generate invoice for completed booking

- GIVEN a completed booking with associated pricing
- WHEN `generateInvoiceForBooking()` is invoked with valid `CreateInvoiceRequest`
- THEN an invoice SHALL be created with calculated totals
- AND booking reference SHALL be set
- AND platform fee SHALL be calculated

#### Scenario: Generate invoice for non-existent booking

- GIVEN a `CreateInvoiceRequest` with invalid booking ID
- WHEN `generateInvoiceForBooking()` is invoked
- THEN `IllegalArgumentException` SHALL be thrown

#### Scenario: Generate invoice for already invoiced booking

- GIVEN a booking that already has an invoice
- WHEN `generateInvoiceForBooking()` is invoked
- THEN `IllegalStateException` SHALL be thrown with "invoice already exists"

#### Scenario: Get invoice by ID - exists

- GIVEN a valid invoice ID
- WHEN `getInvoiceById()` is invoked
- THEN `InvoiceDetailResponse` SHALL be returned with line items

#### Scenario: Get invoice by ID - not exists

- GIVEN an invalid invoice ID
- WHEN `getInvoiceById()` is invoked
- THEN `IllegalArgumentException` SHALL be thrown

#### Scenario: Get invoices by account ID

- GIVEN a valid account ID with multiple invoices
- WHEN `getInvoicesByAccountId()` is invoked
- THEN paginated list of `InvoiceSummaryResponse` SHALL be returned
- AND results SHALL be ordered by date descending

#### Scenario: Send invoice

- GIVEN a valid invoice ID
- WHEN `sendInvoice()` is invoked
- THEN invoice status SHALL be updated to SENT
- AND email notification SHALL be triggered

#### Scenario: Cancel invoice with valid reason

- GIVEN a valid invoice ID and cancellation reason
- WHEN `cancelInvoice()` is invoked
- THEN invoice status SHALL be CANCELLED
- AND cancellation reason SHALL be persisted

#### Scenario: Cancel invoice without reason

- GIVEN a valid invoice ID with null or empty reason
- WHEN `cancelInvoice()` is invoked
- THEN `IllegalArgumentException` SHALL be thrown

---

### Requirement: DiscountCouponServiceImplement Test Suite

The system SHALL have a comprehensive unit test suite for `DiscountCouponServiceImplement`.

#### Scenario: Save valid discount coupon

- GIVEN a valid `DiscountCoupon` entity
- WHEN `saveDiscountCoupon()` is invoked
- THEN the coupon SHALL be saved to repository
- AND the saved entity SHALL be returned

#### Scenario: Get coupon by ID - exists

- GIVEN a valid coupon ID
- WHEN `getDiscountCouponById()` is invoked
- THEN `Optional<DiscountCoupon>` SHALL be returned with coupon data

#### Scenario: Get coupon by ID - not exists

- GIVEN an invalid coupon ID
- WHEN `getDiscountCouponById()` is invoked
- THEN `Optional.empty()` SHALL be returned

#### Scenario: Get coupon by code - exists

- GIVEN a valid coupon code
- WHEN `getDiscountCouponByCode()` is invoked
- THEN `Optional<DiscountCoupon>` SHALL be returned

#### Scenario: Get coupon by code - not exists

- GIVEN a non-existent coupon code
- WHEN `getDiscountCouponByCode()` is invoked
- THEN `Optional.empty()` SHALL be returned

#### Scenario: Get all coupons

- GIVEN multiple discount coupons in database
- WHEN `getAllDiscountCoupons()` is invoked
- THEN list of all coupons SHALL be returned

#### Scenario: Delete coupon

- GIVEN a valid coupon ID
- WHEN `deleteDiscountCoupon()` is invoked
- THEN repository `deleteById()` SHALL be called

---

### Requirement: AppliedCouponServiceImplement Test Suite

The system SHALL have a comprehensive unit test suite for `AppliedCouponServiceImplement`.

#### Scenario: Apply valid coupon to booking

- GIVEN valid booking ID, account ID, and active coupon code
- AND the coupon is not expired and usage limit not reached
- WHEN `applyCoupon()` is invoked
- THEN `AppliedCoupon` record SHALL be created
- AND booking discount SHALL be calculated
- AND coupon usage count SHALL be incremented

#### Scenario: Apply expired coupon

- GIVEN an expired coupon code
- WHEN `applyCoupon()` is invoked
- THEN `IllegalStateException` SHALL be thrown with "coupon expired"

#### Scenario: Apply coupon exceeding usage limit

- GIVEN a coupon that has reached maximum usage
- WHEN `applyCoupon()` is invoked
- THEN `IllegalStateException` SHALL be thrown with "coupon usage limit reached"

#### Scenario: Validate valid coupon

- GIVEN a valid `DiscountCoupon` that is active and not expired
- WHEN `validateCoupon()` is invoked
- THEN `true` SHALL be returned

#### Scenario: Validate inactive coupon

- GIVEN a `DiscountCoupon` with active = false
- WHEN `validateCoupon()` is invoked
- THEN `false` SHALL be returned

#### Scenario: Get coupons by account

- GIVEN a valid account ID with applied coupons
- WHEN `getCouponsByAccount()` is invoked
- THEN list of all `AppliedCoupon` for that account SHALL be returned

#### Scenario: Get coupons by booking

- GIVEN a valid booking ID with applied coupons
- WHEN `getCouponsByBooking()` is invoked
- THEN list of all `AppliedCoupon` for that booking SHALL be returned

---

### Requirement: AccountServiceImplement Test Suite

The system SHALL have a comprehensive unit test suite for `AccountServiceImplement`.

#### Scenario: Create account with valid data

- GIVEN valid `CreateAccountRequest` and owner `User`
- WHEN `createAccount()` is invoked
- THEN `AccountResponse` SHALL be returned
- AND account SHALL be saved with generated account number
- AND user-account relationship SHALL be established

#### Scenario: Get account by ID - exists

- GIVEN a valid account ID
- WHEN `getAccountById()` is invoked
- THEN `AccountResponse` SHALL be returned with all details

#### Scenario: Get account by ID - not exists

- GIVEN an invalid account ID
- THEN appropriate exception SHALL be thrown

#### Scenario: Get all accounts as ADMIN

- GIVEN authenticated ADMIN user
- WHEN `getAllAccounts()` is invoked
- THEN all accounts SHALL be returned

---

### Requirement: ServiceOfferingServiceImplement Test Suite

The system SHALL have a comprehensive unit test suite for `ServiceOfferingServiceImplement`.

#### Scenario: Create service offering

- GIVEN valid `CreateServiceOfferingDTO` and user ID
- WHEN `createServiceOffering()` is invoked
- THEN service offering SHALL be saved
- AND `ServiceOfferingDTO` SHALL be returned

#### Scenario: Get all services

- GIVEN multiple service offerings in database
- WHEN `getAllServices()` is invoked
- THEN list of all active `ServiceOfferingDTO` SHALL be returned

#### Scenario: Get services by user ID

- GIVEN a valid user ID with service offerings
- WHEN `getAllSetvicesByUserId()` is invoked
- THEN only services for that user SHALL be returned

#### Scenario: Get service by ID - exists

- GIVEN a valid service offering ID
- WHEN `getServiceById()` is invoked
- THEN `ServiceOfferingDTO` SHALL be returned

#### Scenario: Get service by ID - not exists

- GIVEN an invalid service offering ID
- WHEN `getServiceById()` is invoked
- THEN `ServiceOfferingNotFoundException` SHALL be thrown

#### Scenario: Update service offering

- GIVEN valid service ID and `UpdateServiceOfferingDTO`
- WHEN `updateServiceOffering()` is invoked
- THEN service SHALL be updated
- AND updated `ServiceOfferingDTO` SHALL be returned

#### Scenario: Delete service offering (soft delete)

- GIVEN a valid service offering ID
- WHEN `deleteServiceOffering()` is invoked
- THEN service active status SHALL be set to false
- AND repository save SHALL be called

---

### Requirement: PlatformFeeServiceImplement Test Suite

The system SHALL have a comprehensive unit test suite for `PlatformFeeServiceImplement`.

#### Scenario: Calculate and create fee

- GIVEN valid `CreatePlatformFeeRequest` with booking ID and percentage
- WHEN `calculateAndCreateFee()` is invoked
- THEN platform fee SHALL be calculated based on booking total
- AND fee SHALL be saved
- AND `PlatformFeeResponse` SHALL be returned

#### Scenario: Recalculate fee on booking update

- GIVEN an existing booking with platform fee
- AND booking price changes
- WHEN `recalculatePlatformFee()` is invoked
- THEN existing fee SHALL be updated with new calculation
- AND fee SHALL be saved

#### Scenario: Create fee for invoice

- GIVEN a saved invoice
- WHEN `calculateAndCreateFee()` is invoked with invoice
- THEN fee SHALL be calculated from invoice total
- AND fee SHALL be associated with invoice

---

### Requirement: NotificationServiceImplement Test Suite

The system SHALL have a comprehensive unit test suite for `NotificationServiceImplement`.

#### Scenario: Send notification successfully

- GIVEN valid recipient, type, and content
- WHEN `sendNotification()` is invoked
- THEN notification SHALL be saved
- AND external notification channel SHALL be called

#### Scenario: Send notification to offline user

- GIVEN a user with no registered devices
- WHEN `sendNotification()` is invoked
- THEN notification SHALL be saved to database only
- AND no external call SHALL be made

#### Scenario: Get user notifications

- GIVEN a valid user ID
- WHEN `getNotificationsForUser()` is invoked
- THEN paginated list of user notifications SHALL be returned
- AND sorted by creation date descending

---

### Requirement: EmailServiceImplement Test Suite

The system SHALL have a comprehensive unit test suite for `EmailServiceImplement`.

#### Scenario: Send verification email

- GIVEN valid email, name, and verification URL
- WHEN `sendVerificationEmail()` is invoked
- THEN Thymeleaf template SHALL be processed
- AND email SHALL be sent via SMTP

#### Scenario: Send booking confirmation email

- GIVEN valid booking details
- WHEN `sendBookingConfirmation()` is invoked
- THEN correct template SHALL be used
- AND email SHALL be sent to client and sitter

#### Scenario: Send invoice email

- GIVEN valid invoice
- WHEN `sendInvoiceEmail()` is invoked
- THEN invoice PDF MAY be attached
- AND email SHALL be sent

---

### Requirement: PdfGenerationServiceImplement Test Suite

The system SHALL have a comprehensive unit test suite for `PdfGenerationServiceImplement`.

#### Scenario: Generate invoice PDF

- GIVEN valid invoice with items
- WHEN `generateInvoicePdf()` is invoked
- THEN PDF bytes SHALL be generated
- AND include all invoice line items
- AND include company branding

#### Scenario: Generate booking confirmation PDF

- GIVEN valid booking details
- WHEN `generateBookingConfirmationPdf()` is invoked
- THEN PDF bytes SHALL be generated
- AND include booking summary

---

## Section 2: Controller Test Requirements

### Requirement: BookingController Test Suite

The system SHALL have integration tests for `BookingController`.

#### Scenario: POST /api/bookings - success

- GIVEN valid booking request and authenticated CLIENT
- WHEN POST request is made to `/api/bookings`
- THEN 201 Created SHALL be returned
- AND response body SHALL contain `BookingDetailResponse`

#### Scenario: POST /api/bookings - invalid data

- GIVEN invalid booking request (missing required fields)
- WHEN POST request is made
- THEN 400 Bad Request SHALL be returned

#### Scenario: POST /api/bookings - unauthenticated

- GIVEN no authentication token
- WHEN POST request is made
- THEN 401 Unauthorized SHALL be returned

#### Scenario: GET /api/bookings/{id} - success

- GIVEN valid booking ID and authenticated owner
- WHEN GET request is made
- THEN 200 OK SHALL be returned with booking details

#### Scenario: GET /api/bookings/{id} - not owner

- GIVEN valid booking ID and authenticated user who is not the owner
- WHEN GET request is made
- THEN 403 Forbidden SHALL be returned

#### Scenario: PUT /api/bookings/{id}/status - admin update

- GIVEN valid booking ID and authenticated ADMIN
- WHEN PUT request is made to update status
- THEN 200 OK SHALL be returned
- AND booking status SHALL be updated

#### Scenario: DELETE /api/bookings/{id} - success

- GIVEN valid booking ID and authenticated owner or ADMIN
- WHEN DELETE request is made
- THEN 204 No Content SHALL be returned

---

### Requirement: InvoiceController Test Suite

The system SHALL have integration tests for `InvoiceController`.

#### Scenario: GET /api/invoices/{id} - success

- GIVEN valid invoice ID and authenticated account owner
- WHEN GET request is made
- THEN 200 OK SHALL be returned with invoice details

#### Scenario: GET /api/invoices - by account

- GIVEN valid account ID and pagination params
- WHEN GET request is made
- THEN 200 OK SHALL be returned with paginated invoices

#### Scenario: GET /api/invoices/{id} - not found

- GIVEN invalid invoice ID
- WHEN GET request is made
- THEN 404 Not Found SHALL be returned

#### Scenario: POST /api/invoices/send/{id} - success

- GIVEN valid invoice ID and authenticated owner
- WHEN POST request is made
- THEN 200 OK SHALL be returned
- AND invoice status SHALL be SENT

---

### Requirement: PetController Test Suite

The system SHALL have integration tests for `PetController` following existing service test patterns.

#### Scenario: POST /api/pets - create pet success

- GIVEN valid pet data and authenticated CLIENT
- WHEN POST request is made
- THEN 201 Created SHALL be returned

#### Scenario: GET /api/pets/{id} - success

- GIVEN valid pet ID and authenticated owner
- WHEN GET request is made
- THEN 200 OK SHALL be returned with pet details

#### Scenario: PUT /api/pets/{id} - update success

- GIVEN valid pet ID, update data, and authenticated owner
- WHEN PUT request is made
- THEN 200 OK SHALL be returned with updated pet

#### Scenario: DELETE /api/pets/{id} - success

- GIVEN valid pet ID and authenticated owner
- WHEN DELETE request is made
- THEN 204 No Content SHALL be returned

---

### Requirement: DiscountCouponController Test Suite

The system SHALL have integration tests for `DiscountCouponController`.

#### Scenario: POST /api/coupons - create success (ADMIN)

- GIVEN valid coupon data and authenticated ADMIN
- WHEN POST request is made
- THEN 201 Created SHALL be returned

#### Scenario: GET /api/coupons - list all (ADMIN)

- GIVEN authenticated ADMIN
- WHEN GET request is made
- THEN 200 OK SHALL be returned with all coupons

#### Scenario: GET /api/coupons/{code} - find by code

- GIVEN valid coupon code
- WHEN GET request is made
- THEN 200 OK SHALL be returned with coupon details

#### Scenario: DELETE /api/coupons/{id} - delete (ADMIN)

- GIVEN valid coupon ID and authenticated ADMIN
- WHEN DELETE request is made
- THEN 204 No Content SHALL be returned

---

### Requirement: AppliedCouponController Test Suite

The system SHALL have integration tests for `AppliedCouponController`.

#### Scenario: POST /api/applied-coupons - apply coupon

- GIVEN valid booking ID, account ID, and coupon code
- WHEN POST request is made
- THEN 201 Created SHALL be returned with applied coupon

#### Scenario: POST /api/applied-coupons - invalid coupon

- GIVEN invalid or expired coupon code
- WHEN POST request is made
- THEN 400 Bad Request SHALL be returned

#### Scenario: GET /api/applied-coupons/booking/{id}

- GIVEN valid booking ID
- WHEN GET request is made
- THEN 200 OK SHALL be returned with applied coupons

---

### Requirement: AccountController Test Suite

The system SHALL have integration tests for `AccountController`.

#### Scenario: POST /api/accounts - create success

- GIVEN valid account data and authenticated user
- WHEN POST request is made
- THEN 201 Created SHALL be returned

#### Scenario: GET /api/accounts/{id} - owner access

- GIVEN valid account ID and authenticated owner
- WHEN GET request is made
- THEN 200 OK SHALL be returned

#### Scenario: GET /api/accounts/{id} - non-owner access

- GIVEN valid account ID and authenticated non-owner
- WHEN GET request is made
- THEN 403 Forbidden SHALL be returned

#### Scenario: GET /api/accounts - ADMIN list all

- GIVEN authenticated ADMIN
- WHEN GET request is made
- THEN 200 OK SHALL be returned with all accounts

---

### Requirement: ServiceOfferingController Test Suite

The system SHALL have integration tests for `ServiceOfferingController`.

#### Scenario: POST /api/services - create success (SITTER)

- GIVEN valid service data and authenticated SITTER
- WHEN POST request is made
- THEN 201 Created SHALL be returned

#### Scenario: GET /api/services - list all active

- GIVEN no authentication required
- WHEN GET request is made
- THEN 200 OK SHALL be returned with all active services

#### Scenario: GET /api/services/user/{id} - user's services

- GIVEN valid user ID
- WHEN GET request is made
- THEN 200 OK SHALL be returned with user's services

#### Scenario: PUT /api/services/{id} - update success

- GIVEN valid service ID, update data, and authenticated owner
- WHEN PUT request is made
- THEN 200 OK SHALL be returned

#### Scenario: DELETE /api/services/{id} - soft delete

- GIVEN valid service ID and authenticated owner
- WHEN DELETE request is made
- THEN 200 OK SHALL be returned
- AND service active status SHALL be false

---

### Requirement: PlatformFeeController Test Suite

The system SHALL have integration tests for `PlatformFeeController`.

#### Scenario: POST /api/platform-fees - create fee (ADMIN)

- GIVEN valid fee request and authenticated ADMIN
- WHEN POST request is made
- THEN 201 Created SHALL be returned

#### Scenario: GET /api/platform-fees/booking/{id} - get fee for booking

- GIVEN valid booking ID
- WHEN GET request is made
- THEN 200 OK SHALL be returned with fee details

---

## Section 3: Test Configuration Requirements

### Requirement: Test Fixtures

The system SHALL provide reusable test fixtures for consistent test data.

#### Fixture: TestUserHelper

- SHALL create users with various roles (CLIENT, SITTER, ADMIN)
- SHALL create users with different states (active, inactive, verified, unverified)
- SHALL return fully persisted User entities

#### Fixture: TestBookingHelper

- SHALL create bookings with various statuses
- SHALL create bookings with associated pets, clients, and sitters
- SHALL handle booking date generation

#### Fixture: TestInvoiceHelper

- SHALL create invoices with line items
- SHALL create invoices with various statuses
- SHALL handle invoice total calculations

#### Fixture: TestCouponHelper

- SHALL create valid discount coupons
- SHALL create expired coupons
- SHALL create coupons at usage limits

### Requirement: TestSecurityConfig Structure

The system SHALL provide proper test security configuration.

#### Configuration: MockAuthenticationHelper

- SHALL set up security context with various roles
- SHALL create JWT tokens for testing
- SHALL handle authentication for different user types

#### Configuration: TestDatabaseConfig

- SHALL use H2 in-memory database
- SHALL clean database between tests
- SHALL support transaction rollback

---

## Section 4: Acceptance Criteria

### BookingServiceImplement Acceptance Criteria

| AC | Criterion | Test Method |
|----|-----------|-------------|
| AC1 | createBooking with valid input returns BookingDetailResponse | `createBooking_WithValidData_ShouldReturnBookingDetailResponse` |
| AC2 | createBooking with inactive service throws ServiceOfferingInactiveException | `createBooking_WithInactiveService_ShouldThrowException` |
| AC3 | getBookingById with valid ID returns booking details | `getBookingById_WhenExists_ShouldReturnDetails` |
| AC4 | getBookingById with invalid ID throws IllegalArgumentException | `getBookingById_WhenNotExists_ShouldThrowException` |
| AC5 | updateBookingStatus with valid transition updates status | `updateBookingStatus_WithValidTransition_ShouldUpdate` |
| AC6 | updateBookingStatus with invalid transition throws IllegalStateException | `updateBookingStatus_WithInvalidTransition_ShouldThrow` |
| AC7 | deleteBooking with pending status deletes successfully | `deleteBooking_WhenPending_ShouldDelete` |
| AC8 | deleteBooking with completed status throws IllegalStateException | `deleteBooking_WhenCompleted_ShouldThrow` |
| AC9 | Repository interactions are verified via Mockito | All methods verify repository calls |

### InvoiceServiceImplement Acceptance Criteria

| AC | Criterion | Test Method |
|----|-----------|-------------|
| AC1 | generateInvoiceForBooking creates invoice with calculated totals | `generateInvoiceForBooking_WithValidBooking_ShouldCreateInvoice` |
| AC2 | generateInvoiceForBooking with invalid booking throws exception | `generateInvoiceForBooking_WithInvalidBooking_ShouldThrow` |
| AC3 | getInvoiceById returns invoice details | `getInvoiceById_WhenExists_ShouldReturnDetails` |
| AC4 | getInvoicesByAccountId returns paginated results | `getInvoicesByAccountId_ShouldReturnPaginatedResults` |
| AC5 | cancelInvoice with valid reason updates status | `cancelInvoice_WithValidReason_ShouldUpdate` |
| AC6 | cancelInvoice without reason throws IllegalArgumentException | `cancelInvoice_WithoutReason_ShouldThrow` |

### DiscountCouponServiceImplement Acceptance Criteria

| AC | Criterion | Test Method |
|----|-----------|-------------|
| AC1 | saveDiscountCoupon persists coupon | `saveDiscountCoupon_ShouldPersistAndReturn` |
| AC2 | getDiscountCouponById with valid ID returns coupon | `getDiscountCouponById_WhenExists_ShouldReturn` |
| AC3 | getDiscountCouponByCode returns coupon for valid code | `getDiscountCouponByCode_WithValidCode_ShouldReturn` |
| AC4 | getDiscountCouponByCode returns empty for invalid code | `getDiscountCouponByCode_WithInvalidCode_ShouldReturnEmpty` |
| AC5 | deleteDiscountCoupon calls repository delete | `deleteDiscountCoupon_ShouldCallRepository` |

### AppliedCouponServiceImplement Acceptance Criteria

| AC | Criterion | Test Method |
|----|-----------|-------------|
| AC1 | applyCoupon with valid coupon creates AppliedCoupon | `applyCoupon_WithValidCoupon_ShouldCreateAppliedCoupon` |
| AC2 | applyCoupon with expired coupon throws exception | `applyCoupon_WithExpiredCoupon_ShouldThrow` |
| AC3 | applyCoupon with exceeded limit throws exception | `applyCoupon_WithExceededLimit_ShouldThrow` |
| AC4 | validateCoupon returns true for valid coupon | `validateCoupon_WithValidCoupon_ShouldReturnTrue` |
| AC5 | validateCoupon returns false for invalid coupon | `validateCoupon_WithInvalidCoupon_ShouldReturnFalse` |
| AC6 | getCouponsByAccount returns coupons for account | `getCouponsByAccount_ShouldReturnCoupons` |

### AccountServiceImplement Acceptance Criteria

| AC | Criterion | Test Method |
|----|-----------|-------------|
| AC1 | createAccount with valid data returns AccountResponse | `createAccount_WithValidData_ShouldReturnResponse` |
| AC2 | getAccountById with valid ID returns account | `getAccountById_WhenExists_ShouldReturn` |
| AC3 | getAccountById with invalid ID throws exception | `getAccountById_WhenNotExists_ShouldThrow` |
| AC4 | getAllAccounts returns all accounts for ADMIN | `getAllAccounts_AsAdmin_ShouldReturnAll` |

### ServiceOfferingServiceImplement Acceptance Criteria

| AC | Criterion | Test Method |
|----|-----------|-------------|
| AC1 | createServiceOffering creates and returns DTO | `createServiceOffering_ShouldCreateAndReturnDTO` |
| AC2 | getAllServices returns all active services | `getAllServices_ShouldReturnActiveOnly` |
| AC3 | getServiceById with valid ID returns DTO | `getServiceById_WhenExists_ShouldReturn` |
| AC4 | getServiceById with invalid ID throws exception | `getServiceById_WhenNotExists_ShouldThrow` |
| AC5 | updateServiceOffering updates and returns DTO | `updateServiceOffering_ShouldUpdateAndReturn` |
| AC6 | deleteServiceOffering performs soft delete | `deleteServiceOffering_ShouldSoftDelete` |

### Controller Test Acceptance Criteria (All Controllers)

| AC | Criterion | Expected HTTP Status |
|----|-----------|---------------------|
| AC1 | Endpoint with valid request returns 200/201 | 200 OK or 201 Created |
| AC2 | Endpoint with invalid request returns 400 | 400 Bad Request |
| AC3 | Endpoint with non-existent resource returns 404 | 404 Not Found |
| AC4 | Endpoint without proper auth returns 401 | 401 Unauthorized |
| AC5 | Endpoint without proper role returns 403 | 403 Forbidden |

---

## Coverage Summary

### Service Tests (10 services requiring coverage)

| Service | Existing Tests | Tests Needed | Priority |
|---------|---------------|--------------|----------|
| BookingServiceImplement | 0 | 10 | HIGH |
| InvoiceServiceImplement | 0 | 8 | HIGH |
| DiscountCouponServiceImplement | 0 | 6 | MEDIUM |
| AppliedCouponServiceImplement | 0 | 7 | HIGH |
| AccountServiceImplement | 0 | 4 | MEDIUM |
| ServiceOfferingServiceImplement | 0 | 6 | HIGH |
| PlatformFeeServiceImplement | 0 | 4 | MEDIUM |
| NotificationServiceImplement | 0 | 4 | LOW |
| EmailServiceImplement | 0 | 4 | LOW |
| PdfGenerationServiceImplement | 0 | 3 | LOW |

### Controller Tests (8 controllers requiring coverage)

| Controller | Existing Tests | Tests Needed | Priority |
|------------|---------------|--------------|----------|
| BookingController | 0 | 6 | HIGH |
| InvoiceController | 0 | 4 | HIGH |
| PetController | 0 | 4 | MEDIUM |
| DiscountCouponController | 0 | 4 | MEDIUM |
| AppliedCouponController | 0 | 3 | MEDIUM |
| AccountController | 0 | 4 | MEDIUM |
| ServiceOfferingController | 0 | 5 | HIGH |
| PlatformFeeController | 0 | 2 | LOW |

### Total Coverage Goals

- **Service Tests**: ~56 tests across 10 services
- **Controller Tests**: ~32 tests across 8 controllers
- **Total New Tests**: ~88 tests
- **Existing Tests**: ~7 test files (already comprehensive)
