package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.GlobalException.ErrorResponseDTO;
import com.Petcare.Petcare.DTOs.Sitter.SitterProfileDTO;
import com.Petcare.Petcare.Exception.Business.SitterProfileAlreadyExistsException;
import com.Petcare.Petcare.Exception.Business.SitterProfileNotFoundException;
import com.Petcare.Petcare.Exception.Business.UserNotFoundException;
import com.Petcare.Petcare.Services.SitterService;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión integral de perfiles de cuidadores (Sitter Profiles).
 *
 * <p>Este controlador expone los endpoints de la API para todas las operaciones relacionadas
 * con los perfiles profesionales de los cuidadores. Actúa como la capa de entrada HTTP,
 * gestionando la validación, autorización y delegando la lógica de negocio a la
 * capa de servicio ({@link SitterService}).</p>
 *
 * <p><b>Funcionalidades Expuestas:</b></p>
 * <ul>
 * <li>Creación de un nuevo perfil de cuidador para un usuario.</li>
 * <li>Consulta detallada de un perfil específico.</li>
 * <li>Actualización de la información de un perfil.</li>
 * <li>Eliminación de un perfil.</li>
 * <li>Listado de todos los perfiles para fines administrativos.</li>
 * </ul>
 *
 * <p>La seguridad se aplica a nivel de método utilizando {@code @PreAuthorize} para garantizar
 * un control de acceso granular, permitiendo que los administradores tengan control total
 * mientras que los usuarios solo pueden gestionar sus propios perfiles.</p>
 *
 * @see SitterService
 * @see com.Petcare.Petcare.Models.SitterProfile
 * @see SitterProfileDTO
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/sitter-profiles")
@Tag(name = "Perfiles de Cuidador", description = "API para la gestión completa de los perfiles profesionales de los cuidadores (Sitters).")
public class SitterProfileController {


    private final SitterService sitterService;
    private final UserRepository userRepository;

    @Autowired
    public SitterProfileController(SitterService sitterService,
                                   UserRepository userRepository) {
        this.sitterService = sitterService;
        this.userRepository = userRepository;
    }

