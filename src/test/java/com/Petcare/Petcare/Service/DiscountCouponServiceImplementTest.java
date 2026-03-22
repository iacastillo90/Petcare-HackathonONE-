package com.Petcare.Petcare.Service;

import com.Petcare.Petcare.Models.DiscountCoupon;
import com.Petcare.Petcare.Models.DiscountType;
import com.Petcare.Petcare.Repositories.DiscountCouponRepository;
import com.Petcare.Petcare.Services.Implement.DiscountCouponServiceImplement;
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
 * Suite de pruebas para {@link DiscountCouponServiceImplement}.
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 2025
 * @see DiscountCouponServiceImplement
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Pruebas Unitarias: DiscountCouponServiceImplement")
class DiscountCouponServiceImplementTest {

    @Mock private DiscountCouponRepository discountCouponRepository;

    @InjectMocks
    private DiscountCouponServiceImplement couponService;

    private DiscountCoupon testCoupon;

    private static final Long VALID_COUPON_ID = 1L;
    private static final String VALID_COUPON_CODE = "DESCUENTO20";

    @BeforeEach
    void setUp() {
        testCoupon = new DiscountCoupon();
        testCoupon.setId(VALID_COUPON_ID);
        testCoupon.setCouponCode(VALID_COUPON_CODE);
        testCoupon.setDiscountType(DiscountType.PERCENTAGE);
        testCoupon.setDiscountValue(BigDecimal.valueOf(20));
        testCoupon.setActive(true);
        testCoupon.setExpiryDate(LocalDateTime.now().plusDays(30));
        testCoupon.setMaxUses(100);
        testCoupon.setUsedCount(0);
    }

    // ========== TESTS: saveDiscountCoupon ==========

    @Nested
    @DisplayName("saveDiscountCoupon")
    class SaveDiscountCouponTests {

        @Test
        @DisplayName("saveDiscountCoupon | Éxito | Debería guardar cupón")
        void saveDiscountCoupon_ShouldSaveCoupon() {
            // Given
            when(discountCouponRepository.save(any(DiscountCoupon.class))).thenReturn(testCoupon);

            // When
            DiscountCoupon result = couponService.saveDiscountCoupon(testCoupon);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCouponCode()).isEqualTo(VALID_COUPON_CODE);
            verify(discountCouponRepository).save(testCoupon);
        }
    }

    // ========== TESTS: getDiscountCouponById ==========

    @Nested
    @DisplayName("getDiscountCouponById")
    class GetDiscountCouponByIdTests {

        @Test
        @DisplayName("getDiscountCouponById | Éxito | Debería retornar cupón por ID")
        void getDiscountCouponById_WhenExists_ShouldReturnCoupon() {
            // Given
            when(discountCouponRepository.findById(VALID_COUPON_ID)).thenReturn(Optional.of(testCoupon));

            // When
            Optional<DiscountCoupon> result = couponService.getDiscountCouponById(VALID_COUPON_ID);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getCouponCode()).isEqualTo(VALID_COUPON_CODE);
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
    }

    // ========== TESTS: getDiscountCouponByCode ==========

    @Nested
    @DisplayName("getDiscountCouponByCode")
    class GetDiscountCouponByCodeTests {

        @Test
        @DisplayName("getDiscountCouponByCode | Éxito | Debería retornar cupón por código")
        void getDiscountCouponByCode_WhenExists_ShouldReturnCoupon() {
            // Given
            when(discountCouponRepository.findByCouponCode(VALID_COUPON_CODE)).thenReturn(Optional.of(testCoupon));

            // When
            Optional<DiscountCoupon> result = couponService.getDiscountCouponByCode(VALID_COUPON_CODE);

            // Then
            assertThat(result).isPresent();
        }
    }

    // ========== TESTS: getAllDiscountCoupons ==========

    @Nested
    @DisplayName("getAllDiscountCoupons")
    class GetAllDiscountCouponsTests {

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
    }

    // ========== TESTS: deleteDiscountCoupon ==========

    @Nested
    @DisplayName("deleteDiscountCoupon")
    class DeleteDiscountCouponTests {

        @Test
        @DisplayName("deleteDiscountCoupon | Éxito | Debería eliminar cupón por ID")
        void deleteDiscountCoupon_ShouldDeleteCoupon() {
            // Given
            doNothing().when(discountCouponRepository).deleteById(VALID_COUPON_ID);

            // When
            couponService.deleteDiscountCoupon(VALID_COUPON_ID);

            // Then
            verify(discountCouponRepository).deleteById(VALID_COUPON_ID);
        }
    }
}
