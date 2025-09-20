package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.Configurations.Security.Jwt.JwtService;
import com.Petcare.Petcare.DTOs.Sitter.SitterProfileDTO;
import com.Petcare.Petcare.Models.SitterProfile;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.SitterProfileRepository;
import com.Petcare.Petcare.Repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración exhaustivas para {@link SitterProfileController} usando base de datos H2 en memoria.
 *
 * <p>Esta suite de pruebas utiliza H2 como base de datos embebida para proporcionar un entorno
 * de testing aislado, rápido y reproducible. H2 simula el comportamiento de una base de datos relacional
 * manteniendo la velocidad de ejecución necesaria para tests de integración.</p>
 *
 * <p><strong>Configuración H2:</strong></p>
 * <ul>
 * <li>Base de datos en memoria que se crea y destruye en cada test.</li>
 * <li>DDL autocreate para generar el esquema automáticamente.</li>
 * <li>Transacciones que se revierten automáticamente con {@code @Transactional}.</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see SpringBootTest
 * @see AutoConfigureMockMvc
 * @see ActiveProfiles
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Pruebas de Integración con H2: SitterProfileController")
class SitterProfileControllerTest {

    // === Dependencias de Spring ===
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SitterProfileRepository sitterProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private DataSource dataSource;

    // === Constantes para Datos de Prueba ===
    private static final String BASE_URL = "/api/sitter-profiles";
    private static final String DEFAULT_PASSWORD = "password123";
    private static final String DEFAULT_BIO = "Biografía de prueba para un cuidador con experiencia.";
    private static final BigDecimal DEFAULT_HOURLY_RATE = new BigDecimal("25.50");
    private static final Integer DEFAULT_RADIUS = 15;
    private static final String DEFAULT_IMAGE_URL = "https://example.com/profile-image.jpg";

    // === Datos de Prueba Reutilizables ===
    private User sitterUser;
    private User clientUser;
    private User adminUser;
    private String sitterToken;
    private String clientToken;
    private String adminToken;

    /**
     * Configuración inicial que se ejecuta antes de cada prueba.
     * Verifica la configuración de la base de datos H2 y prepara los datos de prueba,
     * como usuarios con diferentes roles y sus tokens de autenticación.
     * @throws Exception si ocurre un error durante la configuración.
     */
    @BeforeEach
    void setUp() throws Exception {
        verifyH2Configuration();
        cleanDatabase();
        setupTestUsers();
        generateAuthTokens();
        logDatabaseState("Configuración inicial completada");
    }

    /**
     * Pruebas anidadas para el endpoint {@code POST /api/sitter-profiles}, enfocado en la creación de perfiles.
     */
    @Nested
    @DisplayName("POST /api/sitter-profiles - Crear Perfil (H2)")
    class CreateSitterProfileH2Tests {

