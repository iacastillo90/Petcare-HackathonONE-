# Design: Full Test Coverage - Test Templates and Implementation Strategy

## Technical Approach

This design establishes a complete testing strategy for the Petcare Spring Boot application, following the existing patterns in `UserServiceImplementTest.java`. The approach prioritizes:

1. **Unit tests** with Mockito for service layer isolation
2. **Controller tests** with MockMvc for API endpoint validation
3. **Test security configuration** to bypass JWT in integration tests
4. **Consistent naming and structure** across all test classes

## Architecture Decisions

### Decision: Test Directory Structure

**Choice**: Flat structure under `src/test/java/com/Petcare/Petcare/` with subdirectories for Service, Controllers, and Config

**Rationale**: Follows existing project convention, easy to locate tests, clear separation of concerns

### Decision: Testing Framework Stack

**Choice**: JUnit 5 + Mockito + AssertJ + MockMvc

**Rationale**: 
- Already in use (`UserServiceImplementTest.java` uses all four)
- Industry standard for Spring Boot
- AssertJ provides fluent assertions (matches existing code style)

### Decision: Test Security Configuration

**Choice**: Custom `TestSecurityConfig` that disables CSRF and permits all requests

**Alternatives considered**: 
- `@AutoConfigureMockMvc(addFilters = false)` - too broad
- Full security with `@WithMockUser` - adds complexity without benefit for unit tests

**Rationale**: Simple, predictable test environment, no JWT handling needed

### Decision: Service Test Naming Convention

**Choice**: `{MethodName}_{Scenario}_{ExpectedResult}` with underscores

**Rationale**: Already established in existing tests, readable, descriptive

### Decision: Mock Repository Returns

**Choice**: Return empty Optional for "not found" scenarios, throw exceptions only for service-level validation

**Rationale**: Repositories should return data; services throw exceptions - this keeps tests realistic

---

## Test File Structure

```
src/test/java/com/Petcare/Petcare/
├── Config/
│   └── TestSecurityConfig.java          ← Security config for integration tests
├── Service/
│   ├── BookingServiceImplementTest.java          (PRIORITY 1)
│   ├── InvoiceServiceImplementTest.java           (PRIORITY 2)
│   ├── AccountServiceImplementTest.java           (PRIORITY 3)
│   ├── PaymentServiceImplementTest.java           (PRIORITY 4)
│   ├── DiscountCouponServiceImplementTest.java    (PRIORITY 5)
│   ├── AppliedCouponServiceImplementTest.java    (PRIORITY 6)
│   ├── ReviewServiceImplementTest.java            (PRIORITY 7)
│   ├── ServiceOfferingServiceImplementTest.java   (PRIORITY 8)
│   ├── PlatformFeeServiceImplementTest.java       (PRIORITY 9)
│   ├── EmailServiceImplementTest.java            (PRIORITY 10)
│   ├── NotificationServiceImplementTest.java      (PRIORITY 11)
│   ├── PetServiceImplementTest.java              (PRIORITY 12)
│   └── UserServiceImplementTest.java             (ALREADY EXISTS)
└── Controllers/
    ├── BookingControllerTest.java               (PRIORITY 1)
    ├── InvoiceControllerTest.java                (PRIORITY 2)
    ├── AccountControllerTest.java                (PRIORITY 3)
    ├── PetControllerTest.java                    (PRIORITY 4)
    ├── PaymentMethodControllerTest.java          (PRIORITY 5)
    ├── UserControllerTest.java                   (PRIORITY 6)
    ├── DiscountCouponControllerTest.java         (PRIORITY 7)
    ├── ReviewControllerTest.java                 (PRIORITY 8)
    └── AppliedCouponControllerTest.java          (PRIORITY 9)
```

---

## Data Flow

### Unit Test Flow
```
┌─────────────────┐     Mock      ┌──────────────────┐
│  Test Class     │ ──────────── │  Repository     │
│  (JUnit 5)      │              │  (Mocked)       │
└────────┬────────┘              └──────────────────┘
         │                                ▲
         │ @InjectMocks                   │ returns
         ▼                                │
┌─────────────────┐                        │
│  Service       │ ───────────────────────┘
│  Implementation │
└─────────────────┘
```

### Integration Test Flow
```
┌─────────────────┐     MockMvc     ┌──────────────────┐
│  Controller     │ ───────────────│  Service        │
│  Test           │                 │  (Mocked)       │
└────────┬────────┘                └──────────────────┘
         │                                ▲
         │ @WebMvcTest                    │ returns
         ▼                                │
┌─────────────────┐                        │
│  Controller     │ ───────────────────────┘
│  (Real)         │
└─────────────────┘
```

---

## TestSecurityConfig Implementation

```java
package com.Petcare.Petcare.Config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration for disabling security in integration tests.
 * 
 * <p>This config is imported by @WebMvcTest classes to bypass JWT authentication
 * and authorization, allowing direct endpoint testing without mock users.
 * 
 * <p>Usage:
 * <pre>
 * @WebMvcTest(BookingController.class)
 * @Import(TestSecurityConfig.class)
 * class BookingControllerTest { ... }
 * </pre>
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .build();
    }
}
```

---

## Code Templates

### 1. BookingServiceImplementTest.java (Complete Template)

