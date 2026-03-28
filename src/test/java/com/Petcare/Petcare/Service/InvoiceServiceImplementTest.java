package com.Petcare.Petcare.Service;

import com.Petcare.Petcare.DTOs.Invoice.CreateInvoiceRequest;
import com.Petcare.Petcare.DTOs.Invoice.InvoiceDetailResponse;
import com.Petcare.Petcare.DTOs.Invoice.InvoiceSummaryResponse;
import com.Petcare.Petcare.DTOs.Invoice.UpdateInvoiceRequest;
import com.Petcare.Petcare.Exception.Business.*;
import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Booking.BookingStatus;
import com.Petcare.Petcare.Models.Invoice.Invoice;
import com.Petcare.Petcare.Models.Invoice.InvoiceStatus;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.BookingRepository;
import com.Petcare.Petcare.Repositories.InvoiceRepository;
import com.Petcare.Petcare.Services.Implement.InvoiceServiceImplement;
import com.Petcare.Petcare.Services.NotificationService;
import com.Petcare.Petcare.Services.PdfGenerationService;
import com.Petcare.Petcare.Services.PlatformFeeService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
 * @see InvoiceServiceImplement
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Pruebas Unitarias: InvoiceServiceImplement")
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
    
    private User testOwner;
    private Account testAccount;
    private Booking testBooking;
    private Invoice testInvoice;
    private CreateInvoiceRequest createInvoiceRequest;

    private static final Long VALID_BOOKING_ID = 1L;
    private static final Long VALID_INVOICE_ID = 1L;

    @BeforeEach
    void setUp() {
        // Usuario propietario
        testOwner = new User();
        testOwner.setId(1L);
        testOwner.setEmail("owner@test.com");
        testOwner.setFirstName("John");
        testOwner.setLastName("Doe");
        testOwner.setRole(Role.CLIENT);
        testOwner.setActive(true);

        // Cuenta
        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setOwnerUser(testOwner);
        testAccount.setAccountNumber("ACC-123");
        testAccount.setAccountName("Cuenta Test");
        testAccount.setActive(true);

        // Reserva completada
        testBooking = new Booking();
        testBooking.setId(VALID_BOOKING_ID);
        testBooking.setAccount(testAccount);
        testBooking.setStatus(BookingStatus.COMPLETED);
        testBooking.setTotalPrice(BigDecimal.valueOf(100.00));

        // Factura
        testInvoice = new Invoice();
        testInvoice.setId(VALID_INVOICE_ID);
        testInvoice.setBooking(testBooking);
        testInvoice.setAccount(testAccount);
        testInvoice.setInvoiceNumber("INV-2025-123456");
        testInvoice.setStatus(InvoiceStatus.SENT);
        testInvoice.setSubtotal(BigDecimal.valueOf(90.00));
        testInvoice.setPlatformFee(BigDecimal.valueOf(10.00));
        testInvoice.setTotalAmount(BigDecimal.valueOf(100.00));
        testInvoice.setIssueDate(LocalDateTime.now());
        testInvoice.setDueDate(LocalDateTime.now().plusDays(15));

        // Request de creación
        createInvoiceRequest = new CreateInvoiceRequest();
        createInvoiceRequest.setBookingId(VALID_BOOKING_ID);
        createInvoiceRequest.setAutoSendEmail(false);
    }

    // ========== TESTS: generateInvoiceForBooking ==========

    @Nested
    @DisplayName("generateInvoiceForBooking")
    class GenerateInvoiceForBookingTests {

        @Test
        @DisplayName("generateInvoiceForBooking | Éxito | Debería crear factura para reserva completada")
        void generateInvoiceForBooking_WithValidBooking_ShouldCreateInvoice() throws Exception {
            // Given
            when(bookingRepository.findById(VALID_BOOKING_ID)).thenReturn(Optional.of(testBooking));
            when(invoiceRepository.existsByBookingId(VALID_BOOKING_ID)).thenReturn(false);
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
                Invoice i = inv.getArgument(0);
                i.setId(VALID_INVOICE_ID);
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
            when(bookingRepository.findById(VALID_BOOKING_ID)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> invoiceService.generateInvoiceForBooking(createInvoiceRequest))
                    .isInstanceOf(BookingNotFoundException.class);
        }

        @Test
        @DisplayName("generateInvoiceForBooking | Falla | Debería lanzar BookingStateException si la reserva no está completada")
        void generateInvoiceForBooking_WhenBookingNotCompleted_ShouldThrowException() {
            // Given
            testBooking.setStatus(BookingStatus.PENDING);
            when(bookingRepository.findById(VALID_BOOKING_ID)).thenReturn(Optional.of(testBooking));

            // When/Then
            assertThatThrownBy(() -> invoiceService.generateInvoiceForBooking(createInvoiceRequest))
                    .isInstanceOf(BookingStateException.class);
        }

        @Test
        @DisplayName("generateInvoiceForBooking | Falla | Debería lanzar InvoiceAlreadyExistsException si ya existe factura")
        void generateInvoiceForBooking_WhenInvoiceAlreadyExists_ShouldThrowException() {
            // Given
            when(bookingRepository.findById(VALID_BOOKING_ID)).thenReturn(Optional.of(testBooking));
            when(invoiceRepository.existsByBookingId(VALID_BOOKING_ID)).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> invoiceService.generateInvoiceForBooking(createInvoiceRequest))
                    .isInstanceOf(InvoiceAlreadyExistsException.class);
        }
    }

    // ========== TESTS: getInvoiceById ==========

    @Nested
    @DisplayName("getInvoiceById")
    class GetInvoiceByIdTests {

        @Test
        @DisplayName("getInvoiceById | Éxito | Debería retornar detalles de factura")
        void getInvoiceById_WhenExists_ShouldReturnInvoiceDetails() {
            // Given
            when(invoiceRepository.findById(VALID_INVOICE_ID)).thenReturn(Optional.of(testInvoice));

            // When
            InvoiceDetailResponse response = invoiceService.getInvoiceById(VALID_INVOICE_ID);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(VALID_INVOICE_ID);
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
    }

    // ========== TESTS: sendInvoice ==========

    @Nested
    @DisplayName("sendInvoice")
    class SendInvoiceTests {

        @Test
        @DisplayName("sendInvoice | Éxito | Debería cambiar estado a SENT y procesar envío")
        void sendInvoice_WhenValidInvoice_ShouldSendAndUpdateStatus() throws Exception {
            // Given
            Invoice draftInvoice = new Invoice();
            draftInvoice.setId(VALID_INVOICE_ID);
            draftInvoice.setBooking(testBooking);
            draftInvoice.setAccount(testAccount);
            draftInvoice.setInvoiceNumber("INV-2025-123456");
            draftInvoice.setStatus(InvoiceStatus.DRAFT);
            draftInvoice.setSubtotal(BigDecimal.valueOf(90.00));
            draftInvoice.setPlatformFee(BigDecimal.valueOf(10.00));
            draftInvoice.setTotalAmount(BigDecimal.valueOf(100.00));
            
            when(invoiceRepository.findById(VALID_INVOICE_ID)).thenReturn(Optional.of(draftInvoice));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(draftInvoice);
            when(pdfGenerationService.generateInvoicePdf(any())).thenReturn(new byte[0]);

            // When
            InvoiceDetailResponse response = invoiceService.sendInvoice(VALID_INVOICE_ID);

            // Then
            assertThat(draftInvoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
        }

        @Test
        @DisplayName("sendInvoice | Falla | Debería lanzar InvoiceNotFoundException si no existe")
        void sendInvoice_WhenNotFound_ShouldThrowException() {
            // Given
            when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> invoiceService.sendInvoice(99L))
                    .isInstanceOf(InvoiceNotFoundException.class);
        }
    }

    // ========== TESTS: cancelInvoice ==========

    @Nested
    @DisplayName("cancelInvoice")
    class CancelInvoiceTests {

        @Test
        @DisplayName("cancelInvoice | Éxito | Debería cancelar factura con motivo")
        void cancelInvoice_WithValidReason_ShouldCancelInvoice() {
            // Given
            Invoice sentInvoice = new Invoice();
            sentInvoice.setId(VALID_INVOICE_ID);
            sentInvoice.setBooking(testBooking);
            sentInvoice.setAccount(testAccount);
            sentInvoice.setInvoiceNumber("INV-2025-123456");
            sentInvoice.setStatus(InvoiceStatus.SENT);
            
            when(invoiceRepository.findById(VALID_INVOICE_ID)).thenReturn(Optional.of(sentInvoice));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(sentInvoice);

            // When
            InvoiceDetailResponse response = invoiceService.cancelInvoice(VALID_INVOICE_ID, "Cliente solicitó cancelación");

            // Then
            assertThat(sentInvoice.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
            assertThat(sentInvoice.getNotes()).contains("Cliente solicitó cancelación");
        }

        @Test
        @DisplayName("cancelInvoice | Falla | Debería lanzar CancellationReasonRequiredException sin motivo")
        void cancelInvoice_WithoutReason_ShouldThrowException() {
            // Given
            Invoice sentInvoice = new Invoice();
            sentInvoice.setId(VALID_INVOICE_ID);
            sentInvoice.setStatus(InvoiceStatus.SENT);
            
            when(invoiceRepository.findById(VALID_INVOICE_ID)).thenReturn(Optional.of(sentInvoice));

            // When/Then
            assertThatThrownBy(() -> invoiceService.cancelInvoice(VALID_INVOICE_ID, null))
                    .isInstanceOf(CancellationReasonRequiredException.class);
        }
    }

    // ========== TESTS: getInvoicesByAccountId ==========

    @Nested
    @DisplayName("getInvoicesByAccountId")
    class GetInvoicesByAccountIdTests {

        @Test
        @DisplayName("getInvoicesByAccountId | Éxito | Debería retornar página de facturas")
        void getInvoicesByAccountId_ShouldReturnPagedInvoices() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Invoice> invoicePage = new PageImpl<>(List.of(testInvoice), pageable, 1);
            when(invoiceRepository.findByAccountId(1L, pageable)).thenReturn(invoicePage);

            // When
            Page<InvoiceSummaryResponse> result = invoiceService.getInvoicesByAccountId(1L, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }
}
