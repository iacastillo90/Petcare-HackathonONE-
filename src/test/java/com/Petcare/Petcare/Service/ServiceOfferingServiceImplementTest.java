package com.Petcare.Petcare.Service;

import com.Petcare.Petcare.DTOs.ServiceOffering.CreateServiceOfferingDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.ServiceOfferingDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.UpdateServiceOfferingDTO;
import com.Petcare.Petcare.Exception.Business.ServiceOfferingConflictException;
import com.Petcare.Petcare.Exception.Business.ServiceOfferingNotFoundException;
import com.Petcare.Petcare.Exception.Business.UserNotFoundException;
import com.Petcare.Petcare.Models.ServiceOffering.ServiceOffering;
import com.Petcare.Petcare.Models.ServiceOffering.ServiceType;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.ServiceOfferingRepository;
import com.Petcare.Petcare.Repositories.UserRepository;
import com.Petcare.Petcare.Services.Implement.ServiceOfferingServiceImplement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la clase de servicio {@link ServiceOfferingServiceImplement}.
 *
 * <p>Esta suite de pruebas se enfoca en validar la lógica de negocio de la capa de servicio
 * de forma aislada. Para lograrlo, se utiliza Mockito para simular (mockear) las dependencias
 * externas, como los repositorios, asegurando que solo se prueba la lógica interna del servicio.</p>
 *
 * <p><strong>Filosofía de testing:</strong></p>
 * <ul>
 * <li><b>Aislamiento:</b> Cada prueba verifica una única ruta de ejecución (un "camino feliz" o un caso de error).</li>
 * <li><b>Simulación (Mocking):</b> Las dependencias ({@link UserRepository}, {@link ServiceOfferingRepository})
 * son simuladas para controlar su comportamiento y evitar interacciones con la base de datos real.</li>
 * <li><b>Verificación de Interacciones:</b> Se utiliza {@code verify()} de Mockito para asegurar que el servicio
 * llama a los métodos de sus dependencias de la manera esperada.</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.2
 * @since 1.0
 * @see ServiceOfferingServiceImplement
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias: ServiceOfferingServiceImplement")
class ServiceOfferingServiceImplementTest {

    @Mock
    private ServiceOfferingRepository serviceOfferingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ServiceOfferingServiceImplement service;

    private User sitter;
    private ServiceOffering serviceOffering;

    /**
     * Prepara objetos comunes (mocks y entidades) que se utilizarán en múltiples pruebas.
     * Este método se ejecuta antes de cada prueba individual.
     */
    @BeforeEach
    void setUp() {
        sitter = new User();
        sitter.setId(1L);
        sitter.setRole(Role.SITTER);

        serviceOffering = new ServiceOffering();
        serviceOffering.setId(10L);
        serviceOffering.setSitterId(sitter.getId());
        serviceOffering.setName("Paseo Básico");
        serviceOffering.setActive(true);
    }

    @Nested
    @DisplayName("Método: getAllServices")
    class GetAllServicesTests {
        @Test
        @DisplayName("✅ Éxito: Debería devolver una lista de DTOs cuando hay servicios activos")
        void shouldReturnDtoList_whenActiveServicesExist() {
            // Given
            when(serviceOfferingRepository.findAllByIsActiveTrue()).thenReturn(List.of(serviceOffering));

            // When
            List<ServiceOfferingDTO> result = service.getAllServices();

            // Then
            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(serviceOffering.getId());
            verify(serviceOfferingRepository).findAllByIsActiveTrue();
        }

        @Test
        @DisplayName("✅ Éxito: Debería devolver una lista vacía cuando no hay servicios activos")
        void shouldReturnEmptyList_whenNoActiveServicesExist() {
            // Given
            when(serviceOfferingRepository.findAllByIsActiveTrue()).thenReturn(Collections.emptyList());

            // When
            List<ServiceOfferingDTO> result = service.getAllServices();

            // Then
            assertThat(result).isNotNull().isEmpty();
            verify(serviceOfferingRepository).findAllByIsActiveTrue();
        }
    }