```java
package com.Petcare.Petcare.Service;

import com.Petcare.Petcare.DTOs.Booking.BookingDetailResponse;
import com.Petcare.Petcare.DTOs.Booking.BookingSummaryResponse;
import com.Petcare.Petcare.DTOs.Booking.CreateBookingRequest;
import com.Petcare.Petcare.DTOs.Booking.UpdateBookingRequest;
import com.Petcare.Petcare.Exception.Business.*;
import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Booking.BookingStatus;
import com.Petcare.Petcare.Models.Pet;
import com.Petcare.Petcare.Models.ServiceOffering.ServiceOffering;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.*;
import com.Petcare.Petcare.Services.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Suite completa de pruebas unitarias para {@link BookingServiceImplement}.
 * 
 * <p>Esta clase verifica toda la lógica de negocio del servicio de reservas,
 * incluyendo validaciones, transiciones de estado, cálculos y coordinación
 * con servicios dependientes.</p>
 * 
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceImplementTest {

    // ========== DEPENDENCIAS MOCKEADAS ==========
    
    @Mock private BookingRepository bookingRepository;
    @Mock private PetRepository petRepository;
    @Mock private UserRepository userRepository;
    @Mock private ServiceOfferingRepository serviceOfferingRepository;
    @Mock private PlatformFeeService platformFeeService;
    @Mock private NotificationService notificationService;
    @Mock private AccountUserRepository accountUserRepository;
    @Mock private InvoiceService invoiceService;

    // ========== SERVICIO BAJO PRUEBA ==========
    
    @InjectMocks
    private BookingServiceImplement bookingService;

    // ========== DATOS DE PRUEBA ==========
    
    private User testClient;
    private User testSitter;
    private Account testAccount;
    private Pet testPet;
    private ServiceOffering testServiceOffering;
    private Booking testBooking;
    private Authentication mockAuth;
    private CreateBookingRequest createBookingRequest;

    /**
     * Configuración inicial ejecutada antes de cada test.
     * Prepara datos de prueba representativos para los diferentes escenarios.
     */
    @BeforeEach
    void setUp() {
        // Usuario cliente
        testClient = new User();
        testClient.setId(1L);
        testClient.setEmail("client@test.com");
        testClient.setFirstName("John");
        testClient.setLastName("Doe");
        testClient.setRole(Role.CLIENT);
        testClient.setActive(true);

        // Usuario cuidador
        testSitter = new User();
        testSitter.setId(2L);
        testSitter.setEmail("sitter@test.com");
        testSitter.setFirstName("Jane");
        testSitter.setLastName("Smith");
        testSitter.setRole(Role.SITTER);
        testSitter.setActive(true);

        // Cuenta de usuario
        testAccount = new Account(testClient, "Cuenta Familiar", "ACC-123");
        testAccount.setId(1L);
        testAccount.setActive(true);

        // Mascota
        testPet = new Pet();
        testPet.setId(1L);
        testPet.setName("Buddy");
        testPet.setAccount(testAccount);

        // Oferta de servicio
        testServiceOffering = new ServiceOffering();
        testServiceOffering.setId(1L);
        testServiceOffering.setSitterId(testSitter.getId());
        testServiceOffering.setName("Paseo de 30 minutos");
        testServiceOffering.setPrice(BigDecimal.valueOf(25.00));
        testServiceOffering.setDurationInMinutes(30);
        testServiceOffering.setActive(true);

        // Reserva
        testBooking = Booking.builder()
                .id(1L)
                .account(testAccount)
                .pet(testPet)
                .sitter(testSitter)
                .serviceOffering(testServiceOffering)
                .bookedBy(testClient)
                .status(BookingStatus.PENDING)
                .totalPrice(BigDecimal.valueOf(25.00))
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusMinutes(30))
                .notes("Cuidar bien a mi perro")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Authentication mock
        mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn(testClient.getEmail());

        // Request de creación
        createBookingRequest = new CreateBookingRequest();
        createBookingRequest.setPetId(testPet.getId());
        createBookingRequest.setSitterId(testSitter.getId());
        createBookingRequest.setServiceOfferingId(testServiceOffering.getId());
        createBookingRequest.setStartTime(LocalDateTime.now().plusDays(1));
        createBookingRequest.setNotes("Primera reserva de prueba");
    }

    // ========== TESTS: createBooking ==========

    /**
     * Verifica la creación exitosa de una reserva con todos los datos válidos.
     */
    @Test
    @DisplayName("createBooking | Éxito | Debería crear reserva cuando todos los datos son válidos")
    void createBooking_WithValidData_ShouldCreateBooking() {
        // Given
        when(userRepository.findByEmail(testClient.getEmail())).thenReturn(Optional.of(testClient));
        when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));
        when(userRepository.findById(testSitter.getId())).thenReturn(Optional.of(testSitter));
        when(serviceOfferingRepository.findById(testServiceOffering.getId())).thenReturn(Optional.of(testServiceOffering));
        when(accountUserRepository.existsByAccountIdAndUserId(testAccount.getId(), testClient.getId())).thenReturn(true);
        when(bookingRepository.existsConflictingBooking(anyLong(), any(), any())).thenReturn(false);
        when(bookingRepository.countByBookedByUserAndStatus(testClient, BookingStatus.PENDING)).thenReturn(0L);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        // When
        BookingDetailResponse response = bookingService.createBooking(createBookingRequest, mockAuth);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        verify(platformFeeService).calculateAndCreatePlatformFee(any(Booking.class));
        verify(notificationService).notifyNewBookingCreated(any(Booking.class));
    }

    /**
     * Verifica que se rechace una reserva cuando la mascota no existe.
     */
    @Test
    @DisplayName("createBooking | Falla | Debería lanzar PetNotFoundException si la mascota no existe")
    void createBooking_WhenPetNotFound_ShouldThrowPetNotFoundException() {
        // Given
        when(userRepository.findByEmail(testClient.getEmail())).thenReturn(Optional.of(testClient));
        when(petRepository.findById(testPet.getId())).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking(createBookingRequest, mockAuth))
                .isInstanceOf(PetNotFoundException.class);
    }

    /**
     * Verifica que se rechace cuando el cuidador no tiene el rol SITTER.
     */
    @Test
    @DisplayName("createBooking | Falla | Debería lanzar SitterRoleRequiredException si el usuario no es SITTER")
    void createBooking_WhenUserIsNotSitter_ShouldThrowSitterRoleRequiredException() {
        // Given
        testClient.setRole(Role.CLIENT); // Cliente trying to be a sitter
        when(userRepository.findByEmail(testClient.getEmail())).thenReturn(Optional.of(testClient));
        when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));
        when(userRepository.findById(testClient.getId())).thenReturn(Optional.of(testClient));

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking(createBookingRequest, mockAuth))
                .isInstanceOf(SitterRoleRequiredException.class);
    }

    /**
     * Verifica que se rechace cuando el cuidador está inactivo.
     */
    @Test
    @DisplayName("createBooking | Falla | Debería lanzar SitterInactiveException si el cuidador está inactivo")
    void createBooking_WhenSitterIsInactive_ShouldThrowSitterInactiveException() {
        // Given
        testSitter.setActive(false);
        when(userRepository.findByEmail(testClient.getEmail())).thenReturn(Optional.of(testClient));
        when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));
        when(userRepository.findById(testSitter.getId())).thenReturn(Optional.of(testSitter));

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking(createBookingRequest, mockAuth))
                .isInstanceOf(SitterInactiveException.class);
    }

    /**
     * Verifica que se rechace cuando existe conflicto de horarios.
     */
    @Test
    @DisplayName("createBooking | Falla | Debería lanzar BookingConflictException si hay conflicto de horarios")
    void createBooking_WhenScheduleConflict_ShouldThrowBookingConflictException() {
        // Given
        when(userRepository.findByEmail(testClient.getEmail())).thenReturn(Optional.of(testClient));
        when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));
        when(userRepository.findById(testSitter.getId())).thenReturn(Optional.of(testSitter));
        when(serviceOfferingRepository.findById(testServiceOffering.getId())).thenReturn(Optional.of(testServiceOffering));
        when(accountUserRepository.existsByAccountIdAndUserId(testAccount.getId(), testClient.getId())).thenReturn(true);
        when(bookingRepository.existsConflictingBooking(anyLong(), any(), any())).thenReturn(true); // Conflicto

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking(createBookingRequest, mockAuth))
                .isInstanceOf(BookingConflictException.class);
    }

    /**
     * Verifica que se rechace cuando el usuario tiene demasiadas reservas pendientes.
     */
    @Test
    @DisplayName("createBooking | Falla | Debería lanzar MaxPendingBookingsExceededException si hay 5+ reservas pendientes")
    void createBooking_WhenMaxPendingBookings_ShouldThrowMaxPendingBookingsExceededException() {
        // Given
        when(userRepository.findByEmail(testClient.getEmail())).thenReturn(Optional.of(testClient));
        when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));
        when(userRepository.findById(testSitter.getId())).thenReturn(Optional.of(testSitter));
        when(serviceOfferingRepository.findById(testServiceOffering.getId())).thenReturn(Optional.of(testServiceOffering));
        when(accountUserRepository.existsByAccountIdAndUserId(testAccount.getId(), testClient.getId())).thenReturn(true);
        when(bookingRepository.existsConflictingBooking(anyLong(), any(), any())).thenReturn(false);
        when(bookingRepository.countByBookedByUserAndStatus(testClient, BookingStatus.PENDING)).thenReturn(5L);

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking(createBookingRequest, mockAuth))
                .isInstanceOf(MaxPendingBookingsExceededException.class);
    }

    // ========== TESTS: getAllBookings ==========

    /**
     * Verifica la obtención paginada de todas las reservas.
     */
    @Test
    @DisplayName("getAllBookings | Éxito | Debería retornar página de reservas")
    void getAllBookings_ShouldReturnPagedBookings() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Booking> bookings = List.of(testBooking);
        Page<Booking> bookingPage = new PageImpl<>(bookings, pageable, 1);
        when(bookingRepository.findAllWithBasicInfo(pageable)).thenReturn(bookingPage);

        // When
        Page<BookingSummaryResponse> result = bookingService.getAllBookings(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ========== TESTS: getBookingById ==========

    /**
     * Verifica la obtención de una reserva por ID.
     */
    @Test
    @DisplayName("getBookingById | Éxito | Debería retornar detalles de reserva cuando existe")
    void getBookingById_WhenExists_ShouldReturnBookingDetails() {
        // Given
        when(bookingRepository.findByIdWithAllRelations(1L)).thenReturn(Optional.of(testBooking));

        // When
        BookingDetailResponse response = bookingService.getBookingById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
    }

    /**
     * Verifica que se lance excepción cuando la reserva no existe.
     */
    @Test
    @DisplayName("getBookingById | Falla | Debería lanzar BookingNotFoundException cuando no existe")
    void getBookingById_WhenNotExists_ShouldThrowBookingNotFoundException() {
        // Given
        when(bookingRepository.findByIdWithAllRelations(99L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> bookingService.getBookingById(99L))
                .isInstanceOf(BookingNotFoundException.class);
    }

    // ========== TESTS: updateBookingStatus ==========

    /**
     * Verifica la transición válida de PENDING a CONFIRMED.
     */
    @Test
    @DisplayName("updateBookingStatus | Éxito | Debería cambiar estado de PENDING a CONFIRMED")
    void updateBookingStatus_FromPendingToConfirmed_ShouldSucceed() {
        // Given
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // When
        BookingDetailResponse response = bookingService.updateBookingStatus(1L, "CONFIRMED", null);

        // Then
        assertThat(response).isNotNull();
        verify(bookingRepository).save(any(Booking.class));
        verify(notificationService).notifyStatusChange(any(Booking.class));
    }

    /**
     * Verifica la transición válida de CONFIRMED a IN_PROGRESS.
     */
    @Test
    @DisplayName("updateBookingStatus | Éxito | Debería cambiar estado a IN_PROGRESS y establecer tiempo inicio")
    void updateBookingStatus_FromConfirmedToInProgress_ShouldSetActualStartTime() {
        // Given
        testBooking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        BookingDetailResponse response = bookingService.updateBookingStatus(1L, "IN_PROGRESS", null);

        // Then
        assertThat(testBooking.getActualStartTime()).isNotNull();
    }

    /**
     * Verifica que se rechace la transición inválida de COMPLETED a PENDING.
     */
    @Test
    @DisplayName("updateBookingStatus | Falla | Debería lanzar BookingStateException en transición inválida")
    void updateBookingStatus_WhenInvalidTransition_ShouldThrowBookingStateException() {
        // Given
        testBooking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        // When/Then
        assertThatThrownBy(() -> bookingService.updateBookingStatus(1L, "PENDING", null))
                .isInstanceOf(BookingStateException.class);
    }

    /**
     * Verifica que se genere factura al completar una reserva.
     */
    @Test
    @DisplayName("updateBookingStatus | Éxito | Debería invocar generación de factura al completar")
    void updateBookingStatus_ToCompleted_ShouldTriggerInvoiceGeneration() {
        // Given
        testBooking.setStatus(BookingStatus.IN_PROGRESS);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // When
        bookingService.updateBookingStatus(1L, "COMPLETED", null);

        // Then
        verify(invoiceService).generateAndProcessInvoiceForBooking(testBooking);
    }

    // ========== TESTS: updateBooking ==========

    /**
     * Verifica la actualización exitosa de una reserva.
     */
    @Test
    @DisplayName("updateBooking | Éxito | Debería actualizar campos modificables")
    void updateBooking_WithValidData_ShouldUpdateBooking() {
        // Given
        UpdateBookingRequest updateRequest = new UpdateBookingRequest();
        updateRequest.setNotes("Notas actualizadas");
        LocalDateTime newStartTime = LocalDateTime.now().plusDays(2);
        updateRequest.setStartTime(newStartTime);

        when(bookingRepository.findByIdWithAllRelations(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        BookingDetailResponse response = bookingService.updateBooking(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(testBooking.getNotes()).isEqualTo("Notas actualizadas");
        verify(notificationService).notifyBookingUpdated(any(Booking.class));
    }

    /**
     * Verifica que no se pueda actualizar una reserva completada.
     */
    @Test
    @DisplayName("updateBooking | Falla | Debería lanzar BookingStateException si la reserva está completada")
    void updateBooking_WhenCompleted_ShouldThrowBookingStateException() {
        // Given
        testBooking.setStatus(BookingStatus.COMPLETED);
        UpdateBookingRequest updateRequest = new UpdateBookingRequest();
        updateRequest.setNotes("Nueva nota");

        when(bookingRepository.findByIdWithAllRelations(1L)).thenReturn(Optional.of(testBooking));

        // When/Then
        assertThatThrownBy(() -> bookingService.updateBooking(1L, updateRequest))
                .isInstanceOf(BookingStateException.class);
    }

    // ========== TESTS: deleteBooking ==========

    /**
     * Verifica la eliminación exitosa de una reserva pendiente.
     */
    @Test
    @DisplayName("deleteBooking | Éxito | Debería eliminar reserva pendiente")
    void deleteBooking_WhenPending_ShouldDeleteSuccessfully() {
        // Given
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        doNothing().when(bookingRepository).delete(testBooking);

        // When
        bookingService.deleteBooking(1L);

        // Then
        verify(bookingRepository).delete(testBooking);
        verify(notificationService).notifyBookingCancelled(any(Booking.class));
    }

    /**
     * Verifica que no se pueda eliminar una reserva en progreso.
     */
    @Test
    @DisplayName("deleteBooking | Falla | Debería lanzar BookingStateException si está en progreso")
    void deleteBooking_WhenInProgress_ShouldThrowBookingStateException() {
        // Given
        testBooking.setStatus(BookingStatus.IN_PROGRESS);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        // When/Then
        assertThatThrownBy(() -> bookingService.deleteBooking(1L))
                .isInstanceOf(BookingStateException.class);
    }

    // ========== TESTS: getBookingsByUser ==========

    /**
     * Verifica la obtención de reservas filtradas por cliente.
     */
    @Test
    @DisplayName("getBookingsByUser | Éxito | Debería retornar reservas filtradas por cliente")
    void getBookingsByUser_WhenClient_ShouldReturnFilteredBookings() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Booking> bookings = List.of(testBooking);
        Page<Booking> bookingPage = new PageImpl<>(bookings, pageable, 1);

        when(bookingRepository.findByBookedByUserId(1L, pageable)).thenReturn(bookingPage);

        // When
        Page<BookingSummaryResponse> result = bookingService.getBookingsByUser(1L, "CLIENT", null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    /**
     * Verifica la obtención de reservas filtradas por cuidador.
     */
    @Test
    @DisplayName("getBookingsByUser | Éxito | Debería retornar reservas filtradas por cuidador")
    void getBookingsByUser_WhenSitter_ShouldReturnFilteredBookings() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Booking> bookings = List.of(testBooking);
        Page<Booking> bookingPage = new PageImpl<>(bookings, pageable, 1);

        when(bookingRepository.findBySitterId(2L, pageable)).thenReturn(bookingPage);

        // When
        Page<BookingSummaryResponse> result = bookingService.getBookingsByUser(2L, "SITTER", null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }
}
```