        /**
         * Válida el "camino feliz": un cuidador autenticado crea su perfil exitosamente.
         * La prueba verifica que la respuesta HTTP sea 201 Created y que el perfil
         * se haya persistido correctamente en la base de datos H2.
         * @throws Exception si hay un error en la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Un Sitter autenticado debería crear su perfil y persistirlo en H2")
        void createSitterProfile_WithSitterRole_ShouldPersistSuccessfully() throws Exception {
            // Arrange
            SitterProfileDTO requestDTO = createValidSitterProfileDTO(sitterUser.getId());
            long initialCount = sitterProfileRepository.count();

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL)
                    .header("Authorization", "Bearer " + sitterToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)));

            // Assert - Verificación de la Respuesta HTTP
            result.andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            SitterProfileDTO responseDTO = extractResponse(result, SitterProfileDTO.class);
            assertThat(responseDTO.getId()).isNotNull();
            assertThat(responseDTO.getUserId()).isEqualTo(sitterUser.getId());

            // Assert - Verificación del Estado de la Base de Datos H2
            assertThat(sitterProfileRepository.count()).isEqualTo(initialCount + 1);
            SitterProfile persistedProfile = sitterProfileRepository.findByUserId(sitterUser.getId())
                    .orElseThrow(() -> new AssertionError("El perfil debería estar persistido en H2"));

            assertThat(persistedProfile.getBio()).isEqualTo(DEFAULT_BIO);
            assertThat(persistedProfile.getHourlyRate()).isEqualByComparingTo(DEFAULT_HOURLY_RATE);
            assertThat(persistedProfile.getUser().getId()).isEqualTo(sitterUser.getId());

            logDatabaseState("Después de crear perfil exitosamente");
        }

        /**
         * Válida que la lógica de negocio previene la creación de perfiles duplicados.
         * Si un usuario ya tiene un perfil, un segundo intento debe fallar con un
         * estado HTTP 409 Conflict.
         * @throws Exception si hay un error en la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (Conflicto): Debería retornar 409 si el Sitter ya tiene un perfil")
        void createSitterProfile_WhenProfileAlreadyExists_ShouldReturnConflict() throws Exception {
            // Arrange - Creamos un perfil inicial para el cuidador
            createSitterProfileForUser(sitterUser);
            long initialCount = sitterProfileRepository.count();
            SitterProfileDTO duplicateDTO = createValidSitterProfileDTO(sitterUser.getId());

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + sitterToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateDTO)))
                    .andExpect(status().isConflict());

            // Verificamos que no se crearon perfiles adicionales en la BD
            assertThat(sitterProfileRepository.count()).isEqualTo(initialCount);
            logDatabaseState("Después de intento de duplicado");
        }

        /**
         * Valida la capa de seguridad. Un usuario con rol 'CLIENT' no debe poder
         * crear un perfil de cuidador, resultando en un HTTP 403 Forbidden.
         * @throws Exception si hay un error en la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (Autorización): Un CLIENT no debería poder crear un perfil y debe recibir 403")
        void createSitterProfile_WithClientRole_ShouldReturnForbidden() throws Exception {
            // Arrange
            SitterProfileDTO requestDTO = createValidSitterProfileDTO(clientUser.getId());

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + clientToken) // Usamos el token de un CLIENTE
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden());
        }
    }

    /**
     * Pruebas anidadas para el endpoint {@code GET /api/sitter-profiles/{userId}}, enfocado en la obtención de perfiles.
     */
    @Nested
    @DisplayName("GET /api/sitter-profiles/{userId} - Obtener Perfil (H2)")
    class GetSitterProfileH2Tests {

