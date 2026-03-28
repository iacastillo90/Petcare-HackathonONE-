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
import com.Petcare.Petcare.Models.ServiceOffering.ServiceType;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.*;
import com.Petcare.Petcare.Services.InvoiceService;
import com.Petcare.Petcare.Services.NotificationService;
import com.Petcare.Petcare.Services.PlatformFeeService;
import com.Petcare.Petcare.Services.Implement.BookingServiceImplement;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
 * @see BookingServiceImplement
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Pruebas Unitarias: BookingServiceImplement")
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

    private static final Long VALID_ACCOUNT_ID = 1L;
    private static final Long VALID_PET_ID = 1L;
    private static final Long VALID_SITTER_ID = 2L;
    private static final Long VALID_SERVICE_OFFERING_ID = 1L;
    private static final Long VALID_BOOKING_ID = 1L;
    private static final String TEST_CLIENT_EMAIL = "client@test.com";
    private static final String TEST_SITTER_EMAIL = "sitter@test.com";

    /**
     * Configuración inicial ejecutada antes de cada test.
     * Prepara datos de prueba representativos para los diferentes escenarios.
     */
    @BeforeEach
    void setUp() {
        // Usuario cliente
        testClient = new User();
        testClient.setId(1L);
        testClient.setEmail(TEST_CLIENT_EMAIL);
        testClient.setFirstName("John");
        testClient.setLastName("Doe");
        testClient.setRole(Role.CLIENT);
        testClient.setActive(true);

        // Usuario cuidador
        testSitter = new User();
        testSitter.setId(VALID_SITTER_ID);
        testSitter.setEmail(TEST_SITTER_EMAIL);
        testSitter.setFirstName("Jane");
        testSitter.setLastName("Smith");
        testSitter.setRole(Role.SITTER);
        testSitter.setActive(true);

        // Cuenta de usuario
        testAccount = new Account();
        testAccount.setId(VALID_ACCOUNT_ID);
        testAccount.setOwnerUser(testClient);
        testAccount.setAccountNumber("ACC-123");
        testAccount.setAccountName("Cuenta Familiar");
        testAccount.setActive(true);

        // Mascota
        testPet = new Pet();
        testPet.setId(VALID_PET_ID);
        testPet.setName("Buddy");
        testPet.setAccount(testAccount);
        testPet.setActive(true);

        // Oferta de servicio
        testServiceOffering = new ServiceOffering();
        setField(testServiceOffering, "id", VALID_SERVICE_OFFERING_ID);
        setField(testServiceOffering, "sitterId", VALID_SITTER_ID);
        setField(testServiceOffering, "name", "Paseo de 30 minutos");
        setField(testServiceOffering, "price", BigDecimal.valueOf(25.00));
        setField(testServiceOffering, "durationInMinutes", 30);
        setField(testServiceOffering, "isActive", true);

        // Reserva
        testBooking = new Booking();
        testBooking.setId(VALID_BOOKING_ID);
        testBooking.setAccount(testAccount);
        testBooking.setPet(testPet);
        testBooking.setSitter(testSitter);
        testBooking.setServiceOffering(testServiceOffering);
        testBooking.setBookedByUser(testClient);
        testBooking.setStatus(BookingStatus.PENDING);
        testBooking.setTotalPrice(BigDecimal.valueOf(25.00));
        testBooking.setStartTime(LocalDateTime.now().plusDays(1));
        testBooking.setEndTime(LocalDateTime.now().plusDays(1).plusMinutes(30));
        testBooking.setNotes("Cuidar bien a mi perro");
        testBooking.setCreatedAt(LocalDateTime.now());
        testBooking.setUpdatedAt(LocalDateTime.now());

        // Authentication mock
        mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn(testClient.getEmail());
    }

    /**
     * Helper method to set private fields using reflection (for Lombok entities).
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }

    /**
     * Helper method to create CreateBookingRequest with specific startTime.
     */
    private CreateBookingRequest createBookingRequest(LocalDateTime startTime) {
        return new CreateBookingRequest(
            testPet.getId(),
            testSitter.getId(),
            getServiceOfferingId(testServiceOffering),
            startTime,
            "Primera reserva de prueba"
        );
    }

    private Long getServiceOfferingId(ServiceOffering so) {
        try {
            Field field = ServiceOffering.class.getDeclaredField("id");
            field.setAccessible(true);
            return (Long) field.get(so);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ========== TESTS: createBooking ==========

    @Nested
    @DisplayName("createBooking")
    class CreateBookingTests {

        @Test
        @DisplayName("createBooking | Éxito | Debería crear reserva cuando todos los datos son válidos")
        void createBooking_WithValidData_ShouldCreateBooking() {
            // Given
            LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
            CreateBookingRequest request = createBookingRequest(futureTime);
            
            when(userRepository.findByEmail(testClient.getEmail())).thenReturn(Optional.of(testClient));
            when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));
            when(userRepository.findById(testSitter.getId())).thenReturn(Optional.of(testSitter));
            when(serviceOfferingRepository.findById(getServiceOfferingId(testServiceOffering))).thenReturn(Optional.of(testServiceOffering));
            when(accountUserRepository.existsByAccountIdAndUserId(testAccount.getId(), testClient.getId())).thenReturn(true);
            when(bookingRepository.existsConflictingBooking(anyLong(), any(), any())).thenReturn(false);
            when(bookingRepository.countByBookedByUserAndStatus(any(User.class), eq(BookingStatus.PENDING))).thenReturn(0L);
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
                Booking b = inv.getArgument(0);
                b.setId(VALID_BOOKING_ID);
                return b;
            });

            // When
            BookingDetailResponse response = bookingService.createBooking(request, mockAuth);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(VALID_BOOKING_ID);
            verify(platformFeeService).calculateAndCreatePlatformFee(any(Booking.class));
            verify(notificationService).notifyNewBookingCreated(any(Booking.class));
        }

        @Test
        @DisplayName("createBooking | Falla | Debería lanzar PetNotFoundException si la mascota no existe")
        void createBooking_WhenPetNotFound_ShouldThrowPetNotFoundException() {
            // Given
            LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
            CreateBookingRequest request = createBookingRequest(futureTime);
            
            when(userRepository.findByEmail(testClient.getEmail())).thenReturn(Optional.of(testClient));
            when(petRepository.findById(testPet.getId())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> bookingService.createBooking(request, mockAuth))
                    .isInstanceOf(PetNotFoundException.class);
        }

        @Test
        @DisplayName("createBooking | Falla | Debería lanzar InactiveAccountException si la cuenta está inactiva")
        void createBooking_WhenAccountInactive_ShouldThrowInactiveAccountException() {
            // Given
            testAccount.setActive(false);
            LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
            CreateBookingRequest request = createBookingRequest(futureTime);
            
            when(userRepository.findByEmail(testClient.getEmail())).thenReturn(Optional.of(testClient));
            when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));

            // When/Then
            assertThatThrownBy(() -> bookingService.createBooking(request, mockAuth))
                    .isInstanceOf(InactiveAccountException.class);
        }

        @Test
        @DisplayName("createBooking | Falla | Debería lanzar SitterRoleRequiredException si el usuario no es SITTER")
        void createBooking_WhenUserIsNotSitter_ShouldThrowSitterRoleRequiredException() {
            // Given
            LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
            CreateBookingRequest request = createBookingRequest(futureTime);
            
            when(userRepository.findByEmail(testClient.getEmail())).thenReturn(Optional.of(testClient));
            when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));
            // Mock the sitter lookup - but return the client instead (which has wrong role)
            when(userRepository.findById(testSitter.getId())).thenReturn(Optional.of(testClient));

            // When/Then
            assertThatThrownBy(() -> bookingService.createBooking(request, mockAuth))
                    .isInstanceOf(SitterRoleRequiredException.class);
        }

        @Test
        @DisplayName("createBooking | Falla | Debería lanzar SitterInactiveException si el cuidador está inactivo")
        void createBooking_WhenSitterIsInactive_ShouldThrowSitterInactiveException() {
            // Given
            testSitter.setActive(false);
            LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
            CreateBookingRequest request = createBookingRequest(futureTime);
            
            when(userRepository.findByEmail(testClient.getEmail())).thenReturn(Optional.of(testClient));
            when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));
            when(userRepository.findById(testSitter.getId())).thenReturn(Optional.of(testSitter));

            // When/Then
            assertThatThrownBy(() -> bookingService.createBooking(request, mockAuth))
                    .isInstanceOf(SitterInactiveException.class);
        }

        @Test
        @DisplayName("createBooking | Falla | Debería lanzar BookingConflictException si hay conflicto de horarios")
        void createBooking_WhenScheduleConflict_ShouldThrowBookingConflictException() {
            // Given
            LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
            CreateBookingRequest request = createBookingRequest(futureTime);
            
            when(userRepository.findByEmail(testClient.getEmail())).thenReturn(Optional.of(testClient));
            when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));
            when(userRepository.findById(testSitter.getId())).thenReturn(Optional.of(testSitter));
            when(serviceOfferingRepository.findById(getServiceOfferingId(testServiceOffering))).thenReturn(Optional.of(testServiceOffering));
            when(accountUserRepository.existsByAccountIdAndUserId(testAccount.getId(), testClient.getId())).thenReturn(true);
            when(bookingRepository.existsConflictingBooking(anyLong(), any(), any())).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> bookingService.createBooking(request, mockAuth))
                    .isInstanceOf(BookingConflictException.class);
        }

        @Test
        @DisplayName("createBooking | Falla | Debería lanzar MaxPendingBookingsExceededException si hay 5+ reservas pendientes")
        void createBooking_WhenMaxPendingBookings_ShouldThrowMaxPendingBookingsExceededException() {
            // Given
            LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
            CreateBookingRequest request = createBookingRequest(futureTime);
            
            when(userRepository.findByEmail(testClient.getEmail())).thenReturn(Optional.of(testClient));
            when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));
            when(userRepository.findById(testSitter.getId())).thenReturn(Optional.of(testSitter));
            when(serviceOfferingRepository.findById(getServiceOfferingId(testServiceOffering))).thenReturn(Optional.of(testServiceOffering));
            when(accountUserRepository.existsByAccountIdAndUserId(testAccount.getId(), testClient.getId())).thenReturn(true);
            when(bookingRepository.existsConflictingBooking(anyLong(), any(), any())).thenReturn(false);
            when(bookingRepository.countByBookedByUserAndStatus(any(User.class), eq(BookingStatus.PENDING))).thenReturn(5L);

            // When/Then
            assertThatThrownBy(() -> bookingService.createBooking(request, mockAuth))
                    .isInstanceOf(MaxPendingBookingsExceededException.class);
        }

        @Test
        @DisplayName("createBooking | Falla | Debería lanzar InsufficientTimeException si la fecha es muy próxima")
        void createBooking_WhenInsufficientTime_ShouldThrowInsufficientTimeException() {
            // Given
            CreateBookingRequest request = createBookingRequest(LocalDateTime.now().plusMinutes(30));
            when(userRepository.findByEmail(testClient.getEmail())).thenReturn(Optional.of(testClient));

            // When/Then
            assertThatThrownBy(() -> bookingService.createBooking(request, mockAuth))
                    .isInstanceOf(InsufficientTimeException.class);
        }
    }

    // ========== TESTS: getBookingById ==========

    @Nested
    @DisplayName("getBookingById")
    class GetBookingByIdTests {

        @Test
        @DisplayName("getBookingById | Éxito | Debería retornar detalles de reserva cuando existe")
        void getBookingById_WhenExists_ShouldReturnBookingDetails() {
            // Given
            when(bookingRepository.findByIdWithAllRelations(VALID_BOOKING_ID)).thenReturn(Optional.of(testBooking));

            // When
            BookingDetailResponse response = bookingService.getBookingById(VALID_BOOKING_ID);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(VALID_BOOKING_ID);
        }

        @Test
        @DisplayName("getBookingById | Falla | Debería lanzar BookingNotFoundException cuando no existe")
        void getBookingById_WhenNotExists_ShouldThrowBookingNotFoundException() {
            // Given
            when(bookingRepository.findByIdWithAllRelations(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> bookingService.getBookingById(999L))
                    .isInstanceOf(BookingNotFoundException.class);
        }
    }

    // ========== TESTS: getAllBookings ==========

    @Nested
    @DisplayName("getAllBookings")
    class GetAllBookingsTests {

        @Test
        @DisplayName("getAllBookings | Éxito | Debería retornar página de reservas")
        void getAllBookings_ShouldReturnPagedBookings() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Booking> bookingPage = new PageImpl<>(List.of(testBooking), pageable, 1);
            when(bookingRepository.findAllWithBasicInfo(pageable)).thenReturn(bookingPage);

            // When
            Page<BookingSummaryResponse> result = bookingService.getAllBookings(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("getAllBookings | Éxito | Debería retornar página vacía cuando no hay reservas")
        void getAllBookings_WhenEmpty_ShouldReturnEmptyPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Booking> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(bookingRepository.findAllWithBasicInfo(pageable)).thenReturn(emptyPage);

            // When
            Page<BookingSummaryResponse> result = bookingService.getAllBookings(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }
    }

    // ========== TESTS: updateBookingStatus ==========

    @Nested
    @DisplayName("updateBookingStatus")
    class UpdateBookingStatusTests {

        @Test
        @DisplayName("updateBookingStatus | Éxito | Debería cambiar estado de PENDING a CONFIRMED")
        void updateBookingStatus_FromPendingToConfirmed_ShouldSucceed() {
            // Given
            when(bookingRepository.findById(VALID_BOOKING_ID)).thenReturn(Optional.of(testBooking));
            when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

            // When
            BookingDetailResponse response = bookingService.updateBookingStatus(VALID_BOOKING_ID, "CONFIRMED", null);

            // Then
            assertThat(response).isNotNull();
            verify(bookingRepository).save(any(Booking.class));
            verify(notificationService).notifyStatusChange(any(Booking.class));
        }

        @Test
        @DisplayName("updateBookingStatus | Éxito | Debería cambiar estado a IN_PROGRESS y establecer tiempo inicio")
        void updateBookingStatus_FromConfirmedToInProgress_ShouldSetActualStartTime() {
            // Given
            testBooking.setStatus(BookingStatus.CONFIRMED);
            when(bookingRepository.findById(VALID_BOOKING_ID)).thenReturn(Optional.of(testBooking));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            BookingDetailResponse response = bookingService.updateBookingStatus(VALID_BOOKING_ID, "IN_PROGRESS", null);

            // Then
            assertThat(testBooking.getActualStartTime()).isNotNull();
        }

        @Test
        @DisplayName("updateBookingStatus | Éxito | Debería generar factura al completar reserva")
        void updateBookingStatus_ToCompleted_ShouldTriggerInvoiceGeneration() {
            // Given
            testBooking.setStatus(BookingStatus.IN_PROGRESS);
            when(bookingRepository.findById(VALID_BOOKING_ID)).thenReturn(Optional.of(testBooking));
            when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

            // When
            bookingService.updateBookingStatus(VALID_BOOKING_ID, "COMPLETED", null);

            // Then
            verify(invoiceService).generateAndProcessInvoiceForBooking(testBooking);
        }

        @Test
        @DisplayName("updateBookingStatus | Falla | Debería lanzar BookingStateException en transición inválida")
        void updateBookingStatus_WhenInvalidTransition_ShouldThrowBookingStateException() {
            // Given
            testBooking.setStatus(BookingStatus.COMPLETED);
            when(bookingRepository.findById(VALID_BOOKING_ID)).thenReturn(Optional.of(testBooking));

            // When/Then
            assertThatThrownBy(() -> bookingService.updateBookingStatus(VALID_BOOKING_ID, "PENDING", null))
                    .isInstanceOf(BookingStateException.class);
        }

        @Test
        @DisplayName("updateBookingStatus | Falla | Debería lanzar CancellationReasonRequiredException al cancelar sin motivo")
        void updateBookingStatus_WhenCancelWithoutReason_ShouldThrowException() {
            // Given
            when(bookingRepository.findById(VALID_BOOKING_ID)).thenReturn(Optional.of(testBooking));

            // When/Then
            assertThatThrownBy(() -> bookingService.updateBookingStatus(VALID_BOOKING_ID, "CANCELLED", null))
                    .isInstanceOf(CancellationReasonRequiredException.class);
        }
    }

    // ========== TESTS: updateBooking ==========

    @Nested
    @DisplayName("updateBooking")
    class UpdateBookingTests {

        @Test
        @DisplayName("updateBooking | Éxito | Debería actualizar campos modificables")
        void updateBooking_WithValidData_ShouldUpdateBooking() {
            // Given
            UpdateBookingRequest updateRequest = new UpdateBookingRequest();
            updateRequest.setNotes("Notas actualizadas");

            when(bookingRepository.findByIdWithAllRelations(VALID_BOOKING_ID)).thenReturn(Optional.of(testBooking));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            BookingDetailResponse response = bookingService.updateBooking(VALID_BOOKING_ID, updateRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(testBooking.getNotes()).isEqualTo("Notas actualizadas");
            verify(notificationService).notifyBookingUpdated(any(Booking.class));
        }

        @Test
        @DisplayName("updateBooking | Falla | Debería lanzar BookingStateException si la reserva está completada")
        void updateBooking_WhenCompleted_ShouldThrowBookingStateException() {
            // Given
            testBooking.setStatus(BookingStatus.COMPLETED);
            UpdateBookingRequest updateRequest = new UpdateBookingRequest();
            updateRequest.setNotes("Nueva nota");

            when(bookingRepository.findByIdWithAllRelations(VALID_BOOKING_ID)).thenReturn(Optional.of(testBooking));

            // When/Then
            assertThatThrownBy(() -> bookingService.updateBooking(VALID_BOOKING_ID, updateRequest))
                    .isInstanceOf(BookingStateException.class);
        }
    }

    // ========== TESTS: deleteBooking ==========

    @Nested
    @DisplayName("deleteBooking")
    class DeleteBookingTests {

        @Test
        @DisplayName("deleteBooking | Éxito | Debería eliminar reserva pendiente")
        void deleteBooking_WhenPending_ShouldDeleteSuccessfully() {
            // Given
            when(bookingRepository.findById(VALID_BOOKING_ID)).thenReturn(Optional.of(testBooking));

            // When
            bookingService.deleteBooking(VALID_BOOKING_ID);

            // Then
            verify(bookingRepository).delete(testBooking);
            verify(notificationService).notifyBookingCancelled(any(Booking.class));
        }

        @Test
        @DisplayName("deleteBooking | Falla | Debería lanzar BookingStateException si está en progreso")
        void deleteBooking_WhenInProgress_ShouldThrowBookingStateException() {
            // Given
            testBooking.setStatus(BookingStatus.IN_PROGRESS);
            when(bookingRepository.findById(VALID_BOOKING_ID)).thenReturn(Optional.of(testBooking));

            // When/Then
            assertThatThrownBy(() -> bookingService.deleteBooking(VALID_BOOKING_ID))
                    .isInstanceOf(BookingStateException.class);
        }
    }

    // ========== TESTS: getBookingsByUser ==========

    @Nested
    @DisplayName("getBookingsByUser")
    class GetBookingsByUserTests {

        @Test
        @DisplayName("getBookingsByUser | Éxito | Debería retornar reservas filtradas por cliente")
        void getBookingsByUser_WhenClient_ShouldReturnFilteredBookings() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Booking> bookingPage = new PageImpl<>(List.of(testBooking), pageable, 1);

            when(bookingRepository.findByBookedByUserId(1L, pageable)).thenReturn(bookingPage);

            // When
            Page<BookingSummaryResponse> result = bookingService.getBookingsByUser(1L, "CLIENT", null, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("getBookingsByUser | Éxito | Debería retornar reservas filtradas por cuidador")
        void getBookingsByUser_WhenSitter_ShouldReturnFilteredBookings() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Booking> bookingPage = new PageImpl<>(List.of(testBooking), pageable, 1);

            when(bookingRepository.findBySitterId(VALID_SITTER_ID, pageable)).thenReturn(bookingPage);

            // When
            Page<BookingSummaryResponse> result = bookingService.getBookingsByUser(VALID_SITTER_ID, "SITTER", null, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }
}