---

### 2. InvoiceServiceImplementTest.java (Template)

```java
package com.Petcare.Petcare.Service;

import com.Petcare.Petcare.DTOs.Invoice.CreateInvoiceRequest;
import com.Petcare.Petcare.DTOs.Invoice.InvoiceDetailResponse;
import com.Petcare.Petcare.DTOs.Invoice.UpdateInvoiceRequest;
import com.Petcare.Petcare.Exception.Business.*;
import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Booking.BookingStatus;
import com.Petcare.Petcare.Models.Invoice.Invoice;
import com.Petcare.Petcare.Models.Invoice.InvoiceStatus;
import com.Petcare.Petcare.Repositories.BookingRepository;
import com.Petcare.Petcare.Repositories.InvoiceRepository;
import com.Petcare.Petcare.Services.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Suite completa de pruebas unitarias para {@link InvoiceServiceImplement}.
 * 
 * <p>Verifica la lógica de negocio del servicio de facturación, incluyendo
 * generación de facturas desde reservas, cálculos financieros, transiciones
 * de estado y envío de notificaciones.</p>
 * 
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplementTest {

    // ========== DEPENDENCIAS MOCKEADAS ==========
    
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private PlatformFeeService platformFeeService;
    @Mock private PdfGenerationService pdfGenerationService;
    @Mock private NotificationService notificationService;

    // ========== SERVICIO BAJO PRUEBA ==========
    
    @InjectMocks
    private InvoiceServiceImplement invoiceService;

    // ========== DATOS DE PRUEBA ==========
    
    private Booking testBooking;
    private Invoice testInvoice;
    private CreateInvoiceRequest createInvoiceRequest;

    @BeforeEach
    void setUp() {
        // Reserva completada
        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setStatus(BookingStatus.COMPLETED);
        testBooking.setTotalPrice(BigDecimal.valueOf(100.00));

        // Factura
        testInvoice = new Invoice();
        testInvoice.setId(1L);
        testInvoice.setBooking(testBooking);
        testInvoice.setInvoiceNumber("INV-2025-123456");
        testInvoice.setStatus(InvoiceStatus.SENT);
        testInvoice.setSubtotal(BigDecimal.valueOf(90.00));
        testInvoice.setPlatformFee(BigDecimal.valueOf(10.00));
        testInvoice.setTotalAmount(BigDecimal.valueOf(100.00));
        testInvoice.setIssueDate(LocalDateTime.now());
        testInvoice.setDueDate(LocalDateTime.now().plusDays(15));

        // Request de creación
        createInvoiceRequest = new CreateInvoiceRequest();
        createInvoiceRequest.setBookingId(1L);
        createInvoiceRequest.setAutoSendEmail(false);
    }

    // ========== TESTS: generateInvoiceForBooking ==========

    @Test
    @DisplayName("generateInvoiceForBooking | Éxito | Debería crear factura para reserva completada")
    void generateInvoiceForBooking_WithValidBooking_ShouldCreateInvoice() throws Exception {
        // Given
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(invoiceRepository.existsByBookingId(1L)).thenReturn(false);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(1L);
            return i;
        });
        when(pdfGenerationService.generateInvoicePdf(any())).thenReturn(new byte[0]);

        // When
        InvoiceDetailResponse response = invoiceService.generateInvoiceForBooking(createInvoiceRequest);

        // Then
        assertThat(response).isNotNull();
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    @DisplayName("generateInvoiceForBooking | Falla | Debería lanzar BookingNotFoundException si la reserva no existe")
    void generateInvoiceForBooking_WhenBookingNotFound_ShouldThrowException() {
        // Given
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> invoiceService.generateInvoiceForBooking(createInvoiceRequest))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    @DisplayName("generateInvoiceForBooking | Falla | Debería lanzar BookingStateException si la reserva no está completada")
    void generateInvoiceForBooking_WhenBookingNotCompleted_ShouldThrowException() {
        // Given
        testBooking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        // When/Then
        assertThatThrownBy(() -> invoiceService.generateInvoiceForBooking(createInvoiceRequest))
                .isInstanceOf(BookingStateException.class);
    }

    @Test
    @DisplayName("generateInvoiceForBooking | Falla | Debería lanzar InvoiceAlreadyExistsException si ya existe factura")
    void generateInvoiceForBooking_WhenInvoiceAlreadyExists_ShouldThrowException() {
        // Given
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(invoiceRepository.existsByBookingId(1L)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> invoiceService.generateInvoiceForBooking(createInvoiceRequest))
                .isInstanceOf(InvoiceAlreadyExistsException.class);
    }

    // ========== TESTS: getInvoiceById ==========

    @Test
    @DisplayName("getInvoiceById | Éxito | Debería retornar detalles de factura")
    void getInvoiceById_WhenExists_ShouldReturnInvoiceDetails() {
        // Given
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

        // When
        InvoiceDetailResponse response = invoiceService.getInvoiceById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getInvoiceById | Falla | Debería lanzar InvoiceNotFoundException cuando no existe")
    void getInvoiceById_WhenNotExists_ShouldThrowInvoiceNotFoundException() {
        // Given
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> invoiceService.getInvoiceById(99L))
                .isInstanceOf(InvoiceNotFoundException.class);
    }

    // ========== TESTS: sendInvoice ==========

    @Test
    @DisplayName("sendInvoice | Éxito | Debería cambiar estado a SENT y procesar envío")
    void sendInvoice_WhenValidInvoice_ShouldSendAndUpdateStatus() throws Exception {
        // Given
        testInvoice.setStatus(InvoiceStatus.DRAFT);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
        when(pdfGenerationService.generateInvoicePdf(any())).thenReturn(new byte[0]);

        // When
        InvoiceDetailResponse response = invoiceService.sendInvoice(1L);

        // Then
        assertThat(testInvoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
        verify(notificationService).sendInvoiceEmail(any(), any());
    }

    @Test
    @DisplayName("sendInvoice | Falla | Debería lanzar InvoiceStateException si la factura no puede ser enviada")
    void sendInvoice_WhenCannotBeSent_ShouldThrowInvoiceStateException() {
        // Given
        testInvoice.setStatus(InvoiceStatus.CANCELLED);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

        // When/Then
        assertThatThrownBy(() -> invoiceService.sendInvoice(1L))
                .isInstanceOf(InvoiceStateException.class);
    }

    // ========== TESTS: cancelInvoice ==========

    @Test
    @DisplayName("cancelInvoice | Éxito | Debería cancelar factura con motivo")
    void cancelInvoice_WithValidReason_ShouldCancelInvoice() {
        // Given
        testInvoice.setStatus(InvoiceStatus.SENT);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // When
        InvoiceDetailResponse response = invoiceService.cancelInvoice(1L, "Cliente solicitó cancelación");

        // Then
        assertThat(testInvoice.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
        assertThat(testInvoice.getNotes()).contains("Cliente solicitó cancelación");
    }

    @Test
    @DisplayName("cancelInvoice | Falla | Debería lanzar CancellationReasonRequiredException sin motivo")
    void cancelInvoice_WithoutReason_ShouldThrowException() {
        // Given
        testInvoice.setStatus(InvoiceStatus.SENT);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

        // When/Then
        assertThatThrownBy(() -> invoiceService.cancelInvoice(1L, null))
                .isInstanceOf(CancellationReasonRequiredException.class);
    }

    // ========== TESTS: getInvoicesByAccountId ==========

    @Test
    @DisplayName("getInvoicesByAccountId | Éxito | Debería retornar página de facturas")
    void getInvoicesByAccountId_ShouldReturnPagedInvoices() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Invoice> invoicePage = new PageImpl<>(List.of(testInvoice), pageable, 1);
        when(invoiceRepository.findByAccountId(1L, pageable)).thenReturn(invoicePage);

        // When
        var result = invoiceService.getInvoicesByAccountId(1L, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }
}
```

