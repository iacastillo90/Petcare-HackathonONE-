package com.Petcare.Petcare.Service;

import com.Petcare.Petcare.Configurations.Security.Jwt.JwtService;
import com.Petcare.Petcare.DTOs.Auth.Request.LoginRequest;
import com.Petcare.Petcare.DTOs.Auth.Respone.AuthResponse;
import com.Petcare.Petcare.DTOs.User.CreateUserRequest;
import com.Petcare.Petcare.DTOs.User.UpdateUserRequest;
import com.Petcare.Petcare.DTOs.User.UserResponse;
import com.Petcare.Petcare.DTOs.User.UserSummaryResponse;
import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.AccountRepository;
import com.Petcare.Petcare.Repositories.AccountUserRepository;
import com.Petcare.Petcare.Repositories.UserRepository;
import com.Petcare.Petcare.Services.EmailService;
import com.Petcare.Petcare.Services.Implement.UserServiceImplement;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertThrows;

/**
 * Suite completa de pruebas unitarias para {@link UserServiceImplement}.
 *
 * Esta clase se enfoca en verificar que la lógica de negocio del servicio de usuarios funcione
 * correctamente, especialmente en los aspectos críticos como autenticación, registro y gestión
 * de estados de usuario. Cada test está diseñado para validar un escenario específico, desde
 * los casos exitosos hasta las situaciones de error que podrían comprometer la seguridad o
 * integridad del sistema.
 *
 * Los tests cubren:
 * - Flujos de autenticación con diferentes estados de usuario (activo/inactivo, verificado/no verificado)
 * - Registro de usuarios con distintos roles (CLIENT, SITTER, ADMIN)
 * - Operaciones CRUD con validaciones de negocio
 * - Manejo de excepciones y casos edge
 *
 * @author Equipo de Desarrollo Petcare
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplementTest {

    // Dependencias mockeadas - simulan el comportamiento de las capas inferiores
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private AccountUserRepository accountUserRepository;

    // La instancia real del servicio que vamos a probar, con todas sus dependencias inyectadas
    @InjectMocks
    private UserServiceImplement userService;

    // Objetos de datos reutilizables en multiple tests - evitamos duplicación de código
    private User user;
    private Account account;
    private LoginRequest loginRequest;

    /**
     * Configuración inicial que se ejecuta antes de cada prueba individual.
     *
     * Preparamos un usuario en estado "ideal" (activo y con email verificado) junto con
     * su cuenta asociada. Este es nuestro escenario base exitoso - los tests específicos
     * modificarán estos datos según lo que necesiten probar.
     *
     * Esta estrategia nos permite mantener los tests más legibles y enfocados en lo que
     * realmente quieren verificar, sin repetir la configuración básica.
     */
    @BeforeEach
    void setUp() {
        // Datos básicos de login que usaremos en múltiples tests
        loginRequest = new LoginRequest("test@example.com", "password123");

        // Creamos un usuario con todas las propiedades necesarias para un login exitoso
        user = new User();
        user.setId(1L);
        user.setFirstName("Ivan");
        user.setLastName("Castillo");
        user.setEmail("test@example.com");
        user.setRole(Role.CLIENT);
        user.setActive(true); // Estado activo por defecto - los tests de fallo lo cambiarán
        user.setEmailVerifiedAt(LocalDateTime.now()); // Email verificado por defecto

        // Cada usuario tiene una cuenta asociada en nuestro modelo de negocio
        account = new Account(user, "Cuenta de Familia", "ACC-123");
        account.setId(1L);
    }

    /**
     * Prueba el escenario ideal de login: usuario válido, activo y con email verificado.
     *
     * Este test verifica que cuando todo está correcto, el flujo de autenticación
     * funciona como esperamos: se autentica al usuario, se genera un token JWT,
     * se actualiza la fecha de último login y se retorna una respuesta exitosa.
     */
    @Test
    @DisplayName("login | Éxito | Debería retornar AuthResponse para un usuario válido, activo y verificado")
    void login_WithValidAndVerifiedUser_ShouldReturnAuthResponse() {
        // Configuramos los mocks para simular un escenario exitoso
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(accountRepository.findByOwnerUser(user)).thenReturn(Optional.of(account));
        when(jwtService.getToken(user)).thenReturn("fake-jwt-token");

        // Ejecutamos el método que queremos probar
        AuthResponse response = userService.login(loginRequest);

        // Verificamos que la respuesta sea la esperada
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        // Una parte importante del login es actualizar cuándo fue el último acceso
        assertThat(user.getLastLoginAt()).isNotNull();

        // Confirmamos que se ejecutaron las operaciones esperadas en el orden correcto
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).save(user); // El usuario se guarda para persistir el lastLoginAt
    }

    /**
     * Verifica que el sistema maneje correctamente las credenciales incorrectas.
     *
     * Cuando Spring Security detecta que la contraseña no coincide, lanza una
     * BadCredentialsException. Nuestro servicio debe propagar esta excepción
     * sin realizar ninguna operación adicional (como guardar el usuario).
     */
    @Test
    @DisplayName("login | Falla | Debería lanzar BadCredentialsException si la contraseña es incorrecta")
    void login_WhenAuthenticationFails_ShouldThrowBadCredentialsException() {
        // Simulamos que Spring Security rechaza las credenciales
        doThrow(new BadCredentialsException("Credenciales inválidas"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Verificamos que la excepción se propaga correctamente
        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciales inválidas");

        // Es crucial que NO se guarde el usuario cuando la autenticación falla
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Prueba el manejo de usuarios deshabilitados o inactivos.
     *
     * Aunque las credenciales sean correctas, si un usuario está marcado como
     * inactivo, Spring Security debe bloquear el acceso lanzando una DisabledException.
     * Esta es una medida de seguridad importante para poder suspender cuentas cuando sea necesario.
     */
    @Test
    @DisplayName("login | Falla | Debería propagar DisabledException si el usuario está inactivo")
    void login_WhenUserIsInactive_ShouldThrowDisabledException() {
        // Spring Security detecta que el usuario está deshabilitado
        doThrow(new DisabledException("Usuario deshabilitado"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // La excepción debe propagarse tal como viene de Spring Security
        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(DisabledException.class);

        // No debe realizarse ninguna operación de guardado
        verify(userRepository, never()).save(any());
    }

    /**
     * Verifica nuestra validación de negocio: email no verificado.
     *
     * Este es un caso interesante donde las credenciales son válidas y el usuario
     * está activo, pero agregamos una regla de negocio adicional: el email debe
     * estar verificado para permitir el login. Esta validación ocurre DESPUÉS
     * de que Spring Security aprueba las credenciales.
     */
    @Test
    @DisplayName("login | Falla | Debería lanzar BadCredentialsException si el email no está verificado")
    void login_WhenEmailIsNotVerified_ShouldThrowBadCredentialsException() {
        // Modificamos nuestro usuario base para simular email no verificado
        user.setEmailVerifiedAt(null); // Esta es la condición que queremos probar
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));

        // Las credenciales son válidas, pero nuestra lógica adicional debe rechazar el login
        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("El correo electrónico no ha sido verificado.");

        // Verificamos que Spring Security sí intentó autenticar, pero no guardamos el usuario
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Prueba el registro exitoso de un usuario con rol SITTER.
     *
     * El registro de cuidadores implica varias operaciones: crear el usuario,
     * asignar el rol específico, generar la cuenta asociada y enviar un email
     * de verificación. Este test verifica que toda esta orquestación funcione
     * correctamente y en el orden adecuado.
     */
    @Test
    @DisplayName("registerUserSitter | Éxito | Debería registrar un nuevo cuidador satisfecho")
    void registerUserSitter_WhenEmailIsNotTaken_ShouldSucceedAndAssignSitterRole() throws MessagingException {
        // Datos de entrada para registrar un cuidador
        CreateUserRequest sitterRequest = new CreateUserRequest(
                "Sitter",
                "Test",
                "sitter.test@example.com",
                "ValidPassword123",
                "Calle Falsa 123",
                "987654321"
        );

        // Usamos un ArgumentCaptor para inspeccionar el usuario que se guarda
        // Esto nos permite verificar que el rol se asignó correctamente
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // Configuramos el escenario exitoso: email disponible, operaciones exitosas
        when(userRepository.findByEmail(sitterRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(sitterRequest.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.getToken(any(User.class))).thenReturn("fake-jwt-token");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), anyString(), anyInt());

        // Ejecutamos el registro
        AuthResponse response = userService.registerUserSitter(sitterRequest);

        // Verificamos la respuesta del servicio
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getRole()).isEqualTo(Role.SITTER.name());

        // La verificación más importante: el usuario debe tener el rol SITTER
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getRole()).isEqualTo(Role.SITTER);

        // Confirmamos que se ejecutaron todas las operaciones necesarias
        verify(userRepository).save(any());
        verify(accountRepository).save(any(Account.class));
        verify(accountRepository).save(any()); // Se puede llamar dos veces por la lógica de negocio
    }

    /**
     * Verifica que no se puedan registrar usuarios con emails duplicados.
     *
     * Esta es una validación fundamental para mantener la integridad de los datos.
     * Cada email debe ser único en el sistema, por lo que si alguien intenta
     * registrarse con un email que ya existe, debemos rechazar la operación
     * inmediatamente.
     */
    @Test
    @DisplayName("registerUserSitter | Falla | Debería lanzar IllegalArgumentException si el email ya está en uso")
    void registerUserSitter_WhenEmailIsTaken_ShouldThrowIllegalArgumentException() {
        CreateUserRequest sitterRequest = new CreateUserRequest(
                "Sitter",
                "Test",
                "sitter.test@example.com",
                "ValidPassword123",
                "Calle Falsa 123",
                "987654321"
        );

        // Simulamos que ya existe un usuario con ese email
        when(userRepository.findByEmail(sitterRequest.getEmail())).thenReturn(Optional.of(user));

        // La operación debe fallar inmediatamente con la excepción apropiada
        assertThrows(IllegalArgumentException.class, () -> userService.registerUserSitter(sitterRequest));
    }

    /**
     * Test alternativo para el mismo escenario, usando una sintaxis diferente de AssertJ.
     *
     * Mantuvimos ambos tests para mostrar diferentes estilos de verificación de excepciones.
     * En un proyecto real, elegiríamos uno u otro estilo y lo usaríamos consistentemente.
     */
    @Test
    @DisplayName("registerUserSitter | Debería lanzar excepción si el email ya existe")
    void registerUserSitter_WhenEmailIsTaken_ShouldThrowException() {
        CreateUserRequest sitterRequest = new CreateUserRequest(
                "Sitter", "Test", "sitter.test@example.com",
                "password123", "123 Sitter St", "555-1111"
        );

        // Email ya en uso
        when(userRepository.findByEmail(sitterRequest.getEmail())).thenReturn(Optional.of(new User()));

        // Verificamos tanto el tipo como el mensaje de la excepción
        assertThatThrownBy(() -> userService.registerUserSitter(sitterRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El email ya está registrado: " + sitterRequest.getEmail());

        // Es importante verificar que NO se ejecutaron operaciones de guardado
        verify(userRepository, never()).save(any());
    }

    /**
     * Prueba el registro de un usuario con rol CLIENT (cliente).
     *
     * Similar al registro de cuidadores, pero con el rol CLIENT. Este test
     * verifica que el sistema puede manejar diferentes tipos de usuarios
     * durante el registro, asignando el rol correcto a cada uno.
     */
    @Test
    @DisplayName("registerUser | Debería registrar un nuevo CLIENTE y retornar AuthResponse")
    void registerUser_WhenEmailIsNotTaken_ShouldSucceedAndAssignClientRole() throws MessagingException {
        CreateUserRequest clientRequest = new CreateUserRequest(
                "Cliente", "Prueba", "cliente.prueba@example.com",
                "password123", "Av. Siempre Viva 742", "555-1234"
        );

        // Capturamos el usuario para verificar que se asigna el rol correcto
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // Configuramos mocks para un registro exitoso
        when(userRepository.findByEmail(clientRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(userCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.getToken(any(User.class))).thenReturn("fake-client-token");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), anyString(), anyInt());

        // Ejecutamos el registro de cliente
        AuthResponse response = userService.registerUser(clientRequest);

        // Verificamos la respuesta
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("fake-client-token");
        assertThat(response.getRole()).isEqualTo(Role.CLIENT.name());

        // Lo más importante: verificar que el rol asignado sea CLIENT
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getRole()).isEqualTo(Role.CLIENT);

        // Confirmamos que se ejecutaron todas las operaciones del flujo de registro
        verify(userRepository).save(any(User.class));
        verify(accountRepository).save(any(Account.class));
        verify(accountUserRepository).save(any()); // Relación usuario-cuenta
        verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString(), anyInt());
    }

    /**
     * Verifica que el registro de clientes también valide emails duplicados.
     *
     * La validación de email único debe aplicarse consistentemente sin importar
     * el tipo de usuario que se esté registrando.
     */
    @Test
    @DisplayName("registerUser | Debería lanzar excepción si el email ya existe")
    void registerUser_WhenEmailIsTaken_ShouldThrowException() {
        CreateUserRequest clientRequest = new CreateUserRequest(
                "Cliente", "Prueba", "cliente.prueba@example.com",
                "password123", "Av. Siempre Viva 742", "555-1234"
        );

        // Email ya registrado por otro usuario
        when(userRepository.findByEmail(clientRequest.getEmail())).thenReturn(Optional.of(new User()));

        // La operación debe fallar con el mensaje apropiado
        assertThatThrownBy(() -> userService.registerUser(clientRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El email ya está registrado: " + clientRequest.getEmail());

        // Ninguna operación de guardado debe ejecutarse
        verify(userRepository, never()).save(any());
    }

    /**
     * Prueba la funcionalidad de listar todos los usuarios del sistema.
     *
     * Este test verifica que el servicio pueda recuperar usuarios de la base de datos
     * y transformarlos correctamente a DTOs para la respuesta. Es importante verificar
     * que el mapeo entre entidad y DTO sea correcto.
     */
    @Test
    @DisplayName("getAllUsers | Debería retornar una lista de UserResponse cuando existen usuarios")
    void getAllUsers_WhenUsersExist_ShouldReturnUserResponseList() {
        // Preparamos datos de prueba: dos usuarios con roles diferentes
        User user1 = new User("Ivan", "Castillo", "ivan@example.com", "pass", "123", "addr", Role.CLIENT);
        User user2 = new User("Maria", "Perez", "maria@example.com", "pass", "456", "addr", Role.SITTER);
        List<User> mockUserList = List.of(user1, user2);

        // El repositorio devuelve nuestra lista de prueba
        when(userRepository.findAll()).thenReturn(mockUserList);

        // Ejecutamos la operación
        List<UserResponse> result = userService.getAllUsers();

        // Verificamos que se retorne la cantidad correcta de usuarios
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        // Verificamos que el mapeo entidad->DTO sea correcto para el primer usuario
        assertThat(result.get(0).getEmail()).isEqualTo("ivan@example.com");
        assertThat(result.get(0).getFullName()).isEqualTo("Ivan Castillo");
        assertThat(result.get(0).getRole()).isEqualTo(Role.CLIENT);

        // Confirmamos que se llamó al método correcto del repositorio
        verify(userRepository).findAll();
    }

    /**
     * Verifica el comportamiento cuando no hay usuarios en el sistema.
     *
     * Es importante que el método maneje graciosamente el caso donde la base de datos
     * esté vacía, retornando una lista vacía en lugar de null o lanzar una excepción.
     */
    @Test
    @DisplayName("getAllUsers | Debería retornar una lista vacía cuando no existen usuarios")
    void getAllUsers_WhenNoUsersExist_ShouldReturnEmptyList() {
        // El repositorio devuelve una lista vacía
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Ejecutamos la operación
        List<UserResponse> result = userService.getAllUsers();

        // El resultado debe ser una lista vacía, no null
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(userRepository).findAll();
    }

    /**
     * Prueba la búsqueda de un usuario específico por su ID.
     *
     * Esta operación es fundamental para muchas otras funcionalidades del sistema.
     * Verificamos que cuando el usuario existe, se retorne correctamente mapeado a DTO.
     */
    @Test
    @DisplayName("getUserById | Debería retornar Optional<UserResponse> cuando el usuario existe")
    void getUserById_WhenUserExists_ShouldReturnOptionalOfUserResponse() {
        // Usamos el usuario configurado en setUp()
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Ejecutamos la búsqueda
        Optional<UserResponse> result = userService.getUserById(1L);

        // Verificamos que encontramos el usuario
        assertThat(result).isPresent();

        // Verificamos que el mapeo sea correcto
        assertThat(result.get().getId()).isEqualTo(user.getId());
        assertThat(result.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(result.get().getFirstName()).isEqualTo("Ivan");

        verify(userRepository).findById(1L);
    }

    /**
     * Verifica el manejo cuando se busca un usuario que no existe.
     *
     * El método debe retornar un Optional vacío en lugar de lanzar una excepción,
     * permitiendo que el código cliente maneje la ausencia del usuario apropiadamente.
     */
    @Test
    @DisplayName("getUserById | Debería retornar Optional vacío cuando el usuario no existe")
    void getUserById_WhenUserDoesNotExist_ShouldReturnEmptyOptional() {
        // El repositorio no encuentra ningún usuario con ese ID
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Ejecutamos la búsqueda
        Optional<UserResponse> result = userService.getUserById(99L);

        // El resultado debe ser un Optional vacío
        assertThat(result).isEmpty();

        verify(userRepository).findById(99L);
    }

    /**
     * Prueba la búsqueda de usuario por email.
     *
     * El email es nuestro identificador único para login, por lo que esta funcionalidad
     * es crítica. Verificamos que funcione correctamente cuando el usuario existe.
     */
    @Test
    @DisplayName("getUserByEmail | Debería retornar Optional<UserResponse> cuando el email existe")
    void getUserByEmail_WhenUserExists_ShouldReturnOptionalOfUserResponse() {
        String userEmail = "test@example.com";
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        // Ejecutamos la búsqueda por email
        Optional<UserResponse> result = userService.getUserByEmail(userEmail);

        // Verificamos que se encuentre el usuario correcto
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(user.getId());
        assertThat(result.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(result.get().getFirstName()).isEqualTo("Ivan");

        verify(userRepository).findByEmail(userEmail);
    }

    /**
     * Verifica el comportamiento cuando se busca un email que no existe en el sistema.
     */
    @Test
    @DisplayName("getUserByEmail | Debería retornar Optional vacío cuando el email no existe")
    void getUserByEmail_WhenUserDoesNotExist_ShouldReturnEmptyOptional() {
        String nonExistentEmail = "no.existe@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        Optional<UserResponse> result = userService.getUserByEmail(nonExistentEmail);

        assertThat(result).isEmpty();

        verify(userRepository).findByEmail(nonExistentEmail);
    }

    /**
     * Prueba la actualización de información básica del usuario.
     *
     * Verificamos que se puedan actualizar campos como nombre, dirección y teléfono
     * sin afectar información sensible como contraseñas o email. Cuando la contraseña
     * viene vacía, debe mantenerse la actual.
     */
    @Test
    @DisplayName("updateUser | Debería actualizar solo los datos básicos del usuario")
    void updateUser_ShouldUpdateBasicInfo() {
        Long userId = 1L;
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Ivan Actualizado", "Castillo Actualizado", user.getEmail(), // Mantenemos el email
                "", // Contraseña vacía - no debe actualizarse
                "Nueva Direccion 456", "987654321"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutamos la actualización
        UserResponse result = userService.updateUser(userId, updateRequest);

        // Verificamos que solo se actualizaron los campos esperados
        assertThat(result.getFirstName()).isEqualTo("Ivan Actualizado");
        assertThat(result.getLastName()).isEqualTo("Castillo Actualizado");
        assertThat(result.getAddress()).isEqualTo("Nueva Direccion 456");

        // Importante: la contraseña NO debe haberse procesado
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(any(User.class));
    }

    /**
     * Prueba la actualización del email del usuario.
     *
     * Cuando un usuario cambia su email, esto tiene implicaciones de seguridad importantes:
     * debe resetarse el estado de verificación del email para forzar una nueva verificación.
     */
    @Test
    @DisplayName("updateUser | Debería actualizar el email y resetear la verificación")
    void updateUser_ShouldUpdateEmailAndResetVerification() {
        Long userId = 1L;
        String newEmail = "nuevo.email@example.com";
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                user.getFirstName(), user.getLastName(), newEmail, "", user.getAddress(), user.getPhoneNumber()
        );

        // El usuario inicialmente tiene el email verificado
        user.setEmailVerifiedAt(LocalDateTime.now());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty()); // Nuevo email disponible
        when(userRepository.save(userCaptor.capture())).thenReturn(user);

        // Ejecutamos la actualización
        userService.updateUser(userId, updateRequest);

        // Verificamos que el email se actualizó Y la verificación se reseteo
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(newEmail);
        assertThat(savedUser.getEmailVerifiedAt()).isNull(); // ¡Crítico! Debe ser null
    }

    /**
     * Prueba la actualización de contraseña cuando se proporciona una nueva.
     *
     * Cuando el usuario envía una contraseña nueva (no vacía), debe ser encriptada
     * antes de guardarse en la base de datos.
     */
    @Test
    @DisplayName("updateUser | Debería actualizar la contraseña cuando se proporciona una nueva")
    void updateUser_ShouldUpdatePasswordWhenProvided() {
        Long userId = 1L;
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                user.getFirstName(), user.getLastName(), user.getEmail(),
                "nuevaPassword123", // Nueva contraseña proporcionada
                user.getAddress(), user.getPhoneNumber()
        );

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("nuevaPassword123")).thenReturn("nuevaPasswordCodificada");
        when(userRepository.save(userCaptor.capture())).thenReturn(user);

        // Ejecutamos la actualización
        userService.updateUser(userId, updateRequest);

        // Verificamos que se llamó al encoder con la nueva contraseña
        verify(passwordEncoder).encode("nuevaPassword123");

        // Y que se guardó la versión encriptada
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("nuevaPasswordCodificada");
    }

    /**
     * Verifica que se lance una excepción cuando se intenta actualizar un usuario inexistente.
     *
     * Esta validación es fundamental para evitar operaciones sobre datos que no existen
     * y proporcionar feedback claro al usuario sobre por qué falló la operación.
     */
    @Test
    @DisplayName("updateUser | Debería lanzar RuntimeException si el usuario no existe")
    void updateUser_WhenUserNotFound_ShouldThrowRuntimeException() {
        Long userId = 99L;
        UpdateUserRequest updateRequest = new UpdateUserRequest(user.getFirstName(), user.getLastName(), user.getEmail(),
                "nuevaPassword123", user.getAddress(), user.getPhoneNumber());

        // El repositorio no encuentra el usuario
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Debe lanzarse una excepción descriptiva
        assertThatThrownBy(() -> userService.updateUser(userId, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado con id " + userId);
    }

    /**
     * Prueba la validación de email único durante actualizaciones.
     *
     * Cuando un usuario intenta cambiar su email a uno que ya está en uso por
     * otra persona, debemos rechazar la operación para mantener la integridad
     * de los datos.
     */
    @Test
    @DisplayName("updateUser | Debería lanzar IllegalArgumentException si el nuevo email ya está en uso")
    void updateUser_WhenNewEmailIsTaken_ShouldThrowIllegalArgumentException() {
        Long userId = 1L;
        String takenEmail = "email.usado@example.com";
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                user.getFirstName(), user.getLastName(), takenEmail, "", user.getAddress(), user.getPhoneNumber()
        );

        // Creamos otro usuario que ya tiene el email que se quiere usar
        User anotherUser = new User();
        anotherUser.setId(2L); // ID diferente al usuario que se está actualizando
        anotherUser.setEmail(takenEmail);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(takenEmail)).thenReturn(Optional.of(anotherUser));

        // La operación debe fallar porque el email ya está tomado
        assertThatThrownBy(() -> userService.updateUser(userId, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El nuevo email ya está registrado: " + takenEmail);

        // No debe realizarse ningún guardado
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Prueba la eliminación exitosa de un usuario existente.
     *
     * Esta operación es crítica y debe verificarse que el usuario existe antes
     * de proceder con la eliminación. Si existe, se delega al repositorio la
     * operación de borrado físico.
     */
    @Test
    @DisplayName("deleteUser | Debería llamar al método de eliminación cuando el usuario existe")
    void deleteUser_WhenUserExists_ShouldCallRepositoryDelete() {
        Long userId = 1L;

        // Simulamos que el usuario SÍ existe en la base de datos
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        // Ejecutamos la eliminación
        userService.deleteUser(userId);

        // Verificamos que se llamó al método de eliminación del repositorio
        verify(userRepository).deleteById(userId);
    }

    /**
     * Verifica el manejo de intentos de eliminar usuarios inexistentes.
     *
     * Es importante validar que el usuario existe antes de intentar eliminarlo.
     * Si no existe, debemos informar claramente del problema en lugar de
     * fallar silenciosamente.
     */
    @Test
    @DisplayName("deleteUser | Debería lanzar RuntimeException cuando el usuario no existe")
    void deleteUser_WhenUserDoesNotExist_ShouldThrowRuntimeException() {
        Long userId = 99L;

        // El usuario no existe en la base de datos
        when(userRepository.existsById(userId)).thenReturn(false);

        // Debe lanzarse una excepción descriptiva
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado con id " + userId);

        // Nunca debe llamarse al método de eliminación
        verify(userRepository, never()).deleteById(anyLong());
    }

    /**
     * Prueba la creación de usuarios por parte de administradores.
     *
     * Los administradores pueden crear usuarios con roles específicos sin pasar
     * por el flujo de registro normal. Este test verifica que el rol se asigne
     * correctamente según lo especificado por el admin.
     */
    @Test
    @DisplayName("createUserByAdmin | Debería crear un usuario con el rol especificado")
    void createUserByAdmin_ShouldCreateUserWithSpecifiedRole() {
        CreateUserRequest adminRequest = new CreateUserRequest(
                "AdminCreated", "Sitter", "admin.creates@example.com",
                "password123", "Admin Address", "555-9999"
        );
        Role specifiedRole = Role.SITTER;

        // Capturamos el usuario para verificar que tiene el rol correcto
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findByEmail(adminRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutamos la creación por admin
        UserResponse result = userService.createUserByAdmin(adminRequest, specifiedRole);

        // Verificamos la respuesta
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(adminRequest.getEmail());
        assertThat(result.getRole()).isEqualTo(specifiedRole);

        // Lo más importante: verificar que el usuario guardado tiene el rol especificado
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getRole()).isEqualTo(specifiedRole);

        verify(userRepository).save(any(User.class));
    }

    /**
     * Verifica que los admins también respeten la regla de emails únicos.
     *
     * Incluso cuando un administrador crea un usuario, debe respetarse la
     * integridad de datos básica como la unicidad del email.
     */
    @Test
    @DisplayName("createUserByAdmin | Debería lanzar IllegalArgumentException si el email ya existe")
    void createUserByAdmin_WhenEmailIsTaken_ShouldThrowIllegalArgumentException() {
        CreateUserRequest adminRequest = new CreateUserRequest(
                "AdminCreated", "Sitter", "email.existente@example.com",
                "password123", "Admin Address", "555-9999"
        );
        Role specifiedRole = Role.ADMIN;

        // El email ya está en uso
        when(userRepository.findByEmail(adminRequest.getEmail())).thenReturn(Optional.of(new User()));

        // La operación debe fallar
        assertThatThrownBy(() -> userService.createUserByAdmin(adminRequest, specifiedRole))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El email ya está registrado: " + adminRequest.getEmail());

        // No debe guardarse nada
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Prueba la funcionalidad de activar un usuario previamente inactivo.
     *
     * Los administradores pueden cambiar el estado activo/inactivo de los usuarios.
     * Este test verifica que se pueda activar correctamente a un usuario inactivo.
     */
    @Test
    @DisplayName("toggleUserActive | Debería activar un usuario inactivo")
    void toggleUserActive_ShouldActivateAnInactiveUser() {
        Long userId = 1L;
        // Configuramos el usuario como inactivo inicialmente
        user.setActive(false);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(userCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // Activamos al usuario (parámetro true = activar)
        UserResponse response = userService.toggleUserActive(userId, true);

        // Verificamos que la respuesta indique que está activo
        assertThat(response.isActive()).isTrue();

        // Y que el usuario guardado también esté marcado como activo
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.isActive()).isTrue();

        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    /**
     * Prueba la desactivación de un usuario activo.
     *
     * El lado opuesto de la funcionalidad anterior: poder desactivar usuarios
     * que están actualmente activos en el sistema.
     */
    @Test
    @DisplayName("toggleUserActive | Debería desactivar un usuario activo")
    void toggleUserActive_ShouldDeactivateAnActiveUser() {
        Long userId = 1L;
        // El usuario está activo por defecto según nuestro setUp()
        user.setActive(true);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(userCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // Desactivamos al usuario (parámetro false = desactivar)
        UserResponse response = userService.toggleUserActive(userId, false);

        // Verificamos que tanto la respuesta como la entidad reflejen el estado inactivo
        assertThat(response.isActive()).isFalse();

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.isActive()).isFalse();

        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    /**
     * Verifica el manejo de errores cuando se intenta cambiar el estado de un usuario inexistente.
     */
    @Test
    @DisplayName("toggleUserActive | Debería lanzar RuntimeException si el usuario no existe")
    void toggleUserActive_WhenUserNotFound_ShouldThrowRuntimeException() {
        Long nonExistentUserId = 99L;
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // Intentar cambiar el estado de un usuario que no existe debe fallar
        assertThatThrownBy(() -> userService.toggleUserActive(nonExistentUserId, true))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado con id " + nonExistentUserId);

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Prueba el marcado de email como verificado.
     *
     * Cuando un usuario completa el proceso de verificación de email (por ejemplo,
     * haciendo clic en un enlace enviado por correo), necesitamos marcar su email
     * como verificado estableciendo la fecha de verificación.
     */
    @Test
    @DisplayName("markEmailAsVerified | Debería establecer la fecha de verificación cuando el usuario existe")
    void markEmailAsVerified_WhenUserExists_ShouldSetVerificationDateAndSave() {
        Long userId = 1L;
        // Empezamos con el email no verificado
        user.setEmailVerifiedAt(null);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(userCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // Marcamos el email como verificado
        UserResponse response = userService.markEmailAsVerified(userId);

        // Verificamos que la respuesta tenga fecha de verificación
        assertThat(response.getEmailVerifiedAt()).isNotNull();

        // Y que el usuario guardado también tenga la fecha establecida
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmailVerifiedAt()).isNotNull();

        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    /**
     * Verifica el manejo de errores al intentar verificar el email de un usuario inexistente.
     */
    @Test
    @DisplayName("markEmailAsVerified | Debería lanzar RuntimeException si el usuario no existe")
    void markEmailAsVerified_WhenUserNotFound_ShouldThrowRuntimeException() {
        Long nonExistentUserId = 99L;
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // Intentar verificar el email de un usuario inexistente debe fallar
        assertThatThrownBy(() -> userService.markEmailAsVerified(nonExistentUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado con id " + nonExistentUserId);

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Prueba el filtrado de usuarios por rol.
     *
     * Esta funcionalidad permite obtener listas específicas de usuarios según su rol
     * (por ejemplo, todos los cuidadores o todos los clientes). Es útil para
     * funcionalidades administrativas y reportes.
     */
    @Test
    @DisplayName("getUsersByRole | Debería retornar solo usuarios con el rol especificado")
    void getUsersByRole_ShouldReturnFilteredUserList() {
        Role roleToFind = Role.SITTER;

        // Creamos usuarios con el rol que buscamos
        User sitter1 = new User("Sitter", "Uno", "sitter1@example.com", "pass", "1", "addr", Role.SITTER);
        User sitter2 = new User("Sitter", "Dos", "sitter2@example.com", "pass", "2", "addr", Role.SITTER);
        List<User> mockSitterList = List.of(sitter1, sitter2);

        // El repositorio ya debe filtrar por rol, retornando solo usuarios con ese rol
        when(userRepository.findAllByRole(roleToFind)).thenReturn(mockSitterList);

        // Ejecutamos la búsqueda
        List<UserSummaryResponse> result = userService.getUsersByRole(roleToFind);

        // Verificamos que obtuvimos el número correcto de resultados
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        // Verificamos que todos los usuarios retornados tengan el rol solicitado
        assertThat(result).allMatch(userSummary -> userSummary.getRole() == roleToFind);

        // Verificamos el mapeo correcto del primer usuario
        assertThat(result.get(0).getFullName()).isEqualTo("Sitter Uno");
        assertThat(result.get(0).getEmail()).isEqualTo("sitter1@example.com");

        verify(userRepository).findAllByRole(roleToFind);
    }
}