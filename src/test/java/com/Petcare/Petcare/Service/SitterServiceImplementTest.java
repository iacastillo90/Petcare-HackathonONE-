package com.Petcare.Petcare.Service;

import com.Petcare.Petcare.DTOs.Sitter.SitterProfileDTO;
import com.Petcare.Petcare.DTOs.Sitter.SitterProfileMapper;
import com.Petcare.Petcare.DTOs.Sitter.SitterProfileSummary;
import com.Petcare.Petcare.Exception.Business.SitterProfileAlreadyExistsException;
import com.Petcare.Petcare.Exception.Business.SitterProfileNotFoundException;
import com.Petcare.Petcare.Models.SitterProfile;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.SitterProfileRepository;
import com.Petcare.Petcare.Repositories.UserRepository;
import com.Petcare.Petcare.Services.Implement.SitterServiceImplement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias exhaustivas para {@link SitterServiceImplement}.
 *
 * <p>Esta suite de pruebas se enfoca exclusivamente en la lógica de negocio de la capa de servicio,
 * utilizando mocks para todas las dependencias externas (repositorios, mappers). Se validan
 * todos los escenarios de éxito, fallo y casos borde para cada método público del servicio.</p>
 *
 * <p><strong>Estructura de Pruebas:</strong></p>
 * <ul>
 * <li>Cada método público del servicio tiene su propio {@code @Nested} class</li>
 * <li>Se prueban tanto los casos de éxito como los casos de fallo</li>
 * <li>Se verifican las interacciones con los repositorios usando {@code verify()}</li>
 * <li>Se utilizan nombres descriptivos que expresan claramente la intención</li>
 * </ul>
 *
 * <p><strong>Patrones Aplicados:</strong></p>
 * <ul>
 * <li>Patrón AAA (Arrange-Act-Assert) en cada test</li>
 * <li>Given-When-Then expresado en los nombres de los métodos</li>
 * <li>Isolación completa mediante mocks</li>
 * <li>Verificación de comportamiento además de estado</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see SitterServiceImplement
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias: SitterServiceImplement")
class SitterServiceImplementTest {

    // === Dependencias Mockeadas ===
    @Mock
    private UserRepository userRepository;

    @Mock
    private SitterProfileRepository sitterProfileRepository;

    @Mock
    private SitterProfileMapper sitterProfileMapper;

    // === Sistema Bajo Prueba ===
    @InjectMocks
    private SitterServiceImplement sitterService;

    // === Datos de Prueba Constantes ===
    private static final Long VALID_USER_ID = 1L;
    private static final Long NONEXISTENT_USER_ID = 999L;
    private static final String VALID_BIO = "Experiencia cuidando mascotas por 5 años";
    private static final BigDecimal VALID_HOURLY_RATE = new BigDecimal("25.50");
    private static final BigDecimal VALID_AVERAGE_RATING = new BigDecimal("4.8");
    private static final boolean VALID_AVAILABLE = true;
    private static final Integer VALID_RADIUS = 10;
    private static final String VALID_IMAGE_URL = "https://example.com/profile.jpg";
    private static final String VALID_CITY = "Santiago";

    // === Objetos de Prueba Reutilizables ===
    private User testUser;
    private SitterProfile testSitterProfile;
    private SitterProfileDTO testSitterProfileDTO;

    /**
     * Configuración inicial ejecutada antes de cada test.
     * Inicializa los objetos de prueba con datos válidos y consistentes.
     */
    @BeforeEach
    void setUp() {
        // Arrange - Configuración de objetos de prueba
        testUser = createValidUser();
        testSitterProfile = createValidSitterProfile();
        testSitterProfileDTO = createValidSitterProfileDTO();
    }

    /**
     * Pruebas para el método {@code createSitterProfile}.
     */
    @Nested
    @DisplayName("Pruebas para createSitterProfile()")
    class CreateSitterProfileTests {