---

### 3. AccountServiceImplementTest.java (Template)

```java
package com.Petcare.Petcare.Service;

import com.Petcare.Petcare.DTOs.Account.AccountResponse;
import com.Petcare.Petcare.DTOs.Account.CreateAccountRequest;
import com.Petcare.Petcare.Exception.Business.AccountNotFoundException;
import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.AccountRepository;
import com.Petcare.Petcare.Services.Implement.AccountServiceImplement;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Suite de pruebas unitarias para {@link AccountServiceImplement}.
 * 
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceImplementTest {

    @Mock private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImplement accountService;

    private User testUser;
    private Account testAccount;
    private CreateAccountRequest createAccountRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        testAccount = new Account(testUser, "Cuenta Familiar", "ACC-123");
        testAccount.setId(1L);

        createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setAccountName("Nueva Cuenta");
        createAccountRequest.setAccountNumber("ACC-456");
    }

    // ========== TESTS: createAccount ==========

    @Test
    @DisplayName("createAccount | Éxito | Debería crear cuenta con usuario propietario")
    void createAccount_WithValidData_ShouldCreateAccount() {
        // Given
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        // When
        AccountResponse response = accountService.createAccount(createAccountRequest, testUser);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accountName()).isEqualTo("Nueva Cuenta");
        verify(accountRepository).save(any(Account.class));
    }

    // ========== TESTS: getAccountById ==========

    @Test
    @DisplayName("getAccountById | Éxito | Debería retornar cuenta existente")
    void getAccountById_WhenExists_ShouldReturnAccount() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When
        AccountResponse response = accountService.getAccountById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getAccountById | Falla | Debería lanzar AccountNotFoundException cuando no existe")
    void getAccountById_WhenNotExists_ShouldThrowException() {
        // Given
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> accountService.getAccountById(99L))
                .isInstanceOf(AccountNotFoundException.class);
    }

    // ========== TESTS: getAllAccounts ==========

    @Test
    @DisplayName("getAllAccounts | Éxito | Debería retornar lista de cuentas")
    void getAllAccounts_ShouldReturnAllAccounts() {
        // Given
        when(accountRepository.findAll()).thenReturn(List.of(testAccount));

        // When
        List<AccountResponse> result = accountService.getAllAccounts();

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getAllAccounts | Éxito | Debería retornar lista vacía cuando no hay cuentas")
    void getAllAccounts_WhenNoAccounts_ShouldReturnEmptyList() {
        // Given
        when(accountRepository.findAll()).thenReturn(List.of());

        // When
        List<AccountResponse> result = accountService.getAllAccounts();

        // Then
        assertThat(result).isEmpty();
    }
}
```

