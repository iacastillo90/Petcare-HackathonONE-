package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.GlobalException.DeleteResponseDTO;
import com.Petcare.Petcare.DTOs.GlobalException.ErrorResponseDTO;
import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceRequestDTO;
import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceResponseDTO;
import com.Petcare.Petcare.DTOs.SitterWorkExperience.SitterWorkExperienceSummaryDTO;
import com.Petcare.Petcare.Exception.Business.SitterProfileNotFoundException;
import com.Petcare.Petcare.Exception.Business.WorkExperienceConflictException;
import com.Petcare.Petcare.Exception.Business.WorkExperienceNotFoundException;
import com.Petcare.Petcare.Services.SitterWorkExperienceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/sitter-work-experience")
public class SitterWorkExperienceController {

    private final SitterWorkExperienceService workExperienceService;

    @Autowired
    public SitterWorkExperienceController(SitterWorkExperienceService workExperienceService) {
        this.workExperienceService = workExperienceService;
    }

    // ================= CREATE =================
    /**
     * Crea una nueva experiencia laboral para un perfil de cuidador.
     * <p>
     * Este endpoint permite a un cuidador (Sitter) o a un administrador añadir una nueva entrada
     * de experiencia laboral al perfil profesional de un cuidador. Esta información es crucial
     * para que los clientes puedan evaluar la trayectoria y fiabilidad del cuidador.
     * </p>
     * <b>Desglose del Proceso:</b>
     * <ul>
     * <li><b>Validación:</b> Se valida que los datos del DTO cumplan con las restricciones (ej. fechas no nulas, IDs válidos).</li>
     * <li><b>Búsqueda de Perfil:</b> El servicio localiza el {@link com.Petcare.Petcare.Models.SitterProfile} asociado mediante el {@code sitterProfileId}.</li>
     * <li><b>Verificación de Duplicados:</b> Se comprueba que no exista una experiencia idéntica para el mismo perfil.</li>
     * <li><b>Persistencia:</b> Se crea y guarda la nueva entidad {@code SitterWorkExperience} en la base de datos.</li>
     * <li><b>Respuesta:</b> Se devuelve la experiencia laboral recién creada, incluyendo su ID único generado.</li>
     * </ul>
     *
     * @param requestDTO El DTO que contiene los detalles de la experiencia laboral a crear. Debe ser un JSON válido.
     * @return Un {@link ResponseEntity} que contiene el {@link SitterWorkExperienceResponseDTO} completo del nuevo registro, con un estado HTTP 201 (Created).
     * @throws com.Petcare.Petcare.Exception.Business.SitterProfileNotFoundException Si el ID del perfil de cuidador proporcionado no corresponde a ningún perfil existente.
     * @throws com.Petcare.Petcare.Exception.Business.WorkExperienceConflictException Si ya existe una experiencia laboral idéntica (misma empresa, puesto y fecha de inicio) para el mismo perfil.
     */
    @PostMapping
    @PreAuthorize("hasRole('SITTER') or hasRole('ADMIN')")
    @Operation(
            summary = "Crea una nueva experiencia laboral para un cuidador",
            description = "Registra un nuevo puesto de trabajo en el historial de un perfil de cuidador. Requiere rol 'SITTER' o 'ADMIN'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Experiencia laboral creada exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SitterWorkExperienceResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "id": 1,
                                  "companyName": "Clínica Veterinaria Amigos Fieles",
                                  "jobTitle": "Asistente Veterinario",
                                  "responsibilities": "Asistencia en consultas, cuidado post-operatorio de mascotas y gestión de citas.",
                                  "startDate": "2022-01-15",
                                  "endDate": "2023-12-20"
                                }
                                """))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos proporcionados inválidos. El cuerpo de la solicitud no cumple con las validaciones.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 400,
                                  "message": "Validation failed",
                                  "timestamp": "2025-09-14T10:30:00Z",
                                  "validationErrors": {
                                    "sitterProfileId": "El ID del perfil de cuidador es obligatorio",
                                    "startDate": "La fecha de inicio es obligatoria"
                                  }
                                }
                                """))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere un token JWT válido en la cabecera 'Authorization'.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 401,
                                  "message": "No se proporcionó un token de autenticación válido.",
                                  "timestamp": "2025-09-14T10:30:00Z"
                                }
                                """))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. El usuario no tiene el rol requerido ('SITTER' o 'ADMIN').",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 403,
                                  "message": "Acceso denegado. Se requiere rol SITTER o ADMIN.",
                                  "timestamp": "2025-09-14T10:30:00Z"
                                }
                                """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso no encontrado. El 'sitterProfileId' proporcionado no corresponde a un perfil de cuidador existente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 404,
                                  "message": "No se encontró un perfil de cuidador para el ID: 999",
                                  "timestamp": "2025-09-14T10:30:00Z"
                                }
                                """))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto. Ya existe una experiencia laboral idéntica para este perfil.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 409,
                                  "message": "Conflicto: Ya existe una experiencia laboral con los mismos datos para este perfil.",
                                  "timestamp": "2025-09-14T10:35:00Z"
                                }
                                """))
            )
    })
    public ResponseEntity<SitterWorkExperienceResponseDTO> createWorkExperience(@Valid @RequestBody SitterWorkExperienceRequestDTO requestDTO) {
        SitterWorkExperienceResponseDTO response = workExperienceService.createWorkExperience(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ================= READ =================
    /**
     * Obtiene un listado con todas las experiencias laborales de un perfil de cuidador.
     * <p>
     * Este endpoint público permite a cualquier usuario (clientes, visitantes, etc.) consultar el
     * historial profesional de un cuidador específico. Devuelve una lista de resúmenes
     * de experiencia, optimizada para mostrar en el perfil público del cuidador.
     * </p>
     * <b>Desglose del Proceso:</b>
     * <ul>
     * <li><b>Búsqueda de Perfil:</b> Se localiza el {@link com.Petcare.Petcare.Models.SitterProfile} mediante el ID proporcionado.</li>
     * <li><b>Recuperación de Datos:</b> Se obtienen todas las entidades {@code SitterWorkExperience} asociadas a ese perfil.</li>
     * <li><b>Mapeo a DTO:</b> Cada entidad se convierte a un DTO de resumen ({@link SitterWorkExperienceSummaryDTO}) para exponer solo la información necesaria.</li>
     * <li><b>Respuesta:</b> Se devuelve la lista de experiencias. Si el perfil existe pero no tiene experiencias, se devuelve una lista vacía.</li>
     * </ul>
     *
     * @param sitterProfileId El ID del <b>perfil del cuidador</b> (no el ID del usuario).
     * @return Un {@link ResponseEntity} que contiene una lista de {@link SitterWorkExperienceSummaryDTO}.
     * @throws com.Petcare.Petcare.Exception.Business.SitterProfileNotFoundException Si no se encuentra ningún perfil de cuidador con el ID proporcionado.
     */
    @GetMapping("/sitter/{sitterProfileId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Obtiene las experiencias laborales de un cuidador",
            description = "Devuelve una lista pública con el historial laboral de un cuidador, identificado por el ID de su perfil."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de experiencias laborales obtenida exitosamente.",
                    content = @Content(mediaType = "application/json",
                            array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = SitterWorkExperienceSummaryDTO.class)),
                            examples = {
                                    @ExampleObject(
                                            name = "Perfil con Experiencias",
                                            value = """
                                            [
                                              {
                                                "id": 1,
                                                "companyName": "Clínica Veterinaria Amigos Fieles",
                                                "jobTitle": "Asistente Veterinario",
                                                "startDate": "2022-01-15",
                                                "endDate": "2023-12-20"
                                              },
                                              {
                                                "id": 2,
                                                "companyName": "Refugio de Animales San Roque",
                                                "jobTitle": "Voluntario de Cuidados",
                                                "startDate": "2021-06-01",
                                                "endDate": "2021-12-31"
                                              }
                                            ]
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Perfil sin Experiencias",
                                            summary = "Respuesta para un perfil válido que aún no ha añadido experiencia",
                                            value = "[]"
                                    )
                            })
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso no encontrado. El 'sitterProfileId' proporcionado no corresponde a un perfil de cuidador existente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 404,
                                  "message": "No se encontró un perfil de cuidador para el ID: 999",
                                  "timestamp": "2025-09-14T11:30:00Z"
                                }
                                """))
            )
    })
    public ResponseEntity<List<SitterWorkExperienceSummaryDTO>> getWorkExperiencesBySitter(
            @Parameter(description = "ID del perfil del cuidador a consultar.", required = true, example = "1")
            @PathVariable Long sitterProfileId) {
        List<SitterWorkExperienceSummaryDTO> experiences = workExperienceService.getWorkExperiencesBySitterProfileId(sitterProfileId);
        return ResponseEntity.ok(experiences);
    }

    /**
     * Obtiene los detalles completos de una experiencia laboral específica por su ID.
     * <p>
     * Este endpoint público permite a cualquier usuario consultar los detalles de una entrada
     * específica en el historial laboral de un cuidador, como las responsabilidades del puesto.
     * Es útil para obtener más información de la que se muestra en la vista de resumen del perfil.
     * </p>
     * <b>Desglose del Proceso:</b>
     * <ul>
     * <li><b>Búsqueda:</b> Se busca la entidad {@code SitterWorkExperience} por su clave primaria (ID).</li>
     * <li><b>Mapeo a DTO:</b> Si se encuentra, la entidad se convierte al DTO de respuesta detallado ({@link SitterWorkExperienceResponseDTO}).</li>
     * <li><b>Respuesta:</b> Se devuelve el DTO con los detalles completos. Si no se encuentra, se lanza una excepción que resulta en un error 404.</li>
     * </ul>
     *
     * @param id El ID único de la entrada de experiencia laboral que se desea consultar.
     * @return Un {@link ResponseEntity} que contiene el {@link SitterWorkExperienceResponseDTO} con todos los detalles de la experiencia.
     * @throws com.Petcare.Petcare.Exception.Business.WorkExperienceNotFoundException Si no se encuentra ninguna experiencia laboral con el ID proporcionado.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Obtiene una experiencia laboral por su ID",
            description = "Devuelve los detalles completos de una única entrada de experiencia laboral, incluyendo el campo de responsabilidades."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Detalles de la experiencia laboral obtenidos exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SitterWorkExperienceResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "id": 1,
                                  "companyName": "Clínica Veterinaria Amigos Fieles",
                                  "jobTitle": "Asistente Veterinario",
                                  "responsibilities": "Asistencia en consultas, cuidado post-operatorio de mascotas y gestión de citas.",
                                  "startDate": "2022-01-15",
                                  "endDate": "2023-12-20"
                                }
                                """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso no encontrado. No existe una experiencia laboral con el ID proporcionado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 404,
                                  "message": "Experiencia laboral no encontrada con el ID: 999",
                                  "timestamp": "2025-09-14T12:00:00Z"
                                }
                                """))
            )
    })
    public ResponseEntity<SitterWorkExperienceResponseDTO> getWorkExperienceById(
            @Parameter(description = "ID único de la experiencia laboral a consultar.", required = true, example = "1")
            @PathVariable Long id) {
        SitterWorkExperienceResponseDTO response = workExperienceService.getWorkExperienceById(id);
        return ResponseEntity.ok(response);
    }

    // ================= UPDATE =================
    /**
     * Actualiza una experiencia laboral existente.
     * <p>
     * Permite a un cuidador (Sitter) modificar los detalles de una de sus experiencias laborales
     * previas, o a un administrador corregir la información de cualquier perfil.
     * La autorización se valida para asegurar que un cuidador solo pueda editar sus propias entradas.
     * </p>
     * <b>Desglose del Proceso:</b>
     * <ul>
     * <li><b>Autenticación y Autorización:</b> Se verifica que el usuario esté autenticado y tenga rol 'SITTER' o 'ADMIN'.</li>
     * <li><b>Búsqueda:</b> Se localiza la experiencia laboral existente por su ID único.</li>
     * <li><b>Validación de Propiedad (en la capa de servicio):</b> Se confirma que el cuidador autenticado es el dueño de la experiencia que intenta modificar.</li>
     * <li><b>Aplicación de Cambios:</b> Los campos de la entidad se actualizan con los datos del DTO de la solicitud.</li>
     * <li><b>Persistencia:</b> Se guardan los cambios en la base de datos.</li>
     * <li><b>Respuesta:</b> Se devuelve la experiencia laboral con la información actualizada.</li>
     * </ul>
     *
     * @param id El ID único de la experiencia laboral que se va a actualizar.
     * @param requestDTO El DTO que contiene los nuevos datos para la experiencia laboral.
     * @return Un {@link ResponseEntity} que contiene el {@link SitterWorkExperienceResponseDTO} con los datos actualizados, y un estado HTTP 200 (OK).
     * @throws WorkExperienceNotFoundException Si no se encuentra ninguna experiencia laboral con el ID proporcionado.
     * @throws AccessDeniedException Si el usuario no es el propietario de la experiencia ni un administrador.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SITTER') or hasRole('ADMIN')")
    @Operation(
            summary = "Actualiza una experiencia laboral existente",
            description = "Permite a un cuidador modificar los detalles de su propio historial laboral, o a un administrador modificar cualquier entrada. Requiere rol 'SITTER' o 'ADMIN'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Experiencia laboral actualizada exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SitterWorkExperienceResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "id": 1,
                                  "companyName": "Clínica Veterinaria 'Amigos Leales'",
                                  "jobTitle": "Asistente Técnico Veterinario (ATV)",
                                  "responsibilities": "Asistencia en consultas, cuidado post-operatorio de mascotas, gestión de citas y atención al cliente.",
                                  "startDate": "2022-01-15",
                                  "endDate": "2024-01-20"
                                }
                                """))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos proporcionados inválidos. El cuerpo de la solicitud no cumple con las validaciones.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 400,
                                  "message": "Validation failed",
                                  "timestamp": "2025-09-14T12:15:00Z",
                                  "validationErrors": {
                                    "startDate": "La fecha de inicio es obligatoria"
                                  }
                                }
                                """))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. El usuario autenticado no es el propietario de la experiencia laboral ni un administrador.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 403,
                                  "message": "Acceso denegado. No tiene permisos para modificar este recurso.",
                                  "timestamp": "2025-09-14T12:15:00Z"
                                }
                                """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso no encontrado. No existe una experiencia laboral con el ID proporcionado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 404,
                                  "message": "Experiencia laboral no encontrada con el ID: 999",
                                  "timestamp": "2025-09-14T12:15:00Z"
                                }
                                """))
            )
    })
    public ResponseEntity<SitterWorkExperienceResponseDTO> updateWorkExperience(
            @Parameter(description = "ID único de la experiencia laboral a actualizar.", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody SitterWorkExperienceRequestDTO requestDTO) {
        SitterWorkExperienceResponseDTO updated = workExperienceService.updateWorkExperience(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    // ================= DELETE =================
    /**
     * Elimina permanentemente una experiencia laboral por su ID.
     * <p>
     * Este endpoint permite a un cuidador (Sitter) eliminar una entrada de su historial laboral,
     * o a un administrador eliminar cualquier entrada del sistema. La operación es destructiva
     * y elimina el registro de la base de datos de forma irreversible.
     * </p>
     * <b>Desglose del Proceso de Seguridad y Negocio:</b>
     * <ul>
     * <li><b>Autenticación y Autorización:</b> Se verifica que el usuario esté autenticado y posea el rol 'SITTER' o 'ADMIN'.</li>
     * <li><b>Búsqueda:</b> El servicio localiza la experiencia laboral por su ID. Si no la encuentra, se lanza una excepción.</li>
     * <li><b>Validación de Propiedad (en la capa de servicio):</b> Se confirma que el usuario autenticado es el propietario de la experiencia que intenta eliminar, o es un administrador.</li>
     * <li><b>Eliminación:</b> Se ejecuta la operación de borrado en la base de datos.</li>
     * <li><b>Respuesta de Confirmación:</b> Se devuelve un mensaje explícito indicando que la operación fue exitosa.</li>
     * </ul>
     *
     * @param id El ID único de la experiencia laboral que se va a eliminar.
     * @return Un {@link ResponseEntity} que contiene un {@link com.Petcare.Petcare.DTOs.GlobalException.DeleteResponseDTO} con un mensaje de confirmación y estado HTTP 200 (OK).
     * @throws com.Petcare.Petcare.Exception.Business.WorkExperienceNotFoundException Si no se encuentra ninguna experiencia laboral con el ID proporcionado.
     * @throws org.springframework.security.access.AccessDeniedException Si el usuario no es el propietario de la experiencia ni un administrador.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SITTER') or hasRole('ADMIN')")
    @Operation(
            summary = "Elimina una experiencia laboral por su ID",
            description = "Elimina permanentemente una entrada del historial laboral de un cuidador. Requiere que el solicitante sea el propietario del perfil o un administrador."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Experiencia laboral eliminada exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeleteResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "message": "La experiencia laboral con ID 1 ha sido eliminada exitosamente."
                                }
                                """))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere un token JWT válido.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 401,
                                  "message": "No se proporcionó un token de autenticación válido.",
                                  "timestamp": "2025-09-14T12:30:00Z"
                                }
                                """))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. El usuario no es el propietario de la experiencia laboral ni un administrador.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 403,
                                  "message": "Acceso denegado. No tiene permisos para eliminar este recurso.",
                                  "timestamp": "2025-09-14T12:30:00Z"
                                }
                                """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso no encontrado. No existe una experiencia laboral con el ID proporcionado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 404,
                                  "message": "Experiencia laboral no encontrada con el ID: 999",
                                  "timestamp": "2025-09-14T12:30:00Z"
                                }
                                """))
            )
    })
    public ResponseEntity<DeleteResponseDTO> deleteWorkExperience(
            @Parameter(description = "ID único de la experiencia laboral a eliminar.", required = true, example = "1")
            @PathVariable Long id) {
        workExperienceService.deleteWorkExperience(id);
        String message = String.format("La experiencia laboral con ID %d ha sido eliminada exitosamente.", id);
        return ResponseEntity.ok(new DeleteResponseDTO(message));
    }

}
