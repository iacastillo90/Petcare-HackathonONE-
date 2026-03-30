# AGENTS.md - Petcare Hackathon Project

This file provides guidance for AI agents operating in this repository.

## Project Overview

- **Framework**: Spring Boot 3.5.4
- **Language**: Java 21
- **Build Tool**: Gradle
- **Database**: MySQL 9.2.0 (production), H2 (testing)
- **Architecture**: Layered (Controller → Service → Repository → Model)

## Build & Test Commands

### Development Server
```bash
./gradlew bootRun
```

### Build Application
```bash
./gradlew build
```

### Clean Build
```bash
./gradlew clean build
```

### Run Tests

**Run all tests:**
```bash
./gradlew test
```

**Run single test class:**
```bash
./gradlew test --tests "com.Petcare.Petcare.Service.UserServiceImplementTest"
```

**Run single test method:**
```bash
./gradlew test --tests "com.Petcare.Petcare.Service.UserServiceImplementTest.login_WithValidAndVerifiedUser_ShouldReturnAuthResponse"
```

**Run tests with verbose output:**
```bash
./gradlew test --info
```

**Run tests excluding integration tests:**
```bash
./gradlew test -x integrationTest
```

### Linting
```bash
./gradlew check
./gradlew spotlessApply  # Auto-fix formatting issues
```

---

## Code Style Guidelines

### 1. Package Structure
```
com.Petcare.Petcare/
├── Configurations/     # Spring config, security, JWT
├── Controllers/        # REST endpoints
├── DTOs/               # Data Transfer Objects (grouped by entity)
├── Exception/         # Global handler + Business exceptions
├── Models/             # JPA entities (grouped by domain)
├── Repositories/      # Spring Data JPA interfaces
└── Services/          # Business logic + Implement suffix
```

### 2. Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `UserServiceImplement` |
| Methods | camelCase | `findByEmail()` |
| Variables | camelCase | `userRepository` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Packages | lowercase | `com.petcare.petcare` |
| Interfaces | PascalCase with suffix | `UserService` |
| Implementations | PascalCase with "Implement" | `UserServiceImplement` |
| DTOs | PascalCase with suffix | `UserResponse`, `CreateUserRequest` |
| Tests | PascalCase + "Test" | `UserServiceImplementTest` |
| Test methods | snake_case with underscores | `login_WithValidUser_ShouldReturnAuthResponse` |

### 3. Import Organization

Order imports alphabetically within groups:
1. `java.*` and `javax.*`
2. `org.springframework.*`
3. `com.Petcare.Petcare.*` (internal)
4. Other third-party libraries

### 4. Class Structure

```java
package com.Petcare.Petcare.Services.Implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Javadoc for the class - describes purpose, patterns, and author.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImplement implements UserService {
    
    // Constants
    
    // Dependencies (final fields)
    
    // Public methods first, then private
    
    // Nested classes (if needed)
}
```

### 5. DTO Pattern

#### When to Use Records vs POJOs

| DTO Type | Implementation | Why |
|----------|----------------|-----|
| Response DTOs | **Java records** | Immutable, concise, no mutable state needed |
| Request DTOs | **POJOs** | Need mutable fields for deserialization |
| Complex DTOs with business logic | **POJOs** | May need validation, transformation, or state |

#### File Organization
- DTOs go in `src/main/java/.../DTOs/{Entity}/`
- Request DTOs: `*Request.java`, `Create*Request.java`
- Response DTOs: `*Response.java`, `*Summary.java`

#### Record Pattern (Response DTOs)

```java
// Good record DTO example
public record UserResponse(
    Long id,
    String email,
    String fullName,
    Role role
) {
    /**
     * Factory method to create UserResponse from User entity.
     * Use this instead of constructors for better readability.
     */
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName() + " " + user.getLastName(),
            user.getRole()
        );
    }
}
```

#### POJO Pattern (Request DTOs)

```java
// Good POJO DTO example
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    private String firstName;
    private String lastName;
}
```

#### Factory Methods

**MANDATORY for all DTOs:**
- Use `fromEntity(Entity)` for conversion from JPA entities
- Use `of()` for simple construction when no entity conversion needed
- These methods should be `public static` inside the DTO class

#### Accessor Methods

| Type | Accessor Syntax | Example |
|------|-----------------|---------|
| Record | `record.field()` | `userResponse.id()` |
| POJO | `pojo.getField()` | `request.getEmail()` |

#### @Builder Compatibility

| Type | @Builder Support | Notes |
|------|------------------|-------|
| Records | **Partial** | Use canonical constructor, not @Builder directly |
| POJOs | **Full** | @Builder works natively with Lombok @Data |

