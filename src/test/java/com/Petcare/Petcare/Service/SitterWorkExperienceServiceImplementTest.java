package com.Petcare.Petcare.Service;

import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceMapper;
import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceRequestDTO;
import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceResponseDTO;
import com.Petcare.Petcare.Exception.Business.SitterProfileNotFoundException;
import com.Petcare.Petcare.Exception.Business.WorkExperienceNotFoundException;
import com.Petcare.Petcare.Models.SitterProfile;
import com.Petcare.Petcare.Models.SitterWorkExperience;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.SitterProfileRepository;
import com.Petcare.Petcare.Repositories.SitterWorkExperienceRepository;
import com.Petcare.Petcare.Services.Implement.SitterWorkExperienceServiceImplement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias (refactorizadas) para la clase de servicio {@link SitterWorkExperienceServiceImplement}.
 *
 * <p>Esta versión de la suite de pruebas se ha adaptado para validar las mejoras introducidas
 * en el servicio, incluyendo el uso de excepciones de negocio específicas y, crucialmente,
 * la lógica de autorización a nivel de método. Ahora se simula el contexto de seguridad
 * de Spring para probar adecuadamente los permisos de acceso.</p>
 *
 * <p><strong>Nuevas Características de Prueba:</strong></p>
 * <ul>
 * <li><b>Simulación de Seguridad:</b> Se utiliza un mock de {@code SecurityContextHolder} para
 * simular diferentes usuarios autenticados (propietario, administrador, otro usuario).</li>
 * <li><b>Excepciones Específicas:</b> Las aserciones ahora verifican los nuevos tipos de
 * excepciones de negocio ({@code SitterProfileNotFoundException}, {@code WorkExperienceNotFoundException}).</li>
 * <li><b>Pruebas de Autorización:</b> Se han añadido tests explícitos para los métodos
 * {@code update} y {@code delete} que verifican que solo el propietario o un administrador
 * puedan ejecutar la acción, y que cualquier otro usuario reciba un {@code AccessDeniedException}.</li>
 * </ul>
 *
 * @see SitterWorkExperienceServiceImplement
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 2025-09-14
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para SitterWorkExperienceServiceImplement")
class SitterWorkExperienceServiceImplementTest {

    @Mock
    private SitterWorkExperienceRepository workExperienceRepository;

    @Mock
    private SitterProfileRepository sitterProfileRepository;

    // Mocks para simular el contexto de seguridad de Spring
    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SitterWorkExperienceServiceImplement sitterWorkExperienceService;

    // Constantes de prueba
    private static final Long OWNER_USER_ID = 1L;
    private static final Long ADMIN_USER_ID = 2L;
    private static final Long OTHER_USER_ID = 3L;
    private static final Long VALID_PROFILE_ID = 10L;
    private static final Long VALID_EXPERIENCE_ID = 100L;
    private static final Long NON_EXISTENT_ID = 999L;

    // Objetos de prueba reutilizables
    private User ownerUser, adminUser, otherUser;
    private SitterProfile testSitterProfile;
    private SitterWorkExperience testExperience;
    private SitterWorkExperienceRequestDTO testRequestDTO;