        /**
         * Válida el caso de éxito donde un cuidador solicita su propio perfil.
         * La prueba confirma que la respuesta es 200 OK y que los datos retornados
         * coinciden con los persistidos en la base de datos H2.
         * @throws Exception si hay un error en la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Un Sitter debería obtener su propio perfil")
        void getSitterProfile_AsOwner_ShouldReturnOwnProfile() throws Exception {
            // Arrange: Creamos un perfil para nuestro cuidador de prueba
            SitterProfile persistedProfile = createSitterProfileForUser(sitterUser);

            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL + "/{userId}", sitterUser.getId())
                    .header("Authorization", "Bearer " + sitterToken)); // Autenticado como el propio cuidador

            // Assert: Verificamos la respuesta HTTP y el contenido
            result.andExpect(status().isOk());

            SitterProfileDTO responseDTO = extractResponse(result, SitterProfileDTO.class);
            assertThat(responseDTO.getId()).isEqualTo(persistedProfile.getId());
            assertThat(responseDTO.getUserId()).isEqualTo(sitterUser.getId());
            assertThat(responseDTO.getBio()).isEqualTo(DEFAULT_BIO);

            logDatabaseState("Después de que un Sitter consulta su propio perfil");
        }

        /**
         * Válida el caso de éxito donde un administrador solicita el perfil de cualquier cuidador.
         * @throws Exception si hay un error en la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Un ADMIN debería obtener el perfil de cualquier Sitter")
        void getSitterProfile_AsAdmin_ShouldReturnAnyProfile() throws Exception {
            // Arrange: Creamos un perfil para el cuidador
            SitterProfile persistedProfile = createSitterProfileForUser(sitterUser);

            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL + "/{userId}", sitterUser.getId())
                    .header("Authorization", "Bearer " + adminToken)); // Autenticado como ADMIN

            // Assert
            result.andExpect(status().isOk());
            SitterProfileDTO responseDTO = extractResponse(result, SitterProfileDTO.class);
            assertThat(responseDTO.getId()).isEqualTo(persistedProfile.getId());
            assertThat(responseDTO.getUserId()).isEqualTo(sitterUser.getId());
        }

        /**
         * Válida la regla de seguridad que impide a un usuario ver perfiles ajenos.
         * Un cliente intentando ver el perfil de un cuidador debe recibir 403 Forbidden.
         * @throws Exception si hay un error en la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (Autorización): Un CLIENT no debería poder obtener perfiles y debe recibir 403")
        void getSitterProfile_AsClient_ShouldReturnForbidden() throws Exception {
            // Arrange: Creamos un perfil para el cuidador
            createSitterProfileForUser(sitterUser);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{userId}", sitterUser.getId())
                            .header("Authorization", "Bearer " + clientToken)) // Autenticado como CLIENTE
                    .andExpect(status().isForbidden());
        }

        /**
         * Valida el caso de borde en el que se solicita un perfil para un usuario que existe
         * pero no tiene un perfil de cuidador creado. La respuesta correcta es 404 Not Found.
         * @throws Exception si hay un error en la petición MockMvc.
         */
        @Test
        @DisplayName("🔍 Falla (No Encontrado): Debería retornar 404 si el User existe pero no tiene perfil")
        void getSitterProfile_WhenUserExistsButHasNoProfile_ShouldReturnNotFound() throws Exception {
            // Arrange: Nos aseguramos de que el clientUser existe pero no tiene perfil
            assertThat(sitterProfileRepository.findByUserId(clientUser.getId())).isEmpty();

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{userId}", clientUser.getId())
                            .header("Authorization", "Bearer " + adminToken)) // Como ADMIN para evitar un 403
                    .andExpect(status().isNotFound());

            logDatabaseState("Después de consultar perfil de usuario sin perfil");
        }

        /**
         * Válida que una petición sin token de autenticación sea rechazada con 401 Unauthorized.
         * @throws Exception si hay un error en la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (Autenticación): Una petición sin token debería retornar 403 Forbidden")
        void getSitterProfile_WithoutToken_ShouldReturnForbidden() throws Exception {
            // Arrange
            createSitterProfileForUser(sitterUser);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{userId}", sitterUser.getId()))
                    .andExpect(status().isForbidden());
        }
    }

    /**
     * Pruebas anidadas para el endpoint {@code GET /api/sitter-profiles}, enfocado en la obtención de todos los perfiles.
     */
    @Nested
    @DisplayName("GET /api/sitter-profiles - Obtener Todos los Perfiles (H2)")
    class GetAllSitterProfilesH2Tests {

