# Tasks: Fix Issues and Errors

## Phase 1: Security Context Cleanup

- [x] 1.1 Add `@AfterEach` tearDown() with `SecurityContextHolder.clearContext()` to UserControllerTest
- [x] 1.2 Add `@AfterEach` tearDown() with `SecurityContextHolder.clearContext()` to SitterProfileControllerTest
- [x] 1.3 Add `@AfterEach` tearDown() with `SecurityContextHolder.clearContext()` to SitterWorkExperienceControllerTest
- [x] 1.4 Verify tests work in isolation

## Phase 2: Test Pollution Investigation (FINDINGS)

**Key Finding**: Tests work correctly in ISOLATION but fail when executed as a suite. This is a pre-existing "test pollution" issue NOT related to TestSecurityConfig.

**Analysis**:
- TestSecurityConfig is NOT used by any controller tests - they use production WebAuthorization directly
- Individual tests pass (security works correctly)
- Tests pass in pairs but fail when run as full suite
- Root cause: Shared state between tests that @Transactional doesn't fully clean

**Status**: Phase 1 tasks updated to reflect actual fix applied (SecurityContextHolder cleanup).

## Phase 2: Fix SitterProfileControllerTest

## Phase 2: Fix SitterProfileControllerTest

- [ ] 2.1 Run all SitterProfileControllerTest tests: `./gradlew test --tests "com.Petcare.Petcare.Controllers.SitterProfileControllerTest"`
- [ ] 2.2 Analyze failures - identify if authorization (403), validation (400), or not found (404) issues
- [ ] 2.3 Fix authorization tests - ensure tests use correct roles and tokens via `getAuthTokenForRole(Role role)`
- [ ] 2.4 Fix validation tests - ensure MockMvc has proper validation configuration
- [ ] 2.5 Fix 404 tests - correct test data setup with proper IDs
- [ ] 2.6 Verify all 24 SitterProfileControllerTest tests pass

## Phase 3: Fix SitterWorkExperienceControllerTest

- [ ] 3.1 Run all SitterWorkExperienceControllerTest tests: `./gradlew test --tests "com.Petcare.Petcare.Controllers.SitterWorkExperienceControllerTest"`
- [ ] 3.2 Analyze failures - identify authorization vs validation vs not found issues
- [ ] 3.3 Fix authorization tests - verify role-based access returns correct HTTP status
- [ ] 3.4 Fix 404 tests - ensure SitterProfile exists before posting work experience
- [ ] 3.5 Fix validation tests - verify DTO validation is properly triggered
- [ ] 3.6 Verify all 10 SitterWorkExperienceControllerTest tests pass

## Phase 4: Fix UserControllerTest

- [ ] 4.1 Run all UserControllerTest tests: `./gradlew test --tests "com.Petcare.Petcare.Controllers.UserControllerTest"`
- [ ] 4.2 Analyze failures - identify which authorization tests fail
- [ ] 4.3 Fix GET /api/users authorization - CLIENT should get 403, ADMIN should get 200
- [ ] 4.4 Fix admin endpoints authorization - verify 403 for CLIENT, 201/200 for ADMIN
- [ ] 4.5 Fix PATCH endpoints - verify-email and toggle-active require ADMIN
- [ ] 4.6 Fix GET /summary, /unverified, /role/{role}, /active, /stats endpoints
- [ ] 4.7 Verify all 37 UserControllerTest tests pass

## Phase 5: Verify Full Test Suite

- [ ] 5.1 Run full test suite: `./gradlew test`
- [ ] 5.2 Verify no regression - all previously passing tests still pass
- [ ] 5.3 Confirm 100% pass rate (71/71 tests) for affected classes

## Phase 6: Investigate LSP Errors (if time permits)

- [ ] 6.1 Check InvoiceController for compilation errors - verify if real or IDE false positive
- [ ] 6.2 Check DashboardController for compilation errors
- [ ] 6.3 Check PetController for compilation errors
- [ ] 6.4 Check EmailController for compilation errors
- [ ] 6.5 Check CouponExpiredException for compilation errors
- [ ] 6.6 Fix any real compilation errors found

---

## Implementation Order

1. First fix TestSecurityConfig - this is the root cause for ~35 failing tests
2. Run each test class individually to see which specific tests still fail
3. Fix test data setup and authorization expectations per test class
4. Finally verify full suite passes

## Risks

- JwtAuthenticationFilter may need test configuration to validate tokens
- Some tests may need test data setup changes beyond just the security config
- LSP errors may be false positives that don't affect test execution