        /**
         * Prueba el escenario de éxito donde se crea un perfil para un usuario válido.
         *
         * <p><strong>Escenario:</strong> Usuario existe, no tiene perfil previo, datos válidos</p>
         * <p><strong>Resultado esperado:</strong> Perfil creado exitosamente, retorna DTO poblado</p>
         */
        @Test
        @DisplayName("Éxito: Debería crear perfil cuando usuario existe y no tiene perfil previo")
        void createSitterProfile_WhenUserExistsAndHasNoProfile_ShouldReturnCreatedProfile() {
            // Arrange
            when(userRepository.findById(VALID_USER_ID)).thenReturn(Optional.of(testUser));
            when(sitterProfileRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.empty());
            when(sitterProfileRepository.save(any(SitterProfile.class))).thenReturn(testSitterProfile);

            // Act
            SitterProfileDTO result = sitterService.createSitterProfile(VALID_USER_ID, testSitterProfileDTO);

            // Assert - Verificación del resultado
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testSitterProfile.getId());
            assertThat(result.getUserId()).isEqualTo(VALID_USER_ID);
            assertThat(result.getBio()).isEqualTo(VALID_BIO);
            assertThat(result.getHourlyRate()).isEqualTo(VALID_HOURLY_RATE);

            // Assert - Verificación de interacciones con repositorios
            verify(userRepository, times(1)).findById(VALID_USER_ID);
            verify(sitterProfileRepository, times(1)).findByUserId(VALID_USER_ID);
            verify(sitterProfileRepository, times(1)).save(any(SitterProfile.class));
        }