    @Nested
    @DisplayName("Método: createServiceOffering")
    class CreateServiceOfferingTests {
        @Test
        @DisplayName("✅ Éxito: Debería crear un servicio cuando los datos son válidos")
        void shouldCreateService_whenDataIsValid() {
            // Given
            CreateServiceOfferingDTO createDTO = new CreateServiceOfferingDTO(ServiceType.WALKING, "Nuevo Paseo", "Descripción válida", new BigDecimal("10"), 30);
            when(userRepository.findById(sitter.getId())).thenReturn(Optional.of(sitter));
            when(serviceOfferingRepository.existsBySitterIdAndName(sitter.getId(), createDTO.name())).thenReturn(false);
            when(serviceOfferingRepository.save(any(ServiceOffering.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ServiceOfferingDTO result = service.createServiceOffering(createDTO, sitter.getId());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo(createDTO.name());
            verify(userRepository).findById(sitter.getId());
            verify(serviceOfferingRepository).existsBySitterIdAndName(sitter.getId(), createDTO.name());
            verify(serviceOfferingRepository).save(any(ServiceOffering.class));
        }

        @Test
        @DisplayName("🚫 Falla: Debería lanzar UserNotFoundException si el cuidador no existe")
        void shouldThrowUserNotFound_whenSitterDoesNotExist() {
            // Given
            CreateServiceOfferingDTO createDTO = new CreateServiceOfferingDTO(ServiceType.WALKING, "Nuevo Paseo", "Descripción válida", new BigDecimal("10"), 30);
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(UserNotFoundException.class, () -> service.createServiceOffering(createDTO, 99L));
            verify(serviceOfferingRepository, never()).save(any());
        }

        @Test
        @DisplayName("🚫 Falla: Debería lanzar ServiceOfferingConflictException si el nombre ya existe para el cuidador")
        void shouldThrowConflict_whenServiceNameExistsForSitter() {
            // Given
            CreateServiceOfferingDTO createDTO = new CreateServiceOfferingDTO(ServiceType.WALKING, "Nombre Repetido", "Descripción válida", new BigDecimal("10"), 30);
            when(userRepository.findById(sitter.getId())).thenReturn(Optional.of(sitter));
            when(serviceOfferingRepository.existsBySitterIdAndName(sitter.getId(), "Nombre Repetido")).thenReturn(true);

            // When & Then
            assertThrows(ServiceOfferingConflictException.class, () -> service.createServiceOffering(createDTO, sitter.getId()));
            verify(serviceOfferingRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Método: updateServiceOffering")
    class UpdateServiceTests {
        private UpdateServiceOfferingDTO updateDTO;

        @BeforeEach
        void setupUpdate() {
            updateDTO = new UpdateServiceOfferingDTO(ServiceType.DAYCARE, "Nombre Actualizado", "Desc Actualizada", new BigDecimal("50.00"), 120, true);
        }

        @Test
        @DisplayName("✅ Éxito: Debería actualizar el servicio si el usuario es el propietario")
        void shouldUpdateService_whenUserIsOwner() {
            // Given
            mockSecurityContext(sitter);
            when(serviceOfferingRepository.findById(serviceOffering.getId())).thenReturn(Optional.of(serviceOffering));
            when(serviceOfferingRepository.save(any(ServiceOffering.class))).thenReturn(serviceOffering);

            // When
            ServiceOfferingDTO result = service.updateServiceOffering(serviceOffering.getId(), updateDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Nombre Actualizado");
            verify(serviceOfferingRepository).findById(serviceOffering.getId());
            verify(serviceOfferingRepository).save(any(ServiceOffering.class));
        }

        @Test
        @DisplayName("🚫 Falla: Debería lanzar AccessDeniedException si el usuario no es propietario ni admin")
        void shouldThrowAccessDenied_whenUserIsNotOwnerOrAdmin() {
            // Given
            User anotherUser = new User();
            anotherUser.setId(2L);
            // CORRECCIÓN: Asignar un rol al usuario para evitar NullPointerException.
            anotherUser.setRole(Role.CLIENT);

            mockSecurityContext(anotherUser);
            when(serviceOfferingRepository.findById(serviceOffering.getId())).thenReturn(Optional.of(serviceOffering));

            // When & Then
            assertThrows(AccessDeniedException.class, () -> service.updateServiceOffering(serviceOffering.getId(), updateDTO));
            verify(serviceOfferingRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Método: deleteServiceOffering")
    class DeleteServiceTests {
        @Test
        @DisplayName("✅ Éxito: Debería desactivar el servicio si el usuario es el propietario")
        void shouldDeactivateService_whenUserIsOwner() {
            // Given
            mockSecurityContext(sitter);
            when(serviceOfferingRepository.findById(serviceOffering.getId())).thenReturn(Optional.of(serviceOffering));

            // When
            service.deleteServiceOffering(serviceOffering.getId());

            // Then
            verify(serviceOfferingRepository).findById(serviceOffering.getId());
            verify(serviceOfferingRepository).save(serviceOffering);
            assertThat(serviceOffering.isActive()).isFalse();
        }

        @Test
        @DisplayName("🚫 Falla: Debería lanzar ServiceOfferingNotFoundException si el servicio no existe")
        void shouldThrowNotFound_whenDeletingNonExistentService() {
            // Given
            when(serviceOfferingRepository.findById(99L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ServiceOfferingNotFoundException.class, () -> service.deleteServiceOffering(99L));
            verify(serviceOfferingRepository, never()).save(any());
        }
    }

    /**
     * Método de ayuda para simular el contexto de seguridad de Spring.
     * Esto es crucial para probar métodos que dependen del usuario autenticado.
     *
     * @param user El usuario que se simulará como autenticado.
     */
    private void mockSecurityContext(User user) {
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}