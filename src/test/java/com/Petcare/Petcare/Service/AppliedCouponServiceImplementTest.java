package com.Petcare.Petcare.Service;

import com.Petcare.Petcare.Exception.Business.BookingNotFoundException;
import com.Petcare.Petcare.Exception.Business.CouponExpiredException;
import com.Petcare.Petcare.Exception.Business.CouponNotFoundException;
import com.Petcare.Petcare.Models.AppliedCoupon;
import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Booking.BookingStatus;
import com.Petcare.Petcare.Models.DiscountCoupon;
import com.Petcare.Petcare.Models.DiscountType;
import com.Petcare.Petcare.Repositories.AppliedCouponRepository;
import com.Petcare.Petcare.Repositories.BookingRepository;
import com.Petcare.Petcare.Repositories.DiscountCouponRepository;
import com.Petcare.Petcare.Services.Implement.AppliedCouponServiceImplement;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Suite de pruebas para {@link AppliedCouponServiceImplement}.
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 2025
 * @see AppliedCouponServiceImplement
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Pruebas Unitarias: AppliedCouponServiceImplement")
class AppliedCouponServiceImplementTest {

    @Mock private AppliedCouponRepository appliedCouponRepository;
    @Mock private DiscountCouponRepository discountCouponRepository;
    @Mock private BookingRepository bookingRepository;

    @InjectMocks
    private AppliedCouponServiceImplement appliedCouponService;

    private Booking testBooking;
    private DiscountCoupon testCoupon;
    private AppliedCoupon testAppliedCoupon;

    private static final Long VALID_BOOKING_ID = 1L;
    private static final Long VALID_ACCOUNT_ID = 1L;
    private static final String VALID_COUPON_CODE = "SAVE20";

    @BeforeEach
    void setUp() {
        testBooking = new Booking();
        testBooking.setId(VALID_BOOKING_ID);
        testBooking.setTotalPrice(BigDecimal.valueOf(100.00));
        testBooking.setStatus(BookingStatus.PENDING);

        testCoupon = new DiscountCoupon();
        testCoupon.setId(1L);
        testCoupon.setCouponCode(VALID_COUPON_CODE);
        testCoupon.setDiscountType(DiscountType.PERCENTAGE);
        testCoupon.setDiscountValue(BigDecimal.valueOf(20));
        testCoupon.setActive(true);
        testCoupon.setExpiryDate(LocalDateTime.now().plusDays(30));

        testAppliedCoupon = new AppliedCoupon();
        testAppliedCoupon.setId(1L);
        testAppliedCoupon.setBookingId(VALID_BOOKING_ID);
        testAppliedCoupon.setAccountId(VALID_ACCOUNT_ID);
        testAppliedCoupon.setCoupon(testCoupon);
        testAppliedCoupon.setDiscountAmount(BigDecimal.valueOf(20.00));
        testAppliedCoupon.setAppliedAt(LocalDateTime.now());
    }

    // ========== TESTS: applyCoupon ==========

    @Nested
    @DisplayName("applyCoupon")
    class ApplyCouponTests {

