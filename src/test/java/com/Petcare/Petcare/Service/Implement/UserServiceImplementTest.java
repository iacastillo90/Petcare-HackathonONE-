package com.Petcare.Petcare.Service.Implement;

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
 * Pruebas unitarias para la versión final de login en {@link UserServiceImplement}.
 * Valida la lógica de negocio del servicio, incluyendo pre y post-autenticación.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplementTest {

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

    @InjectMocks
    private UserServiceImplement userService;

    private User user;
    private Account account;
    private LoginRequest loginRequest;

    /**
     * Prepara un usuario 'ideal' (activo y verificado) antes de cada prueba.
     * Los tests de fallo modificarán este estado ideal para probar sus escenarios.
     */
    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("test@example.com", "password123");
        user = new User();
        user.setId(1L);
        user.setFirstName("Ivan");
        user.setLastName("Castillo");
        user.setEmail("test@example.com");
        user.setRole(Role.CLIENT);
        user.setActive(true); // Usuario activo por defecto
        user.setEmailVerifiedAt(LocalDateTime.now()); // Email verificado por defecto

        account = new Account(user, "Cuenta de Familia", "ACC-123");
        account.setId(1L);
    }

    @Test
    @DisplayName("login | Éxito | Debería retornar AuthResponse para un usuario válido, activo y verificado")
    void login_WithValidAndVerifiedUser_ShouldReturnAuthResponse() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(accountRepository.findByOwnerUser(user)).thenReturn(Optional.of(account));
        when(jwtService.getToken(user)).thenReturn("fake-jwt-token");

        // Act
        AuthResponse response = userService.login(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(user.getLastLoginAt()).isNotNull();

        // Verificamos que se llamó a la autenticación y se guardó el usuario
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("login | Falla | Debería lanzar BadCredentialsException si la contraseña es incorrecta")
    void login_WhenAuthenticationFails_ShouldThrowBadCredentialsException() {
        // Arrange
        // Simulamos el fallo principal de autenticación
        doThrow(new BadCredentialsException("Credenciales inválidas"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Act & Assert
        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciales inválidas");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("login | Falla | Debería propagar DisabledException si el usuario está inactivo")
    void login_WhenUserIsInactive_ShouldThrowDisabledException() {
        // Arrange
        // Simulamos que el AuthenticationManager detecta que el usuario está deshabilitado
        doThrow(new DisabledException("Usuario deshabilitado"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Act & Assert
        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(DisabledException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login | Falla | Debería lanzar BadCredentialsException si el email no está verificado")
    void login_WhenEmailIsNotVerified_ShouldThrowBadCredentialsException() {
        // Arrange
        user.setEmailVerifiedAt(null); // Condición de prueba
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        // En este escenario, la autenticación de contraseña SÍ es exitosa
        // pero nuestra validación de negocio posterior debe fallar.

        // Act & Assert
        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("El correo electrónico no ha sido verificado.");

        // Verificamos que SÍ se intentó autenticar, pero NUNCA se guardó el usuario
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Prueba el registro exitoso de un nuevo usuario con el rol SITTER.
     * Verifica que se cree el usuario, la cuenta, se asigne el rol correcto
     * y se envíe el correo de verificación.
     */
    @Test
    @DisplayName("registerUserSitter | Éxito | Debería registrar un nuevo cuidador satisfecho")
    void registerUserSitter_WhenEmailIsNotTaken_ShouldSucceedAndAssignSitterRole() throws MessagingException {

        CreateUserRequest sitterRequest = new CreateUserRequest(
                "Sitter",
                "Test",
                "sitter.test@example.com",
                "ValidPassword123",
                "Calle Falsa 123",
                "987654321"
        );

        // Creamos un ArgumentoCaptor para inspeccionar al usuario que se está guardando
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findByEmail(sitterRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(sitterRequest.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.getToken(any(User.class))).thenReturn("fake-jwt-token");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0
        ));
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), anyString(), anyInt());

        AuthResponse response = userService.registerUserSitter(sitterRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getRole()).isEqualTo(Role.SITTER.name());
        
        // Verificamos que el usuario capturado tiene el rol de cuidadora, que es la logica de negocio
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getRole()).isEqualTo(Role.SITTER);

        // Verificamos que todas las interacciones esperadas ocurrieron
        verify(userRepository).save(any());
        verify(accountRepository).save(any(Account.class));
        verify(accountRepository).save(any());


    }

    /**
     * Prueba que el registro de un SITTER falle si el email ya está en uso.
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

        // Simulamos que el email ya existe
        when(userRepository.findByEmail(sitterRequest.getEmail())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.registerUserSitter(sitterRequest));
    }

    /**
     * Prueba que el registro de un SITTER falle si el email ya está en uso.
     */
    @Test
    @DisplayName("registerUserSitter | Debería lanzar excepción si el email ya existe")
    void registerUserSitter_WhenEmailIsTaken_ShouldThrowException() {
        // Arrange
        CreateUserRequest sitterRequest = new CreateUserRequest(
                "Sitter", "Test", "sitter.test@example.com",
                "password123", "123 Sitter St", "555-1111"
        );
        // Simulamos que el email ya existe
        when(userRepository.findByEmail(sitterRequest.getEmail())).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUserSitter(sitterRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El email ya está registrado: " + sitterRequest.getEmail());

        // Verificamos que no se realizó ninguna operación de guardado
        verify(userRepository, never()).save(any());
    }

    /**
     * Prueba el registro exitoso de un nuevo usuario con el rol CLIENT.
     * Verifica que el usuario se cree con el rol correcto y que se realicen todas las operaciones asociadas.
     */
    @Test
    @DisplayName("registerUser | Debería registrar un nuevo CLIENTE y retornar AuthResponse")
    void registerUser_WhenEmailIsNotTaken_ShouldSucceedAndAssignClientRole() throws MessagingException {
        // Arrange
        CreateUserRequest clientRequest = new CreateUserRequest(
                "Cliente", "Prueba", "cliente.prueba@example.com",
                "password123", "Av. Siempre Viva 742", "555-1234"
        );

        // Captor para verificar el rol del usuario guardado
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findByEmail(clientRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        // Capturamos el usuario al momento de guardarlo
        when(userRepository.save(userCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.getToken(any(User.class))).thenReturn("fake-client-token");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), anyString(), anyInt());

        // Act
        AuthResponse response = userService.registerUser(clientRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("fake-client-token");
        assertThat(response.getRole()).isEqualTo(Role.CLIENT.name());

        // Verificamos que el usuario capturado tiene el rol correcto
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getRole()).isEqualTo(Role.CLIENT);

        // Verificamos las interacciones con los mocks
        verify(userRepository).save(any(User.class));
        verify(accountRepository).save(any(Account.class));
        verify(accountUserRepository).save(any());
        verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString(), anyInt());
    }

    /**
     * Prueba que el registro de un CLIENT falle si el email ya está en uso.
     */
    @Test
    @DisplayName("registerUser | Debería lanzar excepción si el email ya existe")
    void registerUser_WhenEmailIsTaken_ShouldThrowException() {
        // Arrange
        CreateUserRequest clientRequest = new CreateUserRequest(
                "Cliente", "Prueba", "cliente.prueba@example.com",
                "password123", "Av. Siempre Viva 742", "555-1234"
        );
        when(userRepository.findByEmail(clientRequest.getEmail())).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(clientRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El email ya está registrado: " + clientRequest.getEmail());

        verify(userRepository, never()).save(any());
    }

    /**
     * Prueba que el método getAllUsers retorne correctamente una lista de DTOs
     * cuando el repositorio encuentra usuarios.
     */
    @Test
    @DisplayName("getAllUsers | Debería retornar una lista de UserResponse cuando existen usuarios")
    void getAllUsers_WhenUsersExist_ShouldReturnUserResponseList() {
        // Arrange: Preparamos una lista de entidades User de prueba.
        User user1 = new User("Ivan", "Castillo", "ivan@example.com", "pass", "123", "addr", Role.CLIENT);
        User user2 = new User("Maria", "Perez", "maria@example.com", "pass", "456", "addr", Role.SITTER);
        List<User> mockUserList = List.of(user1, user2);

        // Configuramos el mock del repositorio para que devuelva nuestra lista de prueba.
        when(userRepository.findAll()).thenReturn(mockUserList);

        // Act: Llamamos al método que queremos probar.
        List<UserResponse> result = userService.getAllUsers();

        // Assert: Verificamos el resultado.
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2); // La lista debe tener 2 DTOs.

        // Verificamos que el mapeo fue correcto para el primer usuario.
        assertThat(result.get(0).getEmail()).isEqualTo("ivan@example.com");
        assertThat(result.get(0).getFullName()).isEqualTo("Ivan Castillo");
        assertThat(result.get(0).getRole()).isEqualTo(Role.CLIENT);

        // Verificamos que se llamó al método findAll() del repositorio.
        verify(userRepository).findAll();
    }

    /**
     * Prueba que el método getAllUsers retorne una lista vacía
     * cuando el repositorio no encuentra ningún usuario.
     */
    @Test
    @DisplayName("getAllUsers | Debería retornar una lista vacía cuando no existen usuarios")
    void getAllUsers_WhenNoUsersExist_ShouldReturnEmptyList() {
        // Arrange: Configuramos el mock para que devuelva una lista vacía.
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<UserResponse> result = userService.getAllUsers();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty(); // Verificamos que la lista esté vacía.

        verify(userRepository).findAll();
    }

    /**
     * Prueba que el método getUserById retorne un Optional con un UserResponse
     * cuando el usuario es encontrado en el repositorio.
     */
    @Test
    @DisplayName("getUserById | Debería retornar Optional<UserResponse> cuando el usuario existe")
    void getUserById_WhenUserExists_ShouldReturnOptionalOfUserResponse() {
        // Arrange
        // Usamos el 'user' global que ya está configurado en el @BeforeEach.
        // Simulamos que el repositorio encuentra este usuario por su ID.
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        Optional<UserResponse> result = userService.getUserById(1L);

        // Assert
        assertThat(result).isPresent(); // Verificamos que el Optional contiene un valor.

        // Verificamos que el mapeo de User a UserResponse fue correcto.
        assertThat(result.get().getId()).isEqualTo(user.getId());
        assertThat(result.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(result.get().getFirstName()).isEqualTo("Ivan");

        verify(userRepository).findById(1L); // Verificamos que se llamó al método del repositorio.
    }

    /**
     * Prueba que el método getUserById retorne un Optional vacío
     * cuando el usuario no es encontrado en el repositorio.
     */
    @Test
    @DisplayName("getUserById | Debería retornar Optional vacío cuando el usuario no existe")
    void getUserById_WhenUserDoesNotExist_ShouldReturnEmptyOptional() {
        // Arrange
        // Simulamos que el repositorio devuelve un Optional vacío para un ID cualquiera.
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<UserResponse> result = userService.getUserById(99L);

        // Assert
        assertThat(result).isEmpty(); // Verificamos que el Optional está vacío.

        verify(userRepository).findById(99L);
    }

    /**
     * Prueba que el método getUserByEmail retorne un Optional con un UserResponse
     * cuando el usuario es encontrado en el repositorio por su email.
     */
    @Test
    @DisplayName("getUserByEmail | Debería retornar Optional<UserResponse> cuando el email existe")
    void getUserByEmail_WhenUserExists_ShouldReturnOptionalOfUserResponse() {
        // Arrange
        // Usamos el 'user' global que ya tiene el email "test@example.com".
        String userEmail = "test@example.com";
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        // Act
        Optional<UserResponse> result = userService.getUserByEmail(userEmail);

        // Assert
        assertThat(result).isPresent(); // Verificamos que el Optional contiene un valor.

        // Verificamos que los datos del DTO coinciden con los de la entidad original.
        assertThat(result.get().getId()).isEqualTo(user.getId());
        assertThat(result.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(result.get().getFirstName()).isEqualTo("Ivan");

        verify(userRepository).findByEmail(userEmail); // Verificamos que se llamó al método del repositorio con el email correcto.
    }

    /**
     * Prueba que el método getUserByEmail retorne un Optional vacío
     * cuando el email no corresponde a ningún usuario en el repositorio.
     */
    @Test
    @DisplayName("getUserByEmail | Debería retornar Optional vacío cuando el email no existe")
    void getUserByEmail_WhenUserDoesNotExist_ShouldReturnEmptyOptional() {
        // Arrange
        String nonExistentEmail = "no.existe@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // Act
        Optional<UserResponse> result = userService.getUserByEmail(nonExistentEmail);

        // Assert
        assertThat(result).isEmpty(); // Verificamos que el Optional está vacío.

        verify(userRepository).findByEmail(nonExistentEmail);
    }

    @Test
    @DisplayName("updateUser | Debería actualizar solo los datos básicos del usuario")
    void updateUser_ShouldUpdateBasicInfo() {
        // Arrange
        Long userId = 1L;
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Ivan Actualizado", "Castillo Actualizado", user.getEmail(), // Mismo email
                "", // Contraseña vacía para no actualizarla
                "Nueva Direccion 456", "987654321"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponse result = userService.updateUser(userId, updateRequest);

        // Assert
        assertThat(result.getFirstName()).isEqualTo("Ivan Actualizado");
        assertThat(result.getLastName()).isEqualTo("Castillo Actualizado");
        assertThat(result.getAddress()).isEqualTo("Nueva Direccion 456");

        verify(passwordEncoder, never()).encode(anyString()); // Verificar que la contraseña no se codificó
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser | Debería actualizar el email y resetear la verificación")
    void updateUser_ShouldUpdateEmailAndResetVerification() {
        // Arrange
        Long userId = 1L;
        String newEmail = "nuevo.email@example.com";
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                user.getFirstName(), user.getLastName(), newEmail, "", user.getAddress(), user.getPhoneNumber()
        );

        // El usuario tiene el email verificado antes del cambio
        user.setEmailVerifiedAt(LocalDateTime.now());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty()); // El nuevo email está disponible
        when(userRepository.save(userCaptor.capture())).thenReturn(user);

        // Act
        userService.updateUser(userId, updateRequest);

        // Assert
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(newEmail);
        assertThat(savedUser.getEmailVerifiedAt()).isNull(); // ¡Crucial! La verificación debe ser reseteada
    }

    @Test
    @DisplayName("updateUser | Debería actualizar la contraseña cuando se proporciona una nueva")
    void updateUser_ShouldUpdatePasswordWhenProvided() {
        // Arrange
        Long userId = 1L;
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                user.getFirstName(), user.getLastName(), user.getEmail(),
                "nuevaPassword123", // Nueva contraseña
                user.getAddress(), user.getPhoneNumber()
        );

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("nuevaPassword123")).thenReturn("nuevaPasswordCodificada");
        when(userRepository.save(userCaptor.capture())).thenReturn(user);

        // Act
        userService.updateUser(userId, updateRequest);

        // Assert
        verify(passwordEncoder).encode("nuevaPassword123"); // Verificar que se llamó al encoder
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("nuevaPasswordCodificada");
    }

    @Test
    @DisplayName("updateUser | Debería lanzar RuntimeException si el usuario no existe")
    void updateUser_WhenUserNotFound_ShouldThrowRuntimeException() {
        // Arrange
        Long userId = 99L;
        UpdateUserRequest updateRequest = new UpdateUserRequest(user.getFirstName(), user.getLastName(), user.getEmail(),
                "nuevaPassword123", // Nueva contraseña
                user.getAddress(), user.getPhoneNumber());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(userId, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado con id " + userId);
    }

    @Test
    @DisplayName("updateUser | Debería lanzar IllegalArgumentException si el nuevo email ya está en uso")
    void updateUser_WhenNewEmailIsTaken_ShouldThrowIllegalArgumentException() {
        // Arrange
        Long userId = 1L;
        String takenEmail = "email.usado@example.com";
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                user.getFirstName(), user.getLastName(), takenEmail, "", user.getAddress(), user.getPhoneNumber()
        );

        User anotherUser = new User(); // Un usuario diferente que ya tiene el email
        anotherUser.setId(2L);
        anotherUser.setEmail(takenEmail);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(takenEmail)).thenReturn(Optional.of(anotherUser));

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(userId, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El nuevo email ya está registrado: " + takenEmail);

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Prueba que el método deleteUser llame al método deleteById del repositorio
     * cuando el usuario existe.
     */
    @Test
    @DisplayName("deleteUser | Debería llamar al método de eliminación cuando el usuario existe")
    void deleteUser_WhenUserExists_ShouldCallRepositoryDelete() {
        // Arrange
        Long userId = 1L;
        // Simulamos que el usuario SÍ existe.
        when(userRepository.existsById(userId)).thenReturn(true);
        // Para métodos void, podemos usar doNothing() para ser explícitos, aunque no es estrictamente necesario.
        doNothing().when(userRepository).deleteById(userId);

        // Act
        userService.deleteUser(userId);

        // Assert
        // Verificamos que el método deleteById fue llamado exactamente una vez con el ID correcto.
        verify(userRepository).deleteById(userId);
    }

    /**
     * Prueba que el método deleteUser lance una RuntimeException
     * cuando el usuario no existe.
     */
    @Test
    @DisplayName("deleteUser | Debería lanzar RuntimeException cuando el usuario no existe")
    void deleteUser_WhenUserDoesNotExist_ShouldThrowRuntimeException() {
        // Arrange
        Long userId = 99L;
        // Simulamos que el usuario NO existe.
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado con id " + userId);

        // Verificamos que el método deleteById NUNCA fue llamado.
        verify(userRepository, never()).deleteById(anyLong());
    }

    /**
     * Prueba que el método createUserByAdmin cree exitosamente un usuario
     * con el rol específicamente proporcionado (en este caso, SITTER).
     */
    @Test
    @DisplayName("createUserByAdmin | Debería crear un usuario con el rol especificado")
    void createUserByAdmin_ShouldCreateUserWithSpecifiedRole() {
        // Arrange
        CreateUserRequest adminRequest = new CreateUserRequest(
                "AdminCreated", "Sitter", "admin.creates@example.com",
                "password123", "Admin Address", "555-9999"
        );
        Role specifiedRole = Role.SITTER;

        // Captor para verificar que el usuario guardado tiene el rol correcto
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findByEmail(adminRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        // Capturamos el usuario al momento de guardarlo y lo devolvemos
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponse result = userService.createUserByAdmin(adminRequest, specifiedRole);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(adminRequest.getEmail());
        assertThat(result.getRole()).isEqualTo(specifiedRole); // Verificar el rol en la respuesta DTO

        // La aserción más importante: verificar el rol en la entidad que se guardó
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getRole()).isEqualTo(specifiedRole);

        verify(userRepository).save(any(User.class));
    }

    /**
     * Prueba que el método createUserByAdmin falle si se intenta usar un email
     * que ya está registrado en el sistema.
     */
    @Test
    @DisplayName("createUserByAdmin | Debería lanzar IllegalArgumentException si el email ya existe")
    void createUserByAdmin_WhenEmailIsTaken_ShouldThrowIllegalArgumentException() {
        // Arrange
        CreateUserRequest adminRequest = new CreateUserRequest(
                "AdminCreated", "Sitter", "email.existente@example.com",
                "password123", "Admin Address", "555-9999"
        );
        Role specifiedRole = Role.ADMIN;

        // Simulamos que el email ya existe
        when(userRepository.findByEmail(adminRequest.getEmail())).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThatThrownBy(() -> userService.createUserByAdmin(adminRequest, specifiedRole))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El email ya está registrado: " + adminRequest.getEmail());

        // Verificamos que no se intentó guardar nada
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Prueba que se pueda activar a un usuario que previamente estaba inactivo.
     */
    @Test
    @DisplayName("toggleUserActive | Debería activar un usuario inactivo")
    void toggleUserActive_ShouldActivateAnInactiveUser() {
        // Arrange
        Long userId = 1L;
        user.setActive(false); // Empezamos con el usuario inactivo

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(userCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserResponse response = userService.toggleUserActive(userId, true); // La acción es activar

        // Assert
        assertThat(response.isActive()).isTrue();

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.isActive()).isTrue(); // Verificamos que el estado del usuario guardado es 'true'

        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    /**
     * Prueba que se pueda desactivar a un usuario que previamente estaba activo.
     */
    @Test
    @DisplayName("toggleUserActive | Debería desactivar un usuario activo")
    void toggleUserActive_ShouldDeactivateAnActiveUser() {
        // Arrange
        Long userId = 1L;
        user.setActive(true); // El usuario está activo por defecto en el setUp

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(userCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserResponse response = userService.toggleUserActive(userId, false); // La acción es desactivar

        // Assert
        assertThat(response.isActive()).isFalse();

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.isActive()).isFalse(); // Verificamos que el estado del usuario guardado es 'false'

        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    /**
     * Prueba que se lance una excepción si se intenta cambiar el estado de un usuario que no existe.
     */
    @Test
    @DisplayName("toggleUserActive | Debería lanzar RuntimeException si el usuario no existe")
    void toggleUserActive_WhenUserNotFound_ShouldThrowRuntimeException() {
        // Arrange
        Long nonExistentUserId = 99L;
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.toggleUserActive(nonExistentUserId, true))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado con id " + nonExistentUserId);

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Prueba que el método establezca la fecha de verificación del email
     * cuando el usuario existe y su email no estaba verificado.
     */
    @Test
    @DisplayName("markEmailAsVerified | Debería establecer la fecha de verificación cuando el usuario existe")
    void markEmailAsVerified_WhenUserExists_ShouldSetVerificationDateAndSave() {
        // Arrange
        Long userId = 1L;
        // Nos aseguramos de que el estado inicial sea "no verificado"
        user.setEmailVerifiedAt(null);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(userCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserResponse response = userService.markEmailAsVerified(userId);

        // Assert
        // Verificamos la respuesta DTO
        assertThat(response.getEmailVerifiedAt()).isNotNull();

        // Verificamos el estado de la entidad que se guardó
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmailVerifiedAt()).isNotNull();

        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    /**
     * Prueba que se lance una excepción si se intenta verificar el email
     * de un usuario que no existe.
     */
    @Test
    @DisplayName("markEmailAsVerified | Debería lanzar RuntimeException si el usuario no existe")
    void markEmailAsVerified_WhenUserNotFound_ShouldThrowRuntimeException() {
        // Arrange
        Long nonExistentUserId = 99L;
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.markEmailAsVerified(nonExistentUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado con id " + nonExistentUserId);

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Prueba que el método getUsersByRole filtre correctamente por rol
     * y mapee las entidades a DTOs de resumen.
     */
    @Test
    @DisplayName("getUsersByRole | Debería retornar solo usuarios con el rol especificado")
    void getUsersByRole_ShouldReturnFilteredUserList() {
        // Arrange
        Role roleToFind = Role.SITTER;
        User sitter1 = new User("Sitter", "Uno", "sitter1@example.com", "pass", "1", "addr", Role.SITTER);
        User sitter2 = new User("Sitter", "Dos", "sitter2@example.com", "pass", "2", "addr", Role.SITTER);
        // NO debemos incluir usuarios con otros roles en la lista que devuelve el mock.
        List<User> mockSitterList = List.of(sitter1, sitter2);

        // Configuramos el mock para que devuelva nuestra lista filtrada cuando se le pida por el rol SITTER.
        when(userRepository.findAllByRole(roleToFind)).thenReturn(mockSitterList);

        // Act
        List<UserSummaryResponse> result = userService.getUsersByRole(roleToFind);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        // Verificamos que todos los DTOs en la lista tengan el rol correcto
        assertThat(result).allMatch(userSummary -> userSummary.getRole() == roleToFind);

        // Verificamos que el mapeo fue correcto para el primer usuario
        assertThat(result.get(0).getFullName()).isEqualTo("Sitter Uno");
        assertThat(result.get(0).getEmail()).isEqualTo("sitter1@example.com");

        verify(userRepository).findAllByRole(roleToFind);
    }

}