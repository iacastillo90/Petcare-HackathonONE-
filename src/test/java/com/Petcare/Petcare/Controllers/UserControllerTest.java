package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.Configurations.Security.Jwt.JwtService;
import com.Petcare.Petcare.DTOs.Auth.Request.LoginRequest;
import com.Petcare.Petcare.DTOs.Auth.Respone.AuthResponse;
import com.Petcare.Petcare.DTOs.User.CreateUserRequest;
import com.Petcare.Petcare.DTOs.User.UpdateUserRequest;
import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.Account.AccountUser;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.AccountRepository;
import com.Petcare.Petcare.Repositories.AccountUserRepository;
import com.Petcare.Petcare.Repositories.UserRepository;
import com.Petcare.Petcare.Services.EmailService;
import com.Petcare.Petcare.Services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Suite completa de pruebas de integración para el controlador de usuarios.
 *
 * <p>Esta clase valida el funcionamiento integral del sistema de usuarios desde la perspectiva
 * del protocolo HTTP, cubriendo todo el ciclo de vida desde el registro hasta la eliminación.
 * Cada prueba simula peticiones reales de un cliente frontend y verifica tanto las respuestas
 * HTTP como los cambios persistidos en la base de datos.</p>
 *
 * <p><strong>Filosofía de testing:</strong></p>
 * <ul>
 *   <li>Pruebas end-to-end que cubren el flujo completo HTTP → BD</li>
 *   <li>Estados de BD independientes gracias a @Transactional</li>
 *   <li>Validación tanto de códigos HTTP como de contenido JSON</li>
 *   <li>Verificación de efectos secundarios en la persistencia</li>
 * </ul>
 *
 * <p><strong>Cobertura de escenarios:</strong></p>
 * <ul>
 *   <li>Casos de éxito con diferentes roles de usuario</li>
 *   <li>Validaciones de entrada y manejo de errores</li>
 *   <li>Controles de autorización y seguridad</li>
 *   <li>Operaciones CRUD completas con verificación de persistencia</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @see UserController
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Tag(name = "UserControllerTest", description = "Pruebas de integración para el controlador de usuarios")
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountUserRepository accountUserRepository;
    @Autowired private UserService userService;
    @Autowired
    private JwtService jwtService;
    @MockitoBean
    private EmailService emailService;

    /**
     * Verifica el flujo completo de registro exitoso para un usuario cliente.
     *
     * <p>Simula el proceso que seguiría un usuario real al crear su cuenta desde
     * el frontend. Confirma que el sistema responde con credenciales válidas y
     * asigna automáticamente el rol correcto.</p>
     *
     * @throws Exception Sí ocurre algún error durante la creación o autenticación
     * @see CreateUserRequest
     * @see User
     *
     */
    @Test
    @DisplayName("POST /register | Éxito | Debería registrar un cliente y retornar 201 Created")
    void registerUser_WithValidData_ShouldReturnCreated() throws Exception {
        // Preparamos datos que un usuario real ingresaría en el formulario web
        CreateUserRequest registerRequest = new CreateUserRequest(
                "Ivan", "Test", "ivan.test@gmails.com",
                "ValidPassword123", "Calle Falsa 123", "987654321"
        );

        // Simulamos la petición HTTP exacta que haría el frontend
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON) // Indicamos que el contenido es JSON
                        .content(objectMapper.writeValueAsString(registerRequest))) // Convertimos el objeto a JSON RegisterRequest
                // Verificamos que el registro fue exitoso con código 201
                .andExpect(status().isCreated())
                // Confirmamos que recibimos un token para autenticación inmediata
                .andExpect(jsonPath("$.token").isNotEmpty())
                // Validamos que se asignó el rol correcto automáticamente
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    /**
     * Prueba el manejo de duplicación de email durante el registro.
     *
     * <p>Válida que el sistema protege la unicidad de emails y responde
     * adecuadamente cuando un usuario intenta registrarse con un email
     * que ya existe en la base de datos.</p>
     *
     * @throws Exception Sí ocurre algún error durante la creación o autenticación
     */
    @Test
    @DisplayName("POST /register | Falla | Debería retornar 400 Bad Request si el email ya existe")
    void registerUser_WhenEmailExists_ShouldReturnBadRequest() throws Exception {
        // Simulamos que ya existe un usuario con este email en el sistema
        User existingUser = new User("Existing", "User", "ivan.test@example.com",
                "pass", "addr", "phone", Role.CLIENT);
        userRepository.save(existingUser);

        // Intentamos registrar otro usuario con el mismo email
        CreateUserRequest registerRequest = new CreateUserRequest(
                "Ivan", "Test", "ivan.test@example.com",
                "ValidPassword123", "Calle Falsa 123", "987654321"
        );

        // Act & Assert: El sistema debe rechazar la petición y explicar el problema
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON) // Indicamos que el contenido es JSON
                        .content(objectMapper.writeValueAsString(registerRequest))) // Convertimos el objeto a JSON RegisterRequest
                .andExpect(status().isBadRequest()); // Verificamos que el sistema responde con 400 Bad Request
    }

    /**
     * Válida el proceso completo de autenticación exitosa.
     *
     * <p>Crea un escenario realista donde un usuario existente intenta hacer login.
     * Incluye la preparación completa del estado de BD necesario para representar
     * un usuario completamente configurado en el sistema.</p>
     *
     * @throws Exception Sí ocurre algún error durante la creación o autenticación
     * @see LoginRequest
     * @see AuthResponse
     *
     */
    @Test
    @DisplayName("POST /login | Éxito | Debería autenticar y retornar 200 OK con AuthResponse")
    void login_WithValidCredentials_ShouldReturnOk() throws Exception {
        // Arrange: Creamos un estado completo en la BD H2 para un login exitoso.
        User testUser = new User("Login", "User", "login.test@example.com", passwordEncoder.encode("password123"), "456 Login Ave", "555-0000", Role.CLIENT);
        testUser.setActive(true);
        testUser.setEmailVerifiedAt(LocalDateTime.now());
        User savedUser = userRepository.save(testUser);

        // Creamos una cuenta para el usuario
        Account account = new Account(savedUser, "Cuenta de Login", "ACC-LOGIN");
        Account savedAccount = accountRepository.save(account);

        // Creamos la relación entre el usuario y la cuenta
        AccountUser accountUser = new AccountUser(savedAccount, savedUser, Role.CLIENT);
        accountUserRepository.save(accountUser);

        LoginRequest loginRequest = new LoginRequest("login.test@example.com", "password123");

        // Act & Assert: Ejecutamos la petición y verificamos la respuesta.
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON) // Indicamos que el contenido es JSON
                        .content(objectMapper.writeValueAsString(loginRequest))) // Convertimos el objeto a JSON LoginRequest
                .andExpect(status().isOk()) // Verificamos que el login fue exitoso con código 200
                .andExpect(jsonPath("$.token").isNotEmpty()) // Verificamos que el token no está vacío
                .andExpect(jsonPath("$.userProfile.email").value("login.test@example.com")); // Verificamos que el email del usuario es el esperado
    }

    /**
     * Prueba el manejo de credenciales incorrectas durante el login.
     *
     * <p>Simula un intento de login con credenciales que no existen en la base de datos.
     * Verifica que el sistema responde con 401 Unauthorized y que el mensaje de error
     * indica que las credenciales son incorrectas.</p>
     *
     * @throws Exception Sí ocurre algún error durante la creación o autenticación
     */
    @Test
    @DisplayName("POST /login | Falla | Debería retornar 401 Unauthorized con credenciales incorrectas")
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange: Simulamos un intento de login con credenciales que no existen en la base de datos.
        LoginRequest loginRequest = new LoginRequest("no.existe@example.com", "wrong-password");

        // Act & Assert: Ejecutamos la petición y verificamos la respuesta.
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON) // Indicamos que el contenido es JSON
                        .content(objectMapper.writeValueAsString(loginRequest))) // Convertimos el objeto a JSON LoginRequest
                .andExpect(status().isUnauthorized()); // Verificamos que el login fue exitoso con código 200
    }

    /**
     * Prueba el endpoint específico para registro de cuidadores.
     *
     * <p>Válida que el sistema puede manejar diferentes tipos de usuarios
     * desde el registro, asignando automáticamente el rol apropiado para
     * proveedores de servicios de cuidado.</p>
     *
     * @throws Exception Sí ocurre algún error durante la creación o autenticación
     * @see CreateUserRequest
     * @see User
     *
     */
    @Test
    @DisplayName("POST /api/users/register-sitter | Éxito | Debería registrar una nueva cuidadora y retornar 201 Created")
    void registerUserSitter_WithValidData_ShouldReturnCreatedAndSitterRole() throws Exception {
        // Arrange: Preparamos el DTO con los datos de la cuidadora a registrar.
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), anyString(), anyInt());

        CreateUserRequest sitterRequest = new CreateUserRequest(
                "Juana",
                "Arcos",
                "juana.sitter@example.com",
                "SuperSecret123",
                "Avenida de los Cuidadores 456",
                "555-8765432"
        );

        mockMvc.perform(post("/api/users/register-sitter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sitterRequest)))
                .andExpect(status().isCreated()) // *** CORRECCIÓN CLAVE 2: Esperar 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("SITTER"));
    }

    /**
     * Válida el flujo de creación administrativa exitosa con privilegios ADMIN.
     *
     * <p>Simula el escenario donde un administrador crea usuarios con roles específicos.
     * Esta funcionalidad es crítica para la gestión interna del sistema.</p>
     *
     * @throws Exception Sí ocurre algún error durante la creación o autenticación
     * @see Role
     * @see CreateUserRequest
     * @see User
     *
     */
    @Test
    @DisplayName("POST /admin | Éxito | Un ADMIN debería poder crear un nuevo usuario con un rol específico")
    void createUserByAdmin_AsAdmin_ShouldCreateUserAndReturnCreated() throws Exception {
        // Arrange
        // 1. Obtenemos un token de un usuario ADMIN
        String adminToken = getAuthTokenForRole(Role.ADMIN);

        // 2. Preparamos la solicitud para crear un nuevo usuario (en este caso, un SITTER)
        CreateUserRequest newUserRequest = new CreateUserRequest(
                "SitterCreadoPorAdmin", "User", "sitter.admin@example.com",
                "Password123#", "Admin Complex", "555-ADMIN"
        );

        // Act & Assert: Simulamos la petición HTTP exacta que haría el frontend
        mockMvc.perform(post("/api/users/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken) // Usamos el token de ADMIN
                        .param("role", "SITTER") // Especificamos el rol a crear
                        .contentType(MediaType.APPLICATION_JSON) // Indicamos que el contenido es JSON
                        .content(objectMapper.writeValueAsString(newUserRequest))) // Convertimos el objeto a JSON NewUserRequest
                .andExpect(status().isCreated()) // Verificar que el código de estado es 201 Created
                .andExpect(jsonPath("$.email").value("sitter.admin@example.com"))
                .andExpect(jsonPath("$.role").value("SITTER")); // Verificamos que se creó con el rol correcto
    }

    /**
     * Prueba las restricciones de seguridad para operaciones administrativas.
     *
     * <p>Confirma que usuarios sin privilegios no pueden acceder a funciones
     * administrativas, manteniendo la integridad del sistema de permisos.</p>
     *
     * @throws Exception Sí ocurre algún error durante la creación o autenticación
     *
     */
    @Test
    @DisplayName("POST /admin | Falla | Un CLIENT no debería poder crear un usuario y debe recibir 403 Forbidden")
    void createUserByAdmin_AsClient_ShouldReturnForbidden() throws Exception {
        // Arrange
        // 1. Obtenemos un token de un usuario CLIENT
        String clientToken = getAuthTokenForRole(Role.CLIENT);

        // 2. Preparamos la solicitud
        CreateUserRequest newUserRequest = new CreateUserRequest(
                "SitterCreadoPorAdmin", "User", "sitter.admin@example.com",
                "Password123#", "Admin Complex", "555-ADMIN"
        );

        // Act & Assert: Si el usuario no tiene privilegios de ADMIN, debe recibir 403 Forbidden
        mockMvc.perform(post("/api/users/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken) // Usamos el token de CLIENT
                        .param("role", "SITTER") // Especificamos el rol a crear
                        .contentType(MediaType.APPLICATION_JSON) // Indicamos que el contenido es JSON
                        .content(objectMapper.writeValueAsString(newUserRequest))) // Convertimos el objeto a JSON NewUserRequest
                .andExpect(status().isForbidden()); // Esperamos un 403 Forbidden
    }


    /**
     * Crea y autentica usuarios con roles específicos para pruebas.
     *
     * <p>Este método helper evita duplicación de código y garantiza que cada prueba
     * tenga acceso a tokens JWT válidos con los permisos correctos. Simula el
     * proceso completo de registro y autenticación que seguiría un usuario real.</p>
     *
     * @param role El rol específico que necesita el usuario para la prueba
     * @return Token JWT válido para realizar peticiones autenticadas
     * @throws Exception Sí ocurre algún error durante la creación o autenticación
     *
     */
    private String getAuthTokenForRole(Role role) throws Exception {
        // Crear un usuario con el rol especificado
        String email = role.name().toLowerCase() + "@example.com";
        User user = new User(
                "Test", role.name(), email,
                passwordEncoder.encode("password123"), "Addr", "Phone", role
        );
        user.setActive(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        // Crear una cuenta para el usuario
        Account account = new Account(savedUser, "Cuenta de " + role.name(), "ACC-" + role.name());
        accountRepository.save(account);

        // Asignar el rol al usuario
        AccountUser accountUser = new AccountUser(account, savedUser, role);
        accountUserRepository.save(accountUser);

        // Realizar login para obtener el token
        LoginRequest loginRequest = new LoginRequest(email, "password123");

        // Ejecutar la petición de login y obtener el token
        MvcResult result = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON) // Indicamos que el contenido es JSON
                        .content(objectMapper.writeValueAsString(loginRequest))) // Convertimos el objeto a JSON LoginRequest
                .andExpect(status().isOk()) // Esperamos un 200 OK
                .andReturn(); // Devolvemos el resultado de la petición

        // Obtenemos el token de la respuesta
        String responseBody = result.getResponse().getContentAsString();
        // Convertimos el JSON de la respuesta a un objeto AuthResponse
        AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);
        // Devolvemos el token
        return authResponse.getToken();
    }

    /**
     * Verifica la actualización exitosa de datos de usuario por administradores.
     *
     * <p>Válida tanto la respuesta HTTP como la persistencia real de los cambios
     * en la base de datos, asegurando que las actualizaciones se apliquen correctamente.</p>
     *
     * @throws Exception Sí ocurre algún error durante la creación o autenticación
     */
    @Test
    @DisplayName("PUT /api/users/{id} | Éxito | Un ADMIN debería poder actualizar un usuario")
    void updateUser_AsAdmin_ShouldUpdateUserAndReturnOk() throws Exception {
        // Arrange
        // 1. Obtenemos un token de un usuario ADMIN para realizar la petición
        String adminToken = getAuthTokenForRole(Role.ADMIN);

        // 2. Creamos el usuario que vamos a actualizar en la BD H2
        User userToUpdate = new User("Original", "Name", "original.name@example.com", "pass", "addr", "phone", Role.CLIENT);
        User savedUser = userRepository.save(userToUpdate);

        // 3. Preparamos el DTO con los datos nuevos
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Nombre", "Actualizado", "nuevo.email@example.com",
                "passTest", // No actualizamos la contraseña en este test
                "Dirección Actualizada", "111222333"
        );

        // Act & Assert
        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken) // Petición autenticada como ADMIN
                        .contentType(MediaType.APPLICATION_JSON) // Indicamos que el contenido es JSON
                        .content(objectMapper.writeValueAsString(updateRequest))) // Convertimos el objeto a JSON UpdateUserRequest
                .andExpect(status().isOk()) // Esperamos 200 OK para una actualización exitosa
                .andExpect(jsonPath("$.firstName").value("Nombre")) // Verificamos que el nombre se actualizó correctamente
                .andExpect(jsonPath("$.lastName").value("Actualizado")) // Verificamos que el apellido se actualizó correctamente
                .andExpect(jsonPath("$.email").value("nuevo.email@example.com")); // Verificamos que el email se actualizó correctamente

        // Verificación extra: Comprobar el estado en la BD
        User updatedUserFromDb = userRepository.findById(savedUser.getId()).orElseThrow();
        // Verificamos que el usuario se actualizó correctamente en la BD
        assertThat(updatedUserFromDb.getFirstName()).isEqualTo("Nombre");
        // Verificamos que la dirección se actualizó correctamente en la BD
        assertThat(updatedUserFromDb.getAddress()).isEqualTo("Dirección Actualizada");
    }

    /**
     * Prueba el escenario de fallo donde un CLIENT intenta actualizar un usuario.
     *
     * @throws Exception Sí ocurre algún error durante la creación o autenticación
     */
    @Test
    @DisplayName("PUT /api/users/{id} | Falla | Un CLIENT no debería poder actualizar y debe recibir 403 Forbidden")
    void updateUser_AsClient_ShouldReturnForbidden() throws Exception {
        // Arrange: Si el usuario no tiene privilegios de ADMIN, debe recibir 403 Forbidden
        String clientToken = getAuthTokenForRole(Role.CLIENT);
        User targetUser = new User("Target", "User", "target@example.com", "pass", "addr", "phone", Role.CLIENT);
        userRepository.save(targetUser);

        // Creamos un DTO de actualización VÁLIDO
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Nombre", "Actualizado", "nuevo.email@example.com",
                "passTest", // No actualizamos la contraseña en este test
                "Dirección Actualizada", "111222333");
        updateRequest.setFirstName("Hack Attempt");

        // Act & Assert: Si el usuario no tiene privilegios de ADMIN, debe recibir 403 Forbidden
        mockMvc.perform(put("/api/users/{id}", targetUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken) // Petición autenticada como CLIENT
                        .contentType(MediaType.APPLICATION_JSON) // Indicamos que el contenido es JSON
                        .content(objectMapper.writeValueAsString(updateRequest))) // Convertimos el objeto a JSON UpdateUserRequest
                .andExpect(status().isForbidden()); // Esperamos 403 Forbidden para un cliente intentando actualizar
    }

    /**
     * Verifica la funcionalidad de activación/desactivación de cuentas.
     *
     * <p>Prueba una operación crítica que permite a administradores controlar
     * el acceso al sistema sin eliminar datos del usuario.</p>
     *
     * @throws Exception Sí ocurre algún error durante la creación o autenticación
     *
     */
    @Test
    @DisplayName("PATCH /api/users/{id}/toggle-active | Éxito | Un ADMIN debería poder desactivar un usuario")
    void toggleUserActive_AsAdmin_ShouldDeactivateUserAndReturnOk() throws Exception {
        // Arrange
        // 1. Obtenemos un token de un usuario ADMIN para la petición
        String adminToken = getAuthTokenForRole(Role.ADMIN);

        // 2. Creamos el usuario que vamos a desactivar en la BD H2
        User userToToggle = new User("User", "ToToggle", "toggle.user@example.com", "pass", "addr", "phone", Role.CLIENT);
        userToToggle.setActive(true); // Nos aseguramos de que su estado inicial sea activo
        User savedUser = userRepository.save(userToToggle);

        // Act & Assert: Si el usuario tiene privilegios de ADMIN, debe recibir 200 OK
        mockMvc.perform(patch("/api/users/{id}/toggle-active", savedUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken) // Petición autenticada como ADMIN
                        .param("active", "false")) // El parámetro para desactivar
                .andExpect(status().isOk()) // Esperamos 200 OK para una desactivación exitosa
                .andExpect(jsonPath("$.id").value(savedUser.getId())) // Verificamos que el ID coincide
                .andExpect(jsonPath("$.active").value(false)); // Verificamos que la respuesta refleje el nuevo estado

        // Verificación extra contra la BD
        User toggledUserFromDb = userRepository.findById(savedUser.getId()).orElseThrow();
        // Verificamos que el usuario se desactivó correctamente en la BD
        assertThat(toggledUserFromDb.isActive()).isFalse();
    }

    /**
     * Prueba el escenario de fallo donde un CLIENT intenta usar el endpoint.
     *
     * @throws Exception Sí
     */
    @Test
    @DisplayName("PATCH /api/users/{id}/toggle-active | Falla | Un CLIENT no debería poder cambiar el estado y debe recibir 403 Forbidden")
    void toggleUserActive_AsClient_ShouldReturnForbidden() throws Exception {
        // Arrange
        String clientToken = getAuthTokenForRole(Role.CLIENT);
        User targetUser = new User("Target", "User", "target@example.com", "pass", "addr", "phone", Role.CLIENT);
        userRepository.save(targetUser);

        // Act & Assert
        mockMvc.perform(patch("/api/users/{id}/toggle-active", targetUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken) // Petición como CLIENT
                        .param("active", "false"))
                .andExpect(status().isForbidden()); // Esperamos 403 Forbidden
    }

    /**
     * Prueba la verificación manual de email por administradores.
     *
     * <p>Válida la funcionalidad que permite a administradores marcar emails
     * como verificados, útil para soporte técnico y resolución de problemas.</p>
     */
    @Test
    @DisplayName("PATCH /api/users/{id}/verify-email | Éxito | Un ADMIN debería poder verificar un email")
    void markEmailAsVerified_AsAdmin_ShouldVerifyEmailAndReturnOk() throws Exception {
        // Arrange
        // 1. Obtenemos un token de ADMIN para la petición
        String adminToken = getAuthTokenForRole(Role.ADMIN);

        // 2. Creamos el usuario a verificar, asegurándonos de que su email NO esté verificado
        User userToVerify = new User("User", "ToVerify", "verify.this@example.com", "pass", "addr", "phone", Role.CLIENT);
        userToVerify.setEmailVerifiedAt(null); // Estado inicial: no verificado
        User savedUser = userRepository.save(userToVerify);

        // Verificación previa para asegurar el estado inicial
        assertThat(savedUser.getEmailVerifiedAt()).isNull();

        // Act & Assert: Si el usuario tiene privilegios de ADMIN, debe recibir 200 OK
        mockMvc.perform(patch("/api/users/{id}/verify-email", savedUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)) // Petición como ADMIN
                .andExpect(status().isOk()) // Esperamos 200 OK para una verificación exitosa
                .andExpect(jsonPath("$.id").value(savedUser.getId())) // Verificamos que el ID coincide
                .andExpect(jsonPath("$.emailVerifiedAt").exists()) // Verificamos que el campo ahora existe (no es nulo)
                .andExpect(jsonPath("$.emailVerifiedAt").isNotEmpty()); // Verificamos que el campo no está vacío

        // Verificación final contra la BD para confirmar la persistencia del cambio
        User verifiedUserFromDb = userRepository.findById(savedUser.getId()).orElseThrow();
        // Verificamos que el usuario se verificó correctamente en la BD
        assertThat(verifiedUserFromDb.getEmailVerifiedAt()).isNotNull();
    }

    /**
     * Prueba el escenario de fallo donde un CLIENT intenta usar el endpoint.
     *
     *
     */
    @Test
    @DisplayName("PATCH /api/users/{id}/verify-email | Falla | Un CLIENT no debería poder verificar un email y debe recibir 403 Forbidden")
    void markEmailAsVerified_AsClient_ShouldReturnForbidden() throws Exception {
        // Arrange
        String clientToken = getAuthTokenForRole(Role.CLIENT);
        User targetUser = new User("Target", "User", "target.verify@example.com", "pass", "addr", "phone", Role.CLIENT);
        userRepository.save(targetUser);

        // Act & Assert
        mockMvc.perform(patch("/api/users/{id}/verify-email", targetUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken)) // Petición como CLIENT
                .andExpect(status().isForbidden());
    }

    /**
     * Verifica la eliminación permanente de usuarios por administradores.
     *
     * <p>Prueba la operación más destructiva del sistema, validando tanto
     * la respuesta como la eliminación real de datos de la base de datos.</p>
     */
    @Test
    @DisplayName("DELETE /api/users/{id} | Éxito | Un ADMIN debería poder eliminar un usuario")
    void deleteUser_AsAdmin_ShouldDeleteUserAndReturnNoContent() throws Exception {
        // Arrange
        // 1. Obtenemos un token de ADMIN para la petición
        String adminToken = getAuthTokenForRole(Role.ADMIN);

        // 2. Creamos el usuario que vamos a eliminar en la BD H2
        User userToDelete = new User("User", "ToDelete", "delete.user@example.com", "pass", "addr", "phone", Role.CLIENT);
        User savedUser = userRepository.save(userToDelete);
        Long userId = savedUser.getId();

        // Verificación previa: asegurarnos de que el usuario existe antes de la prueba
        assertThat(userRepository.findById(userId)).isPresent();

        // Act & Assert
        mockMvc.perform(delete("/api/users/{id}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)) // Petición autenticada como ADMIN
                .andExpect(status().isOk()); // Esperamos 204 No Content

        // Verificación final y más importante: confirmar que el usuario ya no está en la BD
        assertThat(userRepository.findById(userId)).isEmpty();
    }

    /**
     * Prueba el escenario de fallo donde un CLIENT intenta eliminar a otro usuario.
     */
    @Test
    @DisplayName("DELETE /api/users/{id} | Falla | Un CLIENT no debería poder eliminar un usuario y debe recibir 403 Forbidden")
    void deleteUser_AsClient_ShouldReturnForbidden() throws Exception {
        // Arrange
        String clientToken = getAuthTokenForRole(Role.CLIENT);
        User targetUser = new User("Target", "User", "target@example.com", "pass", "addr", "phone", Role.CLIENT);
        userRepository.save(targetUser);

        // Act & Assert
        mockMvc.perform(delete("/api/users/{id}", targetUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken)) // Petición como CLIENT
                .andExpect(status().isForbidden());
    }

    /**
     * Prueba el escenario de éxito donde un ADMIN obtiene la lista de todos los usuarios.
     */
    @Test
    @DisplayName("GET /api/users | Éxito | Un ADMIN debería obtener la lista de todos los usuarios")
    void getAllUsers_AsAdmin_ShouldReturnUserList() throws Exception {
        // Arrange
        // 1. Creamos y logueamos un ADMIN para obtener su token.
        // Este admin ya cuenta como 1 usuario en la base de datos.
        String adminToken = getAuthTokenForRole(Role.ADMIN);

        // 2. Creamos usuarios adicionales para que la lista no esté vacía.
        userRepository.save(new User("Test", "Client", "client.test@example.com", "pass", "addr", "phone", Role.CLIENT));
        userRepository.save(new User("Test", "Sitter", "sitter.test@example.com", "pass", "addr", "phone", Role.SITTER));

        // En este punto, deberíamos tener 3 usuarios en la BD H2 (el ADMIN, el CLIENT y el SITTER).

        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)) // Petición como ADMIN
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3))) // Verificamos que el array JSON tiene 3 elementos
                .andExpect(jsonPath("$[0].role").exists()) // Verificamos que los objetos tienen la estructura esperada
                .andExpect(jsonPath("$[1].email").exists());
    }

    /**
     * Prueba el escenario de fallo donde un CLIENT intenta obtener la lista de todos los usuarios.
     */
    @Test
    @DisplayName("GET /api/users | Falla | Un CLIENT no debería poder obtener la lista de usuarios y debe recibir 403 Forbidden")
    void getAllUsers_AsClient_ShouldReturnForbidden() throws Exception {
        // Arrange
        String clientToken = getAuthTokenForRole(Role.CLIENT);

        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken)) // Petición como CLIENT
                .andExpect(status().isForbidden());
    }

    /**
     * Válida la funcionalidad completa de paginación y ordenamiento.
     *
     * <p>Prueba características avanzadas que son críticas para interfaces
     * de usuario con grandes volúmenes de datos, verificando tanto la
     * estructura de respuesta como la lógica de ordenamiento.</p>
     */
    @Test
    @DisplayName("GET /summary | Éxito | Un ADMIN debería obtener una lista paginada y ordenada de usuarios")
    void getAllUsersSummary_AsAdmin_ShouldReturnPaginatedAndSortedList() throws Exception {
        // Arrange
        // 1. Obtenemos el token de ADMIN. Este usuario ya existe en la BD para este test.
        String adminToken = getAuthTokenForRole(Role.ADMIN); // El usuario se llama "Test ADMIN"

        // 2. Creamos un conjunto de datos predecible para probar el ordenamiento
        userRepository.save(new User("Carlos", "Santana", "carlos@example.com", "p", "a", "p", Role.CLIENT));
        userRepository.save(new User("Beatriz", "Rojas", "beatriz@example.com", "p", "a", "p", Role.SITTER));

        // Total de usuarios en BD: 3 (ADMIN, Carlos, Beatriz)

        // Act & Assert
        mockMvc.perform(get("/api/users/summary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "2")
                        .param("sortBy", "firstName")
                        .param("sortDir", "asc")) // Ordenar por nombre ascendente
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Verificamos la estructura de la página
                .andExpect(jsonPath("$.content", hasSize(2))) // La página actual debe tener 2 usuarios
                .andExpect(jsonPath("$.totalElements").value(3)) // El total de usuarios en la BD es 3
                .andExpect(jsonPath("$.totalPages").value(2)) // 3 usuarios / tamaño 2 = 2 páginas
                .andExpect(jsonPath("$.number").value(0)) // Estamos en la página 0
                // Verificamos el ordenamiento
                .andExpect(jsonPath("$.content[0].fullName", is("Beatriz Rojas"))) // B Beatriz
                .andExpect(jsonPath("$.content[1].fullName", is("Carlos Santana"))); // C Carlos
    }

    /**
     * Prueba que un usuario no-admin no pueda acceder al endpoint de resumen.
     */
    @Test
    @DisplayName("GET /summary | Falla | Un CLIENT no debería poder obtener el resumen y debe recibir 403 Forbidden")
    void getAllUsersSummary_AsClient_ShouldReturnForbidden() throws Exception {
        // Arrange
        String clientToken = getAuthTokenForRole(Role.CLIENT);

        // Act & Assert
        mockMvc.perform(get("/api/users/summary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }

    /**
     * Prueba el escenario de éxito donde un ADMIN obtiene los datos de otro usuario.
     */
    @Test
    @DisplayName("GET /api/users/{id} | Éxito | Un ADMIN debería poder obtener cualquier usuario por ID")
    void getUserById_AsAdmin_ShouldReturnUserData() throws Exception {
        // Arrange
        String adminToken = getAuthTokenForRole(Role.ADMIN);

        // Creamos un usuario objetivo para ser consultado
        User targetUser = new User("Target", "User", "target@example.com", "pass", "addr", "phone", Role.CLIENT);
        User savedTargetUser = userRepository.save(targetUser);

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", savedTargetUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)) // Petición como ADMIN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTargetUser.getId()))
                .andExpect(jsonPath("$.email", is("target@example.com")));
    }

    /**
     * Válida qué usuarios pueden acceder a su propio perfil.
     *
     * <p>Prueba el control de acceso que permite a usuarios consultar sus
     * propios datos sin necesidad de privilegios administrativos.</p>
     */
    @Test
    @DisplayName("GET /api/users/{id} | Éxito | Un CLIENT debería poder obtener su propio perfil por ID")
    void getUserById_AsClientForSelf_ShouldReturnOwnData() throws Exception {
        // Arrange
        // Para este test, necesitamos el ID y el token del MISMO usuario.
        // La forma más fácil es registrarlo y loguearlo para obtener ambos.
        User client = new User("Self", "Client", "self.client@example.com", passwordEncoder.encode("password123"), "addr", "phone", Role.CLIENT);
        client.setActive(true);
        client.setEmailVerifiedAt(LocalDateTime.now());
        User savedClient = userRepository.save(client);

        Account account = new Account(savedClient, "Cuenta Self", "ACC-SELF");
        accountRepository.save(account);
        accountUserRepository.save(new AccountUser(account, savedClient, Role.CLIENT));

        LoginRequest loginRequest = new LoginRequest(savedClient.getEmail(), "password123");

        MvcResult result = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        String clientToken = authResponse.getToken();
        Long clientId = authResponse.getUserProfile().id();

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", clientId) // Pide su PROPIO ID
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken)) // Usa su PROPIO token
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId))
                .andExpect(jsonPath("$.email", is("self.client@example.com")));
    }

    /**
     * Prueba el escenario de fallo donde un CLIENT intenta obtener los datos de OTRO usuario.
     */
    @Test
    @DisplayName("GET /api/users/{id} | Falla | Un CLIENT no debería poder obtener datos de otro usuario")
    void getUserById_AsClientForOtherUser_ShouldReturnForbidden() throws Exception {
        // Arrange
        // Creamos y logueamos al atacante (CLIENT 1)
        String clientAttackerToken = getAuthTokenForRole(Role.CLIENT);

        // Creamos al usuario víctima (CLIENT 2)
        User victimUser = new User("Victim", "User", "victim@example.com", "pass", "addr", "phone", Role.CLIENT);
        userRepository.save(victimUser);

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", victimUser.getId()) // Intenta obtener el ID de la víctima
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientAttackerToken)) // Usando el token del atacante
                .andExpect(status().isForbidden());
    }

    /**
     * Prueba el escenario de éxito donde un ADMIN obtiene los datos de un usuario por su email.
     */
    @Test
    @DisplayName("GET /api/users/email/{email} | Éxito | Un ADMIN debería poder obtener un usuario por su email")
    void getUserByEmail_AsAdmin_ShouldReturnUserData() throws Exception {
        // Arrange
        // 1. Obtenemos un token de ADMIN.
        String adminToken = getAuthTokenForRole(Role.ADMIN);

        // 2. Creamos el usuario objetivo que vamos a buscar en la BD H2.
        User targetUser = new User("FindByEmail", "User", "find.by.email@example.com", "pass", "addr", "phone", Role.CLIENT);
        userRepository.save(targetUser);

        // Act & Assert
        mockMvc.perform(get("/api/users/email/{email}", "find.by.email@example.com")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)) // Petición como ADMIN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("find.by.email@example.com")))
                .andExpect(jsonPath("$.firstName", is("FindByEmail")));
    }

    /**
     * Prueba el escenario de fallo donde un CLIENT intenta usar el endpoint de búsqueda por email.
     */
    @Test
    @DisplayName("GET /api/users/email/{email} | Falla | Un CLIENT no debería poder buscar usuarios y debe recibir 403 Forbidden")
    void getUserByEmail_AsClient_ShouldReturnForbidden() throws Exception {
        // Arrange
        String clientToken = getAuthTokenForRole(Role.CLIENT);

        // Act & Assert
        mockMvc.perform(get("/api/users/email/{email}", "any.email@example.com")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken)) // Petición como CLIENT
                .andExpect(status().isForbidden());
    }

    /**
     * Prueba el escenario de éxito donde un ADMIN obtiene una lista de usuarios filtrada por rol.
     */
    @Test
    @DisplayName("GET /api/users/role/{role} | Éxito | Un ADMIN debería obtener la lista de usuarios de un rol específico")
    void getUsersByRole_AsAdmin_ShouldReturnFilteredUserList() throws Exception {
        // Arrange
        // 1. Obtenemos un token de ADMIN.
        String adminToken = getAuthTokenForRole(Role.ADMIN);

        // 2. Creamos un conjunto de datos mixto en la BD H2.
        userRepository.save(new User("Test", "Client", "client1@example.com", "pass", "addr", "phone", Role.CLIENT));
        userRepository.save(new User("Test", "Sitter1", "sitter1@example.com", "pass", "addr", "phone", Role.SITTER));
        userRepository.save(new User("Test", "Sitter2", "sitter2@example.com", "pass", "addr", "phone", Role.SITTER));

        // Act & Assert
        mockMvc.perform(get("/api/users/role/{role}", Role.SITTER) // Buscamos específicamente por el rol SITTER
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2))) // Verificamos que la respuesta contenga solo 2 usuarios
                .andExpect(jsonPath("$[0].role", is("SITTER"))) // Verificamos que el rol de los resultados es el correcto
                .andExpect(jsonPath("$[1].role", is("SITTER")));
    }

    /**
     * Prueba el escenario de fallo donde un CLIENT intenta usar el endpoint de búsqueda por rol.
     */
    @Test
    @DisplayName("GET /api/users/role/{role} | Falla | Un CLIENT no debería poder buscar por rol y debe recibir 403 Forbidden")
    void getUsersByRole_AsClient_ShouldReturnForbidden() throws Exception {
        // Arrange
        String clientToken = getAuthTokenForRole(Role.CLIENT);

        // Act & Assert
        mockMvc.perform(get("/api/users/role/{role}", Role.CLIENT)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }

    /**
     * Prueba el escenario de éxito donde un ADMIN obtiene una lista paginada
     * que contiene únicamente a los usuarios activos.
     */
    @Test
    @DisplayName("GET /api/users/active | Éxito | Un ADMIN debería obtener solo los usuarios activos")
    void getActiveUsers_AsAdmin_ShouldReturnOnlyActiveUsers() throws Exception {
        // Arrange
        // 1. Obtenemos el token de ADMIN. Este usuario es ACTIVO por defecto.
        String adminToken = getAuthTokenForRole(Role.ADMIN);

        // 2. Creamos un conjunto de datos mixto en la BD
        // Usuario activo
        User activeClient = new User("Active", "Client", "active.client@example.com", "pass", "addr", "phone", Role.CLIENT);
        activeClient.setActive(true);
        userRepository.save(activeClient);

        // Usuario inactivo
        User inactiveSitter = new User("Inactive", "Sitter", "inactive.sitter@example.com", "pass", "addr", "phone", Role.SITTER);
        inactiveSitter.setActive(false);
        userRepository.save(inactiveSitter);

        // En total, tenemos 2 usuarios activos en la BD (el ADMIN y activeClient).

        // Act & Assert
        mockMvc.perform(get("/api/users/active")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Verificamos que la página contiene solo a los 2 usuarios activos
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                // Verificamos que los usuarios retornados son efectivamente los activos
                .andExpect(jsonPath("$.content[?(@.fullName == 'Test ADMIN')]").exists())
                .andExpect(jsonPath("$.content[?(@.fullName == 'Active Client')]").exists());
    }

    /**
     * Prueba que un usuario no-admin no pueda acceder al endpoint de usuarios activos.
     */
    @Test
    @DisplayName("GET /api/users/active | Falla | Un CLIENT no debería poder obtener la lista y debe recibir 403 Forbidden")
    void getActiveUsers_AsClient_ShouldReturnForbidden() throws Exception {
        // Arrange
        String clientToken = getAuthTokenForRole(Role.CLIENT);

        // Act & Assert
        mockMvc.perform(get("/api/users/active")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }

    /**
     * Prueba el escenario de éxito donde un ADMIN obtiene una lista que contiene
     * únicamente a los usuarios con email sin verificar.
     */
    @Test
    @DisplayName("GET /api/users/unverified | Éxito | Un ADMIN debería obtener solo los usuarios no verificados")
    void getUnverifiedUsers_AsAdmin_ShouldReturnOnlyUnverifiedUsers() throws Exception {
        // Arrange
        // 1. Obtenemos un token de ADMIN.
        String adminToken = getAuthTokenForRole(Role.ADMIN);

        // 2. Creamos un conjunto de datos mixto en la BD H2.
        // Usuario verificado (el helper getAuthTokenForRole ya lo deja verificado)
        getAuthTokenForRole(Role.CLIENT);

        // Usuario NO verificado
        User unverifiedUser = new User("Unverified", "User", "unverified.user@example.com", "pass", "addr", "phone", Role.SITTER);
        unverifiedUser.setEmailVerifiedAt(null); // Explícitamente nulo
        userRepository.save(unverifiedUser);

        // Act & Assert
        mockMvc.perform(get("/api/users/unverified")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1))) // Verificamos que la respuesta contenga solo 1 usuario
                .andExpect(jsonPath("$[0].email", is("unverified.user@example.com"))) // Verificamos que es el usuario correcto
                .andExpect(jsonPath("$[0].emailVerified", is(false)));
    }

    /**
     * Prueba que un usuario no-admin no pueda acceder al endpoint de usuarios no verificados.
     */
    @Test
    @DisplayName("GET /api/users/unverified | Falla | Un CLIENT no debería poder obtener la lista y debe recibir 403 Forbidden")
    void getUnverifiedUsers_AsClient_ShouldReturnForbidden() throws Exception {
        // Arrange
        String clientToken = getAuthTokenForRole(Role.CLIENT);

        // Act & Assert
        mockMvc.perform(get("/api/users/unverified")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }

    /**
     * Verifica generación correcta de estadísticas de usuarios.
     *
     * <p>Válida los cálculos agregados que alimentan dashboards administrativos,
     * asegurando que las métricas reflejen correctamente el estado real de la BD.</p>
     */
    @Test
    @DisplayName("GET /api/users/stats | Éxito | Un ADMIN debería obtener las estadísticas correctas de usuarios")
    void getUserStats_AsAdmin_ShouldReturnCorrectStats() throws Exception {
        // Arrange
        // 1. Obtenemos un token de ADMIN. Este usuario ya es 1 ADMIN, activo y verificado.
        String adminToken = getAuthTokenForRole(Role.ADMIN);

        // 2. Creamos un conjunto de datos específico en la BD
        // Cliente 1: Activo y verificado
        User client1 = new User("C1", "ActiveVerified", "c1@e.com", "p", "a", "p", Role.CLIENT);
        client1.setActive(true);
        client1.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(client1);

        // Cliente 2: Activo pero NO verificado
        User client2 = new User("C2", "ActiveUnverified", "c2@e.com", "p", "a", "p", Role.CLIENT);
        client2.setActive(true);
        client2.setEmailVerifiedAt(null); // No verificado
        userRepository.save(client2);

        // Cuidador 1: Inactivo pero verificado
        User sitter1 = new User("S1", "InactiveVerified", "s1@e.com", "p", "a", "p", Role.SITTER);
        sitter1.setActive(false); // Inactivo
        sitter1.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(sitter1);

        // Estado esperado de la BD:
        // - Total Usuario: 4 (1 ADMIN + 2 CLIENT + 1 SITTER)
        // - Usuarios Activos: 3 (ADMIN, client1, client2)
        // - Total cliente: 2
        // - Total cuidadores: 1
        // - Total admins: 1
        // - Usuarios Verificados: 3 (ADMIN, client1, sitter1)

        // Act & Assert
        mockMvc.perform(get("/api/users/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Verificamos cada uno de los cálculos
                .andExpect(jsonPath("$.totalUsers", is(4)))
                .andExpect(jsonPath("$.activeUsers", is(3)))
                .andExpect(jsonPath("$.clientCount", is(2)))
                .andExpect(jsonPath("$.sitterCount", is(1)))
                .andExpect(jsonPath("$.adminCount", is(1)))
                .andExpect(jsonPath("$.verifiedUsers", is(3)));
    }

    /**
     * Prueba que un usuario no-admin no pueda acceder al endpoint de estadísticas.
     */
    @Test
    @DisplayName("GET /api/users/stats | Falla | Un CLIENT no debería poder obtener estadísticas y debe recibir 403 Forbidden")
    void getUserStats_AsClient_ShouldReturnForbidden() throws Exception {
        // Arrange
        String clientToken = getAuthTokenForRole(Role.CLIENT);

        // Act & Assert
        mockMvc.perform(get("/api/users/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }

    /**
     * Prueba el escenario donde se consulta un email que no está en uso.
     */
    @Test
    @DisplayName("GET /api/users/email-available | Éxito | Debería retornar true si el email está disponible")
    void isEmailAvailable_WhenEmailIsAvailable_ShouldReturnTrue() throws Exception {
        // Arrange
        // No se necesita crear ningún usuario, ya que la base de datos está limpia
        // para este test (gracias a @Transactional).

        // Act & Assert
        mockMvc.perform(get("/api/users/email-available")
                        .param("email", "email.disponible@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("true")); // Verificamos que la respuesta sea el booleano 'true'
    }

    /**
     * Prueba el escenario donde se consulta un email que ya está en uso.
     */
    @Test
    @DisplayName("GET /api/users/email-available | Éxito | Debería retornar false si el email NO está disponible")
    void isEmailAvailable_WhenEmailIsTaken_ShouldReturnFalse() throws Exception {
        // Arrange
        // Creamos un usuario en la BD con un email específico.
        userRepository.save(new User(
                "Existing",
                "User",
                "email.ocupado@example.com",
                "pass",
                "addr",
                "phone",
                Role.CLIENT));

        // Act & Assert
        mockMvc.perform(get("/api/users/email-available")
                        .param("email", "email.ocupado@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("false")); // Verificamos que la respuesta sea el booleano 'false'
    }

    /**
     * Válida endpoint de verificación de salud del servicio, cuantos Usuarios hay realmente (int).
     *
     */
    @Test
    @DisplayName("GET /api/users/health | Éxito | Debería retornar 200 OK si el servicio funciona")
    void healthCheck_WhenServiceIsUp_ShouldReturnOk() throws Exception {
        // Arrange: Creamos un usuario para que el conteo no sea cero.
        userRepository.save(new User("Health",
                "Check",
                "health@example.com",
                "pass",
                "addr",
                "phone",
                Role.CLIENT));

        long userCount = userRepository.count(); // Contamos cuántos usuarios hay realmente.

        // Act & Assert
        mockMvc.perform(get("/api/users/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("UserService está operativo. Total usuarios: " + userCount));
    }

    /**
     * Válida proceso de verificación de email con token válido.
     *
     * <p>Prueba el flujo completo de verificación por email utilizando el JWT real
     * y validando tanto la redirección como la persistencia del cambio.</p>
     *
     * @throws Exception Sí ocurre un error po que el token es inválido o malformado
     */
    @Test
    @DisplayName("GET /verify | Éxito | Debería verificar el email y redirigir a éxito")
    void verifyEmail_WithValidTokenAndH2_ShouldVerifyUserAndRedirectToSuccess() throws Exception {
        // Arrange
        // 1. Creamos un usuario en H2 con el email sin verificar.
        User userToVerify = new User("ToVerify", "User", "verify.h2@example.com", "pass", "addr", "phone", Role.CLIENT);
        userToVerify.setEmailVerifiedAt(null); // Estado inicial: no verificado
        User savedUser = userRepository.save(userToVerify);

        // 2. Usamos el JwtService real para generar un token válido para este usuario.
        String validToken = jwtService.generateVerificationToken(savedUser);

        // Act & Assert
        mockMvc.perform(get("/api/users/verify")
                        .param("token", validToken))
                .andExpect(status().isFound()) // Esperamos un redirect 302
                .andExpect(redirectedUrl("/verification-success.html"));

        // Verificación final: comprobar que el usuario fue actualizado
        User verifiedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        // Verificar que el usuario tenga en true la verificación de email
        assertThat(verifiedUser.isEmailVerified()).isTrue();
        assertThat(verifiedUser.getEmailVerifiedAt()).isNotNull();
    }

    /**
     * Prueba el escenario de fallo donde se usa un token inválido/malformado.
     * Utiliza H2 y el JwtService real.
     *
     * @throws Exception Sí ocurre un error po que el token es inválido o malformado
     */
    @Test
    @DisplayName("GET /verify | Falla | Debería redirigir a error con un token inválido")
    void verifyEmail_WithInvalidTokenAndH2_ShouldRedirectToError() throws Exception {
        // Arrange
        String invalidToken = "este-token-no-es-valido";

        // Este es el mensaje de error exacto que lanza el UserService
        String errorMessage = "El enlace de verificación es inválido o ha expirado. Por favor, solicita uno nuevo.";

        // Construimos la URL de redirección esperada completa
        String expectedRedirectUrl = "http://localhost:8080/verification-error?message=" + errorMessage;

        // Act & Assert: Si el token es inválido, debe redirigir a la URL de error
        mockMvc.perform(get("/api/users/verify")
                        .param("token", invalidToken))
                .andExpect(status().isFound()) // Sigue siendo un redirect
                .andExpect(redirectedUrl(expectedRedirectUrl)); // Verificamos que redirige a la URL de error
    }

}