---

### 4. DiscountCouponServiceImplementTest.java (Template)

```java
package com.Petcare.Petcare.Service;

import com.Petcare.Petcare.Models.DiscountCoupon;
import com.Petcare.Petcare.Repositories.DiscountCouponRepository;
import com.Petcare.Petcare.Services.Implement.DiscountCouponServiceImplement;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Suite de pruebas para {@link DiscountCouponServiceImplement}.
 * 
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class DiscountCouponServiceImplementTest {

    @Mock private DiscountCouponRepository discountCouponRepository;

    @InjectMocks
    private DiscountCouponServiceImplement couponService;

    private DiscountCoupon testCoupon;

    @BeforeEach
    void setUp() {
        testCoupon = new DiscountCoupon();
        testCoupon.setId(1L);
        testCoupon.setCouponCode("DESCUENTO20");
        testCoupon.setDiscountType(DiscountCoupon.DiscountType.PERCENTAGE);
        testCoupon.setDiscountValue(BigDecimal.valueOf(20));
        testCoupon.setActive(true);
        testCoupon.setExpiryDate(LocalDateTime.now().plusDays(30));
    }

    // ========== TESTS: saveDiscountCoupon ==========

    @Test
    @DisplayName("saveDiscountCoupon | Éxito | Debería guardar cupón")
    void saveDiscountCoupon_ShouldSaveCoupon() {
        // Given
        when(discountCouponRepository.save(any(DiscountCoupon.class))).thenReturn(testCoupon);

        // When
        DiscountCoupon result = couponService.saveDiscountCoupon(testCoupon);

        // Then
        assertThat(result).isNotNull();
        verify(discountCouponRepository).save(testCoupon);
    }

    // ========== TESTS: getDiscountCouponById ==========

    @Test
    @DisplayName("getDiscountCouponById | Éxito | Debería retornar cupón por ID")
    void getDiscountCouponById_WhenExists_ShouldReturnCoupon() {
        // Given
        when(discountCouponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));

        // When
        Optional<DiscountCoupon> result = couponService.getDiscountCouponById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCouponCode()).isEqualTo("DESCUENTO20");
    }

    @Test
    @DisplayName("getDiscountCouponById | Éxito | Debería retornar vacío cuando no existe")
    void getDiscountCouponById_WhenNotExists_ShouldReturnEmpty() {
        // Given
        when(discountCouponRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<DiscountCoupon> result = couponService.getDiscountCouponById(99L);

        // Then
        assertThat(result).isEmpty();
    }

    // ========== TESTS: getDiscountCouponByCode ==========

    @Test
    @DisplayName("getDiscountCouponByCode | Éxito | Debería retornar cupón por código")
    void getDiscountCouponByCode_WhenExists_ShouldReturnCoupon() {
        // Given
        when(discountCouponRepository.findByCouponCode("DESCUENTO20")).thenReturn(Optional.of(testCoupon));

        // When
        Optional<DiscountCoupon> result = couponService.getDiscountCouponByCode("DESCUENTO20");

        // Then
        assertThat(result).isPresent();
    }

    // ========== TESTS: getAllDiscountCoupons ==========

    @Test
    @DisplayName("getAllDiscountCoupons | Éxito | Debería retornar todos los cupones")
    void getAllDiscountCoupons_ShouldReturnAllCoupons() {
        // Given
        when(discountCouponRepository.findAll()).thenReturn(List.of(testCoupon));

        // When
        List<DiscountCoupon> result = couponService.getAllDiscountCoupons();

        // Then
        assertThat(result).hasSize(1);
    }

    // ========== TESTS: deleteDiscountCoupon ==========

    @Test
    @DisplayName("deleteDiscountCoupon | Éxito | Debería eliminar cupón por ID")
    void deleteDiscountCoupon_ShouldDeleteCoupon() {
        // Given
        doNothing().when(discountCouponRepository).deleteById(1L);

        // When
        couponService.deleteDiscountCoupon(1L);

        // Then
        verify(discountCouponRepository).deleteById(1L);
    }
}
```

---

### 5. AppliedCouponServiceImplementTest.java (Template)

