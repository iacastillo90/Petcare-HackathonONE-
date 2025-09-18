package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.Auth.Request.LoginRequest;
import com.Petcare.Petcare.DTOs.Auth.Respone.AuthResponse;
import com.Petcare.Petcare.DTOs.GlobalException.DeleteResponseDTO;
import com.Petcare.Petcare.DTOs.GlobalException.ErrorResponseDTO;
import com.Petcare.Petcare.DTOs.User.*;
import com.Petcare.Petcare.Exception.Business.EmailAlreadyExistsException;
import com.Petcare.Petcare.Exception.Business.UserNotFoundException;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para la gestión de usuarios en el sistema Petcare.
 *
 * <p>Proporciona endpoints para todas las operaciones relacionadas con usuarios,
 * incluyendo registro público, operaciones administrativas, consultas especializadas
 * y generación de estadísticas. Mantiene separación clara entre endpoints públicos
 * y protegidos con autorizaciones apropiadas.</p>
 *
 * <p><strong>Categorías de endpoints:</strong></p>
 * <ul>
 *   <li>Públicos: Registro de usuarios clientes</li>
 *   <li>Administrativos: Gestión completa de usuarios (ADMIN only)</li>
 *   <li>Consultas: Búsquedas y listados con diferentes formatos</li>
 *   <li>Estadísticas: Métricas agregadas para dashboards</li>
 * </ul>
 *
 * <p><strong>DTOs utilizados:</strong></p>
 * <ul>
 *   <li>{@link CreateUserRequest}: Input para creación/actualización</li>
 *   <li>{@link UserResponse}: Output completo para detalles</li>
 *   <li>{@link UserSummaryResponse}: Output resumido para listados</li>
 *   <li>{@link UserStatsResponse}: Output para estadísticas</li>
 * </ul>
 *
 * <p><strong>Seguridad implementada:</strong></p>
 * <ul>
 *   <li>Endpoints públicos sin autenticación</li>
 *   <li>Endpoints administrativos requieren rol ADMIN</li>
 *   <li>Acceso a datos propios para usuarios autenticados</li>
 *   <li>Validación de entrada con Bean Validation</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see UserService
 * @see com.Petcare.Petcare.Models.User.User
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuario", description = "Endpoints para la gestión de autenticación de usuarios")
public class UserController {

    private final UserService userService;

    @Value("${petcare.frontend.base-url:http://44.207.65.254:8088/}")
    private String frontendBaseUrl;


    // ========== ENDPOINTS PÚBLICOS ==========

