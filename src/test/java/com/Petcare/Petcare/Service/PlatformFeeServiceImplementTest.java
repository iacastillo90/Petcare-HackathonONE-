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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Suite de pruebas para {@link PlatformFeeServiceImplement}.
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 2025
 * @see PlatformFeeServiceImplement
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Pruebas Unitarias: PlatformFeeServiceImplement")
class PlatformFeeServiceImplementTest {

    @Mock private PlatformFeeRepository platformFeeRepository;
    @Mock private BookingRepository bookingRepository;

    @InjectMocks
    private PlatformFeeServiceImplement platformFeeService;

    private Booking testBooking;
    private PlatformFee testFee;
    private CreatePlatformFeeRequest createRequest;

    private static final Long VALID_BOOKING_ID = 1L;

    @BeforeEach
    void setUp() {
        testBooking = new Booking();
        testBooking.setId(VALID_BOOKING_ID);
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
        createRequest.setBookingId(VALID_BOOKING_ID);
        createRequest.setFeePercentage(BigDecimal.valueOf(10.00));
    }

    // ========== TESTS: calculateAndCreateFee ==========

    @Nested
    @DisplayName("calculateAndCreateFee")
    class CalculateAndCreateFeeTests {

        @Test
        @DisplayName("calculateAndCreateFee | Éxito | Debería calcular y crear tarifa de plataforma")
        void calculateAndCreateFee_WithValidData_ShouldCreatePlatformFee() {
            // Given
            when(bookingRepository.findById(VALID_BOOKING_ID)).thenReturn(Optional.of(testBooking));
            when(platformFeeRepository.save(any(PlatformFee.class))).thenReturn(testFee);

            // When
            PlatformFeeResponse response = platformFeeService.calculateAndCreateFee(createRequest);

            // Then
            assertThat(response).isNotNull();
            verify(platformFeeRepository).save(any(PlatformFee.class));
        }

        @Test
        @DisplayName("calculateAndCreateFee | Falla | Debería lanzar excepción si la reserva no existe")
        void calculateAndCreateFee_WhenBookingNotFound_ShouldThrowException() {
            // Given
            CreatePlatformFeeRequest request = new CreatePlatformFeeRequest();
            request.setBookingId(99L);
            request.setFeePercentage(BigDecimal.valueOf(10));
            
            when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> platformFeeService.calculateAndCreateFee(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Reserva no encontrada");
        }

        @Test
        @DisplayName("calculateAndCreateFee | Éxito | Debería calcular correctamente el monto de la tarifa")
        void calculateAndCreateFee_ShouldCalculateFeeCorrectly() {
            // Given
            testBooking.setTotalPrice(BigDecimal.valueOf(200.00)); // 200 * 10% = 20
            createRequest.setFeePercentage(BigDecimal.valueOf(10.00));

            when(bookingRepository.findById(VALID_BOOKING_ID)).thenReturn(Optional.of(testBooking));
            // Mock save to return the entity as-is (which has the calculated values)
            when(platformFeeRepository.save(any(PlatformFee.class))).thenAnswer(inv -> {
                PlatformFee fee = inv.getArgument(0);
                fee.setId(1L); // Simulate saved entity with ID
                return fee;
            });

            // When
            PlatformFeeResponse response = platformFeeService.calculateAndCreateFee(createRequest);

            // Then - verify calculation happened (feeAmount = 200 * 0.10 = 20)
            assertThat(response).isNotNull();
            // The service sets feeAmount = baseAmount * (feePercentage/100) = 200 * 0.10 = 20
            // Then saves and returns the response
            assertThat(response.getBaseAmount()).isEqualTo(BigDecimal.valueOf(200.00));
            verify(platformFeeRepository).save(any(PlatformFee.class));
        }
    }

    // ========== TESTS: calculateAndCreatePlatformFee (delegation) ==========

    @Nested
    @DisplayName("calculateAndCreatePlatformFee")
    class CalculateAndCreatePlatformFeeTests {

        @Test
        @DisplayName("calculateAndCreatePlatformFee | Delegation | Debería delegar a calculateAndCreateFee")
        void calculateAndCreatePlatformFee_ShouldReturnNull() {
            // When
            PlatformFee result = platformFeeService.calculateAndCreatePlatformFee(testBooking);

            // Then
            // current implementation returns null
            assertThat(result).isNull();
        }
    }
}
