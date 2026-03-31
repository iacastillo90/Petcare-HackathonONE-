package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceMapper;
import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceRequestDTO;
import com.Petcare.Petcare.Models.SitterProfile;
import com.Petcare.Petcare.Models.SitterWorkExperience;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.SitterProfileRepository;
import com.Petcare.Petcare.Repositories.SitterWorkExperienceRepository;
import com.Petcare.Petcare.Repositories.UserRepository;
import com.Petcare.Petcare.Configurations.Security.Jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para {@link SitterWorkExperienceController} utilizando H2.
 *
 * <p>Esta suite de pruebas valida el comportamiento de los endpoints de la API para la gestión
 * de experiencias laborales de cuidadores. Se utiliza una base de datos H2 en memoria para
 * simular el entorno de persistencia real, y {@link MockMvc} para simular las peticiones HTTP.
 * Cada prueba es transaccional, asegurando el aislamiento y la limpieza de datos entre ejecuciones.</p>
 *
 * <p><strong>Estrategia de Pruebas:</strong></p>
 * <ul>
 * <li><b>Flujo Completo:</b> Se prueba desde la capa de controlador hasta la base de datos.</li>
 * <li><b>Seguridad:</b> Se validan las reglas de autorización de Spring Security para cada endpoint.</li>
 * <li><b>Validación de Datos:</b> Se comprueba el manejo de DTOs válidos e inválidos.</li>
 * <li><b>Casos de Éxito y Fallo:</b> Se cubren escenarios exitosos, errores de cliente (4xx) y lógicos.</li>
 * </ul>
 *
 * @see SitterWorkExperienceController
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Pruebas de Integración H2: SitterWorkExperienceController")
class SitterWorkExperienceControllerTest {

    /**
     * Limpia el contexto de seguridad después de cada test para evitar
     * contaminación entre tests.
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Repositorios para preparar el estado de la BD y verificar los resultados
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SitterProfileRepository sitterProfileRepository;

    @Autowired
    private SitterWorkExperienceRepository workExperienceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    // Constantes y datos de prueba reutilizables
    private static final String BASE_URL = "/api/sitter-work-experience";
    private User sitterUser, adminUser, clientUser;
    private SitterProfile sitterProfile;
    private String sitterToken, adminToken, clientToken;

    /**
     * Prepara el entorno antes de cada prueba.
     * Crea usuarios con diferentes roles, genera sus tokens de autenticación
     * y crea un perfil de cuidador base para las pruebas.
     */
    @BeforeEach
    void setUp() {
        // Limpieza explícita por si acaso, aunque @Transactional debería bastar.
        workExperienceRepository.deleteAll();
        sitterProfileRepository.deleteAll();
        userRepository.deleteAll();

        // Crear usuarios con roles distintos
        sitterUser = createAndSaveUser("sitter@petcare.com", Role.SITTER);
        adminUser = createAndSaveUser("admin@petcare.com", Role.ADMIN);
        clientUser = createAndSaveUser("client@petcare.com", Role.CLIENT);

        // Generar tokens para cada usuario
        sitterToken = jwtService.getToken(sitterUser);
        adminToken = jwtService.getToken(adminUser);
        clientToken = jwtService.getToken(clientUser);

        // Crear un perfil de cuidador para el usuario SITTER
        sitterProfile = createSitterProfileForUser(sitterUser);
    }

    /**
     * Contiene las pruebas para el endpoint {@code POST /api/sitter-work-experience}.
     */
    @Nested
    @DisplayName("POST /api/sitter-work-experience - Crear Experiencia Laboral")
    class CreateWorkExperienceTests {

