package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.DTOs.Booking.BookingDetailResponse;
import com.Petcare.Petcare.DTOs.Booking.CreateBookingRequest;
import com.Petcare.Petcare.Models.Booking.Booking;
import com.Petcare.Petcare.Models.Booking.BookingStatus;
import com.Petcare.Petcare.Models.Pet;
import com.Petcare.Petcare.Models.ServiceOffering.ServiceOffering;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.BookingRepository;
import com.Petcare.Petcare.Repositories.PetRepository;
import com.Petcare.Petcare.Repositories.ServiceOfferingRepository;
import com.Petcare.Petcare.Repositories.UserRepository;
import com.Petcare.Petcare.Services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Implementación del servicio de gestión de reservas.
 */
@Service
@RequiredArgsConstructor
public class BookingServiceImplement implements BookingService {

    private final BookingRepository bookingRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;

    @Override
    @Transactional // Es crucial que las operaciones de creación sean transaccionales
    public BookingDetailResponse createBooking(CreateBookingRequest createBookingRequest, User currentUser) {

        // 1. Validar la petición y obtener las entidades relacionadas
        Pet pet = petRepository.findById(createBookingRequest.getPetId())
                .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada con ID: " + createBookingRequest.getPetId()));

        User sitter = userRepository.findById(createBookingRequest.getSitterId())
                .orElseThrow(() -> new IllegalArgumentException("Cuidador no encontrado con ID: " + createBookingRequest.getSitterId()));

        ServiceOffering serviceOffering = serviceOfferingRepository.findById(createBookingRequest.getServiceOfferingId())
                .orElseThrow(() -> new IllegalArgumentException("Oferta de servicio no encontrada con ID: " + createBookingRequest.getServiceOfferingId()));

        // 2. Crear la entidad Booking y calcular campos
        Booking newBooking = new Booking();

        // Mapear datos desde el DTO y el currentUser
        newBooking.setPet(pet);
        newBooking.setSitter(sitter);
        newBooking.setServiceOffering(serviceOffering);
        newBooking.setBookedByUser(currentUser);
        newBooking.setNotes(createBookingRequest.getNotes());

        // Asignar el estado inicial
        newBooking.setStatus(BookingStatus.PENDING);

        // Calcular endTime y totalPrice
        LocalDateTime startTime = createBookingRequest.getStartTime();
        // Convertir Time a minutos
        long durationInMinutes = serviceOffering.getDurationInMinutes().getTime() / (60 * 1000);
        LocalDateTime endTime = startTime.plusMinutes(durationInMinutes);

        newBooking.setStartTime(startTime);
        newBooking.setEndTime(endTime);

        // Lógica de precio
        BigDecimal totalPrice = serviceOffering.getPrice();
        newBooking.setTotalPrice(totalPrice);

        // 3. Persistir la entidad
        Booking savedBooking = bookingRepository.save(newBooking);

        // 4. Devolver la respuesta en formato DTO
        return BookingDetailResponse.fromEntity(savedBooking);
    }
}