For records with @Builder, use a companion approach:
```java
public record UserResponse(
    Long id,
    String email,
    String fullName
) {
    // @Builder-compatible factory method
    public static UserResponseBuilder builder() {
        return new UserResponseBuilder();
    }
    
    // Keep the compact canonical constructor for fromEntity()
    private UserResponse(Long id, String email, String fullName) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
    }
}
```

**Recommendation**: Prefer explicit factory methods (`fromEntity`, `of`) over @Builder for records. Simpler, clearer, and avoids Lombok complexity.

### 6. Service Layer Guidelines

- Use `@RequiredArgsConstructor` for dependency injection
- Always use `@Transactional` for database operations
- Log important operations with `@Slf4j`
- Throw specific exceptions from `Exception.Business` package

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImplement implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());
        // implementation
    }
}
```

### 7. Controller Layer Guidelines

- Use `@RestController` and `@RequestMapping`
- Validate inputs with `@Valid` and Jakarta validation
- Use `@PreAuthorize` for role-based access
- Return `ResponseEntity<?>` for consistent responses
- Document with OpenAPI annotations (`@Operation`, `@ApiResponses`)

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {
    
    @PostMapping
    @Operation(summary = "Register new user")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        // implementation
    }
}
```

### 8. Exception Handling

- Use `GlobalExceptionHandler` with `@RestControllerAdvice`
- Create custom exceptions in `Exception/Business/` package
- Return standardized `ErrorResponseDTO`

```java
// Custom exception
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("Usuario no encontrado con el ID " + id);
    }
}
```

### 9. Entity/Model Guidelines

- Use JPA annotations for mapping
- Use Lombok to reduce boilerplate (`@Entity`, `@Data`, etc.)
- Follow naming conventions in `Models/` subdirectories

```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
}
```

### 10. Testing Guidelines

**Test Structure:**
- Use JUnit 5 (`@Test`, `@BeforeEach`)
- Use AssertJ for assertions
- Use Mockito for mocking
- Follow naming: `{methodName}_{scenario}_{expectedResult}`

```java
@ExtendWith(MockitoExtension.class)
class UserServiceImplementTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserServiceImplement userService;
    
    @Test
    @DisplayName("login | Éxito | Should return AuthResponse for valid user")
    void login_WithValidUser_ShouldReturnAuthResponse() {
        // Given
        LoginRequest request = new LoginRequest("test@example.com", "password");
        
        // When
        AuthResponse response = userService.login(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("expected-token");
    }
}
```

### Testing with Spring Security Context

Some services require Spring Security context (e.g., `PetService`). Use this pattern:

```java
@ExtendWith(MockitoExtension.class)
class PetServiceImplementTest {

    @Mock private PetRepository petRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    
    @InjectMocks
    private PetServiceImplement petService;

    private static final String TEST_USER_EMAIL = "admin@test.com";

    @BeforeEach
    void setUp() {
        // Setup test data...
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setupAdminSecurityContext() {
        List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = 
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"));
        
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                TEST_USER_EMAIL, null, authorities
        );
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getAllPets_WhenAdmin_ShouldReturnAllPets() {
        // Given
        setupAdminSecurityContext();
        when(petRepository.findAll()).thenReturn(List.of(pet));

        // When
        List<PetResponse> result = petService.getAllPets();

        // Then
        assertThat(result).hasSize(1);
    }
}
```

### 11. Database Configuration

- Production: MySQL (configured in `application-prod.properties`)
- Testing: H2 in-memory (configured in `application-test.properties`)
- Use Spring Data JPA repositories with custom query methods

### 12. Security

- JWT-based authentication using `io.jsonwebtoken`
- Passwords encrypted with `PasswordEncoder`
- Role-based access with `@PreAuthorize`

---

## Key Files & Directories

| Path | Purpose |
|------|---------|
| `build.gradle` | Build configuration |
| `src/main/java/.../PetcareApplication.java` | Main entry point |
| `src/main/resources/application.properties` | App configuration |
| `src/main/java/.../Configurations/SecurityConfig.java` | Security setup |
| `src/main/java/.../Exception/GlobalExceptionHandler.java` | Error handling |

---

## Notes for Agents

1. **Do NOT modify `build.gradle` version without approval** - this is a locked hackathon project
2. **Use proper transaction boundaries** - never leave `@Transactional` off service methods
3. **Write tests for new features** - follow existing test patterns in the codebase
4. **Use English for code, Spanish for comments** - maintain consistency with existing code
5. **When in doubt, check the existing implementation** - don't reinvent patterns already in use

---

## API Documentation

**Swagger UI**: `http://localhost:8088/swagger-ui/index.html`
**OpenAPI JSON**: `http://localhost:8088/v3/api-docs`

