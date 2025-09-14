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
 * de testing aislado, rápido y reproducible. H2 simula el comportamiento de PostgreSQL en producción
 * manteniendo la velocidad de ejecución necesaria para tests de integración.</p>
 *
 * <p><strong>Configuración H2:</strong></p>
 * <ul>
 * <li>Base de datos en memoria que se crea y destruye en cada test</li>
 * <li>Modo PostgreSQL para compatibilidad con la BD de producción</li>
 * <li>DDL autocreate para generar el esquema automáticamente</li>
 * <li>Transacciones que se revierten automáticamente con {@code @Transactional}</li>
 * </ul>
 *
 * <p><strong>Beneficios del Uso de H2:</strong></p>
 * <ul>
 * <li>Aislamiento total: cada test inicia con BD limpia</li>
 * <li>Velocidad: operaciones en memoria son órdenes de magnitud más rápidas</li>
 * <li>Sin dependencias externas: no requiere BD real instalada</li>
 * <li>Debugging: H2 Console disponible para inspección en tiempo real</li>
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
@DisplayName("Pruebas de Integración H2: SitterProfileController")
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
     * Configuración inicial ejecutada antes de cada prueba.
     * Verifica la configuración H2 y prepara los datos de prueba.
     */
    @BeforeEach
    void setUp() throws Exception {
        // Verificar que estamos usando H2 correctamente
        verifyH2Configuration();

        // Limpiar la base de datos (aunque @Transactional debería hacerlo)
        cleanDatabase();

        // Crear usuarios de prueba con diferentes roles
        setupTestUsers();

        // Generar tokens JWT para autenticación
        generateAuthTokens();

        // Log estado inicial para debugging
        logDatabaseState("Configuración inicial completada");
    }

    /**
     * Pruebas para el endpoint {@code POST /api/sitter-profiles}.
     */
    @Nested
    @DisplayName("POST /api/sitter-profiles - Crear Perfil (H2)")
    class CreateSitterProfileH2Tests {

        /**
         * Prueba de integración completa: creación exitosa con persistencia H2.
         */
        @Test
        @DisplayName("✅ H2: Creación exitosa persiste correctamente en base de datos")
        void createSitterProfile_WithH2_ShouldPersistSuccessfully() throws Exception {
            // Arrange
            SitterProfileDTO requestDTO = createValidSitterProfileDTO(sitterUser.getId());
            long initialCount = sitterProfileRepository.count();

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL)
                    .header("Authorization", "Bearer " + sitterToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)));

            // Assert - HTTP Response
            result.andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            // Assert - Response Body
            SitterProfileDTO responseDTO = extractResponse(result, SitterProfileDTO.class);
            assertThat(responseDTO.getId()).isNotNull();
            assertThat(responseDTO.getUserId()).isEqualTo(sitterUser.getId());

            // Assert - H2 Database State
            assertThat(sitterProfileRepository.count()).isEqualTo(initialCount + 1);

            SitterProfile persistedProfile = sitterProfileRepository.findByUserId(sitterUser.getId())
                    .orElseThrow(() -> new AssertionError("Profile should be persisted in H2"));

            assertThat(persistedProfile.getBio()).isEqualTo(DEFAULT_BIO);
            assertThat(persistedProfile.getHourlyRate()).isEqualTo(DEFAULT_HOURLY_RATE);
            assertThat(persistedProfile.getUser().getId()).isEqualTo(sitterUser.getId());

            logDatabaseState("Después de crear perfil");
        }

        /**
         * Prueba de transaccionalidad: rollback automático en caso de fallo.
         */
        @Test
        @DisplayName("🔄 H2: Transacción se revierte automáticamente en fallo de validación")
        void createSitterProfile_WithH2_ShouldRollbackOnValidationFailure() throws Exception {
            // Arrange
            long initialCount = sitterProfileRepository.count();
            SitterProfileDTO invalidDTO = new SitterProfileDTO(
                    null, sitterUser.getId(), DEFAULT_BIO,
                    new BigDecimal("-10.00"), // Valor inválido que causará fallo
                    DEFAULT_RADIUS, DEFAULT_IMAGE_URL, true, true
            );

            // Act
            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + sitterToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest());

            // Assert - H2 Database State (sin cambios)
            assertThat(sitterProfileRepository.count()).isEqualTo(initialCount);
            assertThat(sitterProfileRepository.findByUserId(sitterUser.getId())).isEmpty();

            logDatabaseState("Después de fallo de validación");
        }

        /**
         * Prueba de constraints de BD: violación de unicidad.
         */
        @Test
        @DisplayName("🚫 H2: Constraint de unicidad impide perfiles duplicados")
        void createSitterProfile_WithH2_ShouldEnforceUniquenessConstraint() throws Exception {
            // Arrange - Crear perfil inicial
            createSitterProfileForUser(sitterUser);
            long initialCount = sitterProfileRepository.count();

            SitterProfileDTO duplicateDTO = createValidSitterProfileDTO(sitterUser.getId());

            // Act
            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + sitterToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateDTO)))
                    .andExpect(status().isConflict());

            // Assert - H2 Database State (sin cambios)
            assertThat(sitterProfileRepository.count()).isEqualTo(initialCount);

            logDatabaseState("Después de intento de duplicado");
        }
    }

    /**
     * Pruebas para el endpoint {@code GET /api/sitter-profiles/{userId}}.
     */
    @Nested
    @DisplayName("GET /api/sitter-profiles/{userId} - Obtener Perfil (H2)")
    class GetSitterProfileH2Tests {

        /**
         * Prueba de consulta H2: obtención exitosa desde base de datos.
         */
        @Test
        @DisplayName("✅ H2: Consulta exitosa retorna datos persistidos")
        void getSitterProfile_WithH2_ShouldReturnPersistedData() throws Exception {
            // Arrange - Persistir datos en H2
            SitterProfile persistedProfile = createSitterProfileForUser(sitterUser);

            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL + "/{userId}", sitterUser.getId())
                    .header("Authorization", "Bearer " + sitterToken));

            // Assert
            result.andExpect(status().isOk());

            SitterProfileDTO responseDTO = extractResponse(result, SitterProfileDTO.class);
            assertThat(responseDTO.getId()).isEqualTo(persistedProfile.getId());
            assertThat(responseDTO.getUserId()).isEqualTo(sitterUser.getId());
            assertThat(responseDTO.getBio()).isEqualTo(DEFAULT_BIO);

            logDatabaseState("Después de consultar perfil");
        }

        /**
         * Prueba de casos edge: consulta de recurso inexistente.
         */
        @Test
        @DisplayName("🔍 H2: Consulta de recurso inexistente retorna 404")
        void getSitterProfile_WithH2_ShouldReturn404ForNonexistentProfile() throws Exception {
            // Arrange - Verificar que no existe el perfil en H2
            assertThat(sitterProfileRepository.findByUserId(sitterUser.getId())).isEmpty();

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{userId}", sitterUser.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());

            logDatabaseState("Después de consultar perfil inexistente");
        }
    }

    /**
     * Pruebas para el endpoint {@code PUT /api/sitter-profiles/{userId}}.
     */
    @Nested
    @DisplayName("PUT /api/sitter-profiles/{userId} - Actualizar Perfil (H2)")
    class UpdateSitterProfileH2Tests {

        /**
         * Prueba de actualización H2: modificación exitosa de datos persistidos.
         */
        @Test
        @DisplayName("✅ H2: Actualización exitosa modifica datos en base de datos")
        void updateSitterProfile_WithH2_ShouldPersistChanges() throws Exception {
            // Arrange
            SitterProfile originalProfile = createSitterProfileForUser(sitterUser);
            SitterProfileDTO updateDTO = new SitterProfileDTO(
                    originalProfile.getId(), sitterUser.getId(), "Bio actualizada",
                    new BigDecimal("30.00"), 20, "https://new-image.com/updated.jpg",
                    true, false
            );

            // Act
            ResultActions result = mockMvc.perform(put(BASE_URL + "/{userId}", sitterUser.getId())
                    .header("Authorization", "Bearer " + sitterToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)));

            // Assert - HTTP Response
            result.andExpect(status().isOk());

            // Assert - H2 Database Changes
            SitterProfile updatedProfile = sitterProfileRepository.findByUserId(sitterUser.getId())
                    .orElseThrow(() -> new AssertionError("Profile should exist after update"));

            assertThat(updatedProfile.getBio()).isEqualTo("Bio actualizada");
            assertThat(updatedProfile.getHourlyRate()).isEqualTo(new BigDecimal("30.00"));
            assertThat(updatedProfile.getServicingRadius()).isEqualTo(20);
            assertThat(updatedProfile.isAvailableForBookings()).isFalse();

            // Assert - ID unchanged
            assertThat(updatedProfile.getId()).isEqualTo(originalProfile.getId());

            logDatabaseState("Después de actualizar perfil");
        }
    }

    /**
     * Pruebas para el endpoint {@code DELETE /api/sitter-profiles/{userId}}.
     */
    @Nested
    @DisplayName("DELETE /api/sitter-profiles/{userId} - Eliminar Perfil (H2)")
    class DeleteSitterProfileH2Tests {

        /**
         * Prueba de eliminación H2: remoción exitosa de datos persistidos.
         */
        @Test
        @DisplayName("✅ H2: Eliminación exitosa remueve datos de base de datos")
        void deleteSitterProfile_WithH2_ShouldRemoveFromDatabase() throws Exception {
            // Arrange
            createSitterProfileForUser(sitterUser);
            long initialCount = sitterProfileRepository.count();
            assertThat(initialCount).isGreaterThan(0);

            // Act
            ResultActions result = mockMvc.perform(delete(BASE_URL + "/{userId}", sitterUser.getId())
                    .header("Authorization", "Bearer " + sitterToken));

            // Assert - HTTP Response
            result.andExpect(status().isOk());

            // Assert - H2 Database Changes
            assertThat(sitterProfileRepository.count()).isEqualTo(initialCount - 1);
            assertThat(sitterProfileRepository.findByUserId(sitterUser.getId())).isEmpty();

            logDatabaseState("Después de eliminar perfil");
        }
    }

    // =====================================================================================
    // ========================= MÉTODOS DE CONFIGURACIÓN H2 ============================
    // =====================================================================================

    /**
     * Verifica que la configuración H2 esté funcionando correctamente.
     */
    private void verifyH2Configuration() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            String url = metaData.getURL();

            assertThat(databaseProductName).isEqualTo("H2");
            assertThat(url).contains("mem:sitter_profile_test");

            System.out.println("✅ H2 Database Configuration Verified:");
            System.out.println("   Database: " + databaseProductName);
            System.out.println("   URL: " + url);
            System.out.println("   Mode: In-Memory");
        }
    }

    /**
     * Limpia completamente la base de datos H2.
     */
    private void cleanDatabase() {
        sitterProfileRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    /**
     * Configura los usuarios de prueba con diferentes roles.
     */
    private void setupTestUsers() {
        sitterUser = createAndSaveUser("sitter@h2test.com", Role.SITTER);
        clientUser = createAndSaveUser("client@h2test.com", Role.CLIENT);
        adminUser = createAndSaveUser("admin@h2test.com", Role.ADMIN);
    }

    /**
     * Genera tokens JWT para todos los usuarios de prueba.
     */
    private void generateAuthTokens() {
        sitterToken = jwtService.getToken(sitterUser);
        clientToken = jwtService.getToken(clientUser);
        adminToken = jwtService.getToken(adminUser);
    }

    /**
     * Registra el estado actual de la base de datos H2 para debugging.
     */
    private void logDatabaseState(String context) {
        long userCount = userRepository.count();
        long profileCount = sitterProfileRepository.count();

        System.out.println("🗃️  H2 Database State - " + context + ":");
        System.out.println("   Users: " + userCount);
        System.out.println("   Sitter Profiles: " + profileCount);
    }

    // =====================================================================================
    // ========================= MÉTODOS DE UTILIDAD HEREDADOS ===========================
    // =====================================================================================

    /**
     * Crea, configura y persiste un usuario con el rol especificado.
     */
    private User createAndSaveUser(String email, Role role) {
        User user = new User("Test", "User", email, passwordEncoder.encode(DEFAULT_PASSWORD),
                "123 Test Street", "555-1234", role);
        user.setEmailVerifiedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Crea y persiste un perfil de cuidador para el usuario especificado.
     */
    private SitterProfile createSitterProfileForUser(User user) {
        SitterProfile profile = new SitterProfile(user, DEFAULT_BIO, DEFAULT_HOURLY_RATE,
                DEFAULT_RADIUS, DEFAULT_IMAGE_URL);
        profile.setVerified(true);
        profile.setAvailableForBookings(true);
        return sitterProfileRepository.save(profile);
    }

    /**
     * Crea un DTO de perfil de cuidador válido para pruebas.
     */
    private SitterProfileDTO createValidSitterProfileDTO(Long userId) {
        return new SitterProfileDTO(null, userId, DEFAULT_BIO, DEFAULT_HOURLY_RATE,
                DEFAULT_RADIUS, DEFAULT_IMAGE_URL, true, true);
    }

    /**
     * Extrae y deserializa la respuesta HTTP a un objeto del tipo especificado.
     */
    private <T> T extractResponse(ResultActions result, Class<T> clazz) throws Exception {
        String responseContent = result.andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(responseContent, clazz);
    }
}