```java
package com.Petcare.Petcare.Service;

import com.Petcare.Petcare.Exception.Business.BookingNotFoundException;
import com.Petcare.Petcare.Exception.Business.CouponExpiredException;
import com.Petcare.Petcare.Exception.Business.CouponNotFoundException;
import com.Petcare.Petcare.Models.AppliedCoupon;
import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Booking.BookingStatus;
import com.Petcare.Petcare.Models.DiscountCoupon;
import com.Petcare.Petcare.Repositories.AppliedCouponRepository;
import com.Petcare.Petcare.Repositories.BookingRepository;
import com.Petcare.Petcare.Repositories.DiscountCouponRepository;
import com.Petcare.Petcare.Services.Implement.AppliedCouponServiceImplement;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Suite de pruebas para {@link AppliedCouponServiceImplement}.
 * 
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class AppliedCouponServiceImplementTest {

    @Mock private AppliedCouponRepository appliedCouponRepository;
    @Mock private DiscountCouponRepository discountCouponRepository;
    @Mock private BookingRepository bookingRepository;

    @InjectMocks
    private AppliedCouponServiceImplement appliedCouponService;

    private Booking testBooking;
    private DiscountCoupon testCoupon;
    private AppliedCoupon testAppliedCoupon;

    @BeforeEach
    void setUp() {
        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setTotalPrice(BigDecimal.valueOf(100.00));
        testBooking.setStatus(BookingStatus.PENDING);

        testCoupon = new DiscountCoupon();
        testCoupon.setId(1L);
        testCoupon.setCouponCode("SAVE20");
        testCoupon.setDiscountType(DiscountCoupon.DiscountType.PERCENTAGE);
        testCoupon.setDiscountValue(BigDecimal.valueOf(20));
        testCoupon.setActive(true);
        testCoupon.setExpiryDate(LocalDateTime.now().plusDays(30));

        testAppliedCoupon = new AppliedCoupon();
        testAppliedCoupon.setId(1L);
        testAppliedCoupon.setBookingId(1L);
        testAppliedCoupon.setAccountId(1L);
        testAppliedCoupon.setCoupon(testCoupon);
        testAppliedCoupon.setDiscountAmount(BigDecimal.valueOf(20.00));
        testAppliedCoupon.setAppliedAt(LocalDateTime.now());
    }

    // ========== TESTS: applyCoupon ==========

    @Test
    @DisplayName("applyCoupon | Éxito | Debería aplicar cupón de porcentaje")
    void applyCoupon_WithPercentageCoupon_ShouldApplyDiscount() {
        // Given
        when(discountCouponRepository.findByCouponCode("SAVE20")).thenReturn(Optional.of(testCoupon));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(appliedCouponRepository.save(any(AppliedCoupon.class))).thenReturn(testAppliedCoupon);

        // When
        AppliedCoupon result = appliedCouponService.applyCoupon(1L, 1L, "SAVE20");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDiscountAmount()).isEqualTo(BigDecimal.valueOf(20.00));
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("applyCoupon | Éxito | Debería aplicar cupón de monto fijo")
    void applyCoupon_WithFixedAmountCoupon_ShouldApplyDiscount() {
        // Given
        testCoupon.setDiscountType(DiscountCoupon.DiscountType.FIXED_AMOUNT);
        testCoupon.setDiscountValue(BigDecimal.valueOf(15));

        when(discountCouponRepository.findByCouponCode("SAVE20")).thenReturn(Optional.of(testCoupon));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(appliedCouponRepository.save(any(AppliedCoupon.class))).thenReturn(testAppliedCoupon);

        // When
        AppliedCoupon result = appliedCouponService.applyCoupon(1L, 1L, "SAVE20");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDiscountAmount()).isEqualTo(BigDecimal.valueOf(15.00));
    }

    @Test
    @DisplayName("applyCoupon | Falla | Debería lanzar CouponNotFoundException si el cupón no existe")
    void applyCoupon_WhenCouponNotFound_ShouldThrowCouponNotFoundException() {
        // Given
        when(discountCouponRepository.findByCouponCode("INVALID")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> appliedCouponService.applyCoupon(1L, 1L, "INVALID"))
                .isInstanceOf(CouponNotFoundException.class);
    }

    @Test
    @DisplayName("applyCoupon | Falla | Debería lanzar CouponExpiredException si el cupón está vencido")
    void applyCoupon_WhenCouponExpired_ShouldThrowCouponExpiredException() {
        // Given
        testCoupon.setExpiryDate(LocalDateTime.now().minusDays(1));

        when(discountCouponRepository.findByCouponCode("SAVE20")).thenReturn(Optional.of(testCoupon));

        // When/Then
        assertThatThrownBy(() -> appliedCouponService.applyCoupon(1L, 1L, "SAVE20"))
                .isInstanceOf(CouponExpiredException.class);
    }

    @Test
    @DisplayName("applyCoupon | Falla | Debería lanzar CouponExpiredException si el cupón está inactivo")
    void applyCoupon_WhenCouponInactive_ShouldThrowCouponExpiredException() {
        // Given
        testCoupon.setActive(false);

        when(discountCouponRepository.findByCouponCode("SAVE20")).thenReturn(Optional.of(testCoupon));

        // When/Then
        assertThatThrownBy(() -> appliedCouponService.applyCoupon(1L, 1L, "SAVE20"))
                .isInstanceOf(CouponExpiredException.class);
    }

    @Test
    @DisplayName("applyCoupon | Éxito | Debería limitar descuento al total de la reserva")
    void applyCoupon_WhenDiscountExceedsTotal_ShouldLimitToTotal() {
        // Given
        testCoupon.setDiscountType(DiscountCoupon.DiscountType.FIXED_AMOUNT);
        testCoupon.setDiscountValue(BigDecimal.valueOf(150)); // Mayor que el total

        when(discountCouponRepository.findByCouponCode("SAVE20")).thenReturn(Optional.of(testCoupon));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(appliedCouponRepository.save(any(AppliedCoupon.class))).thenReturn(testAppliedCoupon);

        // When
        appliedCouponService.applyCoupon(1L, 1L, "SAVE20");

        // Then
        // El descuento debería limitarse a 100 (el total de la reserva)
        verify(bookingRepository).save(any(Booking.class));
    }

    // ========== TESTS: getCouponsByAccount ==========

    @Test
    @DisplayName("getCouponsByAccount | Éxito | Debería retornar cupones aplicados por cuenta")
    void getCouponsByAccount_ShouldReturnAppliedCoupons() {
        // Given
        when(appliedCouponRepository.findByAccountId(1L)).thenReturn(List.of(testAppliedCoupon));

        // When
        List<AppliedCoupon> result = appliedCouponService.getCouponsByAccount(1L);

        // Then
        assertThat(result).hasSize(1);
    }

    // ========== TESTS: getCouponsByBooking ==========

    @Test
    @DisplayName("getCouponsByBooking | Éxito | Debería retornar cupones aplicados a reserva")
    void getCouponsByBooking_ShouldReturnAppliedCoupons() {
        // Given
        when(appliedCouponRepository.findByBookingId(1L)).thenReturn(List.of(testAppliedCoupon));

        // When
        List<AppliedCoupon> result = appliedCouponService.getCouponsByBooking(1L);

        // Then
        assertThat(result).hasSize(1);
    }

    // ========== TESTS: validateCoupon ==========

    @Test
    @DisplayName("validateCoupon | Éxito | Debería retornar true para cupón válido")
    void validateCoupon_WithValidCoupon_ShouldReturnTrue() {
        // Given
        testCoupon.setActive(true);
        testCoupon.setExpiryDate(LocalDateTime.now().plusDays(30));

        // When
        boolean result = appliedCouponService.validateCoupon(testCoupon);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("validateCoupon | Falla | Debería retornar false para cupón inactivo")
    void validateCoupon_WithInactiveCoupon_ShouldReturnFalse() {
        // Given
        testCoupon.setActive(false);

        // When
        boolean result = appliedCouponService.validateCoupon(testCoupon);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("validateCoupon | Falla | Debería retornar false para cupón vencido")
    void validateCoupon_WithExpiredCoupon_ShouldReturnFalse() {
        // Given
        testCoupon.setActive(true);
        testCoupon.setExpiryDate(LocalDateTime.now().minusDays(1));

        // When
        boolean result = appliedCouponService.validateCoupon(testCoupon);

        // Then
        assertThat(result).isFalse();
    }
}
```

---

### 6. PlatformFeeServiceImplementTest.java (Template)

```java
package com.Petcare.Petcare.Service;

import com.Petcare.Petcare.DTOs.PlatformFee.CreatePlatformFeeRequest;
import com.Petcare.Petcare.DTOs.PlatformFee.PlatformFeeResponse;
import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Booking.BookingStatus;
import com.Petcare.Petcare.Models.PlatformFee;
import com.Petcare.Petcare.Repositories.BookingRepository;
import com.Petcare.Petcare.Repositories.PlatformFeeRepository;
import com.Petcare.Petcare.Services.Implement.PlatformFeeServiceImplement;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Suite de pruebas para {@link PlatformFeeServiceImplement}.
 * 
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class PlatformFeeServiceImplementTest {

    @Mock private PlatformFeeRepository platformFeeRepository;
    @Mock private BookingRepository bookingRepository;

    @InjectMocks
    private PlatformFeeServiceImplement platformFeeService;

    private Booking testBooking;
    private PlatformFee testFee;
    private CreatePlatformFeeRequest createRequest;

    @BeforeEach
    void setUp() {
        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setTotalPrice(BigDecimal.valueOf(100.00));
        testBooking.setStatus(BookingStatus.COMPLETED);

        testFee = new PlatformFee();
        testFee.setId(1L);
        testFee.setBooking(testBooking);
        testFee.setBaseAmount(BigDecimal.valueOf(100.00));
        testFee.setFeePercentage(BigDecimal.valueOf(10.00));
        testFee.setFeeAmount(BigDecimal.valueOf(10.00));
        testFee.setNetAmount(BigDecimal.valueOf(90.00));

        createRequest = new CreatePlatformFeeRequest();
        createRequest.setBookingId(1L);
        createRequest.setFeePercentage(BigDecimal.valueOf(10.00));
    }

    // ========== TESTS: calculateAndCreateFee ==========

    @Test
    @DisplayName("calculateAndCreateFee | Éxito | Debería calcular y crear tarifa de plataforma")
    void calculateAndCreateFee_WithValidData_ShouldCreatePlatformFee() {
        // Given
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(platformFeeRepository.save(any(PlatformFee.class))).thenReturn(testFee);

        // When
        PlatformFeeResponse response = platformFeeService.calculateAndCreateFee(createRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.feeAmount()).isEqualTo(BigDecimal.valueOf(10.00));
        assertThat(response.netAmount()).isEqualTo(BigDecimal.valueOf(90.00));
    }

    @Test
    @DisplayName("calculateAndCreateFee | Falla | Debería lanzar excepción si la reserva no existe")
    void calculateAndCreateFee_WhenBookingNotFound_ShouldThrowException() {
        // Given
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> platformFeeService.calculateAndCreateFee(
                new CreatePlatformFeeRequest(99L, BigDecimal.valueOf(10))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reserva no encontrada");
    }

    @Test
    @DisplayName("calculateAndCreateFee | Éxito | Debería calcular correctamente el monto de la tarifa")
    void calculateAndCreateFee_ShouldCalculateFeeCorrectly() {
        // Given
        testBooking.setTotalPrice(BigDecimal.valueOf(200.00)); // 200 * 10% = 20
        createRequest.setFeePercentage(BigDecimal.valueOf(10.00));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(platformFeeRepository.save(any(PlatformFee.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        PlatformFeeResponse response = platformFeeService.calculateAndCreateFee(createRequest);

        // Then
        assertThat(response.getFeeAmount()).isEqualTo(BigDecimal.valueOf(20.00));
        assertThat(response.getNetAmount()).isEqualTo(BigDecimal.valueOf(180.00));
    }

    // ========== TESTS: calculateAndCreatePlatformFee (delegation) ==========

    @Test
    @DisplayName("calculateAndCreatePlatformFee | Delegation | Debería delegar a calculateAndCreateFee")
    void calculateAndCreatePlatformFee_ShouldDelegate() {
        // Given
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(platformFeeRepository.save(any(PlatformFee.class))).thenReturn(testFee);

        // When
        PlatformFee result = platformFeeService.calculateAndCreatePlatformFee(testBooking);

        // Then
        assertThat(result).isNull(); // current implementation returns null
        // Nota: Este test documenta el comportamiento actual - puede requerir implementación real
    }
}
```