    /**
     * Autentica un usuario en el sistema.
     *
     * <p>Endpoint público que permite autenticar usuarios registrados
     * usando email y contraseña. Retorna un token JWT válido para acceso
     * a recursos protegidos y actualiza el timestamp de último login.</p>
     *
     * <p><strong>Proceso de autenticación:</strong></p>
     * <ul>
     *   <li>Valida las credenciales usando Spring Security</li>
     *   <li>Actualiza el timestamp de último login</li>
     *   <li>Genera token JWT con información del rol</li>
     *   <li>Retorna token y rol para autorización del frontend</li>
     * </ul>
     *
     * @param request datos de autenticación (email y contraseña)
     * @param sessionId identificador de sesión opcional para auditoría
     * @return ResponseEntity con token JWT y rol del usuario
     * @throws org.springframework.security.authentication.BadCredentialsException si las credenciales son inválidas
     * @throws IllegalStateException si el usuario no existe después de autenticación exitosa
     */
    @Operation(
            summary = "Autentica un usuario en el sistema",
            description = "Este endpoint permite iniciar sesión con email y contraseña."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa...",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo de Autenticación Exitosa",
                                    value = """
                                            {
                                            "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
                                            "role": "CLIENT",
                                            "userProfile": {
                                                "id": "1",
                                                "firstName": "Abdon",
                                                "lastName": "Migliaccio",
                                                "email": "abdon.migliaccio@gmail.com",
                                                "role": "CLIENT",
                                                "initials": "AM",
                                                "accountId": 1
                                                }
                                            }
                                            """))),

            @ApiResponse(responseCode = "401", description = "Las credenciales proporcionadas son incorrectas...",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo de Error 401",
                                    value = """
                                            {
                                              "status": 401,
                                              "message": "Las credenciales proporcionadas son incorrectas.",
                                              "timestamp": "2025-09-12T23:22:29.479Z",
                                              "validationErrors": null
                                            }
                                            """
                            ))),

            @ApiResponse(responseCode = "403", description = "La cuenta de usuario está desactivada...",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo de Error 403",
                                    value = """
                                            {
                                              "status": 403,
                                              "message": "La cuenta de usuario está desactivada.",
                                              "timestamp": "2025-09-12T23:22:29.479Z",
                                              "validationErrors": null
                                            }
                                            """
                            ))),

            @ApiResponse(responseCode = "400", description = "Los datos enviados no cumplen con el formato requerido...",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo de Error 400",
                                    value = """
                                            {
                                              "status": 400,
                                              "message": "Validation failed",
                                              "timestamp": "2025-09-12T23:22:29.479Z",
                                              "validationErrors": {
                                                "email": "El email no es válido.",
                                                "password": "La contraseña debe tener al menos 8 caracteres."
                                              }
                                            }
                                            """
                            )))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
                    @Parameter(description = "Credenciales de acceso del usuario", required = true)
                    @Valid @RequestBody LoginRequest request,
                    @Parameter(description = "Identificador único de sesión para trazabilidad")
                    @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        log.info("Solicitud de login para email: {} [Session: {}]", request.getEmail(), sessionId);

        AuthResponse authResponse = userService.login(request);

        log.info("Login exitoso para email: {} [Session: {}]", request.getEmail(), sessionId);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Registra un nuevo usuario cliente en el sistema.
     * <p>
     * Este endpoint público permite a cualquier persona crear una cuenta con rol CLIENT.
     * El proceso es completamente transaccional y automatizado, asegurando una experiencia
     * de registro fluida y segura. El usuario queda automáticamente autenticado tras
     * el registro exitoso.
     * </p>
     * <p><strong>Flujo de Proceso Detallado:</strong></p>
     * <ul>
     * <li><b>Validación de Unicidad:</b> Se verifica que el email proporcionado no esté ya registrado en el sistema.</li>
     * <li><b>Creación de Entidades:</b> Se crea y persiste una nueva entidad <code>User</code> con el rol <code>CLIENT</code> y la contraseña cifrada.</li>
     * <li><b>Creación de Cuenta:</b> Se genera automáticamente una cuenta familiar (<code>Account</code>) asociada al nuevo usuario.</li>
     * <li><b>Generación de Token:</b> Se genera un token JWT para el nuevo usuario, permitiendo el inicio de sesión inmediato.</li>
     * <li><b>Notificación:</b> Se envía un correo electrónico de verificación de manera asíncrona para activar la cuenta.</li>
     * </ul>
     *
     * @param request DTO con los datos de registro del usuario (nombre, email, contraseña, etc.).
     * @return ResponseEntity con un DTO {@link AuthResponse} que contiene el token JWT y el perfil del usuario recién creado.
     * @throws IllegalArgumentException si el email proporcionado ya está registrado en el sistema.
     */
    @Operation(
            summary = "Registra un nuevo usuario cliente",
            description = "Permite a cualquier persona crear una cuenta con rol de cliente en el sistema. El proceso incluye validación de email único, cifrado de contraseña y generación automática de token de acceso. El usuario queda inmediatamente autenticado tras completar el registro."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario registrado exitosamente. El cliente puede comenzar a usar el sistema inmediatamente con el token proporcionado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Registro Exitoso",
                                    summary = "Respuesta para un registro exitoso",
                                    value = """
                        {
                          "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTcxNTY0NjQzNSwiZXhwIjoxNzE1NzMyODM1fQ.token_de_ejemplo",
                          "role": "CLIENT",
                          "userProfile": {
                            "id": 1,
                            "firstName": "Ivan",
                            "lastName": "Castillo",
                            "email": "ivan.Castillo@example.com",
                            "role": "CLIENT",
                            "initials": "IC",
                            "accountId": 1
                          }
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos. Ocurre si algún campo del cuerpo de la solicitud no cumple con las validaciones (ej. email con formato incorrecto, contraseña demasiado corta).",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Validación",
                                    summary = "Respuesta para datos de entrada inválidos",
                                    value = """
                        {
                          "status": 400,
                          "message": "Validation failed",
                          "timestamp": "2025-09-12T23:22:29.479Z",
                          "validationErrors": {
                            "email": "El formato del email no es válido",
                            "password": "La contraseña debe tener al menos 8 caracteres"
                          }
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto de datos. Ocurre si el email proporcionado ya está registrado en el sistema.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Email Duplicado",
                                    summary = "Respuesta cuando el email ya existe",
                                    value = """
                        {
                          "status": 409,
                          "message": "El email 'ivan.Castillo@example.com' ya está registrado.",
                          "timestamp": "2025-09-12T23:25:10.123Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor. Ocurre si hay un problema inesperado durante el proceso de registro, como un fallo en la base de datos.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error del Servidor",
                                    summary = "Respuesta para un error interno",
                                    value = """
                        {
                          "status": 500,
                          "message": "Ocurrió un error inesperado al procesar la solicitud.",
                          "timestamp": "2025-09-12T23:28:05.543Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Solicitud de registro público para email: {}", request.getEmail());

        AuthResponse authResponse = userService.registerUser(request);

        log.info("Usuario registrado exitosamente para email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    /**
     * Registra un nuevo usuario cuidador (Sitter) en el sistema.
     * <p>
     * Este endpoint público está diseñado específicamente para que los cuidadores se registren en la plataforma.
     * Al igual que el registro de clientes, el proceso es transaccional y automatizado. El cuidador
     * queda autenticado inmediatamente después de un registro exitoso, permitiéndole configurar su perfil.
     * </p>
     * <p><strong>Flujo de Proceso Detallado:</strong></p>
     * <ul>
     * <li><b>Validación de Unicidad:</b> Confirma que el email proporcionado no esté en uso.</li>
     * <li><b>Creación de Usuario y Cuenta:</b> Crea una nueva entidad <code>User</code> con el rol <code>SITTER</code> y genera una cuenta de cuidador (<code>Account</code>) asociada.</li>
     * <li><b>Generación de Token:</b> Emite un token JWT para el nuevo cuidador, facilitando el acceso inmediato.</li>
     * <li><b>Notificación:</b> Despacha de forma asíncrona un correo de bienvenida y verificación de email.</li>
     * <li><b>Próximo Paso Esperado:</b> El cuidador deberá completar su {@link com.Petcare.Petcare.Models.SitterProfile} para poder ofrecer servicios.</li>
     * </ul>
     *
     * @param request DTO con los datos de registro del cuidador (nombre, email, contraseña, etc.).
     * @return ResponseEntity con un DTO {@link AuthResponse} que contiene el token JWT y el perfil del cuidador recién creado.
     * @throws IllegalArgumentException si el email proporcionado ya está registrado.
     */
    @Operation(
            summary = "Registra un nuevo usuario cuidador (Sitter)",
            description = "Permite a una persona registrarse como cuidador (Sitter) en el sistema. El proceso valida que el email sea único, cifra la contraseña, asigna el rol 'SITTER' y genera un token de acceso para que el usuario pueda empezar a configurar su perfil de cuidador inmediatamente."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Cuidador registrado exitosamente. El usuario recibe un token para autenticarse y configurar su perfil.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Registro de Cuidador Exitoso",
                                    summary = "Respuesta para un registro de cuidador exitoso",
                                    value = """
                        {
                          "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJpc2FiZWwuY29udHJlcmFzQGV4YW1wbGUuY29tIiwiaWF0IjoxNzE1NjQ4MjM1LCJleHAiOjE3MTU3MzQ2MzV9.token_ejemplo_sitter",
                          "role": "SITTER",
                          "userProfile": {
                            "id": 2,
                            "firstName": "Juana",
                            "lastName": "Contreras",
                            "email": "juana.contreras@example.com",
                            "role": "SITTER",
                            "initials": "JC",
                            "accountId": 2
                          }
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos. Ocurre si algún campo del cuerpo de la solicitud no cumple con las validaciones (ej. contraseña demasiado corta).",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Validación",
                                    summary = "Respuesta para datos de entrada inválidos",
                                    value = """
                        {
                          "status": 400,
                          "message": "Validation failed",
                          "timestamp": "2025-09-12T23:22:29.479Z",
                          "validationErrors": {
                            "password": "La contraseña debe tener al menos 8 caracteres",
                            "phoneNumber": "El número de teléfono es obligatorio"
                          }
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto de datos. Ocurre si el email proporcionado ya está registrado en el sistema.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Email Duplicado",
                                    summary = "Respuesta cuando el email ya existe",
                                    value = """
                        {
                          "status": 409,
                          "message": "El email 'juana.contreras@example.com' ya está registrado.",
                          "timestamp": "2025-09-12T23:25:10.123Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor. Ocurre si hay un problema inesperado durante el proceso de registro.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error del Servidor",
                                    summary = "Respuesta para un error interno",
                                    value = """
                        {
                          "status": 500,
                          "message": "Ocurrió un error inesperado al procesar la solicitud.",
                          "timestamp": "2025-09-12T23:28:05.543Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            )
    })
    @PostMapping("/register-sitter")
    public ResponseEntity<AuthResponse> registerUserSitter(@Valid @RequestBody CreateUserRequest request) {
        log.info("Solicitud de registro público de cuidador para email: {}", request.getEmail());

        AuthResponse authResponse = userService.registerUserSitter(request);

        log.info("Cuidador registrado exitosamente para email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    // ========== ENDPOINTS ADMINISTRATIVOS ==========

    /**
     * Crea un nuevo usuario con un rol específico (Operación Administrativa).
     * <p>
     * Este endpoint permite a los administradores del sistema crear nuevos usuarios con cualquier rol
     * (CLIENT, SITTER, ADMIN). A diferencia de los endpoints de registro público, esta operación
     * no genera un token de autenticación, ya que está diseñada para la gestión interna.
     * </p>
     * <p><strong>Casos de Uso Principales:</strong></p>
     * <ul>
     * <li>Registro interno de nuevos cuidadores (Sitters) por parte del personal de Petcare.</li>
     * <li>Creación de cuentas para nuevos administradores del sistema.</li>
     * <li>Asistencia en la creación de cuentas para clientes corporativos o especiales.</li>
     * </ul>
     *
     * @param request El DTO {@link CreateUserRequest} que contiene los datos del nuevo usuario a crear.
     * @param role El {@link Role} que se asignará al nuevo usuario. Es un parámetro de consulta y su valor por defecto es 'CLIENT'.
     * @return ResponseEntity con un DTO {@link UserResponse} que contiene la información completa del usuario recién creado.
     * @throws IllegalArgumentException si el email proporcionado ya está en uso.
     * @throws org.springframework.security.access.AccessDeniedException si el usuario que realiza la solicitud no tiene el rol 'ADMIN'.
     */
    @Operation(
            summary = "Crea un nuevo usuario (Admin)",
            description = "Endpoint privilegiado que permite a un administrador crear un nuevo usuario con un rol específico (CLIENT, SITTER, o ADMIN). Requiere autenticación con rol 'ADMIN'. A diferencia del registro público, no devuelve un token de autenticación."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario creado exitosamente por un administrador.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Creación de Cuidador Exitosa",
                                    summary = "Respuesta al crear un usuario SITTER",
                                    value = """
                        {
                          "id": 15,
                          "firstName": "Alvaro",
                          "lastName": "Ruiz",
                          "email": "alvaro.ruiz.sitter@example.com",
                          "address": "Avenida Providencia 123, Mexico",
                          "phoneNumber": "987654321",
                          "role": "SITTER",
                          "isActive": true,
                          "emailVerifiedAt": null,
                          "lastLoginAt": null,
                          "createdAt": "2025-09-13T14:30:00.123Z",
                          "updatedAt": "2025-09-13T14:30:00.123Z"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos. Ocurre si el cuerpo de la solicitud no cumple con las validaciones.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Validación",
                                    summary = "Respuesta para datos de entrada inválidos",
                                    value = """
                        {
                          "status": 400,
                          "message": "Validation failed",
                          "timestamp": "2025-09-13T14:31:00.456Z",
                          "validationErrors": {
                            "email": "El formato del email no es válido"
                          }
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Ocurre si no se proporciona un token JWT válido en la cabecera de autorización.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Autenticación",
                                    summary = "Respuesta para token inválido o ausente",
                                    value = """
                        {
                          "status": 401,
                          "message": "Authentication failed: Bad credentials",
                          "timestamp": "2025-09-13T14:32:15.789Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Ocurre si el usuario autenticado no tiene el rol 'ADMIN'.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Autorización",
                                    summary = "Respuesta para usuario sin permisos",
                                    value = """
                        {
                          "status": 403,
                          "message": "Access Denied: You do not have permission to access this resource.",
                          "timestamp": "2025-09-13T14:33:05.912Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto de datos. Ocurre si el email proporcionado ya está registrado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Email Duplicado",
                                    summary = "Respuesta cuando el email ya existe",
                                    value = """
                        {
                          "status": 409,
                          "message": "El email 'alvaro.ruiz.sitter@example.com' ya está registrado.",
                          "timestamp": "2025-09-13T14:34:20.321Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error del Servidor",
                                    summary = "Respuesta para un error interno",
                                    value = """
                        {
                          "status": 500,
                          "message": "Ocurrió un error inesperado al procesar la solicitud.",
                          "timestamp": "2025-09-13T14:35:00.111Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            )
    })
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUserByAdmin(
            @Valid @RequestBody CreateUserRequest request,
            @RequestParam(defaultValue = "CLIENT") Role role) {

        log.info("Creación administrativa de usuario con rol {} para email: {}", role, request.getEmail());

        UserResponse userResponse = userService.createUserByAdmin(request, role);

        log.info("Usuario creado administrativamente con ID: {} y rol: {}", userResponse.getId(), role);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    /**
     * Actualiza los datos de un usuario existente (Operación Administrativa).
     * <p>
     * Permite a los administradores modificar la información de cualquier usuario del sistema.
     * Los campos que se pueden actualizar incluyen nombre, apellido, email, dirección y número de teléfono.
     * El rol del usuario no se puede modificar a través de este endpoint.
     * </p>
     * <p><strong>Lógica de Negocio Importante:</strong></p>
     * <ul>
     * <li><b>Cambio de Email:</b> Si se modifica la dirección de correo electrónico, el estado de verificación del email se anulará (<code>emailVerifiedAt</code> se establecerá a <code>null</code>), requiriendo que el usuario vuelva a verificar su nuevo correo.</li>
     * <li><b>Actualización de Contraseña:</b> Si se proporciona un valor en el campo de contraseña, esta será cifrada y actualizada. Si el campo es nulo o vacío, la contraseña actual se mantendrá sin cambios.</li>
     * </ul>
     *
     * @param id El identificador único (ID) del usuario que se desea actualizar.
     * @param request Un DTO {@link UpdateUserRequest} con los campos y los nuevos valores a actualizar.
     * @return ResponseEntity que contiene un DTO {@link UserResponse} con la vista completa del usuario actualizado.
     * @throws UserNotFoundException si no se encuentra ningún usuario con el ID proporcionado.
     * @throws EmailAlreadyExistsException si el nuevo email especificado ya está en uso por otro usuario.
     */
    @Operation(
            summary = "Actualiza un usuario existente (Admin)",
            description = "Permite a un administrador modificar los datos de un usuario por su ID. Si se cambia el email, se requerirá una nueva verificación. La contraseña solo se actualiza si se proporciona un nuevo valor. Requiere rol 'ADMIN'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado exitosamente.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Actualización Exitosa",
                                    summary = "Ejemplo de respuesta de un usuario actualizado",
                                    value = """
                        {
                          "id": 12,
                          "firstName": "Jorge",
                          "lastName": "Doe",
                          "email": "jorge.doe.new@example.com",
                          "address": "456 New Avenue Colombia",
                          "phoneNumber": "555-9876",
                          "role": "CLIENT",
                          "isActive": true,
                          "emailVerifiedAt": null,
                          "lastLoginAt": "2025-09-12T20:15:00Z",
                          "createdAt": "2025-08-01T10:00:00Z",
                          "updatedAt": "2025-09-13T18:05:30.123Z"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos. El cuerpo de la solicitud no cumple con las validaciones de formato.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Validación",
                                    value = """
                        {
                          "status": 400,
                          "message": "Validation failed",
                          "timestamp": "2025-09-13T18:10:15.456Z",
                          "validationErrors": {
                            "email": "El formato del email no es válido"
                          }
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Se requiere el rol 'ADMIN' para esta operación.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Autorización",
                                    value = """
                        {
                          "status": 403,
                          "message": "Access Denied: You do not have permission to access this resource.",
                          "timestamp": "2025-09-13T18:11:05.912Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado. El ID proporcionado no corresponde a ningún usuario.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Usuario No Encontrado",
                                    value = """
                        {
                          "status": 404,
                          "message": "Usuario no encontrado con el ID 999",
                          "timestamp": "2025-09-13T18:12:00.789Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto de datos. El nuevo email ya está en uso por otro usuario.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Conflicto de Email",
                                    value = """
                        {
                          "status": 409,
                          "message": "El email 'jorge.doe.new@example.com' ya está en uso.",
                          "timestamp": "2025-09-13T18:14:20.321Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("Actualizando usuario ID: {}", id);

        UserResponse updatedUser = userService.updateUser(id, request);

        log.info("Usuario ID: {} actualizado exitosamente", id);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Activa o desactiva la cuenta de un usuario (Operación Administrativa).
     * <p>
     * Este endpoint permite a un administrador cambiar el estado de activación de cualquier cuenta de usuario
     * en el sistema. Es una operación no destructiva que sirve para suspender o rehabilitar el acceso
     * de un usuario sin eliminar sus datos.
     * </p>
     * <p><strong>Efectos de la Operación:</strong></p>
     * <ul>
     * <li><b>Desactivar (active=false):</b> El usuario no podrá iniciar sesión en el sistema. Cualquier token JWT existente será invalidado en la siguiente solicitud.</li>
     * <li><b>Activar (active=true):</b> El usuario recuperará la capacidad de iniciar sesión con sus credenciales.</li>
     * </ul>
     *
     * @param id El identificador único (ID) del usuario cuyo estado se va a modificar.
     * @param active El nuevo estado de activación deseado (`true` para activar, `false` para desactivar).
     * @return ResponseEntity que contiene un DTO {@link UserResponse} con el estado actualizado del usuario.
     * @throws UserNotFoundException si no se encuentra ningún usuario con el ID proporcionado.
     * @throws org.springframework.security.access.AccessDeniedException si el solicitante no tiene el rol 'ADMIN'.
     */
    @Operation(
            summary = "Activa o desactiva un usuario (Admin)",
            description = "Permite a un administrador habilitar o deshabilitar una cuenta de usuario por su ID. Un usuario inactivo no podrá autenticarse. Requiere rol 'ADMIN'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado del usuario cambiado exitosamente.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Usuario Desactivado",
                                            summary = "Respuesta al desactivar un usuario",
                                            value = """
                            {
                              "id": 12,
                              "firstName": "Abdon",
                              "lastName": "Doe",
                              "email": "abdon.doe@example.com",
                              "address": "123 Main St, Argentina",
                              "phoneNumber": "555-1234",
                              "role": "CLIENT",
                              "isActive": false,
                              "emailVerifiedAt": "2025-08-01T11:00:00Z",
                              "lastLoginAt": "2025-09-12T20:15:00Z",
                              "createdAt": "2025-08-01T10:00:00Z",
                              "updatedAt": "2025-09-13T19:20:00.123Z"
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "Usuario Activado",
                                            summary = "Respuesta al activar un usuario",
                                            value = """
                            {
                              "id": 12,
                              "firstName": "Alvaro",
                              "lastName": "Doe",
                              "email": "alvaro.doe@example.com",
                              "address": "123 Main St, Mexico",
                              "phoneNumber": "555-1234",
                              "role": "CLIENT",
                              "isActive": true,
                              "emailVerifiedAt": "2025-08-01T11:00:00Z",
                              "lastLoginAt": "2025-09-12T20:15:00Z",
                              "createdAt": "2025-08-01T10:00:00Z",
                              "updatedAt": "2025-09-13T19:21:15.456Z"
                            }
                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parámetro inválido. Ocurre si el parámetro 'active' no es un booleano válido (ej. 'active=yes').",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Parámetro",
                                    value = """
                        {
                          "status": 400,
                          "message": "Failed to convert value of type 'java.lang.String' to required type 'boolean'",
                          "timestamp": "2025-09-13T19:22:00.789Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. El usuario no tiene el rol 'ADMIN'.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Autorización",
                                    value = """
                        {
                          "status": 403,
                          "message": "Access Denied: You do not have permission to access this resource.",
                          "timestamp": "2025-09-13T19:23:05.912Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado. El ID proporcionado no corresponde a ningún usuario en el sistema.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Usuario No Encontrado",
                                    value = """
                        {
                          "status": 404,
                          "message": "Usuario no encontrado con el ID 999",
                          "timestamp": "2025-09-13T19:24:00.321Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            )
    })
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> toggleUserActive(
            @PathVariable Long id,
            @RequestParam boolean active) {

        log.info("Cambiando estado activo del usuario ID: {} a: {}", id, active);
        UserResponse updatedUser = userService.toggleUserActive(id, active);
        log.info("Estado del usuario ID: {} cambiado a: {}", id, active ? "activo" : "inactivo");
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Verifica manualmente el email de un usuario (Operación Administrativa).
     * <p>
     * Este endpoint es una herramienta administrativa para marcar el correo electrónico de un usuario
     * como verificado, estableciendo la fecha y hora actual en el campo <code>emailVerifiedAt</code>.
     * Esto anula la necesidad de que el usuario haga clic en un enlace de verificación.
     * </p>
     * <p><strong>Casos de Uso Principales:</strong></p>
     * <ul>
     * <li>Asistencia de soporte técnico a usuarios que no recibieron o no pueden usar el enlace de verificación.</li>
     * <li>Activación manual de cuentas durante un proceso de onboarding interno.</li>
     * <li>Corrección de problemas de estado de cuenta.</li>
     * </ul>
     *
     * @param id El identificador único (ID) del usuario cuyo email se va a verificar.
     * @return ResponseEntity que contiene un DTO {@link UserResponse} con el estado del usuario actualizado, mostrando el nuevo timestamp de verificación.
     * @throws UserNotFoundException si no se encuentra ningún usuario con el ID proporcionado.
     * @throws org.springframework.security.access.AccessDeniedException si el solicitante no tiene el rol 'ADMIN'.
     */
    @Operation(
            summary = "Verifica el email de un usuario (Admin)",
            description = "Permite a un administrador marcar manualmente el email de un usuario como verificado. Esta es una operación de anulación para casos de soporte. Requiere rol 'ADMIN'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email del usuario verificado exitosamente.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Verificación Exitosa",
                                    summary = "Respuesta tras verificar un email",
                                    value = """
                        {
                          "id": 25,
                          "firstName": "Ana",
                          "lastName": "Garcia",
                          "email": "ana.garcia@example.com",
                          "address": "Calle de la Verificación 789",
                          "phoneNumber": "555-4433",
                          "role": "CLIENT",
                          "isActive": true,
                          "emailVerifiedAt": "2025-09-13T20:10:00.543Z",
                          "lastLoginAt": null,
                          "createdAt": "2025-09-13T10:00:00Z",
                          "updatedAt": "2025-09-13T20:10:00.543Z"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere un token JWT válido.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Autenticación",
                                    value = """
                        {
                          "status": 401,
                          "message": "Authentication failed: Bad credentials",
                          "timestamp": "2025-09-13T20:11:00.123Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Se requiere el rol 'ADMIN' para esta operación.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Autorización",
                                    value = """
                        {
                          "status": 403,
                          "message": "Access Denied: You do not have permission to access this resource.",
                          "timestamp": "2025-09-13T20:12:05.912Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado. El ID proporcionado no corresponde a ningún usuario.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Usuario No Encontrado",
                                    value = """
                        {
                          "status": 404,
                          "message": "Usuario no encontrado con el ID 999",
                          "timestamp": "2025-09-13T20:13:00.789Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            )
    })
    @PatchMapping("/{id}/verify-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> markEmailAsVerified(@PathVariable Long id) {
        log.info("Verificando email del usuario ID: {}", id);

        UserResponse updatedUser = userService.markEmailAsVerified(id);

        log.info("Email verificado para usuario ID: {}", id);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Elimina permanentemente un usuario del sistema (Operación Administrativa).
     * <p>
     * <strong>¡ADVERTENCIA: Esta es una operación destructiva e irreversible!</strong>
     * Elimina todos los datos del usuario, incluyendo su perfil, membresías de cuenta y
     * cualquier otra información asociada que no sea crítica para la integridad histórica del sistema.
     * </p>
     * <p><strong>Casos de Uso:</strong></p>
     * <ul>
     * <li>Atender solicitudes de "derecho al olvido" (GDPR).</li>
     * <li>Eliminación de cuentas de prueba o creadas por error.</li>
     * <li>Limpieza de datos en entornos de no producción.</li>
     * </ul>
     *
     * @param id El identificador único (ID) del usuario que se va a eliminar permanentemente.
     * @return ResponseEntity con un DTO {@link DeleteResponseDTO} confirmando que la eliminación fue exitosa.
     * @throws UserNotFoundException si no se encuentra ningún usuario con el ID proporcionado.
     * @throws DataIntegrityViolationException si el usuario no puede ser eliminado debido a restricciones de clave foránea (ej. tiene reservas facturadas).
     */
    @Operation(
            summary = "Elimina un usuario (Admin)",
            description = "Operación destructiva que elimina permanentemente a un usuario y sus datos asociados del sistema. Requiere rol 'ADMIN' y debe usarse con extrema precaución."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario eliminado exitosamente. La respuesta confirma la operación.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class), // Asumiendo un DTO genérico de respuesta
                            examples = @ExampleObject(
                                    name = "Eliminación Exitosa",
                                    summary = "Respuesta tras eliminar un usuario",
                                    value = """
                        {
                          "message": "El usuario con ID 42 ha sido eliminado exitosamente."
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Se requiere el rol 'ADMIN' para esta operación.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Autorización",
                                    value = """
                        {
                          "status": 403,
                          "message": "Access Denied: You do not have permission to access this resource.",
                          "timestamp": "2025-09-13T21:15:00.123Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado. El ID proporcionado no corresponde a ningún usuario.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Usuario No Encontrado",
                                    value = """
                        {
                          "status": 404,
                          "message": "Usuario no encontrado con el ID 999",
                          "timestamp": "2025-09-13T21:16:00.456Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto de integridad de datos. El usuario no puede ser eliminado porque tiene registros asociados (ej. reservas facturadas) que impiden su borrado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Integridad",
                                    value = """
                        {
                          "status": 409,
                          "message": "No se puede eliminar el usuario: existen registros dependientes (ej. facturas). Considere desactivar la cuenta en su lugar.",
                          "timestamp": "2025-09-13T21:18:00.789Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteUser(@PathVariable Long id) {
        log.warn("Eliminando permanentemente usuario ID: {}", id);

        userService.deleteUser(id);

        log.warn("Usuario ID: {} eliminado permanentemente del sistema", id);

        // Crear una respuesta de éxito explícita en lugar de un cuerpo vacío.
        var response = Map.of("message", "El usuario con ID " + id + " ha sido eliminado exitosamente.");

        return ResponseEntity.ok(response);
    }

    // ========== ENDPOINTS DE CONSULTA ==========

    /**
     * Obtiene una lista completa de todos los usuarios del sistema (Operación Administrativa).
     * <p>
     * Este endpoint, de uso exclusivo para administradores, recupera <strong>todos</strong> los perfiles de
     * usuario registrados en la plataforma sin paginación, devolviendo la información completa de cada uno.
     * </p>
     * <p><strong>ADVERTENCIA DE RENDIMIENTO:</strong></p>
     * <p>
     * Esta operación puede ser costosa en términos de memoria y tiempo de respuesta si la base de datos
     * contiene un gran número de usuarios. Su uso en entornos de producción debe ser limitado y consciente.
     * </p>
     * <ul>
     * <li>Para interfaces de usuario o integraciones, se recomienda <strong>encarecidamente</strong> utilizar el endpoint paginado
     * <code>GET /api/users/summary</code>, que está optimizado para el rendimiento.</li>
     * <li>Este endpoint es útil principalmente para tareas de exportación de datos o procesos batch donde se
     * necesita el conjunto de datos completo de una sola vez.</li>
     * </ul>
     *
     * @return ResponseEntity que contiene una lista de DTOs {@link UserResponse} con los datos completos de todos los usuarios.
     * @throws org.springframework.security.access.AccessDeniedException si el solicitante no tiene el rol 'ADMIN'.
     */
    @Operation(
            summary = "Obtiene todos los usuarios (Admin, No Paginado)",
            description = """
                  Recupera una lista completa con la información detallada de todos los usuarios registrados.
                  **ADVERTENCIA:** Este endpoint no está paginado y puede causar problemas de rendimiento con un gran volumen de datos.
                  Para aplicaciones cliente e integraciones, se recomienda usar el endpoint paginado `GET /api/users/summary`.
                  """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de todos los usuarios obtenida exitosamente.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Respuesta Exitosa",
                                    summary = "Ejemplo de una lista con dos usuarios",
                                    value = """
                        [
                          {
                            "id": 1,
                            "firstName": "Admin",
                            "lastName": "User",
                            "email": "admin@petcare.com",
                            "address": "123 Admin Avenue",
                            "phoneNumber": "555-0100",
                            "role": "ADMIN",
                            "isActive": true,
                            "emailVerifiedAt": "2025-01-01T12:00:00Z",
                            "lastLoginAt": "2025-09-14T10:00:00Z",
                            "createdAt": "2025-01-01T12:00:00Z",
                            "updatedAt": "2025-09-14T10:00:00Z"
                          },
                          {
                            "id": 2,
                            "firstName": "John",
                            "lastName": "Doe",
                            "email": "john.doe@example.com",
                            "address": "456 Client Street",
                            "phoneNumber": "555-0101",
                            "role": "CLIENT",
                            "isActive": true,
                            "emailVerifiedAt": "2025-02-10T15:30:00Z",
                            "lastLoginAt": "2025-09-13T18:30:00Z",
                            "createdAt": "2025-02-10T15:25:00Z",
                            "updatedAt": "2025-09-13T18:30:00Z"
                          }
                        ]
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Se requiere el rol 'ADMIN' para esta operación.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Autorización",
                                    value = """
                        {
                          "status": 403,
                          "message": "Access Denied: You do not have permission to access this resource.",
                          "timestamp": "2025-09-14T10:05:00.123Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor. Podría ocurrir si la cantidad de datos a cargar excede la memoria disponible.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error del Servidor",
                                    value = """
                        {
                          "status": 500,
                          "message": "Ocurrió un error inesperado al procesar la solicitud.",
                          "timestamp": "2025-09-14T10:06:15.456Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.debug("Obteniendo todos los usuarios del sistema");

        List<UserResponse> users = userService.getAllUsers();

        log.debug("Retornando {} usuarios del sistema", users.size());
        return ResponseEntity.ok(users);
    }

    /**
     * Obtiene un listado paginado y resumido de todos los usuarios (Operación Administrativa).
     * <p>
     * Este es el endpoint recomendado para obtener listas de usuarios en interfaces de administración,
     * ya que está optimizado para el rendimiento. Devuelve una "página" de resultados en lugar de la
     * lista completa, evitando problemas de memoria y mejorando la velocidad de respuesta.
     * </p>
     * <p><strong>Características:</strong></p>
     * <ul>
     * <li><b>Paginación:</b> Controla cuántos usuarios se devuelven y desde qué punto de la lista.</li>
     * <li><b>Ordenamiento:</b> Permite ordenar los resultados por diferentes campos y en distintas direcciones.</li>
     * <li><b>Respuesta Optimizada:</b> Utiliza el DTO {@link UserSummaryResponse}, que contiene solo los datos
     * esenciales para un listado, reduciendo el tamaño de la respuesta.</li>
     * </ul>
     *
     * @param page El número de la página que se desea obtener (comienza en 0). Valor por defecto: 0.
     * @param size El número de usuarios a devolver por página. Valor por defecto: 20.
     * @param sortBy El campo por el cual se ordenarán los resultados. Campos válidos incluyen 'id', 'fullName', 'email', 'createdAt'. Valor por defecto: 'createdAt'.
     * @param sortDir La dirección del ordenamiento. Puede ser 'asc' (ascendente) o 'desc' (descendente). Valor por defecto: 'desc'.
     * @return ResponseEntity que contiene un objeto {@link Page} con la lista de usuarios resumidos en el campo `content` y metadatos de paginación.
     * @throws org.springframework.security.access.AccessDeniedException si el solicitante no tiene el rol 'ADMIN'.
     */
    @Operation(
            summary = "Obtiene un listado paginado de usuarios (Admin)",
            description = "Recupera una lista paginada y ordenada de usuarios con información resumida. Este es el método preferido para consumir listas de usuarios desde una interfaz de cliente. Requiere rol 'ADMIN'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Página de usuarios obtenida exitosamente.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class), // SpringDoc interpretará esto correctamente como una Página de UserSummaryResponse
                            examples = @ExampleObject(
                                    name = "Respuesta Paginada",
                                    summary = "Ejemplo de una página de resultados",
                                    value = """
                        {
                          "content": [
                            {
                              "id": 18,
                              "fullName": "Zoe Williams",
                              "email": "zoe.williams@example.com",
                              "role": "SITTER",
                              "isActive": true,
                              "emailVerified": true,
                              "createdAt": "2025-09-14T12:30:00Z"
                            },
                            {
                              "id": 15,
                              "fullName": "Carlos Ruiz",
                              "email": "carlos.ruiz.sitter@example.com",
                              "role": "SITTER",
                              "isActive": true,
                              "emailVerified": false,
                              "createdAt": "2025-09-13T14:30:00Z"
                            }
                          ],
                          "pageable": {
                            "pageNumber": 0,
                            "pageSize": 2,
                            "sort": {
                              "sorted": true,
                              "unsorted": false,
                              "empty": false
                            },
                            "offset": 0,
                            "paged": true,
                            "unpaged": false
                          },
                          "totalPages": 10,
                          "totalElements": 20,
                          "last": false,
                          "size": 2,
                          "number": 0,
                          "sort": {
                            "sorted": true,
                            "unsorted": false,
                            "empty": false
                          },
                          "numberOfElements": 2,
                          "first": true,
                          "empty": false
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Se requiere el rol 'ADMIN' para esta operación.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor. Puede ocurrir si se especifica un campo de ordenamiento ('sortBy') inválido.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserSummaryResponse>> getAllUsersSummary(
            @Parameter(description = "Número de la página a obtener (0-indexed).")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Número de usuarios por página.")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Campo por el cual ordenar los resultados. Campos válidos: 'id', 'fullName', 'email', 'createdAt'.")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Dirección del ordenamiento ('asc' o 'desc').")
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.debug("Obteniendo resumen paginado - Página: {}, Tamaño: {}, Orden: {} {}", page, size, sortBy, sortDir);

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserSummaryResponse> userPage = userService.getAllUsersSummary(pageable);

        log.debug("Retornando página {} de {} con {} usuarios. Total de elementos: {}",
                userPage.getNumber() + 1, userPage.getTotalPages(), userPage.getNumberOfElements(), userPage.getTotalElements());

        return ResponseEntity.ok(userPage);
    }

    /**
     * Obtiene la información completa de un usuario específico por su ID.
     * <p>
     * Este endpoint aplica reglas de autorización granulares basadas en el rol del solicitante:
     * </p>
     * <ul>
     * <li><b>Administradores:</b> Pueden solicitar la información de cualquier usuario del sistema.</li>
     * <li><b>Usuarios Regulares (CLIENT, SITTER):</b> Solo pueden solicitar su propia información, verificando que el ID en la URL coincida con el ID de su token de autenticación.</li>
     * </ul>
     * <p>
     * La respuesta incluye todos los datos no sensibles del usuario a través del DTO {@link UserResponse}.
     * </p>
     *
     * @param id El identificador único (ID) del usuario a consultar.
     * @param authentication Objeto inyectado por Spring Security que contiene los detalles del usuario autenticado, usado para la validación de permisos.
     * @return ResponseEntity que contiene un DTO {@link UserResponse} con el perfil completo del usuario solicitado.
     * @throws UserNotFoundException si no se encuentra ningún usuario con el ID proporcionado.
     * @throws org.springframework.security.access.AccessDeniedException si un usuario no administrador intenta acceder al perfil de otro usuario.
     */
    @Operation(
            summary = "Obtiene un usuario por su ID",
            description = "Recupera el perfil completo de un usuario. Los administradores pueden ver cualquier perfil, mientras que los demás usuarios solo pueden acceder al suyo propio."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado y devuelto exitosamente.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Respuesta Exitosa",
                                    summary = "Ejemplo del perfil de un usuario",
                                    value = """
                        {
                          "id": 1,
                          "firstName": "John",
                          "lastName": "Doe",
                          "email": "john.doe@example.com",
                          "address": "456 Client Street",
                          "phoneNumber": "555-0101",
                          "role": "CLIENT",
                          "isActive": true,
                          "emailVerifiedAt": "2025-02-10T15:30:00Z",
                          "lastLoginAt": "2025-09-13T18:30:00Z",
                          "createdAt": "2025-02-10T15:25:00Z",
                          "updatedAt": "2025-09-13T18:30:00Z"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Ocurre si un usuario intenta acceder al perfil de otro usuario sin ser administrador.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado. El ID proporcionado no corresponde a ningún usuario en el sistema.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Usuario No Encontrado",
                                    value = """
                        {
                          "status": 404,
                          "message": "Usuario no encontrado con el ID 999",
                          "timestamp": "2025-09-14T11:20:00.123Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "ID único del usuario a obtener.", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(hidden = true)
            Authentication authentication) {

        log.debug("Solicitud para obtener usuario por ID: {}", id);


        UserResponse userResponse = userService.getUserById(id);

        log.debug("Usuario encontrado: {}", userResponse.getEmail());
        return ResponseEntity.ok(userResponse);
    }

    /**
     * Obtiene la información completa de un usuario por su email (Operación Administrativa).
     * <p>
     * Endpoint de uso exclusivo para administradores que permite buscar un usuario utilizando su
     * dirección de correo electrónico, la cual es un identificador único en el sistema.
     * Es una herramienta fundamental para operaciones de soporte y gestión.
     * </p>
     * <p><strong>Casos de Uso:</strong></p>
     * <ul>
     * <li>Verificar la existencia de un usuario durante una llamada de soporte.</li>
     * <li>Obtener el ID de un usuario conociendo solo su email.</li>
     * <li>Revisar el perfil completo de un usuario para tareas administrativas.</li>
     * </ul>
     *
     * @param email La dirección de correo electrónico única del usuario que se desea consultar.
     * @return ResponseEntity que contiene un DTO {@link UserResponse} con el perfil completo del usuario solicitado.
     * @throws UserNotFoundException si no se encuentra ningún usuario con el email proporcionado.
     * @throws org.springframework.security.access.AccessDeniedException si el solicitante no tiene el rol 'ADMIN'.
     */
    @Operation(
            summary = "Obtiene un usuario por su email (Admin)",
            description = "Recupera el perfil completo de un usuario utilizando su dirección de email como identificador. Requiere rol 'ADMIN'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado y devuelto exitosamente.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Respuesta Exitosa",
                                    summary = "Ejemplo del perfil de un usuario encontrado por email",
                                    value = """
                        {
                          "id": 1,
                          "firstName": "John",
                          "lastName": "Doe",
                          "email": "john.doe@example.com",
                          "address": "456 Client Street",
                          "phoneNumber": "555-0101",
                          "role": "CLIENT",
                          "isActive": true,
                          "emailVerifiedAt": "2025-02-10T15:30:00Z",
                          "lastLoginAt": "2025-09-13T18:30:00Z",
                          "createdAt": "2025-02-10T15:25:00Z",
                          "updatedAt": "2025-09-13T18:30:00Z"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Se requiere el rol 'ADMIN' para esta operación.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado. El email proporcionado no corresponde a ningún usuario.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Usuario No Encontrado por Email",
                                    value = """
                        {
                          "status": 404,
                          "message": "Usuario no encontrado con el email 'no.existe@example.com'",
                          "timestamp": "2025-09-14T11:25:00.123Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            )
    })
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserByEmail(
            @Parameter(description = "Email único del usuario a obtener.", required = true, example = "john.doe@example.com")
            @PathVariable String email) {

        log.debug("Solicitud para obtener usuario por email: {}", email);
        UserResponse userResponse = userService.getUserByEmail(email);
        log.debug("Usuario encontrado con email: {}", email);

        return ResponseEntity.ok(userResponse);
    }

    /**
     * Obtiene usuarios filtrados por un rol específico.
     * <p>
     * Endpoint de uso administrativo que devuelve una lista de todos los usuarios que pertenecen a un rol
     * determinado (CLIENT, SITTER, o ADMIN). La respuesta utiliza el DTO resumido {@link UserSummaryResponse}
     * para ser más ligera.
     * </p>
     * <p><strong>ADVERTENCIA DE RENDIMIENTO:</strong></p>
     * <p>
     * Este endpoint no está paginado. Si se espera un gran número de usuarios para un rol específico (ej. miles de clientes),
     * su uso podría impactar el rendimiento. Para tales casos, se debería considerar implementar una versión paginada.
     * </p>
     *
     * @param role El rol por el cual se filtrarán los usuarios. Spring convierte automáticamente el texto de la URL (ej. "SITTER") al enum {@link Role}.
     * @return ResponseEntity que contiene una lista de DTOs {@link UserSummaryResponse} para todos los usuarios que coinciden con el rol.
     * @throws org.springframework.security.access.AccessDeniedException si el solicitante no tiene el rol 'ADMIN'.
     */
    @Operation(
            summary = "Obtiene usuarios por rol (Admin, No Paginado)",
            description = "Recupera una lista con información resumida de todos los usuarios que coinciden con un rol específico. ADVERTENCIA: No está paginado y puede ser lento con grandes volúmenes de datos. Requiere rol 'ADMIN'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuarios filtrada por rol obtenida exitosamente.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserSummaryResponse.class),
                            examples = @ExampleObject(
                                    name = "Respuesta para Rol SITTER",
                                    summary = "Ejemplo de una lista de cuidadores",
                                    value = """
                        [
                          {
                            "id": 18,
                            "fullName": "Isabel Contreras",
                            "email": "isabel.contreras@example.com",
                            "role": "SITTER",
                            "isActive": true,
                            "emailVerified": true,
                            "createdAt": "2025-09-14T12:30:00Z"
                          },
                          {
                            "id": 25,
                            "fullName": "Carlos Ruiz",
                            "email": "carlos.ruiz@example.com",
                            "role": "SITTER",
                            "isActive": false,
                            "emailVerified": true,
                            "createdAt": "2025-09-13T14:30:00Z"
                          }
                        ]
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Rol inválido. Ocurre si el valor en la URL no es un rol válido (CLIENT, SITTER, ADMIN).",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Rol Inválido",
                                    value = """
                        {
                          "status": 400,
                          "message": "Invalid value 'INVALID_ROLE' for parameter 'role'.",
                          "timestamp": "2025-09-14T12:35:00.123Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Se requiere el rol 'ADMIN' para esta operación.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSummaryResponse>> getUsersByRole(
            @Parameter(description = "El rol por el cual filtrar los usuarios (CLIENT, SITTER, ADMIN).", required = true, example = "SITTER")
            @PathVariable Role role) {

        log.debug("Solicitud para obtener usuarios con rol: {}", role);
        List<UserSummaryResponse> users = userService.getUsersByRole(role);
        log.debug("Encontrados {} usuarios con rol: {}", users.size(), role);
        return ResponseEntity.ok(users);
    }

    /**
     * Obtiene un listado paginado y resumido de todos los usuarios activos (Operación Administrativa).
     * <p>
     * Este endpoint es una vista filtrada del listado principal de usuarios, devolviendo únicamente
     * aquellos cuya cuenta está marcada como <code>isActive = true</code>. Es ideal para interfaces
     * administrativas que necesitan operar sobre usuarios habilitados en el sistema.
     * </p>
     * <p><strong>Características:</strong></p>
     * <ul>
     * <li><b>Filtrado por Estado:</b> Excluye automáticamente a los usuarios inactivos.</li>
     * <li><b>Paginación y Ordenamiento:</b> Ofrece control total sobre la paginación y el orden de los resultados.</li>
     * <li><b>Respuesta Optimizada:</b> Devuelve el DTO ligero {@link UserSummaryResponse}.</li>
     * </ul>
     *
     * @param page El número de la página que se desea obtener (comienza en 0). Valor por defecto: 0.
     * @param size El número de usuarios a devolver por página. Valor por defecto: 20.
     * @param sortBy El campo por el cual se ordenarán los resultados. Valor por defecto: 'createdAt'.
     * @param sortDir La dirección del ordenamiento ('asc' o 'desc'). Valor por defecto: 'desc'.
     * @return ResponseEntity que contiene un objeto {@link Page} con la lista de usuarios activos y resumidos.
     * @throws org.springframework.security.access.AccessDeniedException si el solicitante no tiene el rol 'ADMIN'.
     */
    @Operation(
            summary = "Obtiene usuarios activos (Admin, Paginado)",
            description = "Recupera una lista paginada y ordenada de usuarios cuyo estado es 'activo'. Este es el endpoint ideal para poblar listas de usuarios habilitados en paneles de administración. Requiere rol 'ADMIN'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Página de usuarios activos obtenida exitosamente.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "Respuesta Paginada de Activos",
                                    summary = "Ejemplo de una página con usuarios activos",
                                    value = """
                        {
                          "content": [
                            {
                              "id": 1,
                              "fullName": "Admin User",
                              "email": "admin@petcare.com",
                              "role": "ADMIN",
                              "isActive": true,
                              "emailVerified": true,
                              "createdAt": "2025-09-14T12:30:00Z"
                            },
                            {
                              "id": 12,
                              "fullName": "Jane Doe",
                              "email": "jane.doe@example.com",
                              "role": "CLIENT",
                              "isActive": true,
                              "emailVerified": true,
                              "createdAt": "2025-09-13T10:00:00Z"
                            }
                          ],
                          "pageable": { "pageNumber": 0, "pageSize": 2, "sort": { "sorted": true, "unsorted": false, "empty": false }, "offset": 0, "paged": true, "unpaged": false },
                          "totalPages": 5,
                          "totalElements": 10,
                          "last": false,
                          "size": 2,
                          "number": 0,
                          "sort": { "sorted": true, "unsorted": false, "empty": false },
                          "numberOfElements": 2,
                          "first": true,
                          "empty": false
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Se requiere el rol 'ADMIN' para esta operación.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor. Puede ocurrir si se especifica un campo de ordenamiento ('sortBy') inválido.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserSummaryResponse>> getActiveUsers(
            @Parameter(description = "Número de la página a obtener (0-indexed).")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Número de usuarios por página.")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Campo para ordenar los resultados. Ej: 'createdAt', 'fullName'.")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Dirección del ordenamiento ('asc' o 'desc').")
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.debug("Obteniendo usuarios activos paginados - Página: {}, Tamaño: {}, Orden: {} {}", page, size, sortBy, sortDir);

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserSummaryResponse> activeUsersPage = userService.getActiveUsers(pageable);

        log.debug("Retornando página {}/{} con {} usuarios activos",
                activeUsersPage.getNumber() + 1, activeUsersPage.getTotalPages(), activeUsersPage.getNumberOfElements());

        return ResponseEntity.ok(activeUsersPage);
    }

    /**
     * Obtiene una lista resumida de usuarios con el email sin verificar (Operación Administrativa).
     * <p>
     * Este endpoint es una herramienta administrativa para identificar todas las cuentas que no han completado
     * el proceso de verificación de correo electrónico. Es útil para realizar campañas de reactivación o para
     * analizar la tasa de conversión en el embudo de registro.
     * </p>
     * <p><strong>ADVERTENCIA DE RENDIMIENTO:</strong></p>
     * <p>
     * Esta operación no está paginada. Si se acumula un gran número de usuarios no verificados,
     * este endpoint podría devolver una respuesta muy grande, afectando el rendimiento. Se recomienda
     * considerar una versión paginada para entornos de producción a gran escala.
     * </p>
     *
     * @return ResponseEntity que contiene una lista de DTOs {@link UserSummaryResponse} de todos los usuarios no verificados.
     * @throws org.springframework.security.access.AccessDeniedException si el solicitante no tiene el rol 'ADMIN'.
     */
    @Operation(
            summary = "Obtiene usuarios no verificados (Admin)",
            description = "Recupera una lista con información resumida de todos los usuarios que no han verificado su dirección de email. ADVERTENCIA: No está paginado. Requiere rol 'ADMIN'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuarios no verificados obtenida exitosamente.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserSummaryResponse.class),
                            examples = @ExampleObject(
                                    name = "Respuesta Exitosa",
                                    summary = "Ejemplo de una lista con usuarios no verificados",
                                    value = """
                        [
                          {
                            "id": 30,
                            "fullName": "Pedro Pendiente",
                            "email": "pedro.pendiente@example.com",
                            "role": "CLIENT",
                            "isActive": true,
                            "emailVerified": false,
                            "createdAt": "2025-09-14T15:10:00Z"
                          },
                          {
                            "id": 31,
                            "fullName": "Laura Limbo",
                            "email": "laura.limbo@example.com",
                            "role": "SITTER",
                            "isActive": true,
                            "emailVerified": false,
                            "createdAt": "2025-09-14T14:05:00Z"
                          }
                        ]
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Se requiere el rol 'ADMIN' para esta operación.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/unverified")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSummaryResponse>> getUnverifiedUsers() {
        log.debug("Obteniendo usuarios con email sin verificar");

        List<UserSummaryResponse> unverifiedUsers = userService.getUnverifiedUsers();

        log.debug("Encontrados {} usuarios sin verificar email", unverifiedUsers.size());
        return ResponseEntity.ok(unverifiedUsers);
    }

    // ========== ENDPOINTS DE ESTADÍSTICAS ==========

    /**
     * Obtiene estadísticas agregadas de la base de usuarios (Operación Administrativa).
     * <p>
     * Este endpoint proporciona una vista de alto nivel de la base de usuarios de la plataforma,
     * calculando diversas métricas en tiempo real. Está diseñado para alimentar dashboards
     * administrativos, generar reportes de negocio y monitorear la salud general de la comunidad.
     * </p>
     * <p><strong>Métricas Clave Incluidas:</strong></p>
     * <ul>
     * <li><b>Conteos Generales:</b> Número total de usuarios, cuántos están activos e inactivos.</li>
     * <li><b>Distribución por Rol:</b> Desglose de cuántos usuarios son CLIENT, SITTER y ADMIN.</li>
     * <li><b>Estado de Verificación:</b> Total y porcentaje de usuarios que han verificado su email.</li>
     * <li><b>Cálculos Derivados:</b> El DTO {@link UserStatsResponse} puede incluir porcentajes y análisis adicionales.</li>
     * </ul>
     *
     * @return ResponseEntity que contiene un DTO {@link UserStatsResponse} con todas las métricas calculadas.
     * @throws org.springframework.security.access.AccessDeniedException si el solicitante no tiene el rol 'ADMIN'.
     */
    @Operation(
            summary = "Obtiene estadísticas de usuarios (Admin)",
            description = "Recupera un objeto con métricas agregadas sobre la base de usuarios, como conteos totales, desglose por rol y estado de verificación. Requiere rol 'ADMIN'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estadísticas generadas y devueltas exitosamente.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserStatsResponse.class),
                            examples = @ExampleObject(
                                    name = "Respuesta de Estadísticas",
                                    summary = "Ejemplo de las métricas de la plataforma",
                                    value = """
                        {
                          "totalUsers": 150,
                          "activeUsers": 145,
                          "clientCount": 120,
                          "sitterCount": 25,
                          "adminCount": 5,
                          "verifiedUsers": 148,
                          "activeUsersPercentage": 96.67,
                          "verifiedUsersPercentage": 98.67,
                          "clientPercentage": 80.0,
                          "sitterPercentage": 16.67,
                          "adminPercentage": 3.33
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Se requiere el rol 'ADMIN' para esta operación.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor durante el cálculo de las estadísticas.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserStatsResponse> getUserStats() {
        log.debug("Generando estadísticas de usuarios");

        UserStatsResponse stats = userService.getUserStats();

        log.info("Estadísticas generadas: {}", stats.getExecutiveSummary());
        return ResponseEntity.ok(stats);
    }

    // ========== ENDPOINTS DE UTILIDAD ==========

    /**
     * Verifica si una dirección de email está disponible para el registro.
     * <p>
     * Este es un endpoint público de utilidad, diseñado para ser consumido por el frontend
     * durante el proceso de registro. Permite verificar en tiempo real si un correo electrónico
     * ya está en uso, mejorando la experiencia del usuario al proporcionar feedback inmediato.
     * </p>
     * <p><strong>Funcionamiento:</strong></p>
     * <ul>
     * <li>Devuelve <strong><code>true</code></strong> si el email no existe en la base de datos y puede ser utilizado.</li>
     * <li>Devuelve <strong><code>false</code></strong> si el email ya está registrado por otro usuario.</li>
     * </ul>
     *
     * @param email La dirección de correo electrónico que se desea verificar.
     * @return ResponseEntity que contiene un booleano: <code>true</code> si el email está disponible, <code>false</code> si ya está en uso.
     */
    @Operation(
            summary = "Verifica si un email está disponible",
            description = "Endpoint público para comprobar si una dirección de email ya está registrada en el sistema. Ideal para validaciones en tiempo real en formularios de registro."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Verificación completada. El cuerpo de la respuesta es un booleano que indica la disponibilidad.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Email Disponible",
                                            summary = "Respuesta cuando el email NO está en uso",
                                            value = "true"
                                    ),
                                    @ExampleObject(
                                            name = "Email No Disponible",
                                            summary = "Respuesta cuando el email YA está en uso",
                                            value = "false"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parámetro Faltante. Ocurre si no se proporciona el parámetro 'email' en la solicitud.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/email-available")
    public ResponseEntity<Boolean> isEmailAvailable(
            @Parameter(description = "La dirección de email a verificar.", required = true, example = "nuevo.usuario@example.com")
            @RequestParam String email) {

        log.debug("Verificando disponibilidad de email: {}", email);

        // Se utiliza un método de servicio dedicado y más eficiente para esta comprobación.
        boolean isAvailable = userService.isEmailAvailable(email);

        log.debug("El email {} está {}", email, isAvailable ? "disponible" : "en uso");
        return ResponseEntity.ok(isAvailable);
    }

    /**
     * Verifica el estado operativo del servicio de usuarios y su conexión a la base de datos.
     * <p>
     * Este es un endpoint público de monitoreo que realiza una verificación "profunda" del servicio.
     * A diferencia de un 'ping' simple, esta operación intenta activamente realizar una consulta
     * a la base de datos (contar usuarios) para confirmar que tanto la aplicación como su
     * conexión a la base de datos están funcionales.
     * </p>
     * <p><strong>Funcionamiento:</strong></p>
     * <ul>
     * <li><b>Éxito:</b> Si la consulta a la base de datos es exitosa, devuelve un estado <code>200 OK</code>
     * con un mensaje de texto plano que confirma la operatividad e incluye el número total de usuarios.</li>
     * <li><b>Fallo:</b> Si ocurre cualquier excepción durante la consulta (ej. la base de datos está caída),
     * devuelve un estado <code>503 Service Unavailable</code> con un mensaje de error.</li>
     * </ul>
     *
     * @return ResponseEntity que contiene un <code>String</code> con el mensaje de estado del servicio.
     */
    @Operation(
            summary = "Verifica el estado del servicio y BD (Health Check)",
            description = "Endpoint público que confirma que la API está operativa realizando una consulta a la base de datos para contar los usuarios. Devuelve una respuesta de texto plano."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "El servicio y la conexión a la base de datos están operativos.",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(
                                    name = "Servicio Operativo",
                                    summary = "Respuesta cuando el servicio funciona",
                                    value = "UserService está operativo. Total usuarios: 152"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "El servicio no está disponible o no puede conectar con la base de datos.",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(
                                    name = "Fallo de Conexión",
                                    summary = "Respuesta si la BD no responde",
                                    value = "UserService presenta problemas: Cannot create PoolableConnectionFactory (Communications link failure)"
                            )
                    )
            )
    })
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.debug("Verificación de salud del servicio de usuarios");

        try {
            // Verificación básica contando usuarios
            long userCount = userService.getUserStats().getTotalUsers();
            return ResponseEntity.ok("UserService está operativo. Total usuarios: " + userCount);
        } catch (Exception e) {
            log.error("Error en verificación de salud: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("UserService presenta problemas: " + e.getMessage());
        }
    }

    /**
     * Procesa un token de verificación de email para activar la cuenta de un usuario.
     * <p>
     * Este endpoint es el destino del enlace que los usuarios reciben en su correo de bienvenida.
     * No es un endpoint de API REST tradicional, ya que su función es manejar una interacción
     * directa con el navegador del usuario.
     * </p>
     * <p><strong>Flujo de Proceso:</strong></p>
     * <ul>
     * <li><b>Validación del Token:</b> El servicio valida que el token sea auténtico, no haya expirado y no haya sido utilizado.</li>
     * <li><b>Activación de Cuenta:</b> Si el token es válido, se marca el email del usuario como verificado en la base de datos.</li>
     * <li><b>Redirección:</b> El navegador del usuario es redirigido a una página estática de éxito (<code>/verification-success.html</code>) o a una página de error con un mensaje explicativo.</li>
     * </ul>
     *
     * @param token El token JWT de un solo uso, enviado como parámetro de consulta en el enlace de verificación.
     * @return Un objeto {@link RedirectView} que instruye al navegador a redirigir a la página de resultado correspondiente.
     */
    @Operation(
            summary = "Verifica un email usando un token",
            description = "Procesa el token de verificación enviado por email. Este endpoint no devuelve JSON, sino que realiza una redirección 302 a una página de éxito o de error, diseñado para ser abierto en un navegador."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "Redirección procesada. El resultado (éxito o error) se indica en la cabecera 'Location' de la respuesta.",
                    content = @Content, // No hay cuerpo en una respuesta de redirección
                    headers = {
                            @io.swagger.v3.oas.annotations.headers.Header(
                                    name = "Location",
                                    description = "URL a la que el navegador será redirigido. Será '/verification-success.html' en caso de éxito, o '/verification-error?message=...' en caso de fallo."
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parámetro 'token' faltante en la solicitud.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/verify")
    public RedirectView verifyEmail(
            @Parameter(description = "Token de verificación recibido en el correo electrónico.", required = true, example = "eyJhbGciOiJIUzI1NiJ9...")
            @RequestParam("token") String token) {
        try {
            userService.verifyEmailToken(token);
            log.info("Verificación de email exitosa para el token proporcionado.");
            // Redirige a una página estática de éxito dentro de la aplicación
            return new RedirectView("/verification-success.html");
        } catch (RuntimeException e) {
            log.warn("Falló la verificación de email: {}", e.getMessage());
            // Redirige a una página de error (potencialmente en el frontend) con el mensaje de error
            return new RedirectView(frontendBaseUrl + "/verification-error?message=" + e.getMessage());
        }
    }

}
