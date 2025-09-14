package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.Configurations.Security.Jwt.JwtService;
import com.Petcare.Petcare.DTOs.ServiceOffering.CreateServiceOfferingDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.UpdateServiceOfferingDTO;
import com.Petcare.Petcare.Models.ServiceOffering.ServiceOffering;
import com.Petcare.Petcare.Models.ServiceOffering.ServiceType;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.ServiceOfferingRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración exhaustivas para {@link ServiceOfferingController} usando base de datos H2 en memoria.
 *
 * <p>Esta suite de pruebas válida el comportamiento de los endpoints de la API para la gestión
 * de ofertas de servicios de cuidadores. Se utiliza una base de datos H2 en memoria para
 * simular el entorno de persistencia real y {@link MockMvc} para simular las peticiones HTTP.</p>
 *
 * <p><strong>Filosofía de testing:</strong></p>
 * <ul>
 * <li>Pruebas end-to-end que cubren el flujo completo HTTP → Service → DB (cuando aplique).</li>
 * <li>Uso de {@code @Transactional} para asegurar que cada prueba se ejecute en una transacción aislada y se revierta al finalizar.</li>
 * <li>Validación tanto de los códigos de estado HTTP como del contenido de las respuestas JSON.</li>
 * <li>Simulación de peticiones HTTP realistas para validar el comportamiento del controlador.</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see ServiceOfferingController
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Pruebas de Integración con H2: ServiceOfferingController")
class ServiceOfferingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ServiceOfferingRepository serviceOfferingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;


    /**
     * Limpia las tablas relacionadas antes de cada prueba anidada para asegurar un estado limpio.
     */
    @BeforeEach
    void setUp() {
        serviceOfferingRepository.deleteAll();
        userRepository.deleteAll();
    }


    /**
     * Contiene las pruebas para el endpoint de verificación de estado {@code GET /api/services/test}.
     */
    @Nested
    @DisplayName("GET /api/services/test - Health Check")
    class HealthCheckTests {

        /**
         * Válida que el endpoint de "health check" responde correctamente.
         *
         * <p><strong>Escenario:</strong> Se realiza una petición GET al endpoint público {@code /api/services/test}.</p>
         * <p><strong>Resultado esperado:</strong></p>
         * <ul>
         * <li>Un código de estado HTTP 200 (OK).</li>
         * <li>Una respuesta en formato JSON.</li>
         * <li>El cuerpo de la respuesta debe contener los campos {@code status}, {@code message}, y {@code timestamp} con los valores esperados.</li>
         * </ul>
         * <p>Esta prueba no requiere autenticación ni interacción con la base de datos, verificando únicamente que la capa del controlador está activa y respondiendo.</p>
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Debería retornar 200 OK con un mensaje de estado operativo")
        void testEndpoint_ShouldReturnOkAndUpStatus() throws Exception {
            // Act
            ResultActions result = mockMvc.perform(get("/api/services/test")
                    .contentType(MediaType.APPLICATION_JSON));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status", is("UP")))
                    .andExpect(jsonPath("$.message", is("ServiceOfferingController está operativo.")))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }
    }

    /**
     * Contiene las pruebas para el endpoint público {@code GET /api/services}.
     */
    @Nested
    @DisplayName("GET /api/services - Listar Todos los Servicios")
    class GetAllServicesTests {

        /**
         * Válida el caso de éxito donde existen servicios activos en la base de datos.
         *
         * <p><strong>Escenario:</strong> Se preparan varias ofertas de servicio en la base de datos H2,
         * incluyendo una activa y una inactiva.</p>
         * <p><strong>Resultado esperado:</strong></p>
         * <ul>
         * <li>Un código de estado HTTP 200 (OK).</li>
         * <li>Una respuesta JSON que es un array.</li>
         * <li>El array debe contener <strong>solo</strong> los servicios activos (en este caso, 2).</li>
         * <li>Los campos de los objetos en el array deben coincidir con los datos de los servicios activos creados.</li>
         * </ul>
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Debería retornar 200 OK con la lista de servicios activos si existen")
        void shouldReturnServiceList_whenServicesExist() throws Exception {
            // Arrange: Preparamos la base de datos con datos de prueba.
            User sitter1 = createAndSaveUser("sitter1@petcare.com", Role.SITTER);
            User sitter2 = createAndSaveUser("sitter2@petcare.com", Role.SITTER);

            // Servicio 1 (Activo)
            createAndSaveService(sitter1, "Paseo de 30 min", true);
            // Servicio 2 (Activo)
            createAndSaveService(sitter2, "Guardería Diurna", true);
            // Servicio 3 (Inactivo - NO debe aparecer en la respuesta)
            createAndSaveService(sitter1, "Corte de Pelo", false);

            // Act: Realizamos la petición GET al endpoint principal.
            ResultActions result = mockMvc.perform(get("/api/services")
                    .contentType(MediaType.APPLICATION_JSON));

            // Assert: Verificamos la respuesta.
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2))) // Solo esperamos los 2 servicios activos
                    .andExpect(jsonPath("$[0].name", is("Paseo de 30 min")))
                    .andExpect(jsonPath("$[1].name", is("Guardería Diurna")));
        }


        /**
         * Válida el caso de borde donde no hay ninguna oferta de servicio en la base de datos.
         *
         * <p><strong>Escenario:</strong> La tabla `service_offering` está vacía.</p>
         * <p><strong>Resultado esperado:</strong></p>
         * <ul>
         * <li>Un código de estado HTTP 204 (No Content).</li>
         * <li>El cuerpo de la respuesta debe estar vacío.</li>
         * </ul>
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Debería retornar 204 No Content si no existen servicios")
        void shouldReturnNoContent_whenNoServicesExist() throws Exception {
            // Arrange: No insertamos ningún dato, la BD está limpia gracias a @Transactional.

            // Act
            ResultActions result = mockMvc.perform(get("/api/services")
                    .contentType(MediaType.APPLICATION_JSON));

            // Assert
            result.andExpect(status().isNoContent());
        }
    }

    /**
     * Contiene las pruebas para el endpoint de creación de servicios {@code POST /api/services/create/{id}}.
     */
    @Nested
    @DisplayName("POST /api/services/create/{id} - Crear Servicio")
    class CreateServiceOfferingTests {

        private User sitterUser;
        private User adminUser;
        private User clientUser;
        private String sitterToken;
        private String adminToken;
        private String clientToken;

        /**
         * Prepara usuarios con diferentes roles y sus tokens antes de cada prueba de este grupo.
         */
        @BeforeEach
        void setUp() {
            // Se crean usuarios frescos para este conjunto de pruebas para asegurar el aislamiento
            sitterUser = createAndSaveUser("sitter.creator@petcare.com", Role.SITTER);
            adminUser = createAndSaveUser("admin.creator@petcare.com", Role.ADMIN);
            clientUser = createAndSaveUser("client.creator@petcare.com", Role.CLIENT);

            sitterToken = jwtService.getToken(sitterUser);
            adminToken = jwtService.getToken(adminUser);
            clientToken = jwtService.getToken(clientUser);
        }

        /**
         * Válida el caso de éxito donde un cuidador (SITTER) crea un servicio para sí mismo.
         *
         * <p><strong>Escenario:</strong> Un usuario con rol SITTER envía una petición POST válida a {@code /api/services/create/{id}}, donde {id} es su propio ID de usuario.</p>
         * <p><strong>Resultado esperado:</strong></p>
         * <ul>
         * <li>Un código de estado HTTP 201 (Created).</li>
         * <li>El cuerpo de la respuesta contiene el DTO del servicio recién creado.</li>
         * <li>Un nuevo registro de {@code ServiceOffering} se persiste en la base de datos H2.</li>
         * </ul>
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Un SITTER debería poder crear un servicio para sí mismo")
        void createService_AsSitterForSelf_ShouldSucceed() throws Exception {
            // Arrange
            CreateServiceOfferingDTO requestDTO = createValidRequestDTO();
            long initialCount = serviceOfferingRepository.count();

            // Act
            ResultActions result = mockMvc.perform(post("/api/services/create/{id}", sitterUser.getId())
                    .header("Authorization", "Bearer " + sitterToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)));

            // Assert
            result.andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.sitterId", is(sitterUser.getId().intValue())))
                    .andExpect(jsonPath("$.name", is("Paseo Matutino (30 min)")));

            assertThat(serviceOfferingRepository.count()).isEqualTo(initialCount + 1);
        }

        /**
         * Válida el caso de éxito donde un administrador (ADMIN) crea un servicio para un cuidador.
         *
         * <p><strong>Escenario:</strong> Un usuario con rol ADMIN crea un servicio para un usuario SITTER existente.</p>
         * <p><strong>Resultado esperado:</strong> Un código de estado HTTP 201 (Created) y la persistencia del nuevo servicio.</p>
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Un ADMIN debería poder crear un servicio para cualquier Sitter")
        void createService_AsAdminForSitter_ShouldSucceed() throws Exception {
            // Arrange
            CreateServiceOfferingDTO requestDTO = createValidRequestDTO();

            // Act & Assert
            mockMvc.perform(post("/api/services/create/{id}", sitterUser.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated());
        }

        /**
         * Válida que un usuario con rol CLIENT no puede crear una oferta de servicio.
         *
         * <p><strong>Escenario:</strong> Un usuario con rol CLIENT intenta acceder al endpoint de creación.</p>
         * <p><strong>Resultado esperado:</strong> Un código de estado HTTP 403 (Forbidden).</p>
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (Autorización): Un CLIENT no debería poder crear servicios y debe recibir 403")
        void createService_AsClient_ShouldReturnForbidden() throws Exception {
            // Arrange
            CreateServiceOfferingDTO requestDTO = createValidRequestDTO();

            // Act & Assert
            mockMvc.perform(post("/api/services/create/{id}", clientUser.getId())
                            .header("Authorization", "Bearer " + clientToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden());
        }

        /**
         * Válida que la creación de un servicio falla si el cuidador ya tiene otro con el mismo nombre.
         *
         * <p><strong>Escenario:</strong> Se intenta crear un servicio con un nombre que ya existe para ese cuidador.</p>
         * <p><strong>Resultado esperado:</strong> Un código de estado HTTP 409 (Conflict).</p>
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (Conflicto): Debería retornar 409 si el servicio ya existe para ese Sitter")
        void createService_WhenNameIsDuplicateForSitter_ShouldReturnConflict() throws Exception {
            // Arrange: Creamos un primer servicio
            createAndSaveService(sitterUser, "Servicio Repetido", true);
            CreateServiceOfferingDTO requestDTO = new CreateServiceOfferingDTO(
                    ServiceType.SITTING, "Servicio Repetido", "Una descripción lo suficientemente larga.", new BigDecimal("20.00"), 30
            );

            // Act & Assert
            mockMvc.perform(post("/api/services/create/{id}", sitterUser.getId())
                            .header("Authorization", "Bearer " + sitterToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isConflict());
        }

        /**
         * Válida que la creación de un servicio falla si el ID del cuidador no existe en la base de datos.
         *
         * <p><strong>Escenario:</strong> Se intenta crear un servicio para un `sitterId` que no corresponde a ningún usuario.</p>
         * <p><strong>Resultado esperado:</strong> Un código de estado HTTP 404 (Not Found).</p>
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (No Encontrado): Debería retornar 404 si el SitterId no existe")
        void createService_WhenSitterIdNotFound_ShouldReturnNotFound() throws Exception {
            // Arrange
            long nonExistentSitterId = 9999L;
            CreateServiceOfferingDTO requestDTO = createValidRequestDTO();

            // Act & Assert
            mockMvc.perform(post("/api/services/create/{id}", nonExistentSitterId)
                            .header("Authorization", "Bearer " + adminToken) // Usamos admin para pasar la autorización
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound());
        }

        /**
         * Válida el manejo de datos de entrada inválidos.
         *
         * <p><strong>Escenario:</strong> La petición contiene datos que no cumplen con las validaciones del DTO (ej. precio nulo, nombre en blanco).</p>
         * <p><strong>Resultado esperado:</strong> Un código de estado HTTP 400 (Bad Request) con los detalles de los errores de validación.</p>
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (Validación): Debería retornar 400 si los datos del DTO son inválidos")
        void createService_WithInvalidData_ShouldReturnBadRequest() throws Exception {
            // Arrange
            // CORRECCIÓN: Devolver el 'name' a un estado inválido ("") para que la prueba tenga sentido.
            CreateServiceOfferingDTO invalidDTO = new CreateServiceOfferingDTO(
                    null, // serviceType nulo
                    "",   // name en blanco (inválido)
                    "desc", // description demasiado corto (inválido)
                    new BigDecimal("-10.00"), // price negativo (inválido)
                    10    // durationInMinutes menor a 15 (inválido)
            );

            // Act & Assert
            mockMvc.perform(post("/api/services/create/{id}", sitterUser.getId())
                            .header("Authorization", "Bearer " + sitterToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors.serviceType", is("El tipo de servicio es obligatorio")))
                    // CORRECCIÓN: Ahora que 'name' es inválido, esta aserción volverá a funcionar correctamente.
                    .andExpect(jsonPath("$.validationErrors.name", is("El nombre del servicio es obligatorio")))
                    .andExpect(jsonPath("$.validationErrors.price", is("El precio debe ser mayor a 0")))
                    .andExpect(jsonPath("$.validationErrors.description", is("La descripción debe tener entre 10 y 500 caracteres")))
                    .andExpect(jsonPath("$.validationErrors.durationInMinutes", is("La duración mínima del servicio es 15 minutos")));
        }
    }

    /**
     * Contiene las pruebas para el endpoint de consulta de servicio por ID {@code GET /api/services/{id}}.
     */
    @Nested
    @DisplayName("GET /api/services/{id} - Obtener Servicio por ID")
    class GetServiceByIdTests {

        private User clientUser;
        private String clientToken;
        private ServiceOffering existingService;

        /**
         * Prepara los datos necesarios para las pruebas de consulta: un usuario cliente para realizar
         * la petición y un servicio existente para ser consultado.
         */
        @BeforeEach
        void setUp() {
            clientUser = createAndSaveUser("client.viewer@petcare.com", Role.CLIENT);
            clientToken = jwtService.getToken(clientUser);

            User sitterOwner = createAndSaveUser("sitter.owner@petcare.com", Role.SITTER);
            existingService = createAndSaveService(sitterOwner, "Paseo Estándar", true);
        }

        /**
         * Válida el caso de éxito donde un usuario autenticado solicita un servicio existente.
         *
         * <p><strong>Escenario:</strong> Un usuario con rol CLIENT (o cualquier otro rol autenticado) realiza una petición GET a {@code /api/services/{id}} con un ID válido.</p>
         * <p><strong>Resultado esperado:</strong></p>
         * <ul>
         * <li>Un código de estado HTTP 200 (OK).</li>
         * <li>El cuerpo de la respuesta contiene el DTO del servicio solicitado, con todos sus campos correctos.</li>
         * </ul>
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Un usuario autenticado debería obtener los detalles de un servicio existente")
        void getServiceById_AsAuthenticatedUser_ShouldReturnServiceDetails() throws Exception {
            // Act
            ResultActions result = mockMvc.perform(get("/api/services/{id}", existingService.getId())
                    .header("Authorization", "Bearer " + clientToken));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(existingService.getId().intValue())))
                    .andExpect(jsonPath("$.name", is("Paseo Estándar")))
                    .andExpect(jsonPath("$.price", is(15.00)))
                    .andExpect(jsonPath("$.isActive", is(true)));
        }

        /**
         * Válida que la API responde con un 404 Not Found cuando se solicita un ID de servicio que no existe.
         *
         * <p><strong>Escenario:</strong> Un usuario autenticado realiza una petición GET con un ID que no corresponde a ningún servicio en la base de datos.</p>
         * <p><strong>Resultado esperado:</strong> Un código de estado HTTP 404 (Not Found).</p>
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (No Encontrado): Debería retornar 404 si el ID del servicio no existe")
        void getServiceById_WhenIdDoesNotExist_ShouldReturnNotFound() throws Exception {
            // Arrange
            long nonExistentId = 9999L;

            // Act & Assert
            mockMvc.perform(get("/api/services/{id}", nonExistentId)
                            .header("Authorization", "Bearer " + clientToken))
                    .andExpect(status().isNotFound());
        }

        /**
         * Válida que el endpoint está protegido y requiere autenticación.
         *
         * <p><strong>Escenario:</strong> Se realiza una petición GET sin proporcionar un token de autenticación en la cabecera.</p>
         * <p><strong>Resultado esperado:</strong> Un código de estado HTTP 403 (Forbidden), según la configuración de seguridad que deniega el acceso a usuarios anónimos.</p>
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (Autenticación): Debería retornar 403 si no se proporciona token")
        void getServiceById_WithoutToken_ShouldReturnForbidden() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/services/{id}", existingService.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    /**
     * Contiene las pruebas para el endpoint {@code GET /api/services/all/{id}}.
     */
    @Nested
    @DisplayName("GET /api/services/all/{id} - Listar Servicios por ID de Cuidador")
    class GetAllServicesByUserIdTests {

        private User sitterUser;
        private User anotherSitter;
        private String clientToken;

        /**
         * Prepara los datos necesarios para las pruebas: un cuidador con servicios,
         * otro sin servicios y un cliente autenticado para realizar las consultas.
         */
        @BeforeEach
        void setUp() {
            sitterUser = createAndSaveUser("sitter.with.services@petcare.com", Role.SITTER);
            anotherSitter = createAndSaveUser("sitter.without.services@petcare.com", Role.SITTER);
            User clientUser = createAndSaveUser("client.viewer@petcare.com", Role.CLIENT);
            clientToken = jwtService.getToken(clientUser);

            // Añadir servicios solo al primer cuidador
            createAndSaveService(sitterUser, "Paseo de Tarde", true);
            createAndSaveService(sitterUser, "Visita a Domicilio", true);
        }

        /**
         * Válida el caso de éxito donde se solicita el catálogo de un cuidador que tiene múltiples servicios.
         *
         * <p><strong>Escenario:</strong> Un cliente autenticado solicita los servicios de un cuidador (`sitterUser`) que tiene dos ofertas activas.</p>
         * <p><strong>Resultado esperado:</strong></p>
         * <ul>
         * <li>Un código de estado HTTP 200 (OK).</li>
         * <li>El cuerpo de la respuesta es un array JSON que contiene exactamente dos elementos.</li>
         * <li>Los datos de los servicios en la respuesta coinciden con los que se crearon.</li>
         * </ul>
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Debería retornar 200 OK con la lista de servicios del cuidador")
        void shouldReturnServiceList_whenSitterHasServices() throws Exception {
            // Act
            ResultActions result = mockMvc.perform(get("/api/services/all/{id}", sitterUser.getId())
                    .header("Authorization", "Bearer " + clientToken));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name", is("Paseo de Tarde")))
                    .andExpect(jsonPath("$[1].sitterId", is(sitterUser.getId().intValue())));
        }

        /**
         * Válida el caso de borde donde un cuidador existe pero aún no ha registrado ningún servicio.
         *
         * <p><strong>Escenario:</strong> Se solicitan los servicios de un cuidador (`anotherSitter`) que no tiene ninguna oferta asociada.</p>
         * <p><strong>Resultado esperado:</strong> Un código de estado HTTP 204 (No Content).</p>
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Debería retornar 204 No Content si el cuidador no tiene servicios")
        void shouldReturnNoContent_whenSitterHasNoServices() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/services/all/{id}", anotherSitter.getId())
                            .header("Authorization", "Bearer " + clientToken))
                    .andExpect(status().isNoContent());
        }

        /**
         * Válida que la API responde con 404 Not Found si se solicita el catálogo de un ID de usuario que no existe.
         *
         * <p><strong>Escenario:</strong> Se realiza una petición para un ID de usuario que no está en la base de datos.</p>
         * <p><strong>Resultado esperado:</strong> Un código de estado HTTP 404 (Not Found).</p>
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (No Encontrado): Debería retornar 404 si el ID del cuidador no existe")
        void shouldReturnNotFound_whenSitterIdDoesNotExist() throws Exception {
            // Arrange
            long nonExistentSitterId = 9999L;

            // Act & Assert
            mockMvc.perform(get("/api/services/all/{id}", nonExistentSitterId)
                            .header("Authorization", "Bearer " + clientToken))
                    .andExpect(status().isNotFound());
        }

        /**
         * Válida que el endpoint está protegido y requiere un token de autenticación.
         *
         * <p><strong>Escenario:</strong> Se realiza una petición sin la cabecera `Authorization`.</p>
         * <p><strong>Resultado esperado:</strong> Un código de estado HTTP 403 (Forbidden).</p>
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (Autenticación): Debería retornar 403 si no se proporciona token")
        void shouldReturnForbidden_whenNotAuthenticated() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/services/all/{id}", sitterUser.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    /**
     * Contiene las pruebas para el endpoint de actualización de servicios {@code PATCH /api/services/{id}}.
     */
    @Nested
    @DisplayName("PATCH /api/services/{id} - Actualizar Servicio")
    class UpdateServiceTests {

        private User sitterOwner;
        private User anotherSitter;
        private User adminUser;
        private User clientUser;
        private String ownerToken;
        private String adminToken;
        private String clientToken;
        private ServiceOffering existingService;

        /**
         * Prepara el entorno para las pruebas de actualización.
         * Crea un cuidador propietario, un servicio para él, un administrador y un cliente.
         * Genera los tokens de autenticación para cada rol.
         */
        @BeforeEach
        void setUp() {
            sitterOwner = createAndSaveUser("sitter.owner@petcare.com", Role.SITTER);
            anotherSitter = createAndSaveUser("another.sitter@petcare.com", Role.SITTER);
            adminUser = createAndSaveUser("admin.updater@petcare.com", Role.ADMIN);
            clientUser = createAndSaveUser("client.updater@petcare.com", Role.CLIENT);

            ownerToken = jwtService.getToken(sitterOwner);
            adminToken = jwtService.getToken(adminUser);
            clientToken = jwtService.getToken(clientUser);

            existingService = createAndSaveService(sitterOwner, "Servicio Original", true);
        }

        /**
         * Válida el caso de éxito donde un cuidador (SITTER) actualiza su propio servicio.
         *
         * <p><strong>Escenario:</strong> Un cuidador envía una petición PATCH válida a su propio recurso de servicio.</p>
         * <p><strong>Resultado esperado:</strong></p>
         * <ul>
         * <li>Un código de estado HTTP 200 (OK).</li>
         * <li>El cuerpo de la respuesta contiene el DTO del servicio con los datos actualizados.</li>
         * <li>El registro en la base de datos H2 refleja los cambios.</li>
         * </ul>
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Un SITTER debería poder actualizar su propio servicio")
        void shouldUpdateService_whenUserIsSitterOwner() throws Exception {
            // Arrange
            UpdateServiceOfferingDTO updateDTO = createValidUpdateDTO("Nombre Actualizado", new BigDecimal("25.00"));

            // Act
            ResultActions result = mockMvc.perform(patch("/api/services/{id}", existingService.getId())
                    .header("Authorization", "Bearer " + ownerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)));

            // Assert (Respuesta HTTP)
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Nombre Actualizado")))
                    .andExpect(jsonPath("$.price", is(25.00)));

            // Assert (Estado de la Base de Datos)
            ServiceOffering updatedService = serviceOfferingRepository.findById(existingService.getId()).orElseThrow();
            assertThat(updatedService.getName()).isEqualTo("Nombre Actualizado");
            assertThat(updatedService.getPrice()).isEqualByComparingTo("25.00");
        }

        /**
         * Válida que un administrador puede actualizar el servicio de cualquier cuidador.
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Un ADMIN debería poder actualizar el servicio de cualquier cuidador")
        void shouldUpdateService_whenUserIsAdmin() throws Exception {
            // Arrange
            UpdateServiceOfferingDTO updateDTO = createValidUpdateDTO("Actualizado por Admin", new BigDecimal("99.00"));

            // Act & Assert
            mockMvc.perform(patch("/api/services/{id}", existingService.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Actualizado por Admin")));
        }


        /**
         * Válida que un cuidador no puede modificar el servicio de otro cuidador.
         *
         * <p><strong>Escenario:</strong> Un cuidador (`anotherSitter`) intenta actualizar un servicio que pertenece a `sitterOwner`.</p>
         * <p><strong>Resultado esperado:</strong> Un código de estado HTTP 403 (Forbidden).</p>
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (Autorización): Un SITTER no debería poder actualizar el servicio de otro y debe recibir 403")
        void shouldReturnForbidden_whenSitterUpdatesAnothersService() throws Exception {
            // Arrange
            String anotherSitterToken = jwtService.getToken(anotherSitter);
            UpdateServiceOfferingDTO updateDTO = createValidUpdateDTO("Intento de hackeo", BigDecimal.ONE);

            // Act & Assert
            mockMvc.perform(patch("/api/services/{id}", existingService.getId())
                            .header("Authorization", "Bearer " + anotherSitterToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isForbidden());
        }

        /**
         * Válida que un cliente no puede actualizar ninguna oferta de servicio.
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (Autorización): Un CLIENT no debería poder actualizar servicios y debe recibir 403")
        void shouldReturnForbidden_whenUserIsClient() throws Exception {
            // Arrange
            UpdateServiceOfferingDTO updateDTO = createValidUpdateDTO("Intento no válido", BigDecimal.TEN);

            // Act & Assert
            mockMvc.perform(patch("/api/services/{id}", existingService.getId())
                            .header("Authorization", "Bearer " + clientToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isForbidden());
        }

        /**
         * Válida que la API responde con 404 Not Found si se intenta actualizar un servicio que no existe.
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (No Encontrado): Debería retornar 404 si el servicio a actualizar no existe")
        void shouldReturnNotFound_whenServiceDoesNotExist() throws Exception {
            // Arrange
            long nonExistentId = 9999L;
            UpdateServiceOfferingDTO updateDTO = createValidUpdateDTO("No importa", BigDecimal.TEN);

            // Act & Assert
            mockMvc.perform(patch("/api/services/{id}", nonExistentId)
                            .header("Authorization", "Bearer " + adminToken) // Admin para evitar 403
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isNotFound());
        }

        /**
         * Válida que la API responde con 409 Conflict si se intenta renombrar un servicio a un nombre que ya existe para ese cuidador.
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (Conflicto): Debería retornar 409 si el nuevo nombre ya existe para el cuidador")
        void shouldReturnConflict_whenNewNameIsDuplicate() throws Exception {
            // Arrange
            // Creamos un segundo servicio para el mismo cuidador con un nombre único
            createAndSaveService(sitterOwner, "Nombre Ya Existente", true);
            // Preparamos un DTO para actualizar el primer servicio ("Servicio Original")
            // con el nombre del segundo servicio.
            UpdateServiceOfferingDTO updateDTO = createValidUpdateDTO("Nombre Ya Existente", new BigDecimal("30.00"));

            // Act & Assert
            mockMvc.perform(patch("/api/services/{id}", existingService.getId())
                            .header("Authorization", "Bearer " + ownerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isConflict());
        }

    }

    /**
     * Contiene las pruebas para el endpoint de eliminación (desactivación) de servicios {@code DELETE /api/services/{id}}.
     */
    @Nested
    @DisplayName("DELETE /api/services/{id} - Eliminar (Desactivar) Servicio")
    class DeleteServiceTests {

        private User sitterOwner;
        private User anotherSitter;
        private User adminUser;
        private User clientUser;
        private String ownerToken;
        private String adminToken;
        private String clientToken;
        private ServiceOffering existingService;

        /**
         * Prepara el entorno para las pruebas de eliminación.
         * Crea un cuidador, un servicio activo para él, y otros usuarios con diferentes roles.
         * Genera los tokens de autenticación para cada rol.
         */
        @BeforeEach
        void setUp() {
            sitterOwner = createAndSaveUser("sitter.owner.delete@petcare.com", Role.SITTER);
            anotherSitter = createAndSaveUser("another.sitter.delete@petcare.com", Role.SITTER);
            adminUser = createAndSaveUser("admin.deleter@petcare.com", Role.ADMIN);
            clientUser = createAndSaveUser("client.deleter@petcare.com", Role.CLIENT);

            ownerToken = jwtService.getToken(sitterOwner);
            adminToken = jwtService.getToken(adminUser);
            clientToken = jwtService.getToken(clientUser);

            existingService = createAndSaveService(sitterOwner, "Servicio a Desactivar", true);
        }

        /**
         * Válida que un cuidador puede desactivar exitosamente su propio servicio.
         *
         * <p><strong>Escenario:</strong> Un cuidador envía una petición DELETE a un servicio que le pertenece.</p>
         * <p><strong>Resultado esperado:</strong></p>
         * <ul>
         * <li>Un código de estado HTTP 200 (OK) con un mensaje de confirmación.</li>
         * <li>El servicio en la base de datos H2 no se elimina, pero su campo `isActive` se establece en `false`.</li>
         * </ul>
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Un SITTER debería poder desactivar su propio servicio")
        void shouldDeactivateService_whenUserIsSitterOwner() throws Exception {
            // Arrange
            long serviceId = existingService.getId();
            assertThat(serviceOfferingRepository.findById(serviceId).get().isActive()).isTrue();

            // Act
            ResultActions result = mockMvc.perform(delete("/api/services/{id}", serviceId)
                    .header("Authorization", "Bearer " + ownerToken));

            // Assert (Respuesta HTTP)
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", containsString("eliminada (desactivada) exitosamente")));

            // Assert (Estado de la Base de Datos)
            ServiceOffering deactivatedService = serviceOfferingRepository.findById(serviceId).orElseThrow();
            assertThat(deactivatedService.isActive()).isFalse();
        }

        /**
         * Válida que un administrador puede desactivar el servicio de cualquier cuidador.
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("✅ Éxito: Un ADMIN debería poder desactivar el servicio de cualquier cuidador")
        void shouldDeactivateService_whenUserIsAdmin() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/services/{id}", existingService.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            ServiceOffering deactivatedService = serviceOfferingRepository.findById(existingService.getId()).orElseThrow();
            assertThat(deactivatedService.isActive()).isFalse();
        }

        /**
         * Válida que un cuidador no puede desactivar el servicio de otro cuidador.
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (Autorización): Un SITTER no debería poder desactivar el servicio de otro y debe recibir 403")
        void shouldReturnForbidden_whenSitterDeletesAnotherSittersService() throws Exception {
            // Arrange
            String anotherSitterToken = jwtService.getToken(anotherSitter);

            // Act & Assert
            mockMvc.perform(delete("/api/services/{id}", existingService.getId())
                            .header("Authorization", "Bearer " + anotherSitterToken))
                    .andExpect(status().isForbidden());

            // Verificar que el servicio sigue activo en la BD
            ServiceOffering service = serviceOfferingRepository.findById(existingService.getId()).orElseThrow();
            assertThat(service.isActive()).isTrue();
        }

        /**
         * Válida que un cliente no puede desactivar ninguna oferta de servicio.
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (Autorización): Un CLIENT no debería poder desactivar servicios y debe recibir 403")
        void shouldReturnForbidden_whenUserIsClient() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/services/{id}", existingService.getId())
                            .header("Authorization", "Bearer " + clientToken))
                    .andExpect(status().isForbidden());
        }

        /**
         * Válida que la API responde con 404 Not Found si se intenta desactivar un servicio que no existe.
         *
         * @throws Exception Si ocurre un error durante la ejecución de la petición MockMvc.
         */
        @Test
        @DisplayName("🚫 Falla (No Encontrado): Debería retornar 404 si el servicio a desactivar no existe")
        void shouldReturnNotFound_whenServiceDoesNotExist() throws Exception {
            // Arrange
            long nonExistentId = 9999L;

            // Act & Assert
            mockMvc.perform(delete("/api/services/{id}", nonExistentId)
                            .header("Authorization", "Bearer " + adminToken)) // Usamos admin para evitar un 403
                    .andExpect(status().isNotFound());
        }
    }


    // =====================================================================================
    // ========================= MÉTODOS AUXILIARES (HELPERS) ==============================
    // =====================================================================================

    /**
     * Método de ayuda para crear y persistir un usuario en la base de datos H2.
     * Facilita la creación de los cuidadores (Sitters) necesarios para las pruebas de servicios.
     * Usa saveAndFlush para garantizar la escritura inmediata en la BD.
     *
     * @param email El email del usuario (debe ser único).
     * @param role El rol a asignar al usuario.
     * @return La entidad {@link User} ya persistida en la base de datos.
     */
    private User createAndSaveUser(String email, Role role) {
        User user = new User("Test", role.name(), email, passwordEncoder.encode("password123"),
                "Dirección de prueba", "123456789", role);
        user.setEmailVerifiedAt(LocalDateTime.now());
        return userRepository.saveAndFlush(user); // CORRECCIÓN: Usar saveAndFlush
    }

    /**
     * Método de ayuda para crear y persistir una oferta de servicio en la base de datos H2.
     * Asocia el servicio a un cuidador (Sitter) existente.
     * Usa saveAndFlush para garantizar la escritura inmediata en la BD.
     *
     * @param sitter El usuario (cuidador) que ofrece el servicio.
     * @param name El nombre del servicio.
     * @param isActive El estado de activación del servicio.
     * @return La entidad {@link ServiceOffering} ya persistida en la base de datos.
     */
    private ServiceOffering createAndSaveService(User sitter, String name, boolean isActive) {
        ServiceOffering service = new ServiceOffering();
        service.setSitterId(sitter.getId());
        service.setName(name);
        service.setServiceType(ServiceType.WALKING);
        service.setDescription("Descripción de " + name);
        service.setPrice(new BigDecimal("15.00"));
        service.setDurationInMinutes(60);
        service.setActive(isActive);
        service.setCreatedAt(LocalDateTime.now());
        return serviceOfferingRepository.saveAndFlush(service);
    }

    /**
     * Método de ayuda para crear un DTO de creación de servicio válido para las pruebas.
     *
     * @return Una instancia de {@link CreateServiceOfferingDTO} con datos válidos.
     */
    private CreateServiceOfferingDTO createValidRequestDTO() {
        return new CreateServiceOfferingDTO(
                ServiceType.WALKING,
                "Paseo Matutino (30 min)",
                "Un paseo energizante para empezar el día con buen pie.",
                new BigDecimal("12.50"),
                30
        );
    }

    /**
     * Método de ayuda para crear un DTO de actualización de servicio válido.
     *
     * @param newName El nuevo nombre para el servicio.
     * @param newPrice El nuevo precio para el servicio.
     * @return Una instancia de {@link UpdateServiceOfferingDTO}.
     */
    private UpdateServiceOfferingDTO createValidUpdateDTO(String newName, BigDecimal newPrice) {
        return new UpdateServiceOfferingDTO(
                ServiceType.WALKING,
                newName,
                "Descripción actualizada.",
                newPrice,
                45,
                true
        );
    }
}