All controllers should use OpenAPI annotations:
- `@Tag` for controller grouping
- `@Operation` for endpoint descriptions
- `@ApiResponses` for response documentation

---

## Entity Relationships & Patterns

### Common Entity Structure
```
Models/
├── User/           # User + Role enum + PermissionLevel enum
├── Booking/        # Booking + BookingStatus enum
├── Payment/        # Payment + PaymentStatus enum + PaymentMethod
├── Invoice/        # Invoice + InvoiceStatus enum + InvoiceItem
├── Account/        # Account + AccountUser (join table)
└── Pet.java, Review.java, SitterProfile.java, etc.
```

### Relationship Examples

```java
// One-to-Many (Bidirectional)
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
private List<Pet> pets;

// Many-to-One
@ManyToOne
@JoinColumn(name = "booking_id")
private Booking booking;

// Enum storage
@Enumerated(EnumType.STRING)
private Role role;
```

---

## Email Templates

Templates are located in: `src/main/resources/templates/`

Existing templates:
- `email-verified.html`
- `booking-status-confirmed.html`
- `booking-status-completed.html`
- `invoice-notification.html`
- `new-booking-client.html`
- `new-booking-sitter.html`

Use Thymeleaf for rendering with `emailService.sendEmail()`.

---

## Configuration Properties

### application.properties
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/petcare
spring.jpa.hibernate.ddl-auto=update
spring.mail.username=${MAIL_USERNAME}
petcare.api.base-url=http://localhost:8088
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION}
```

### Profile Activation
```bash
./gradlew bootRun --spring.profiles.active=prod
```

---

## Role-Based Access Control

| Role | Description | Access Level |
|------|-------------|--------------|
| `CLIENT` | Regular users | Own resources only |
| `SITTER` | Pet sitters | Own resources + limited admin |
| `ADMIN` | Administrators | Full system access |

Use `@PreAuthorize("hasRole('ADMIN')")` or `@PreAuthorize("hasAnyRole('ADMIN', 'SITTER')")`.

---

## Error Response Format

All errors return `ErrorResponseDTO`:

```json
{
  "status": 404,
  "message": "Usuario no encontrado con el ID 1",
  "timestamp": "2025-01-15T10:30:00",
  "errors": null
}
```

For validation errors:
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2025-01-15T10:30:00",
  "errors": {
    "email": "must be a valid email",
    "password": "must not be blank"
  }
}
```

---

## Controller Test Patterns

```java
@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void getUser_WithValidId_ShouldReturn200() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/users/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}
```

---

## Git Conventions

### Commit Message Format
```
feat: add user registration endpoint
fix: resolve NPE in UserService login
docs: update API documentation
test: add unit tests for BookingService
refactor: extract PaymentService interface
```

### Branch Naming
```
feature/user-registration
fix/email-verification-bug
hotfix/security-patch
```

---

## Environment Setup Checklist

1. **Database**: MySQL 9.2.0 running on port 3306
2. **Mail**: Configure SMTP settings in environment variables
3. **Environment Variables**:
   ```bash
   export JWT_SECRET="your-secret-key"
   export JWT_EXPIRATION="86400000"
   export MAIL_USERNAME="noreply@petcare.com"
   export MAIL_PASSWORD="mail-password"
   export PETCARE_API_BASE_URL="http://localhost:8088"
   ```
4. **IDE**: Use Lombok annotation processor support

---

## Important Notes

- **Email templates**: Use lowercase names (e.g., `email-verified`, not `Email-verified`)
- **Test profile**: Redis is disabled in test profile (`@Profile("!test")` in RedisConfig)
- **Production**: Use HTTP (not HTTPS) for `petcare.api.base-url` unless Traefik handles SSL

---

## Known Limitations

- `ddl-auto=update` - Use Flyway/Liquibase for production migrations
- Test coverage gaps in controller and integration tests
- JWT implementation needs security audit before production

---

## Docker Commands

```bash
# Build and start all services
docker compose up -d

# View logs
docker compose logs -f app

# Rebuild application
docker compose build --no-cache app
docker compose up -d

# Stop all services
docker compose down
```

---

## Async Implementation

This project uses `@Async` for concurrent operations:

- **AsyncConfig**: Configured with `DelegatingSecurityContextAsyncTaskExecutor` for proper Spring Security context propagation
- **AsyncService wrappers**: Located in `Services/` folder (e.g., `UserAsyncService`)
- **@Async methods in Implement classes**: Most service implementations have async variants (e.g., `getAllUsersAsync()`)

Key notes:
- Use `@Async("taskExecutor")` for async methods
- Security context is automatically propagated to async threads
- Configure thread pool in `AsyncConfig.java`