        /**
         * Verifica el caso de éxito donde un cuidador (SITTER) crea una experiencia
         * para su propio perfil.
         *
         * @throws Exception Si ocurre un error en la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Debería crear experiencia si el usuario es SITTER y propietario del perfil")
        void shouldCreateExperience_whenUserIsSitterOwner() throws Exception {
            // Arrange
            SitterWorkExperienceRequestDTO requestDTO = createValidRequestDTO(sitterProfile.getId());
            long initialCount = workExperienceRepository.count();

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL)
                    .header("Authorization", "Bearer " + sitterToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)));

            // Assert (Respuesta HTTP)
            result.andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.companyName").value("Pet Care Excellence"));

            // Assert (Estado de la Base de Datos)
            assertThat(workExperienceRepository.count()).isEqualTo(initialCount + 1);
        }

        /**
         * Verifica el caso de éxito donde un administrador (ADMIN) crea una experiencia
         * para el perfil de cualquier cuidador.
         *
         * @throws Exception Si ocurre un error en la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Debería crear experiencia si el usuario es ADMIN")
        void shouldCreateExperience_whenUserIsAdmin() throws Exception {
            // Arrange
            SitterWorkExperienceRequestDTO requestDTO = createValidRequestDTO(sitterProfile.getId());
            long initialCount = workExperienceRepository.count();

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL)
                    .header("Authorization", "Bearer " + adminToken) // Usando token de ADMIN
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)));

            // Assert
            result.andExpect(status().isCreated());
            assertThat(workExperienceRepository.count()).isEqualTo(initialCount + 1);
        }

        /**
         * Verifica que un usuario con rol CLIENT no pueda crear una experiencia laboral,
         * esperando una respuesta 403 Forbidden.
         *
         * @throws Exception Si ocurre un error en la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Fallo [403]: Debería devolver Forbidden si el usuario es CLIENT")
        void shouldReturnForbidden_whenUserIsClient() throws Exception {
            // Arrange
            SitterWorkExperienceRequestDTO requestDTO = createValidRequestDTO(sitterProfile.getId());

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + clientToken) // Usando token de CLIENT
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden());
        }

        /**
         * Verifica que se devuelva un 404 Not Found si se intenta crear una experiencia
         * para un perfil de cuidador que no existe.
         *
         * @throws Exception Si ocurre un error en la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Fallo [404]: Debería devolver Not Found si SitterProfileId no existe")
        void shouldReturnNotFound_whenSitterProfileDoesNotExist() throws Exception {
            // Arrange
            long nonExistentProfileId = 999L;
            SitterWorkExperienceRequestDTO requestDTO = createValidRequestDTO(nonExistentProfileId);

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound());
        }

        /**
         * Verifica que se devuelva un 400 Bad Request si los datos de la solicitud
         * son inválidos (p. ej., campos obligatorios nulos).
         *
         * @throws Exception Si ocurre un error en la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Fallo [400]: Debería devolver Bad Request si los datos son inválidos")
        void shouldReturnBadRequest_whenDataIsInvalid() throws Exception {
            // Arrange
            SitterWorkExperienceRequestDTO invalidRequest = new SitterWorkExperienceRequestDTO();
            invalidRequest.setSitterProfileId(null); // Campo obligatorio nulo
            invalidRequest.setStartDate(null);       // Campo obligatorio nulo

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + sitterToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors.sitterProfileId").value("El ID del perfil de cuidador es obligatorio"))
                    .andExpect(jsonPath("$.validationErrors.startDate").value("La fecha de inicio es obligatoria"));
        }
    }

    /**
     * Contiene las pruebas para el endpoint {@code GET /api/sitter-work-experience/sitter/{sitterProfileId}}.
     */
    @Nested
    @DisplayName("GET /sitter/{sitterProfileId} - Obtener Experiencias por Perfil")
    class GetWorkExperiencesBySitterProfileTests {