    /**
     * Crea un perfil de cuidador para el usuario autenticado.
     * <p>
     * Este endpoint permite que un usuario existente en la plataforma, típicamente con el rol 'CLIENT',
     * pueda registrarse como cuidador (Sitter). Al enviar sus datos profesionales, se crea una entidad
     * SitterProfile vinculada a su cuenta de usuario. Esta es una operación de un solo paso que
     * habilita al usuario para ofrecer servicios.
     *
     * <p>Flujo del Proceso:</p>
     * <ul>
     * <li><b>Validación:</b> Se valida que los datos del DTO cumplan con las restricciones (ej. tarifas positivas, biografía no muy larga).</li>
     * <li><b>Verificación:</b> El servicio comprueba que el usuario autenticado no tenga ya un perfil de cuidador existente.</li>
     * <li><b>Persistencia:</b> Se crea y guarda la nueva entidad {@code SitterProfile} en la base de datos.</li>
     * <li><b>Respuesta:</b> Se devuelve el perfil de cuidador recién creado, incluyendo su ID único.</li>
     * </ul>
     *
     * @param currentUser El objeto {@link User} del usuario autenticado, inyectado automáticamente por Spring Security.
     * @param sitterProfileDTO El DTO que contiene los datos del perfil a crear, como la biografía y la tarifa por hora.
     * @return Un {@link ResponseEntity} que contiene el {@link SitterProfileDTO} completo del perfil recién creado, con un estado HTTP 201 (Created).
     * @throws SitterProfileAlreadyExistsException Si el usuario ya tiene un perfil de cuidador, manejado por el GlobalExceptionHandler (resultando en un 409 Conflict).
     * @throws UserNotFoundException Si el usuario autenticado no se encuentra en la base de datos.
     */
    @PostMapping
    @Transactional
    @PreAuthorize("hasRole('SITTER') or hasRole('ADMIN')")
    @Operation(
            summary = "Crea un perfil de cuidador para el usuario autenticado",
            description = "Permite a un usuario con rol 'CLIENT' o 'ADMIN' registrarse como cuidador en la plataforma. Esta operación crea el perfil profesional asociado a su cuenta y solo puede realizarse una vez por usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Perfil de cuidador creado exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SitterProfileDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "id": 123,
                      "userId": 45,
                      "bio": "Amante de los animales con más de 5 años de experiencia en el cuidado de perros y gatos. Especialista en razas grandes.",
                      "hourlyRate": 15.50,
                      "servicingRadius": 10,
                      "profileImageUrl": "https://example.com/images/sitter_profile_45.jpg",
                      "verified": false,
                      "availableForBookings": true
                    }
                    """))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos proporcionados inválidos. La tarifa por hora es negativa o la biografía excede el límite de caracteres.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "status": 400,
                      "message": "Validation failed",
                      "timestamp": "2025-09-13T20:30:00Z",
                      "validationErrors": {
                        "hourlyRate": "La tarifa por hora debe ser un valor positivo."
                      }
                    }
                    """))),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere un token JWT válido en la cabecera 'Authorization'.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "status": 401,
                      "message": "No se proporcionó un token de autenticación válido.",
                      "timestamp": "2025-09-13T20:30:00Z",
                      "validationErrors": null
                    }
                    """))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. El usuario no tiene el rol requerido ('CLIENT' o 'ADMIN').",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "status": 403,
                      "message": "Acceso denegado. Se requiere rol CLIENT o ADMIN.",
                      "timestamp": "2025-09-13T20:30:00Z",
                      "validationErrors": null
                    }
                    """))),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto. El usuario ya posee un perfil de cuidador.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "status": 409,
                      "message": "Conflicto: El usuario con ID 45 ya tiene un perfil de cuidador registrado.",
                      "timestamp": "2025-09-13T20:30:00Z",
                      "validationErrors": null
                    }
                    """)))
    })
    public ResponseEntity<SitterProfileDTO> createSitterProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody SitterProfileDTO sitterProfileDTO
    ) {
        log.info("Creando perfil de Sitter para userId={}", currentUser.getId());
        SitterProfileDTO created = sitterService.createSitterProfile(currentUser.getId(), sitterProfileDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Obtiene el perfil de cuidador asociado a un ID de usuario específico.
     * <p>
     * Este endpoint recupera la información detallada y profesional de un cuidador,
     * como su biografía, tarifa por hora, radio de servicio y estado de verificación.
     * Es fundamental para que los clientes puedan evaluar y seleccionar al cuidador
     * adecuado para sus mascotas.
     * <p>
     * El acceso a este recurso está protegido y sigue una lógica de permisos estricta:
     * <ul>
     * <li>Un <b>Administrador</b> puede solicitar el perfil de cualquier usuario.</li>
     * <li>Un <b>usuario regular</b> (CLIENT o SITTER) solo puede acceder a su <b>propio</b> perfil.
     * Cualquier intento de acceder al perfil de otro usuario resultará en un error 403 Forbidden.</li>
     * </ul>
     *
     * @param userId El ID del <b>usuario</b> (no del perfil) cuyo perfil de cuidador se desea obtener.
     * @return Un {@link ResponseEntity} con el {@link SitterProfileDTO} si se encuentra, encapsulado en un código de estado 200 OK.
     * @throws SitterProfileNotFoundException si no existe un perfil de cuidador para el ID de usuario proporcionado.
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @Operation(
            summary = "Obtener Perfil de Cuidador por ID de Usuario",
            description = "Recupera los detalles completos del perfil de un cuidador, incluyendo biografía, tarifas y disponibilidad. El acceso está restringido a administradores o al propio usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil de cuidador encontrado y devuelto exitosamente.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SitterProfileDTO.class),
                            examples = @ExampleObject(
                                    name = "Respuesta Exitosa 200",
                                    value = """
                                            {
                                              "id": 1,
                                              "userId": 5,
                                              "bio": "Amante de los perros con más de 5 años de experiencia en el cuidado de razas grandes. Ofrezco paseos largos y juegos en el parque.",
                                              "hourlyRate": 15.50,
                                              "servicingRadius": 10,
                                              "profileImageUrl": "https://example.com/images/sitter5.jpg",
                                              "verified": true,
                                              "availableForBookings": true
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere un token JWT válido en la cabecera 'Authorization'.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error 401 - No Autenticado",
                                    value = """
                                            {
                                              "status": 401,
                                              "message": "Autenticación requerida. Por favor, incluya un token válido.",
                                              "timestamp": "2025-09-13T18:47:05.789Z",
                                              "validationErrors": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Un usuario solo puede ver su propio perfil, a menos que sea un administrador.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error 403 - Acceso Denegado",
                                    value = """
                                            {
                                              "status": 403,
                                              "message": "Acceso denegado. No tiene permiso para acceder a este recurso.",
                                              "timestamp": "2025-09-13T18:46:20.456Z",
                                              "validationErrors": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró un perfil de cuidador para el ID de usuario proporcionado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error 404 - No Encontrado",
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "No se encontró un perfil de cuidador para el usuario con ID: 99",
                                              "timestamp": "2025-09-13T18:45:12.123Z",
                                              "validationErrors": null
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<SitterProfileDTO> getSitterProfile(@PathVariable Long userId) {
        log.info("Obteniendo perfil de Sitter para userId={}", userId);
        // La lógica de negocio ahora está completamente encapsulada en el servicio.
        // Si el perfil no se encuentra, el servicio lanzará SitterProfileNotFoundException,
        // que será capturada por el GlobalExceptionHandler para devolver un 404 estandarizado.
        SitterProfileDTO profileDTO = sitterService.getSitterProfile(userId);
        return ResponseEntity.ok(profileDTO);
    }

    /**
     * Obtiene una lista de todos los perfiles de cuidadores registrados en el sistema.
     * <p>
     * Este es un endpoint de carácter administrativo, diseñado para la supervisión y gestión
     * de la plataforma. Devuelve una lista completa y sin paginar de todos los perfiles de
     * cuidadores, tanto activos como inactivos, verificados o no.
     *
     * <p>Casos de Uso:</p>
     * <ul>
     * <li>Supervisión por parte de los administradores.</li>
     * <li>Generación de reportes internos sobre la comunidad de cuidadores.</li>
     * <li>Tareas de auditoría y gestión de calidad.</li>
     * </ul>
     *
     * @return Un {@link ResponseEntity} que contiene una lista de {@link SitterProfileDTO} con los
     * perfiles completos de todos los cuidadores y un estado HTTP 200 (OK).
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Obtener todos los perfiles de cuidadores (Admin)",
            description = "Endpoint administrativo que devuelve una lista completa de todos los perfiles de cuidadores registrados en la plataforma. Requiere rol de 'ADMIN'.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de perfiles de cuidadores obtenida exitosamente.",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SitterProfileDTO.class)),
                            examples = @ExampleObject(value = """
                    [
                      {
                        "id": 123,
                        "userId": 45,
                        "bio": "Amante de los animales con más de 5 años de experiencia.",
                        "hourlyRate": 15.50,
                        "servicingRadius": 10,
                        "profileImageUrl": "https://example.com/images/sitter_1.jpg",
                        "verified": true,
                        "availableForBookings": true
                      },
                      {
                        "id": 124,
                        "userId": 52,
                        "bio": "Estudiante de veterinaria, disponible los fines de semana.",
                        "hourlyRate": 12.00,
                        "servicingRadius": 5,
                        "profileImageUrl": "https://example.com/images/sitter_2.jpg",
                        "verified": false,
                        "availableForBookings": true
                      }
                    ]
                    """))),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere un token JWT válido.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "status": 401,
                      "message": "No se proporcionó un token de autenticación válido.",
                      "timestamp": "2025-09-13T21:05:00Z",
                      "validationErrors": null
                    }
                    """))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Se requiere el rol 'ADMIN' para acceder a este recurso.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "status": 403,
                      "message": "Acceso denegado. Se requiere rol ADMIN.",
                      "timestamp": "2025-09-13T21:05:00Z",
                      "validationErrors": null
                    }
                    """))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor al consultar los perfiles.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "status": 500,
                      "message": "Ocurrió un error inesperado al procesar la solicitud.",
                      "timestamp": "2025-09-13T21:05:00Z",
                      "validationErrors": null
                    }
                    """)))
    })
    public ResponseEntity<List<SitterProfileDTO>> getAllSitterProfiles() {
        log.info("Obteniendo todos los perfiles de Sitter (petición de admin)");
        List<SitterProfileDTO> profiles = sitterService.getAllSitterProfiles();
        return ResponseEntity.ok(profiles);
    }

    /**
     * Actualiza un perfil de cuidador existente.
     * <p>
     * Este endpoint permite a un cuidador modificar su propio perfil profesional o a un administrador
     * actualizar el perfil de cualquier cuidador. La lógica de autorización es estricta: un usuario
     * solo puede afectar su propio perfil, mientras que un administrador tiene permisos globales.
     *
     * <p>Flujo del Proceso:</p>
     * <ul>
     * <li><b>Autorización:</b> Spring Security verifica si el usuario autenticado es el propietario del perfil (coincide con {@code userId}) o si tiene el rol 'ADMIN'.</li>
     * <li><b>Validación:</b> Los datos del cuerpo de la solicitud (DTO) son validados para asegurar su integridad.</li>
     * <li><b>Búsqueda:</b> El servicio localiza el perfil de cuidador existente asociado al {@code userId}.</li>
     * <li><b>Actualización:</b> Se aplican los cambios del DTO a la entidad persistida.</li>
     * <li><b>Respuesta:</b> Se devuelve el perfil de cuidador completo con los datos actualizados.</li>
     * </ul>
     *
     * @param userId El ID del usuario cuyo perfil de cuidador se va a actualizar.
     * @param sitterProfileDTO DTO que contiene los nuevos datos para el perfil.
     * @return Un {@link ResponseEntity} con el {@link SitterProfileDTO} actualizado y un estado HTTP 200 (OK).
     * @throws SitterProfileNotFoundException Si no se encuentra un perfil asociado al {@code userId}, manejado por el GlobalExceptionHandler.
     */
    @PutMapping("/{userId}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @Operation(
            summary = "Actualiza un perfil de cuidador existente",
            description = "Permite a un cuidador actualizar su propio perfil o a un administrador actualizar cualquier perfil. La autorización se gestiona para que un usuario solo pueda modificar sus propios datos.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil de cuidador actualizado exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SitterProfileDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "id": 123,
                      "userId": 45,
                      "bio": "Amante de los animales con más de 5 años de experiencia. Ahora también cuido reptiles!",
                      "hourlyRate": 18.00,
                      "servicingRadius": 15,
                      "profileImageUrl": "https://example.com/images/new_sitter_photo.jpg",
                      "verified": true,
                      "availableForBookings": true
                    }
                    """))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos proporcionados inválidos, como una tarifa negativa.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "status": 400,
                      "message": "Validation failed",
                      "timestamp": "2025-09-13T21:15:00Z",
                      "validationErrors": {
                        "hourlyRate": "La tarifa por hora debe ser mayor a cero."
                      }
                    }
                    """))),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere un token JWT válido.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "status": 401,
                      "message": "No se proporcionó un token de autenticación válido.",
                      "timestamp": "2025-09-13T21:15:00Z",
                      "validationErrors": null
                    }
                    """))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. El usuario autenticado no es el propietario del perfil ni un administrador.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "status": 403,
                      "message": "Acceso denegado. No tiene permisos para modificar este perfil.",
                      "timestamp": "2025-09-13T21:15:00Z",
                      "validationErrors": null
                    }
                    """))),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró el perfil de cuidador para el 'userId' proporcionado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "status": 404,
                      "message": "No se encontró un perfil de cuidador para el usuario con ID 999.",
                      "timestamp": "2025-09-13T21:15:00Z",
                      "validationErrors": null
                    }
                    """)))
    })
    public ResponseEntity<SitterProfileDTO> updateSitterProfile(
            @Parameter(description = "ID del usuario cuyo perfil se actualizará.", required = true) @PathVariable Long userId,
            @Valid @RequestBody SitterProfileDTO sitterProfileDTO) {

        log.info("Actualizando perfil de Sitter para userId={}", userId);
        SitterProfileDTO updated = sitterService.updateSitterProfile(userId, sitterProfileDTO);
        return ResponseEntity.ok(updated);
    }


    /**
     * Elimina el perfil de cuidador de un usuario específico.
     * <p>
     * Esta es una operación destructiva que elimina el perfil profesional de un cuidador. La lógica
     * de negocio en el servicio determinará si se trata de una eliminación física o lógica (desactivación).
     *
     * <p>Reglas de Autorización:</p>
     * <ul>
     * <li>Un <b>ADMIN</b> puede eliminar el perfil de cualquier cuidador.</li>
     * <li>Un <b>usuario regular</b> solo puede eliminar su propio perfil de cuidador.</li>
     * </ul>
     * La violación de estas reglas resultará en una respuesta HTTP 403 (Forbidden).
     *
     * @param userId El ID del usuario (`User`) cuyo perfil de cuidador será eliminado.
     * @return Un {@link ResponseEntity} con un objeto JSON de confirmación y un estado HTTP 200 (OK).
     * @throws SitterProfileNotFoundException Si no se encuentra un perfil asociado al {@code userId},
     * lo que resultará en una respuesta HTTP 404 (Not Found).
     */
    @DeleteMapping("/{userId}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @Operation(
            summary = "Elimina un perfil de cuidador",
            description = "Permite a un cuidador eliminar su propio perfil o a un administrador eliminar cualquier perfil. La operación devuelve un mensaje de confirmación.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil de cuidador eliminado exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class), // Usamos Map para un DTO genérico de respuesta
                            examples = @ExampleObject(value = """
                    {
                      "message": "El perfil de cuidador para el usuario con ID 45 ha sido eliminado exitosamente.",
                      "timestamp": "2025-09-13T21:45:10Z"
                    }
                    """))),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere un token JWT válido.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "status": 401,
                      "message": "No se proporcionó un token de autenticación válido.",
                      "timestamp": "2025-09-13T21:45:00Z",
                      "validationErrors": null
                    }
                    """))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. El usuario no es administrador ni el propietario del perfil.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "status": 403,
                      "message": "Acceso denegado. No tiene permisos para eliminar este perfil.",
                      "timestamp": "2025-09-13T21:45:00Z",
                      "validationErrors": null
                    }
                    """))),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró el perfil de cuidador para el 'userId' proporcionado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "status": 404,
                      "message": "No se encontró un perfil de cuidador para el usuario con ID 999.",
                      "timestamp": "2025-09-13T21:45:00Z",
                      "validationErrors": null
                    }
                    """)))
    })
    public ResponseEntity<Map<String, Object>> deleteSitterProfile(
            @Parameter(description = "ID del usuario cuyo perfil se eliminará.", required = true)
            @PathVariable Long userId) {
        log.warn("Solicitud de eliminación para el perfil de cuidador del usuario con ID: {}", userId);

        sitterService.deleteSitterProfile(userId);

        Map<String, Object> responseBody = Map.of(
                "message", "El perfil de cuidador para el usuario con ID " + userId + " ha sido eliminado exitosamente.",
                "timestamp", LocalDateTime.now()
        );

        log.info("Perfil de cuidador para el usuario con ID {} eliminado correctamente.", userId);
        return ResponseEntity.ok(responseBody);
    }
}