        /**
         * Válida el caso de éxito donde un administrador obtiene la lista de todos los perfiles.
         * La prueba crea múltiples perfiles y verifica que la respuesta los contenga todos.
         * @throws Exception si hay un error en la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Un ADMIN debería obtener la lista de todos los perfiles")
        void getAllSitterProfiles_AsAdmin_ShouldReturnFullListOfProfiles() throws Exception {
            // Arrange: Creamos dos perfiles de cuidadores distintos.
            createSitterProfileForUser(sitterUser);
            // Creamos un segundo cuidador para tener más de un perfil en la lista.
            User anotherSitter = createAndSaveUser("sitter2@petcare.com", Role.SITTER);
            createSitterProfileForUser(anotherSitter);

            assertThat(sitterProfileRepository.count()).isEqualTo(2); // Verificamos el estado inicial de la BD.

            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL)
                    .header("Authorization", "Bearer " + adminToken)); // Autenticado como ADMIN

            // Assert: Verificamos que la respuesta sea 200 OK y contenga dos elementos.
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2));

            logDatabaseState("Después de que ADMIN consulta todos los perfiles");
        }

        /**
         * Válida el caso de borde donde un administrador solicita la lista pero no hay perfiles creados.
         * La respuesta esperada es un 200 OK con un array JSON vacío.
         * @throws Exception si hay un error en la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Un ADMIN debería obtener una lista vacía si no hay perfiles")
        void getAllSitterProfiles_AsAdminWhenNoProfilesExist_ShouldReturnEmptyList() throws Exception {
            // Arrange: Nos aseguramos de que no haya perfiles en la BD.
            assertThat(sitterProfileRepository.count()).isZero();

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0)); // Esperamos un array vacío
        }

        /**
         * Valida que un usuario con rol 'SITTER' no pueda acceder a la lista completa de perfiles.
         * @throws Exception si hay un error en la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (Autorización): Un SITTER no debería poder obtener la lista y debe recibir 403")
        void getAllSitterProfiles_AsSitter_ShouldReturnForbidden() throws Exception {
            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                            .header("Authorization", "Bearer " + sitterToken)) // Autenticado como SITTER
                    .andExpect(status().isForbidden());
        }

        /**
         * Valida que un usuario con rol 'CLIENT' no pueda acceder a la lista completa de perfiles.
         * @throws Exception si hay un error en la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (Autorización): Un CLIENT no debería poder obtener la lista y debe recibir 403")
        void getAllSitterProfiles_AsClient_ShouldReturnForbidden() throws Exception {
            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                            .header("Authorization", "Bearer " + clientToken)) // Autenticado como CLIENT
                    .andExpect(status().isForbidden());
        }

        /**
         * Valida que una petición sin token de autenticación sea rechazada.
         * Se espera un 403 Forbidden basado en la configuración de seguridad actual.
         * @throws Exception si hay un error en la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (Autenticación): Una petición sin token debería retornar 403")
        void getAllSitterProfiles_WithoutToken_ShouldReturnForbidden() throws Exception {
            // Act & Assert
            mockMvc.perform(get(BASE_URL))
                    // *** CORRECCIÓN APLICADA AQUÍ ***
                    // Esperamos 403 porque @PreAuthorize se ejecuta sobre un usuario "anónimo"
                    .andExpect(status().isForbidden());
        }
    }

    /**
     * Pruebas anidadas para el endpoint {@code PUT /api/sitter-profiles/{userId}}, enfocado en la actualización de perfiles.
     */
    /**
     * Pruebas anidadas para el endpoint {@code PUT /api/sitter-profiles/{userId}}, enfocado en la actualización de perfiles.
     */
    @Nested
    @DisplayName("PUT /api/sitter-profiles/{userId} - Actualizar Perfil (H2)")
    class UpdateSitterProfileH2Tests {