        /**
         * Verifica el caso de éxito donde se solicitan las experiencias de un perfil que sí tiene registros.
         * Se asegura de que cualquier usuario autenticado pueda ver esta información pública.
         *
         * @throws Exception Si ocurre un error en la petición MockMvc.
         */
        /**
         * Verifica el caso de éxito donde se solicitan las experiencias de un perfil que sí tiene registros.
         * Se asegura de que cualquier usuario autenticado pueda ver esta información pública.
         *
         * @throws Exception Si ocurre un error en la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Debería devolver la lista de experiencias para un perfil válido")
        void shouldReturnExperienceList_whenProfileExistsAndHasExperiences() throws Exception {
            // Arrange
            // 1. Añadimos dos experiencias laborales directamente al perfil del cuidador en la BD H2.
            addWorkExperienceToProfile(sitterProfile, "Clínica Veterinaria Amigos Fieles", "Asistente Veterinario");
            addWorkExperienceToProfile(sitterProfile, "Refugio de Animales San Roque", "Voluntario de Cuidados");

            // Act
            // 2. Realizamos la petición como un cliente (CLIENT), ya que este endpoint es público para usuarios logueados.
            ResultActions result = mockMvc.perform(get(BASE_URL + "/sitter/{sitterProfileId}", sitterProfile.getId())
                    .header("Authorization", "Bearer " + clientToken));

            // Assert
            // 3. Verificamos que la respuesta sea 200 OK y que el cuerpo JSON sea un array con 2 elementos.
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].companyName").value("Clínica Veterinaria Amigos Fieles"))
                    .andExpect(jsonPath("$[1].jobTitle").value("Voluntario de Cuidados"));
        }


        /**
         * Verifica el caso de borde donde un perfil de cuidador existe pero aún no ha añadido ninguna
         * experiencia laboral. La API debe responder con éxito, pero con una lista vacía.
         *
         * @throws Exception Si ocurre un error en la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Debería devolver una lista vacía si el perfil existe pero no tiene experiencias")
        void shouldReturnEmptyList_whenProfileExistsButHasNoExperiences() throws Exception {
            // Arrange
            // No añadimos ninguna experiencia al 'sitterProfile' creado en setUp().

            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL + "/sitter/{sitterProfileId}", sitterProfile.getId())
                    .header("Authorization", "Bearer " + adminToken));

            // Assert
            // La respuesta debe ser 200 OK y el cuerpo un array JSON vacío `[]`.
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        /**
         * Verifica el manejo de errores cuando se solicita un ID de perfil de cuidador que no existe.
         * La API debe responder con un código 404 Not Found.
         *
         * @throws Exception Si ocurre un error en la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Fallo [404]: Debería devolver Not Found si el SitterProfileId no existe")
        void shouldReturnNotFound_whenSitterProfileDoesNotExist() throws Exception {
            // Arrange
            long nonExistentProfileId = 999L;

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/sitter/{sitterProfileId}", nonExistentProfileId)
                            .header("Authorization", "Bearer " + sitterToken))
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * Contiene las pruebas para el endpoint {@code GET /api/sitter-work-experience/{id}}.
     */
    @Nested
    @DisplayName("GET /{id} - Obtener Experiencia por su ID")
    class GetWorkExperienceByIdTests {

        /**
         * Verifica el caso de éxito donde se solicita una experiencia laboral por su ID y esta existe.
         * Se asegura de que cualquier usuario autenticado pueda ver los detalles completos, ya que
         * la información del historial laboral se considera pública dentro de la plataforma.
         *
         * @throws Exception Si ocurre un error en la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Debería devolver los detalles completos de una experiencia existente")
        void shouldReturnFullExperienceDetails_whenExperienceExists() throws Exception {
            // Arrange
            // 1. Creamos y persistimos una experiencia de prueba en la BD H2.
            SitterWorkExperience experience = addWorkExperienceToProfile(
                    sitterProfile,
                    "Clínica Veterinaria Amigos Fieles",
                    "Asistente Veterinario"
            );
            // Obtenemos el ID generado por la base de datos para usarlo en la petición.
            Long experienceId = experience.getId();

            // Act
            // 2. Realizamos la petición GET al endpoint específico, usando el token de un CLIENT
            // para demostrar que el acceso es permitido para cualquier rol autenticado.
            ResultActions result = mockMvc.perform(get(BASE_URL + "/{id}", experienceId)
                    .header("Authorization", "Bearer " + clientToken));

            // Assert
            // 3. Verificamos que la respuesta sea 200 OK y que el cuerpo JSON contenga
            // todos los campos detallados del DTO de respuesta.
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(experienceId))
                    .andExpect(jsonPath("$.companyName").value("Clínica Veterinaria Amigos Fieles"))
                    .andExpect(jsonPath("$.jobTitle").value("Asistente Veterinario"))
                    .andExpect(jsonPath("$.responsibilities").value("Paseos diarios y entrenamiento básico."));
        }

        /**
         * Verifica el manejo de errores cuando se solicita un ID de experiencia laboral que no existe.
         * La API debe responder con un código 404 Not Found gracias al manejador de excepciones
         * que hemos mejorado.
         *
         * @throws Exception Si ocurre un error en la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Fallo [404]: Debería devolver Not Found si el ID de la experiencia no existe")
        void shouldReturnNotFound_whenExperienceIdDoesNotExist() throws Exception {
            // Arrange
            long nonExistentExperienceId = 999L;

            // Act & Assert
            // Realizamos la petición con un ID inexistente y esperamos un 404.
            mockMvc.perform(get(BASE_URL + "/{id}", nonExistentExperienceId)
                            .header("Authorization", "Bearer " + adminToken)) // Un admin también recibe 404
                    .andExpect(status().isNotFound());
        }
    }

    // --- Métodos de Ayuda (Helpers) ---

    /**
     * Crea y persiste un usuario en la base de datos H2 para las pruebas.
     *
     * @param email El email del usuario (debe ser único).
     * @param role El rol a asignar al usuario.
     * @return La entidad User persistida.
     */
    private User createAndSaveUser(String email, Role role) {
        User user = new User("Test", role.name(), email, passwordEncoder.encode("password123"),
                "Dirección de prueba", "123456789", role);
        user.setEmailVerifiedAt(LocalDateTime.now());
        return userRepository.saveAndFlush(user);
    }