    /**
     * Prepara el entorno antes de cada test.
     * Inicializa usuarios con diferentes roles y configura el mock del contexto de seguridad.
     */
    @BeforeEach
    void setUp() {
        // Crear usuarios con diferentes roles para las pruebas de autorización
        ownerUser = new User();
        ownerUser.setId(OWNER_USER_ID);
        ownerUser.setRole(Role.SITTER);

        adminUser = new User();
        adminUser.setId(ADMIN_USER_ID);
        adminUser.setRole(Role.ADMIN);

        otherUser = new User();
        otherUser.setId(OTHER_USER_ID);
        otherUser.setRole(Role.SITTER);

        // Configurar el perfil de cuidador asociado al propietario
        testSitterProfile = new SitterProfile();
        testSitterProfile.setId(VALID_PROFILE_ID);
        testSitterProfile.setUser(ownerUser); // El dueño del perfil es ownerUser

        // Configurar el DTO de solicitud
        testRequestDTO = new SitterWorkExperienceRequestDTO();
        testRequestDTO.setSitterProfileId(VALID_PROFILE_ID);
        testRequestDTO.setCompanyName("Pet Care Excellence");
        testRequestDTO.setJobTitle("Senior Dog Walker");
        testRequestDTO.setStartDate(LocalDate.of(2021, 1, 15));

        // Configurar la entidad de experiencia
        testExperience = SitterWorkExperienceMapper.toEntity(testRequestDTO, testSitterProfile);
        testExperience.setId(VALID_EXPERIENCE_ID);

        // Configurar el mock del SecurityContextHolder para que devuelva nuestros mocks
        // Esto es crucial para que el método `validateOwnershipOrAdmin` funcione en los tests.
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Simula un usuario autenticado en el sistema.
     * @param user El usuario que se quiere simular como logueado.
     */
    private void mockAuthenticatedUser(User user) {
        // Se utiliza lenient() porque no todos los tests necesitan simular un usuario,
        // y Mockito se quejaría de stubs innecesarios sin él.
        lenient().when(authentication.getPrincipal()).thenReturn(user);
    }

    @Nested
    @DisplayName("Pruebas del método createWorkExperience()")
    class CreateWorkExperienceTests {

        @Test
        @DisplayName("Debería crear la experiencia si el perfil del cuidador existe")
        void shouldCreateExperience_whenProfileExists() {
            // Arrange
            when(sitterProfileRepository.findById(VALID_PROFILE_ID)).thenReturn(Optional.of(testSitterProfile));
            when(workExperienceRepository.save(any(SitterWorkExperience.class))).thenReturn(testExperience);

            // Act
            SitterWorkExperienceResponseDTO responseDTO = sitterWorkExperienceService.createWorkExperience(testRequestDTO);

            // Assert
            assertThat(responseDTO).isNotNull();
            assertThat(responseDTO.getId()).isEqualTo(VALID_EXPERIENCE_ID);
            assertThat(responseDTO.getCompanyName()).isEqualTo(testRequestDTO.getCompanyName());
            verify(sitterProfileRepository, times(1)).findById(VALID_PROFILE_ID);
            verify(workExperienceRepository, times(1)).save(any(SitterWorkExperience.class));
        }

        @Test
        @DisplayName("Debería lanzar SitterProfileNotFoundException si el perfil del cuidador no existe")
        void shouldThrowException_whenSitterProfileNotFound() {
            // Arrange
            testRequestDTO.setSitterProfileId(NON_EXISTENT_ID);
            when(sitterProfileRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> sitterWorkExperienceService.createWorkExperience(testRequestDTO))
                    .isInstanceOf(SitterProfileNotFoundException.class)
                    .hasMessageContaining("No se encontró un perfil de cuidador para el ID: " + NON_EXISTENT_ID);

            verify(workExperienceRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Pruebas del método updateWorkExperience()")
    class UpdateWorkExperienceTests {

        @Test
        @DisplayName("Debería permitir la actualización si el usuario es el PROPIETARIO del perfil")
        void shouldAllowUpdate_whenUserIsOwner() {
            // Arrange
            mockAuthenticatedUser(ownerUser); // Simular que el propietario está logueado
            when(workExperienceRepository.findById(VALID_EXPERIENCE_ID)).thenReturn(Optional.of(testExperience));
            when(workExperienceRepository.save(any(SitterWorkExperience.class))).thenReturn(testExperience);

            // Act
            SitterWorkExperienceResponseDTO result = sitterWorkExperienceService.updateWorkExperience(VALID_EXPERIENCE_ID, testRequestDTO);

            // Assert
            assertThat(result).isNotNull();
            verify(workExperienceRepository, times(1)).save(testExperience);
        }

        @Test
        @DisplayName("Debería permitir la actualización si el usuario es ADMIN")
        void shouldAllowUpdate_whenUserIsAdmin() {
            // Arrange
            mockAuthenticatedUser(adminUser); // Simular que un admin está logueado
            when(workExperienceRepository.findById(VALID_EXPERIENCE_ID)).thenReturn(Optional.of(testExperience));
            when(workExperienceRepository.save(any(SitterWorkExperience.class))).thenReturn(testExperience);

            // Act
            SitterWorkExperienceResponseDTO result = sitterWorkExperienceService.updateWorkExperience(VALID_EXPERIENCE_ID, testRequestDTO);

            // Assert
            assertThat(result).isNotNull();
            verify(workExperienceRepository, times(1)).save(testExperience);
        }

        @Test
        @DisplayName("Debería lanzar AccessDeniedException si el usuario NO es propietario NI admin")
        void shouldDenyUpdate_whenUserIsNotOwnerOrAdmin() {
            // Arrange
            mockAuthenticatedUser(otherUser); // Simular que otro usuario está logueado
            when(workExperienceRepository.findById(VALID_EXPERIENCE_ID)).thenReturn(Optional.of(testExperience));

            // Act & Assert
            assertThatThrownBy(() -> sitterWorkExperienceService.updateWorkExperience(VALID_EXPERIENCE_ID, testRequestDTO))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("No tiene permisos para modificar la experiencia laboral de otro cuidador.");

            verify(workExperienceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería lanzar WorkExperienceNotFoundException al actualizar si la experiencia no existe")
        void shouldThrowExceptionOnUpdate_whenExperienceNotFound() {
            // Arrange
            mockAuthenticatedUser(adminUser); // Simular un admin para pasar la validación de permisos
            when(workExperienceRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> sitterWorkExperienceService.updateWorkExperience(NON_EXISTENT_ID, testRequestDTO))
                    .isInstanceOf(WorkExperienceNotFoundException.class)
                    .hasMessageContaining("Experiencia laboral no encontrada con el ID: " + NON_EXISTENT_ID);

            verify(workExperienceRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Pruebas del método deleteWorkExperience()")
    class DeleteWorkExperienceTests {

        @Test
        @DisplayName("Debería permitir la eliminación si el usuario es el PROPIETARIO del perfil")
        void shouldAllowDelete_whenUserIsOwner() {
            // Arrange
            mockAuthenticatedUser(ownerUser);
            when(workExperienceRepository.findById(VALID_EXPERIENCE_ID)).thenReturn(Optional.of(testExperience));
            doNothing().when(workExperienceRepository).delete(testExperience);

            // Act
            sitterWorkExperienceService.deleteWorkExperience(VALID_EXPERIENCE_ID);

            // Assert
            verify(workExperienceRepository, times(1)).delete(testExperience);
        }

        @Test
        @DisplayName("Debería permitir la eliminación si el usuario es ADMIN")
        void shouldAllowDelete_whenUserIsAdmin() {
            // Arrange
            mockAuthenticatedUser(adminUser);
            when(workExperienceRepository.findById(VALID_EXPERIENCE_ID)).thenReturn(Optional.of(testExperience));
            doNothing().when(workExperienceRepository).delete(testExperience);

            // Act
            sitterWorkExperienceService.deleteWorkExperience(VALID_EXPERIENCE_ID);

            // Assert
            verify(workExperienceRepository, times(1)).delete(testExperience);
        }

        @Test
        @DisplayName("Debería lanzar AccessDeniedException si el usuario NO es propietario NI admin")
        void shouldDenyDelete_whenUserIsNotOwnerOrAdmin() {
            // Arrange
            mockAuthenticatedUser(otherUser);
            when(workExperienceRepository.findById(VALID_EXPERIENCE_ID)).thenReturn(Optional.of(testExperience));

            // Act & Assert
            assertThatThrownBy(() -> sitterWorkExperienceService.deleteWorkExperience(VALID_EXPERIENCE_ID))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("No tiene permisos para modificar la experiencia laboral de otro cuidador.");

            verify(workExperienceRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Debería lanzar WorkExperienceNotFoundException al eliminar si la experiencia no existe")
        void shouldThrowExceptionOnDelete_whenExperienceNotFound() {
            // Arrange
            mockAuthenticatedUser(adminUser);
            when(workExperienceRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> sitterWorkExperienceService.deleteWorkExperience(NON_EXISTENT_ID))
                    .isInstanceOf(WorkExperienceNotFoundException.class)
                    .hasMessageContaining("Experiencia laboral no encontrada con el ID: " + NON_EXISTENT_ID);

            verify(workExperienceRepository, never()).delete(any());
        }
    }
}