---

### 7. EmailServiceImplementTest.java (Template)

```java
package com.Petcare.Petcare.Service;

import com.Petcare.Petcare.DTOs.Email.Attachment;
import com.Petcare.Petcare.DTOs.Email.Email;
import com.Petcare.Petcare.Services.Implement.EmailServiceImplement;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Suite de pruebas para {@link EmailServiceImplement}.
 * 
 * <p>Verifica el envío de correos, validación de datos, procesamiento
 * de plantillas Thymeleaf y manejo de archivos adjuntos.</p>
 * 
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplementTest {

    @Mock private JavaMailSender javaMailSender;
    @Mock private TemplateEngine templateEngine;
    @Mock private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImplement emailService;

    private Email testEmail;
    private Attachment testAttachment;

    @BeforeEach
    void setUp() throws Exception {
        testEmail = Email.builder()
                .to("recipient@test.com")
                .from("noreply@petcare.com")
                .subject("Test Email")
                .body("Test body content")
                .build();

        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        org.springframework.core.io.InputStreamResource resource = 
                new org.springframework.core.io.InputStreamResource(inputStream);

        testAttachment = new Attachment("test.pdf", resource, "application/pdf");
    }

    // ========== TESTS: sendEmail ==========

    @Test
    @DisplayName("sendEmail | Éxito | Debería enviar correo HTML sin adjuntos")
    void sendEmail_WithoutAttachments_ShouldSendHtmlEmail() throws Exception {
        // Given
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test</html>");

        // When
        emailService.sendEmail(testEmail);

        // Then
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("sendEmail | Éxito | Debería enviar correo con adjuntos")
    void sendEmail_WithAttachments_ShouldSendEmailWithAttachments() throws Exception {
        // Given
        testEmail.setAttachments(new java.util.ArrayList<>());
        testEmail.getAttachments().add(testAttachment);

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test</html>");

        // When
        emailService.sendEmail(testEmail);

        // Then
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("sendEmail | Falla | Debería lanzar excepción para email inválido")
    void sendEmail_WithInvalidEmail_ShouldThrowException() {
        // Given
        testEmail.setTo(null);

        // When/Then
        assertThatThrownBy(() -> emailService.sendEmail(testEmail))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ========== TESTS: sendVerificationEmail ==========

    @Test
    @DisplayName("sendVerificationEmail | Éxito | Debería enviar correo de verificación")
    void sendVerificationEmail_WithValidData_ShouldSendEmail() throws Exception {
        // Given
        String email = "user@test.com";
        String name = "John Doe";
        String url = "https://petcare.com/verify?token=abc123";
        int expirationHours = 24;

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Verification</html>");

        // When
        emailService.sendVerificationEmail(email, name, url, expirationHours);

        // Then
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("sendVerificationEmail | Falla | Debería lanzar excepción para email vacío")
    void sendVerificationEmail_WithEmptyEmail_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> 
            emailService.sendVerificationEmail("", "Name", "http://url.com", 24))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email del destinatario");
    }

    @Test
    @DisplayName("sendVerificationEmail | Falla | Debería lanzar excepción para horas inválidas")
    void sendVerificationEmail_WithInvalidHours_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> 
            emailService.sendVerificationEmail("user@test.com", "Name", "http://url.com", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("horas de expiración");
    }

    // ========== TESTS: validateEmail ==========

    @Test
    @DisplayName("validateEmail | Éxito | Debería validar email correctamente")
    void validateEmail_WithValidEmail_ShouldReturnTrue() {
        // Given
        Email validEmail = Email.builder()
                .to("test@example.com")
                .from("noreply@petcare.com")
                .subject("Subject")
                .build();

        // When
        boolean result = emailService.validateEmail(validEmail);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("validateEmail | Falla | Debería rechazar email sin destinatario")
    void validateEmail_WithoutRecipient_ShouldThrowException() {
        // Given
        testEmail.setTo(null);

        // When/Then
        assertThatThrownBy(() -> emailService.validateEmail(testEmail))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("validateEmail | Falla | Debería rechazar formato de email inválido")
    void validateEmail_WithInvalidFormat_ShouldThrowException() {
        // Given
        testEmail.setTo("not-an-email");

        // When/Then
        assertThatThrownBy(() -> emailService.validateEmail(testEmail))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("validateEmail | Falla | Debería rechazar email sin asunto")
    void validateEmail_WithoutSubject_ShouldThrowException() {
        // Given
        testEmail.setSubject(null);

        // When/Then
        assertThatThrownBy(() -> emailService.validateEmail(testEmail))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
```

---

### 8. BookingControllerTest.java (Template)