        @Test
        @DisplayName("applyCoupon | Éxito | Debería aplicar cupón de porcentaje")
        void applyCoupon_WithPercentageCoupon_ShouldApplyDiscount() {
            // Given
            when(discountCouponRepository.findByCouponCode(VALID_COUPON_CODE)).thenReturn(Optional.of(testCoupon));
            when(bookingRepository.findById(VALID_BOOKING_ID)).thenReturn(Optional.of(testBooking));
            when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
            when(appliedCouponRepository.save(any(AppliedCoupon.class))).thenReturn(testAppliedCoupon);

            // When
            AppliedCoupon result = appliedCouponService.applyCoupon(VALID_BOOKING_ID, VALID_ACCOUNT_ID, VALID_COUPON_CODE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDiscountAmount()).isEqualTo(BigDecimal.valueOf(20.00));
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("applyCoupon | Éxito | Debería aplicar cupón de monto fijo")
        void applyCoupon_WithFixedAmountCoupon_ShouldApplyDiscount() {
            // Given
            testCoupon.setDiscountType(DiscountType.FIXED);
            testCoupon.setDiscountValue(BigDecimal.valueOf(15));

            when(discountCouponRepository.findByCouponCode(VALID_COUPON_CODE)).thenReturn(Optional.of(testCoupon));
            when(bookingRepository.findById(VALID_BOOKING_ID)).thenReturn(Optional.of(testBooking));
            when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
            when(appliedCouponRepository.save(any(AppliedCoupon.class))).thenAnswer(inv -> {
                AppliedCoupon ac = inv.getArgument(0);
                ac.setId(1L);
                return ac;
            });

            // When
            AppliedCoupon result = appliedCouponService.applyCoupon(VALID_BOOKING_ID, VALID_ACCOUNT_ID, VALID_COUPON_CODE);

            // Then
            assertThat(result).isNotNull();
            // For FIXED type, the discount should be 15 (the fixed value)
            assertThat(result.getDiscountAmount().compareTo(BigDecimal.valueOf(15))).isZero();
        }

        @Test
        @DisplayName("applyCoupon | Falla | Debería lanzar CouponNotFoundException si el cupón no existe")
        void applyCoupon_WhenCouponNotFound_ShouldThrowCouponNotFoundException() {
            // Given
            when(discountCouponRepository.findByCouponCode("INVALID")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> appliedCouponService.applyCoupon(VALID_BOOKING_ID, VALID_ACCOUNT_ID, "INVALID"))
                    .isInstanceOf(CouponNotFoundException.class);
        }

        @Test
        @DisplayName("applyCoupon | Falla | Debería lanzar CouponExpiredException si el cupón está vencido")
        void applyCoupon_WhenCouponExpired_ShouldThrowCouponExpiredException() {
            // Given
            testCoupon.setExpiryDate(LocalDateTime.now().minusDays(1));

            when(discountCouponRepository.findByCouponCode(VALID_COUPON_CODE)).thenReturn(Optional.of(testCoupon));

            // When/Then
            assertThatThrownBy(() -> appliedCouponService.applyCoupon(VALID_BOOKING_ID, VALID_ACCOUNT_ID, VALID_COUPON_CODE))
                    .isInstanceOf(CouponExpiredException.class);
        }

        @Test
        @DisplayName("applyCoupon | Falla | Debería lanzar CouponExpiredException si el cupón está inactivo")
        void applyCoupon_WhenCouponInactive_ShouldThrowCouponExpiredException() {
            // Given
            testCoupon.setActive(false);

            when(discountCouponRepository.findByCouponCode(VALID_COUPON_CODE)).thenReturn(Optional.of(testCoupon));

            // When/Then
            assertThatThrownBy(() -> appliedCouponService.applyCoupon(VALID_BOOKING_ID, VALID_ACCOUNT_ID, VALID_COUPON_CODE))
                    .isInstanceOf(CouponExpiredException.class);
        }
    }

    // ========== TESTS: getCouponsByAccount ==========

    @Nested
    @DisplayName("getCouponsByAccount")
    class GetCouponsByAccountTests {

        @Test
        @DisplayName("getCouponsByAccount | Éxito | Debería retornar cupones aplicados por cuenta")
        void getCouponsByAccount_ShouldReturnAppliedCoupons() {
            // Given
            when(appliedCouponRepository.findByAccountId(VALID_ACCOUNT_ID)).thenReturn(List.of(testAppliedCoupon));

            // When
            List<AppliedCoupon> result = appliedCouponService.getCouponsByAccount(VALID_ACCOUNT_ID);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    // ========== TESTS: getCouponsByBooking ==========

    @Nested
    @DisplayName("getCouponsByBooking")
    class GetCouponsByBookingTests {

        @Test
        @DisplayName("getCouponsByBooking | Éxito | Debería retornar cupones aplicados a reserva")
        void getCouponsByBooking_ShouldReturnAppliedCoupons() {
            // Given
            when(appliedCouponRepository.findByBookingId(VALID_BOOKING_ID)).thenReturn(List.of(testAppliedCoupon));

            // When
            List<AppliedCoupon> result = appliedCouponService.getCouponsByBooking(VALID_BOOKING_ID);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    // ========== TESTS: validateCoupon ==========

    @Nested
    @DisplayName("validateCoupon")
    class ValidateCouponTests {

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
}