        /**
         * Prueba el caso de fallo cuando el usuario no existe en la base de datos.
         *
         * <p><strong>Escenario:</strong> ID de usuario no existe</p>
         * <p><strong>Resultado esperado:</strong> Lanza IllegalArgumentException</p>
         */
        @Test
        @DisplayName("Fallo: Debería lanzar IllegalArgumentException cuando usuario no existe")
        void createSitterProfile_WhenUserNotFound_ShouldThrowIllegalArgumentException() {
            // Arrange
            when(userRepository.findById(NONEXISTENT_USER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> sitterService.createSitterProfile(NONEXISTENT_USER_ID, testSitterProfileDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cuidador no encontrado con ID: " + NONEXISTENT_USER_ID);

            // Assert - Verificación de que no se intentó crear el perfil
            verify(userRepository, times(1)).findById(NONEXISTENT_USER_ID);
            verify(sitterProfileRepository, never()).findByUserId(any());
            verify(sitterProfileRepository, never()).save(any());
        }

        /**
         * Prueba el caso de fallo cuando el usuario ya tiene un perfil existente.
         *
         * <p><strong>Escenario:</strong> Usuario válido pero ya posee un SitterProfile</p>
         * <p><strong>Resultado esperado:</strong> Lanza SitterProfileAlreadyExistsException</p>
         */
        @Test
        @DisplayName("Fallo: Debería lanzar SitterProfileAlreadyExistsException cuando usuario ya tiene perfil")
        void createSitterProfile_WhenUserAlreadyHasProfile_ShouldThrowSitterProfileAlreadyExistsException() {
            // Arrange
            when(userRepository.findById(VALID_USER_ID)).thenReturn(Optional.of(testUser));
            when(sitterProfileRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.of(testSitterProfile));

            // Act & Assert
            assertThatThrownBy(() -> sitterService.createSitterProfile(VALID_USER_ID, testSitterProfileDTO))
                    .isInstanceOf(SitterProfileAlreadyExistsException.class)
                    .hasMessageContaining("Cuidador ya tiene un perfil");

            // Assert - Verificación de que no se intentó guardar un nuevo perfil
            verify(userRepository, times(1)).findById(VALID_USER_ID);
            verify(sitterProfileRepository, times(1)).findByUserId(VALID_USER_ID);
            verify(sitterProfileRepository, never()).save(any());
        }
    }


    /**
     * Pruebas para el método {@code getSitterProfile}.
     */
    @Nested
    @DisplayName("Pruebas para getSitterProfile()")
    class GetSitterProfileTests {

        /**
         * Prueba el escenario de éxito donde se obtiene un perfil existente.
         *
         * <p><strong>Escenario:</strong> Usuario tiene perfil de cuidador</p>
         * <p><strong>Resultado esperado:</strong> Retorna DTO con datos del perfil</p>
         */
        @Test
        @DisplayName("Éxito: Debería retornar perfil cuando existe para el usuario")
        void getSitterProfile_WhenProfileExists_ShouldReturnProfile() {
            // Arrange
            when(sitterProfileRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.of(testSitterProfile));

            // Act
            SitterProfileDTO result = sitterService.getSitterProfile(VALID_USER_ID);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(VALID_USER_ID);
            assertThat(result.getBio()).isEqualTo(VALID_BIO);
            assertThat(result.getHourlyRate()).isEqualTo(VALID_HOURLY_RATE);

            verify(sitterProfileRepository, times(1)).findByUserId(VALID_USER_ID);
        }

        /**
         * Prueba el caso de fallo cuando no existe perfil para el usuario.
         *
         * <p><strong>Escenario:</strong> Usuario válido pero sin perfil de cuidador</p>
         * <p><strong>Resultado esperado:</strong> Lanza SitterProfileNotFoundException</p>
         */
        @Test
        @DisplayName("Fallo: Debería lanzar excepción cuando perfil no existe")
        void getSitterProfile_WhenProfileNotFound_ShouldThrowSitterProfileNotFoundException() {
            // Arrange
            when(sitterProfileRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> sitterService.getSitterProfile(VALID_USER_ID))
                    .isInstanceOf(SitterProfileNotFoundException.class)
                    .hasMessageContaining("No se encontró un perfil de cuidador para el usuario con ID: " + VALID_USER_ID);

            verify(sitterProfileRepository, times(1)).findByUserId(VALID_USER_ID);
        }
    }

    /**
     * Pruebas para el método {@code updateSitterProfile}.
     */
    @Nested
    @DisplayName("Pruebas para updateSitterProfile()")
    class UpdateSitterProfileTests {

        /**
         * Prueba el escenario de éxito donde se actualiza un perfil existente.
         *
         * <p><strong>Escenario:</strong> Perfil existe, datos de actualización válidos</p>
         * <p><strong>Resultado esperado:</strong> Perfil actualizado exitosamente</p>
         */
        @Test
        @DisplayName("Éxito: Debería actualizar perfil cuando existe")
        void updateSitterProfile_WhenProfileExists_ShouldUpdateAndReturnProfile() {
            // Arrange
            SitterProfileDTO updateDTO = createUpdatedSitterProfileDTO();
            SitterProfile updatedProfile = createUpdatedSitterProfile();

            when(sitterProfileRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.of(testSitterProfile));
            when(sitterProfileRepository.save(any(SitterProfile.class))).thenReturn(updatedProfile);

            // Act
            SitterProfileDTO result = sitterService.updateSitterProfile(VALID_USER_ID, updateDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getBio()).isEqualTo("Bio actualizada");
            assertThat(result.getHourlyRate()).isEqualTo(new BigDecimal("30.00"));

            verify(sitterProfileRepository, times(1)).findByUserId(VALID_USER_ID);
            verify(sitterProfileRepository, times(1)).save(any(SitterProfile.class));
        }

        /**
         * Prueba el caso de fallo cuando se intenta actualizar un perfil inexistente.
         *
         * <p><strong>Escenario:</strong> No existe perfil para el usuario</p>
         * <p><strong>Resultado esperado:</strong> Lanza SitterProfileNotFoundException</p>
         */
        @Test
        @DisplayName("Fallo: Debería lanzar excepción cuando perfil no existe para actualizar")
        void updateSitterProfile_WhenProfileNotFound_ShouldThrowSitterProfileNotFoundException() {
            // Arrange
            when(sitterProfileRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> sitterService.updateSitterProfile(VALID_USER_ID, testSitterProfileDTO))
                    .isInstanceOf(SitterProfileNotFoundException.class)
                    .hasMessageContaining("El perfil no se encuentra");

            verify(sitterProfileRepository, times(1)).findByUserId(VALID_USER_ID);
            verify(sitterProfileRepository, never()).save(any());
        }
    }

    /**
     * Pruebas para el método {@code getAllSitterProfiles}.
     */
    @Nested
    @DisplayName("Pruebas para getAllSitterProfiles()")
    class GetAllSitterProfilesTests {

        /**
         * Prueba el escenario de éxito donde existen múltiples perfiles.
         *
         * <p><strong>Escenario:</strong> Base de datos con varios perfiles de cuidador</p>
         * <p><strong>Resultado esperado:</strong> Lista de DTOs con todos los perfiles</p>
         */
        @Test
        @DisplayName("Éxito: Debería retornar lista de perfiles cuando existen")
        void getAllSitterProfiles_WhenProfilesExist_ShouldReturnListOfProfiles() {
            // Arrange
            List<SitterProfile> profiles = Arrays.asList(testSitterProfile, createAnotherSitterProfile());
            when(sitterProfileRepository.findAll()).thenReturn(profiles);

            // Act
            List<SitterProfileDTO> result = sitterService.getAllSitterProfiles();

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(dto -> dto.getId() != null);
            assertThat(result).allMatch(dto -> dto.getUserId() != null);

            verify(sitterProfileRepository, times(1)).findAll();
        }

        /**
         * Prueba el escenario donde no existen perfiles en la base de datos.
         *
         * <p><strong>Escenario:</strong> Base de datos vacía</p>
         * <p><strong>Resultado esperado:</strong> Lista vacía (no null)</p>
         */
        @Test
        @DisplayName("Éxito: Debería retornar lista vacía cuando no hay perfiles")
        void getAllSitterProfiles_WhenNoProfilesExist_ShouldReturnEmptyList() {
            // Arrange
            when(sitterProfileRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<SitterProfileDTO> result = sitterService.getAllSitterProfiles();

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(sitterProfileRepository, times(1)).findAll();
        }
    }

    /**
     * Pruebas para el método {@code deleteSitterProfile}.
     */
    @Nested
    @DisplayName("Pruebas para deleteSitterProfile()")
    class DeleteSitterProfileTests {

        /**
         * Prueba el escenario de éxito donde se elimina un perfil existente.
         *
         * <p><strong>Escenario:</strong> Perfil existe y se elimina correctamente</p>
         * <p><strong>Resultado esperado:</strong> Operación completada sin excepciones</p>
         */
        @Test
        @DisplayName("Éxito: Debería eliminar perfil cuando existe")
        void deleteSitterProfile_WhenProfileExists_ShouldDeleteProfile() {
            // Arrange
            when(sitterProfileRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.of(testSitterProfile));

            // Act
            assertThatNoException().isThrownBy(() -> sitterService.deleteSitterProfile(VALID_USER_ID));

            // Assert
            verify(sitterProfileRepository, times(1)).findByUserId(VALID_USER_ID);
            verify(sitterProfileRepository, times(1)).delete(testSitterProfile);
        }

        /**
         * Prueba el caso de fallo cuando se intenta eliminar un perfil inexistente.
         *
         * <p><strong>Escenario:</strong> No existe perfil para el usuario</p>
         * <p><strong>Resultado esperado:</strong> Lanza SitterProfileNotFoundException</p>
         */
        @Test
        @DisplayName("Fallo: Debería lanzar excepción cuando perfil no existe para eliminar")
        void deleteSitterProfile_WhenProfileNotFound_ShouldThrowSitterProfileNotFoundException() {
            // Arrange
            when(sitterProfileRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> sitterService.deleteSitterProfile(VALID_USER_ID))
                    .isInstanceOf(SitterProfileNotFoundException.class)
                    .hasMessageContaining("El perfil no se encuentra");

            verify(sitterProfileRepository, times(1)).findByUserId(VALID_USER_ID);
            verify(sitterProfileRepository, never()).delete(any());
        }
    }

    /**
     * Pruebas para el método {@code findSitters}.
     */
    @Nested
    @DisplayName("Pruebas para findSitters()")
    class FindSittersTests {

        /**
         * Prueba la búsqueda de cuidadores con filtro de ciudad.
         *
         * <p><strong>Escenario:</strong> Se proporciona ciudad válida, existen cuidadores</p>
         * <p><strong>Resultado esperado:</strong> Lista filtrada de cuidadores</p>
         */
        @Test
        @DisplayName("Éxito: Debería filtrar cuidadores por ciudad cuando se proporciona")
        void findSitters_WhenCityProvided_ShouldFilterByCity() {
            // Arrange
            List<SitterProfile> profiles = Arrays.asList(testSitterProfile);
            SitterProfileSummary summary = createSitterProfileSummary();

            when(sitterProfileRepository.findByIsVerifiedTrueAndIsAvailableForBookingsTrueAndUser_AddressContainingIgnoreCase(VALID_CITY))
                    .thenReturn(profiles);
            when(sitterProfileMapper.toSummaryDto(testSitterProfile)).thenReturn(summary);

            // Act
            List<SitterProfileSummary> result = sitterService.findSitters(VALID_CITY);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(summary);

            verify(sitterProfileRepository, times(1))
                    .findByIsVerifiedTrueAndIsAvailableForBookingsTrueAndUser_AddressContainingIgnoreCase(VALID_CITY);
            verify(sitterProfileMapper, times(1)).toSummaryDto(testSitterProfile);
        }

        /**
         * Prueba la búsqueda de cuidadores sin filtro de ciudad.
         *
         * <p><strong>Escenario:</strong> Ciudad es null o vacía</p>
         * <p><strong>Resultado esperado:</strong> Lista de todos los cuidadores verificados</p>
         */
        @Test
        @DisplayName("Éxito: Debería retornar todos los cuidadores cuando no se proporciona ciudad")
        void findSitters_WhenNoCityProvided_ShouldReturnAllVerifiedSitters() {
            // Arrange
            List<SitterProfile> profiles = Arrays.asList(testSitterProfile);
            SitterProfileSummary summary = createSitterProfileSummary();

            when(sitterProfileRepository.findByIsVerifiedTrueAndIsAvailableForBookingsTrue())
                    .thenReturn(profiles);
            when(sitterProfileMapper.toSummaryDto(testSitterProfile)).thenReturn(summary);

            // Act
            List<SitterProfileSummary> result = sitterService.findSitters(null);

            // Assert
            assertThat(result).hasSize(1);

            verify(sitterProfileRepository, times(1)).findByIsVerifiedTrueAndIsAvailableForBookingsTrue();
            verify(sitterProfileMapper, times(1)).toSummaryDto(testSitterProfile);
        }

        /**
         * Prueba la búsqueda cuando la ciudad está vacía (string vacío).
         *
         * <p><strong>Escenario:</strong> Ciudad es string vacío o solo espacios</p>
         * <p><strong>Resultado esperado:</strong> Comportamiento igual a ciudad null</p>
         */
        @Test
        @DisplayName("Éxito: Debería tratar ciudad vacía como null")
        void findSitters_WhenEmptyCity_ShouldTreatAsNoCity() {
            // Arrange
            List<SitterProfile> profiles = Arrays.asList(testSitterProfile);
            SitterProfileSummary summary = createSitterProfileSummary();

            when(sitterProfileRepository.findByIsVerifiedTrueAndIsAvailableForBookingsTrue())
                    .thenReturn(profiles);
            when(sitterProfileMapper.toSummaryDto(testSitterProfile)).thenReturn(summary);

            // Act
            List<SitterProfileSummary> result = sitterService.findSitters("   ");

            // Assert
            assertThat(result).hasSize(1);

            verify(sitterProfileRepository, times(1)).findByIsVerifiedTrueAndIsAvailableForBookingsTrue();
            verify(sitterProfileRepository, never())
                    .findByIsVerifiedTrueAndIsAvailableForBookingsTrueAndUser_AddressContainingIgnoreCase(anyString());
        }
    }

    // =====================================================================================
    // ========================= MÉTODOS DE CREACIÓN DE OBJETOS DE PRUEBA =================
    // =====================================================================================

    /**
     * Crea un usuario de prueba válido con datos consistentes.
     *
     * @return Instancia de {@link User} configurada para pruebas
     */
    private User createValidUser() {
        User user = new User("Juan", "Pérez", "juan.perez@test.com",
                "encodedPassword", "123 Test Street", "555-1234", Role.SITTER);
        user.setId(VALID_USER_ID);
        user.setEmailVerifiedAt(LocalDateTime.now());
        return user;
    }

    /**
     * Crea un perfil de cuidador de prueba válido.
     *
     * @return Instancia de {@link SitterProfile} configurada para pruebas
     */
    private SitterProfile createValidSitterProfile() {
        SitterProfile profile = new SitterProfile(testUser, VALID_BIO, VALID_HOURLY_RATE,
                VALID_RADIUS, VALID_IMAGE_URL);
        profile.setId(1L);
        profile.setVerified(true);
        profile.setAvailableForBookings(true);
        return profile;
    }

    /**
     * Crea un DTO de perfil de cuidador válido para pruebas.
     *
     * @return Instancia de {@link SitterProfileDTO} configurada para pruebas
     */
    private SitterProfileDTO createValidSitterProfileDTO() {
        return new SitterProfileDTO(null, VALID_USER_ID, VALID_BIO, VALID_HOURLY_RATE,
                VALID_RADIUS, VALID_IMAGE_URL, true, true);
    }

    /**
     * Crea un DTO actualizado para pruebas de actualización.
     *
     * @return Instancia de {@link SitterProfileDTO} con datos actualizados
     */
    private SitterProfileDTO createUpdatedSitterProfileDTO() {
        return new SitterProfileDTO(1L, VALID_USER_ID, "Bio actualizada",
                new BigDecimal("30.00"), 15, VALID_IMAGE_URL, true, true);
    }

    /**
     * Crea un perfil de cuidador actualizado para pruebas.
     *
     * @return Instancia de {@link SitterProfile} con datos actualizados
     */
    private SitterProfile createUpdatedSitterProfile() {
        SitterProfile profile = new SitterProfile(testUser, "Bio actualizada",
                new BigDecimal("30.00"), 15, VALID_IMAGE_URL);
        profile.setId(1L);
        profile.setVerified(true);
        profile.setAvailableForBookings(true);
        return profile;
    }

    /**
     * Crea un segundo perfil de cuidador para pruebas con múltiples elementos.
     *
     * @return Instancia de {@link SitterProfile} diferente al principal
     */
    private SitterProfile createAnotherSitterProfile() {
        User anotherUser = new User("Ana", "García", "ana.garcia@test.com",
                "encodedPassword", "456 Another St", "555-5678", Role.SITTER);
        anotherUser.setId(2L);

        SitterProfile profile = new SitterProfile(anotherUser, "Otra biografía",
                new BigDecimal("20.00"), 8, "https://example.com/other.jpg");
        profile.setId(2L);
        profile.setVerified(true);
        profile.setAvailableForBookings(true);
        return profile;
    }

    /**
     * Crea un resumen de perfil de cuidador para pruebas.
     *
     * @return Instancia de {@link SitterProfileSummary} configurada para pruebas
     */
    private SitterProfileSummary createSitterProfileSummary() {
        return new SitterProfileSummary(
                1L,
                "Juan Pérez",
                VALID_IMAGE_URL,
                VALID_HOURLY_RATE,
                VALID_AVERAGE_RATING,
                VALID_AVAILABLE,
                "Argetina");
    }
}