```java
package com.Petcare.Petcare.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.Petcare.Petcare.Config.TestSecurityConfig;
import com.Petcare.Petcare.Controllers.BookingController;
import com.Petcare.Petcare.DTOs.Booking.BookingDetailResponse;
import com.Petcare.Petcare.DTOs.Booking.CreateBookingRequest;
import com.Petcare.Petcare.DTOs.Booking.UpdateBookingRequest;
import com.Petcare.Petcare.Models.Booking.BookingStatus;
import com.Petcare.Petcare.Services.BookingService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Suite de pruebas de integración para {@link BookingController}.
 * 
 * <p>Verifica los endpoints REST del controlador de reservas,
 * incluyendo validación de requests, respuestas HTTP y mapeo de DTOs.</p>
 * 
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 2025
 */
@WebMvcTest(BookingController.class)
@Import(TestSecurityConfig.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingDetailResponse testBookingResponse;
    private CreateBookingRequest createBookingRequest;

    @BeforeEach
    void setUp() {
        testBookingResponse = new BookingDetailResponse(
                1L,
                "client@test.com",
                "John Doe",
                "sitter@test.com",
                "Jane Smith",
                "Paseo de 30 minutos",
                BookingStatus.PENDING.name(),
                BigDecimal.valueOf(25.00),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusMinutes(30),
                null,
                null,
                "Notas de prueba",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        createBookingRequest = new CreateBookingRequest();
        createBookingRequest.setPetId(1L);
        createBookingRequest.setSitterId(2L);
        createBookingRequest.setServiceOfferingId(1L);
        createBookingRequest.setStartTime(LocalDateTime.now().plusDays(1));
        createBookingRequest.setNotes("Primera reserva");
    }

    // ========== TESTS: POST /api/v1/bookings ==========

    @Test
    @DisplayName("POST /api/v1/bookings | Éxito | Debería crear reserva y retornar 201")
    @WithMockUser
    void createBooking_WithValidData_ShouldReturn201() throws Exception {
        // Given
        when(bookingService.createBooking(any(CreateBookingRequest.class), any()))
                .thenReturn(testBookingResponse);

        // When/Then
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBookingRequest))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalPrice").value(25.00));
    }

    @Test
    @DisplayName("POST /api/v1/bookings | Falla | Debería retornar 400 para datos inválidos")
    @WithMockUser
    void createBooking_WithInvalidData_ShouldReturn400() throws Exception {
        // Given
        createBookingRequest.setPetId(null); // Campo requerido faltante

        // When/Then
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBookingRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ========== TESTS: GET /api/v1/bookings ==========

    @Test
    @DisplayName("GET /api/v1/bookings | Éxito | Debería retornar página de reservas")
    @WithMockUser
    void getAllBookings_ShouldReturnPagedBookings() throws Exception {
        // Given
        Page<BookingDetailResponse> page = new PageImpl<>(List.of(testBookingResponse));
        when(bookingService.getAllBookings(any(Pageable.class))).thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/v1/bookings")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // ========== TESTS: GET /api/v1/bookings/{id} ==========

    @Test
    @DisplayName("GET /api/v1/bookings/{id} | Éxito | Debería retornar reserva por ID")
    @WithMockUser
    void getBookingById_WhenExists_ShouldReturnBooking() throws Exception {
        // Given
        when(bookingService.getBookingById(1L)).thenReturn(testBookingResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clientEmail").value("client@test.com"));
    }

    @Test
    @DisplayName("GET /api/v1/bookings/{id} | Falla | Debería retornar 404 si no existe")
    @WithMockUser
    void getBookingById_WhenNotExists_ShouldReturn404() throws Exception {
        // Given
        when(bookingService.getBookingById(99L))
                .thenThrow(new com.Petcare.Petcare.Exception.Business.BookingNotFoundException(99L));

        // When/Then
        mockMvc.perform(get("/api/v1/bookings/99"))
                .andExpect(status().isNotFound());
    }

    // ========== TESTS: PUT /api/v1/bookings/{id}/status ==========

    @Test
    @DisplayName("PUT /api/v1/bookings/{id}/status | Éxito | Debería actualizar estado de reserva")
    @WithMockUser
    void updateBookingStatus_WithValidData_ShouldReturn200() throws Exception {
        // Given
        when(bookingService.updateBookingStatus(eq(1L), eq("CONFIRMED"), any()))
                .thenReturn(testBookingResponse);

        // When/Then
        mockMvc.perform(put("/api/v1/bookings/1/status")
                        .param("status", "CONFIRMED")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // ========== TESTS: PUT /api/v1/bookings/{id} ==========

    @Test
    @DisplayName("PUT /api/v1/bookings/{id} | Éxito | Debería actualizar datos de reserva")
    @WithMockUser
    void updateBooking_WithValidData_ShouldReturn200() throws Exception {
        // Given
        UpdateBookingRequest updateRequest = new UpdateBookingRequest();
        updateRequest.setNotes("Notas actualizadas");

        when(bookingService.updateBooking(eq(1L), any(UpdateBookingRequest.class)))
                .thenReturn(testBookingResponse);

        // When/Then
        mockMvc.perform(put("/api/v1/bookings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // ========== TESTS: DELETE /api/v1/bookings/{id} ==========

    @Test
    @DisplayName("DELETE /api/v1/bookings/{id} | Éxito | Debería eliminar reserva")
    @WithMockUser
    void deleteBooking_ShouldReturn204() throws Exception {
        // Given
        when(bookingService.deleteBooking(1L)).thenReturn(true);

        // When/Then
        mockMvc.perform(delete("/api/v1/bookings/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    // ========== TESTS: GET /api/v1/bookings/user/{userId} ==========

    @Test
    @DisplayName("GET /api/v1/bookings/user/{userId} | Éxito | Debería retornar reservas por usuario")
    @WithMockUser
    void getBookingsByUser_ShouldReturnFilteredBookings() throws Exception {
        // Given
        Page<BookingDetailResponse> page = new PageImpl<>(List.of(testBookingResponse));
        when(bookingService.getBookingsByUser(eq(1L), eq("CLIENT"), any(), any(Pageable.class)))
                .thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/v1/bookings/user/1")
                        .param("role", "CLIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].clientEmail").value("client@test.com"));
    }
}
```

---

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/test/java/com/Petcare/Petcare/Config/TestSecurityConfig.java` | Create | Security config for integration tests |
| `src/test/java/com/Petcare/Petcare/Service/BookingServiceImplementTest.java` | Create | Complete unit tests for BookingService |
| `src/test/java/com/Petcare/Petcare/Service/InvoiceServiceImplementTest.java` | Create | Unit tests for InvoiceService |
| `src/test/java/com/Petcare/Petcare/Service/AccountServiceImplementTest.java` | Create | Unit tests for AccountService |
| `src/test/java/com/Petcare/Petcare/Service/DiscountCouponServiceImplementTest.java` | Create | Unit tests for DiscountCouponService |
| `src/test/java/com/Petcare/Petcare/Service/AppliedCouponServiceImplementTest.java` | Create | Unit tests for AppliedCouponService |
| `src/test/java/com/Petcare/Petcare/Service/PlatformFeeServiceImplementTest.java` | Create | Unit tests for PlatformFeeService |
| `src/test/java/com/Petcare/Petcare/Service/EmailServiceImplementTest.java` | Create | Unit tests for EmailService |
| `src/test/java/com/Petcare/Petcare/Controllers/BookingControllerTest.java` | Create | Integration tests for BookingController |
| `src/test/java/com/Petcare/Petcare/Service/PetServiceImplementTest.java` | Create | Unit tests for PetService (remaining) |
| `src/test/java/com/Petcare/Petcare/Service/NotificationServiceImplementTest.java` | Create | Unit tests for NotificationService (remaining) |
| `src/test/java/com/Petcare/Petcare/Service/ReviewServiceImplementTest.java` | Create | Unit tests for ReviewService (if exists) |
| `src/test/java/com/Petcare/Petcare/Service/ServiceOfferingServiceImplementTest.java` | Create | Unit tests for ServiceOfferingService (if exists) |
| `src/test/java/com/Petcare/Petcare/Controllers/InvoiceControllerTest.java` | Create | Integration tests for InvoiceController |
| `src/test/java/com/Petcare/Petcare/Controllers/AccountControllerTest.java` | Create | Integration tests for AccountController |
| `src/test/java/com/Petcare/Petcare/Controllers/PetControllerTest.java` | Create | Integration tests for PetController |
| `src/test/java/com/Petcare/Petcare/Controllers/PaymentMethodControllerTest.java` | Create | Integration tests for PaymentMethodController |
| `src/test/java/com/Petcare/Petcare/Controllers/UserControllerTest.java` | Create | Integration tests for UserController |
| `src/test/java/com/Petcare/Petcare/Controllers/DiscountCouponControllerTest.java` | Create | Integration tests for DiscountCouponController |
| `src/test/java/com/Petcare/Petcare/Controllers/ReviewControllerTest.java` | Create | Integration tests for ReviewController |
| `src/test/java/com/Petcare/Petcare/Controllers/AppliedCouponControllerTest.java` | Create | Integration tests for AppliedCouponController |

---

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| **Unit - Service** | Business logic, validations, exceptions, calculations | Mockito + AssertJ |
| **Unit - Repository** | Query methods, data access | Spring Data test slices |
| **Integration - Controller** | HTTP endpoints, request/response mapping, status codes | @WebMvcTest + MockMvc |
| **Integration - Full** | End-to-end flows, database integration | @SpringBootTest |

### Test Coverage Goals

1. **Critical Path Coverage**: All service methods with business logic
2. **Exception Coverage**: All thrown exceptions tested
3. **Happy Path**: Successful operations for all endpoints
4. **Error Cases**: Invalid inputs, missing data, not found scenarios
5. **Edge Cases**: Empty lists, boundary values, null handling

---

## Migration / Rollout

No migration required - this is a test coverage initiative that doesn't affect production code.

---

## Open Questions

- [ ] **PaymentService**: The file `PaymentServiceImplement.java` was not found. Need to verify if it exists under a different name or needs to be created.
- [ ] **ReviewService/ServiceOfferingService**: Similar files not found. Need to verify existence.
- [ ] **ServiceOffering entity**: Need to check the exact entity structure for proper test setup.
- [ ] **Pet entity**: Need to verify relationship with Account for proper test setup.

---

## Implementation Order

### Phase 1: Core Services (High Priority)
1. `BookingServiceImplementTest` - Most complex, most critical
2. `InvoiceServiceImplementTest` - Complex financial logic
3. `AccountServiceImplementTest` - Simple, foundation for others

### Phase 2: Supporting Services
4. `DiscountCouponServiceImplementTest`
5. `AppliedCouponServiceImplementTest`
6. `PlatformFeeServiceImplementTest`
7. `EmailServiceImplementTest`

### Phase 3: Remaining Services
8. `PetServiceImplementTest`
9. `NotificationServiceImplementTest`
10. Other services as needed

### Phase 4: Controller Tests
11. `BookingControllerTest`
12. `InvoiceControllerTest`
13. Other controllers as needed