        @Test
        @DisplayName("✅ Éxito: Un Sitter debería poder actualizar su propio perfil")
        void updateSitterProfile_AsOwner_ShouldUpdateAndPersistChanges() throws Exception {
            SitterProfile originalProfile = createSitterProfileForUser(sitterUser);
            SitterProfileDTO updateDTO = new SitterProfileDTO(
                    originalProfile.getId(), sitterUser.getId(), "Biografía actualizada por el propietario",
                    new BigDecimal("30.00"), 20, "https://example.com/new-image.jpg",
                    true, false
            );

            mockMvc.perform(put(BASE_URL + "/{userId}", sitterUser.getId())
                            .header("Authorization", "Bearer " + sitterToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bio").value("Biografía actualizada por el propietario"))
                    .andExpect(jsonPath("$.availableForBookings").value(false));

            SitterProfile updatedProfileFromDb = sitterProfileRepository.findById(originalProfile.getId())
                    .orElseThrow(() -> new AssertionError("El perfil no debería haber sido eliminado."));

            assertThat(updatedProfileFromDb.getBio()).isEqualTo("Biografía actualizada por el propietario");
            // Usamos isAfterOrEqualTo para manejar casos donde la ejecución es tan rápida que el timestamp no cambia.
            assertThat(updatedProfileFromDb.getUpdatedAt()).isAfterOrEqualTo(originalProfile.getUpdatedAt());

            logDatabaseState("Después de que un Sitter actualiza su propio perfil");
        }

        @Test
        @DisplayName("✅ Éxito: Un ADMIN debería poder actualizar el perfil de cualquier Sitter")
        void updateSitterProfile_AsAdmin_ShouldUpdateAnyProfile() throws Exception {
            SitterProfile originalProfile = createSitterProfileForUser(sitterUser);
            SitterProfileDTO updateDTO = new SitterProfileDTO(
                    originalProfile.getId(), sitterUser.getId(), "Perfil actualizado por un Admin",
                    new BigDecimal("99.99"), 50, null, true, true
            );

            mockMvc.perform(put(BASE_URL + "/{userId}", sitterUser.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bio").value("Perfil actualizado por un Admin"));
        }

        @Test
        @DisplayName("🚫 Falla (Autorización): Un CLIENT no debería poder actualizar perfiles y debe recibir 403")
        void updateSitterProfile_AsClient_ShouldReturnForbidden() throws Exception {
            createSitterProfileForUser(sitterUser);
            SitterProfileDTO updateDTO = createValidSitterProfileDTO(sitterUser.getId());

            // *** CORRECCIÓN: AÑADIR CUERPO A LA PETICIÓN ***
            mockMvc.perform(put(BASE_URL + "/{userId}", sitterUser.getId())
                            .header("Authorization", "Bearer " + clientToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("🚫 Falla (Autorización): Un SITTER no debería poder actualizar el perfil de OTRO Sitter y debe recibir 403")
        void updateSitterProfile_AsAnotherSitter_ShouldReturnForbidden() throws Exception {
            User anotherSitter = createAndSaveUser("another.sitter@petcare.com", Role.SITTER);
            createSitterProfileForUser(anotherSitter);
            SitterProfileDTO updateDTO = createValidSitterProfileDTO(anotherSitter.getId());

            // *** CORRECCIÓN: AÑADIR CUERPO A LA PETICIÓN ***
            mockMvc.perform(put(BASE_URL + "/{userId}", anotherSitter.getId())
                            .header("Authorization", "Bearer " + sitterToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("🔍 Falla (No Encontrado): Debería retornar 404 si el perfil a actualizar no existe")
        void updateSitterProfile_WhenProfileDoesNotExist_ShouldReturnNotFound() throws Exception {
            SitterProfileDTO updateDTO = createValidSitterProfileDTO(clientUser.getId());

            // *** CORRECCIÓN: AÑADIR CUERPO A LA PETICIÓN ***
            mockMvc.perform(put(BASE_URL + "/{userId}", clientUser.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("📉 Falla (Validación): Debería retornar 400 si los datos del DTO son inválidos")
        void updateSitterProfile_WithInvalidData_ShouldReturnBadRequest() throws Exception {
            SitterProfile originalProfile = createSitterProfileForUser(sitterUser);
            // DTO con tarifa negativa, lo cual debería ser rechazado por las nuevas anotaciones de validación.
            SitterProfileDTO invalidUpdateDTO = new SitterProfileDTO(
                    originalProfile.getId(), sitterUser.getId(), "Bio válida que cumple los requisitos de tamaño",
                    new BigDecimal("-5.00"), // Tarifa inválida
                    10, null, true, true
            );

            mockMvc.perform(put(BASE_URL + "/{userId}", sitterUser.getId())
                            .header("Authorization", "Bearer " + sitterToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidUpdateDTO)))
                    .andExpect(status().isBadRequest()); // Ahora esperamos 400

            // Verificamos que la BD no se modificó
            SitterProfile profileFromDb = sitterProfileRepository.findById(originalProfile.getId()).get();
            assertThat(profileFromDb.getHourlyRate()).isEqualByComparingTo(DEFAULT_HOURLY_RATE);
        }
    }

    /**
     * Pruebas anidadas para el endpoint {@code DELETE /api/sitter-profiles/{userId}}, enfocado en la eliminación de perfiles.
     */
    @Nested
    @DisplayName("DELETE /api/sitter-profiles/{userId} - Eliminar Perfil (H2)")
    class DeleteSitterProfileH2Tests {

        @Test
        @DisplayName("✅ Éxito: Un Sitter debería poder eliminar su propio perfil")
        void deleteSitterProfile_AsOwner_ShouldRemoveFromDatabase() throws Exception {
            SitterProfile profileToDelete = createSitterProfileForUser(sitterUser);
            long initialCount = sitterProfileRepository.count();
            assertThat(initialCount).isGreaterThan(0);

            mockMvc.perform(delete(BASE_URL + "/{userId}", sitterUser.getId())
                            .header("Authorization", "Bearer " + sitterToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("El perfil de cuidador para el usuario con ID " + sitterUser.getId() + " ha sido eliminado exitosamente."));

            assertThat(sitterProfileRepository.count()).isEqualTo(initialCount - 1);
            assertThat(sitterProfileRepository.findById(profileToDelete.getId())).isEmpty();

            logDatabaseState("Después de que un Sitter elimina su propio perfil");
        }

        @Test
        @DisplayName("✅ Éxito: Un ADMIN debería poder eliminar el perfil de cualquier Sitter")
        void deleteSitterProfile_AsAdmin_ShouldRemoveAnyProfile() throws Exception {
            SitterProfile profileToDelete = createSitterProfileForUser(sitterUser);
            assertThat(sitterProfileRepository.existsById(profileToDelete.getId())).isTrue();

            mockMvc.perform(delete(BASE_URL + "/{userId}", sitterUser.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            assertThat(sitterProfileRepository.existsById(profileToDelete.getId())).isFalse();
        }

        @Test
        @DisplayName("🚫 Falla (Autorización): Un CLIENT no debería poder eliminar perfiles y debe recibir 403")
        void deleteSitterProfile_AsClient_ShouldReturnForbidden() throws Exception {
            createSitterProfileForUser(sitterUser);
            long initialCount = sitterProfileRepository.count();

            mockMvc.perform(delete(BASE_URL + "/{userId}", sitterUser.getId())
                            .header("Authorization", "Bearer " + clientToken))
                    .andExpect(status().isForbidden());

            assertThat(sitterProfileRepository.count()).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("🔍 Falla (No Encontrado): Debería retornar 404 si el perfil a eliminar no existe")
        void deleteSitterProfile_WhenProfileDoesNotExist_ShouldReturnNotFound() throws Exception {
            assertThat(sitterProfileRepository.findByUserId(sitterUser.getId())).isEmpty();

            mockMvc.perform(delete(BASE_URL + "/{userId}", sitterUser.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("🚫 Falla (Autenticación): Una petición sin token debería retornar 403")
        void deleteSitterProfile_WithoutToken_ShouldReturnForbidden() throws Exception {
            createSitterProfileForUser(sitterUser);
            mockMvc.perform(delete(BASE_URL + "/{userId}", sitterUser.getId()))
                    .andExpect(status().isForbidden());
        }
    }

    // =====================================================================================
    // ========================= MÉTODOS AUXILIARES (HELPERS) ==============================
    // =====================================================================================

    /**
     * Verifica que la base de datos configurada para las pruebas es H2.
     * Es una comprobación de sanidad para asegurar que no estamos corriendo
     * accidentalmente contra una base de datos de producción.
     * @throws Exception si no se puede obtener la conexión a la BD.
     */
    private void verifyH2Configuration() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            assertThat(databaseProductName).isEqualTo("H2");
            System.out.println("✅ Conexión a H2 en modo memoria verificada.");
        }
    }

    /**
     * Limpia las tablas de la base de datos para asegurar un estado limpio
     * antes de cada prueba.
     */
    private void cleanDatabase() {
        sitterProfileRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    /**
     * Crea y persiste los usuarios de prueba con roles SITTER, CLIENT y ADMIN.
     */
    private void setupTestUsers() {
        sitterUser = createAndSaveUser("sitter@petcare.com", Role.SITTER);
        clientUser = createAndSaveUser("client@petcare.com", Role.CLIENT);
        adminUser = createAndSaveUser("admin@petcare.com", Role.ADMIN);
    }

    /**
     * Genera tokens JWT para cada uno de los usuarios de prueba.
     */
    private void generateAuthTokens() {
        sitterToken = jwtService.getToken(sitterUser);
        clientToken = jwtService.getToken(clientUser);
        adminToken = jwtService.getToken(adminUser);
    }

    /**
     * Registra el estado actual de las tablas en la base de datos para facilitar el debugging.
     * @param context Mensaje descriptivo del punto en el que se registra el estado.
     */
    private void logDatabaseState(String context) {
        long userCount = userRepository.count();
        long profileCount = sitterProfileRepository.count();
        System.out.printf("🗃️  Estado BD H2 - %s: [Usuarios: %d, Perfiles: %d]%n", context, userCount, profileCount);
    }

    /**
     * Método helper para crear y guardar un usuario.
     * @param email Email para el nuevo usuario.
     * @param role Rol del nuevo usuario.
     * @return La entidad User persistida.
     */
    private User createAndSaveUser(String email, Role role) {
        User user = new User("Test", role.name(), email, passwordEncoder.encode(DEFAULT_PASSWORD),
                "123 Test Street", "555-1234", role);
        user.setEmailVerifiedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Método helper para crear y guardar un perfil de cuidador.
     * @param user El usuario al que se asociará el perfil.
     * @return La entidad SitterProfile persistida.
     */
    private SitterProfile createSitterProfileForUser(User user) {
        SitterProfile profile = new SitterProfile(user, DEFAULT_BIO, DEFAULT_HOURLY_RATE,
                DEFAULT_RADIUS, DEFAULT_IMAGE_URL);
        profile.setVerified(true);
        profile.setAvailableForBookings(true);
        return sitterProfileRepository.save(profile);
    }

    /**
     * Método helper para crear un DTO de perfil de cuidador válido.
     * @param userId El ID del usuario al que se asociará el perfil.
     * @return Una instancia de {@link SitterProfileDTO}.
     */
    private SitterProfileDTO createValidSitterProfileDTO(Long userId) {
        return new SitterProfileDTO(null, userId, DEFAULT_BIO, DEFAULT_HOURLY_RATE,
                DEFAULT_RADIUS, DEFAULT_IMAGE_URL, true, true);
    }

    /**
     * Extrae y deserializa el contenido de una respuesta MockMvc a un objeto de una clase específica.
     * @param result El resultado de la petición MockMvc.
     * @param clazz La clase a la que se debe deserializar el JSON.
     * @return Una instancia de la clase especificada.
     * @throws Exception si hay un error en la deserialización.
     */
    private <T> T extractResponse(ResultActions result, Class<T> clazz) throws Exception {
        String responseContent = result.andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(responseContent, clazz);
    }
}