    /**
     * Crea y persiste un perfil de cuidador para un usuario.
     *
     * @param user El usuario al que se le asociará el perfil.
     * @return La entidad SitterProfile persistida.
     */
    private SitterProfile createSitterProfileForUser(User user) {
        SitterProfile profile = new SitterProfile(user, "Bio de prueba", new BigDecimal("15.0"), 10, null);
        return sitterProfileRepository.saveAndFlush(profile);
    }

    /**
     * Crea un DTO de solicitud válido para las pruebas.
     *
     * @param profileId El ID del perfil al que se asociará la experiencia.
     * @return Una instancia de {@link SitterWorkExperienceRequestDTO}.
     */
    private SitterWorkExperienceRequestDTO createValidRequestDTO(Long profileId) {
        SitterWorkExperienceRequestDTO dto = new SitterWorkExperienceRequestDTO();
        dto.setSitterProfileId(profileId);
        dto.setCompanyName("Pet Care Excellence");
        dto.setJobTitle("Senior Dog Walker");
        dto.setResponsibilities("Paseos diarios y entrenamiento básico.");
        dto.setStartDate(LocalDate.of(2021, 1, 15));
        dto.setEndDate(LocalDate.of(2023, 5, 20));
        return dto;
    }

    /**
     * Método de ayuda para añadir y persistir una experiencia laboral a un perfil.
     * Este método ha sido CORREGIDO para manejar adecuadamente la relación bidireccional.
     *
     * @param profile El perfil al que se añadirá la experiencia.
     * @param companyName El nombre de la empresa.
     * @param jobTitle El título del puesto.
     * @return La entidad SitterWorkExperience persistida con su ID generado.
     */
    private SitterWorkExperience addWorkExperienceToProfile(SitterProfile profile, String companyName, String jobTitle) {
        SitterWorkExperienceRequestDTO dto = new SitterWorkExperienceRequestDTO();
        dto.setSitterProfileId(profile.getId());
        dto.setCompanyName(companyName);
        dto.setJobTitle(jobTitle);
        dto.setResponsibilities("Paseos diarios y entrenamiento básico.");
        dto.setStartDate(LocalDate.now().minusYears(2));
        dto.setEndDate(LocalDate.now().minusYears(1));

        // Mapeamos el DTO a la entidad, lo que establece la relación desde la experiencia hacia el perfil.
        SitterWorkExperience experience = SitterWorkExperienceMapper.toEntity(dto, profile);

        // *** LA CORRECCIÓN CLAVE ***
        // Añadimos la experiencia a la colección en memoria del perfil.
        // Esto mantiene la consistencia para la sesión de Hibernate.
        profile.getWorkExperiences().add(experience);

        // Guardamos y flusheamos la experiencia.
        // Como la relación está establecida, el FK se persistirá correctamente.
        return workExperienceRepository.saveAndFlush(experience);
    }



}