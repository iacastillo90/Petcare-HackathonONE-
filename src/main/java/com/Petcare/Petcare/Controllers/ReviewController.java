package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.GlobalException.DeleteResponseDTO;
import com.Petcare.Petcare.DTOs.GlobalException.ErrorResponseDTO;
import com.Petcare.Petcare.DTOs.Review.ReviewDTO;
import com.Petcare.Petcare.DTOs.Review.ReviewResponse;
import com.Petcare.Petcare.Exception.Business.*;
import com.Petcare.Petcare.Services.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar reseñas.
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Crea una nueva reseña para un servicio de cuidado de mascotas completado.
     *
     * <p>
     * Este endpoint permite a un usuario cliente publicar una calificación y un comentario
     * sobre un servicio que ha recibido para una de sus mascotas. La creación de una reseña
     * es un paso crucial en el ciclo de vida de una reserva, ya que impacta directamente
     * en la calificación promedio del cuidador (Sitter).
     * </p>
     *
     * <b>Desglose del Proceso de Negocio:</b>
     * <ul>
     * <li><b>Validación de Entrada:</b> Se verifica que el DTO de entrada contenga un `userId`,
     * `petId` y un `rating` válido (entre 1 y 5).</li>
     * <li><b>Verificación de Entidades:</b> El servicio valida que tanto el usuario como la
     * mascota especificados por sus IDs existan en el sistema.</li>
     * <li><b>Validación de Legitimidad:</b> Se confirma que existe una reserva completada
     * que vincule al usuario (`userId`) con el cuidador de la mascota (`petId`). Un
     * usuario no puede dejar una reseña si no ha completado un servicio para esa mascota.</li>
     * <li><b>Prevención de Duplicados:</b> El sistema verifica que el usuario no haya dejado
     * previamente una reseña para la misma reserva.</li>
     * <li><b>Persistencia:</b> Se guarda la nueva reseña en la base de datos.</li>
     * <li><b>Efecto Secundario:</b> Tras guardar la reseña, se recalcula y actualiza la
     * calificación promedio (`averageRating`) del perfil del cuidador afectado.</li>
     * </ul>
     *
     * @param reviewDTO El DTO que contiene los datos de la reseña a crear.
     * @return Un {@link ResponseEntity} con el DTO {@link ReviewResponse} de la reseña
     * recién creada, incluyendo su ID y timestamps, con un estado HTTP 201 (Created).
     * @throws UserNotFoundException Si el `userId` proporcionado no corresponde a un usuario existente.
     * @throws PetNotFoundException Si el `petId` proporcionado no corresponde a una mascota existente.
     * @throws InvalidReviewException Si no existe una reserva completada que justifique la reseña.
     * @throws ReviewAlreadyExistsException Si el usuario ya ha enviado una reseña para esta reserva.
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    @Operation(
            summary = "Crear una nueva reseña",
            description = "Permite a un cliente publicar una calificación y un comentario sobre un servicio completado para una de sus mascotas. La creación exitosa de una reseña recalcula la calificación promedio del cuidador.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Reseña creada exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReviewResponse.class),
                            examples = @ExampleObject(value = """
                {
                  "id": 1,
                  "userId": 15,
                  "petId": 10,
                  "rating": 5,
                  "comment": "¡El cuidador fue excelente! Mi perro volvió muy feliz y cansado. Definitivamente lo recomiendo.",
                  "createdAt": "2025-09-14T21:30:00Z",
                  "updatedAt": "2025-09-14T21:30:00Z"
                }
                """))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos. Ocurre si faltan campos obligatorios o no cumplen con el formato requerido (ej. rating fuera de 1-5).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 400,
                  "message": "Validation failed",
                  "timestamp": "2025-09-14T21:28:15Z",
                  "validationErrors": {
                    "rating": "El rating mínimo es 1",
                    "userId": "El ID del usuario es obligatorio"
                  }
                }
                """))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. El token JWT no fue proporcionado o es inválido.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 401,
                  "message": "Autenticación requerida. Por favor, incluya un token válido.",
                  "timestamp": "2025-09-14T21:27:00Z",
                  "validationErrors": null
                }
                """))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. El usuario no tiene el rol 'CLIENT' o 'ADMIN'.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 403,
                  "message": "Acceso Denegado: No tiene permiso para realizar esta acción.",
                  "timestamp": "2025-09-14T21:27:30Z",
                  "validationErrors": null
                }
                """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso no encontrado. Ocurre si el 'userId' o 'petId' proporcionados no existen en el sistema.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 404,
                  "message": "No se encontró la mascota con ID: 999",
                  "timestamp": "2025-09-14T21:29:05Z",
                  "validationErrors": null
                }
                """))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto de negocio. Ocurre si el usuario ya ha enviado una reseña para este servicio/mascota específico.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 409,
                  "message": "Conflicto: Ya ha enviado una reseña para este servicio.",
                  "timestamp": "2025-09-14T21:31:10Z",
                  "validationErrors": null
                }
                """))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Entidad no procesable. Ocurre cuando los datos son sintácticamente correctos, pero violan una regla de negocio, como intentar dejar una reseña sin un servicio completado previo.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 422,
                  "message": "No se puede crear la reseña: No existe una reserva completada que vincule al usuario 15 con la mascota 10.",
                  "timestamp": "2025-09-14T21:32:00Z",
                  "validationErrors": null
                }
                """))
            )
    })
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewDTO reviewDTO) {
        ReviewResponse response = reviewService.createReview(reviewDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Obtiene todas las reseñas publicadas para una mascota específica.
     * <p>
     * Este endpoint es de acceso público para cualquier usuario autenticado en la plataforma.
     * Su propósito es permitir a los clientes consultar el historial de calificaciones y comentarios
     * asociados a una mascota, lo cual es útil para evaluar la calidad de los servicios
     * que ha recibido y, por extensión, la reputación de los cuidadores que la han atendido.
     * </p>
     * <b>Desglose del Proceso:</b>
     * <ul>
     * <li><b>Validación de Existencia:</b> El servicio primero verifica que el {@code petId}
     * proporcionado corresponda a una mascota registrada en el sistema.</li>
     * <li><b>Recuperación de Datos:</b> Se consultan todas las entidades {@code Review}
     * asociadas al ID de la mascota.</li>
     * <li><b>Mapeo y Respuesta:</b> Las entidades se transforman en DTOs {@link ReviewResponse}
     * y se devuelven en una lista. Si la mascota no tiene reseñas, se retorna una lista vacía.</li>
     * </ul>
     *
     * @param petId El ID único de la mascota cuyas reseñas se desean obtener.
     * @return Un {@link ResponseEntity} que contiene una lista de {@code ReviewResponse}.
     * La lista estará vacía si la mascota no tiene reseñas, pero nunca será nula.
     * @throws PetNotFoundException Si el ID de la mascota no corresponde a ninguna mascota existente.
     */
    @GetMapping("/pet/{petId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Obtener reseñas por ID de mascota",
            description = "Recupera una lista pública de todas las reseñas y calificaciones asociadas a una mascota específica. Requiere que el usuario esté autenticado para acceder a la información.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de reseñas obtenida exitosamente. La lista puede estar vacía si la mascota no tiene reseñas.",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ReviewResponse.class)),
                            examples = {
                                    @ExampleObject(
                                            name = "Mascota con Reseñas",
                                            value = """
                    [
                      {
                        "id": 1,
                        "userId": 15,
                        "petId": 10,
                        "rating": 5,
                        "comment": "¡El cuidador fue excelente! Mi perro volvió muy feliz.",
                        "createdAt": "2025-09-14T21:30:00Z",
                        "updatedAt": "2025-09-14T21:30:00Z"
                      },
                      {
                        "id": 2,
                        "userId": 22,
                        "petId": 10,
                        "rating": 4,
                        "comment": "Buen servicio, aunque llegó un poco tarde.",
                        "createdAt": "2025-08-10T15:00:00Z",
                        "updatedAt": "2025-08-10T15:00:00Z"
                      }
                    ]
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Mascota sin Reseñas",
                                            summary = "Respuesta para una mascota válida que aún no ha recibido reseñas.",
                                            value = "[]"
                                    )
                            })
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. El token JWT no fue proporcionado o es inválido.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 401,
                  "message": "Autenticación requerida. Por favor, incluya un token válido.",
                  "timestamp": "2025-09-14T22:05:00Z",
                  "validationErrors": null
                }
                """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mascota no encontrada. Ocurre si el 'petId' proporcionado no corresponde a ninguna mascota en el sistema.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 404,
                  "message": "No se encontró la mascota con ID: 999",
                  "timestamp": "2025-09-14T22:06:15Z",
                  "validationErrors": null
                }
                """))
            )
    })
    public ResponseEntity<List<ReviewResponse>> getReviewsByPet(
            @Parameter(description = "ID único de la mascota a consultar.", required = true, example = "10")
            @PathVariable Long petId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByPetId(petId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Obtiene todas las reseñas escritas por un usuario específico.
     * <p>
     * Este endpoint permite a un usuario consultar su propio historial de reseñas o a un administrador
     * revisar la actividad de cualquier usuario con fines de moderación o soporte. El acceso a las
     * reseñas de otros usuarios está restringido para proteger la privacidad.
     * </p>
     * <b>Desglose del Proceso:</b>
     * <ul>
     * <li><b>Autorización:</b> Se verifica que el solicitante sea un administrador o que el `userId`
     * de la ruta coincida con el ID del usuario autenticado.</li>
     * <li><b>Validación de Usuario:</b> El servicio confirma que el `userId` corresponde a un usuario existente.</li>
     * <li><b>Recuperación de Datos:</b> Se obtienen todas las entidades {@code Review} cuyo autor
     * coincida con el `userId`.</li>
     * <li><b>Respuesta:</b> Se devuelve una lista de DTOs {@link ReviewResponse}. Si el usuario existe
     * pero no ha escrito reseñas, se devuelve una lista vacía.</li>
     * </ul>
     *
     * @param userId El ID único del usuario cuyas reseñas se desean obtener.
     * @return Un {@link ResponseEntity} que contiene una lista de {@code ReviewResponse}.
     * @throws UserNotFoundException Si el ID del usuario no corresponde a ningún usuario existente.
     * @throws AccessDeniedException Si un usuario no administrador intenta acceder a las reseñas de otro usuario.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @Operation(
            summary = "Obtener reseñas por ID de autor (usuario)",
            description = "Recupera una lista de todas las reseñas escritas por un usuario específico. El acceso está restringido: solo los administradores pueden ver las reseñas de otros, mientras que los usuarios regulares solo pueden ver las suyas.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de reseñas del usuario obtenida exitosamente. La lista puede estar vacía.",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ReviewResponse.class)),
                            examples = {
                                    @ExampleObject(
                                            name = "Usuario con Reseñas",
                                            value = """
                    [
                      {
                        "id": 1,
                        "userId": 15,
                        "petId": 10,
                        "rating": 5,
                        "comment": "¡El cuidador fue excelente!",
                        "createdAt": "2025-09-14T21:30:00Z",
                        "updatedAt": "2025-09-14T21:30:00Z"
                      },
                      {
                        "id": 8,
                        "userId": 15,
                        "petId": 12,
                        "rating": 4,
                        "comment": "Buen servicio en general, muy puntual.",
                        "createdAt": "2025-07-22T11:00:00Z",
                        "updatedAt": "2025-07-22T11:00:00Z"
                      }
                    ]
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Usuario sin Reseñas",
                                            summary = "Respuesta para un usuario válido que aún no ha escrito reseñas.",
                                            value = "[]"
                                    )
                            })
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. El token JWT no fue proporcionado o es inválido.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 401,
                  "message": "Autenticación requerida. Por favor, incluya un token válido.",
                  "timestamp": "2025-09-14T22:10:00Z",
                  "validationErrors": null
                }
                """))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Ocurre si un usuario intenta ver las reseñas de otro sin ser administrador.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 403,
                  "message": "Acceso Denegado: No tiene permiso para realizar esta acción.",
                  "timestamp": "2025-09-14T22:11:00Z",
                  "validationErrors": null
                }
                """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado. Ocurre si el 'userId' no corresponde a ningún usuario en el sistema.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 404,
                  "message": "Usuario no encontrado con el ID 999",
                  "timestamp": "2025-09-14T22:12:00Z",
                  "validationErrors": null
                }
                """))
            )
    })
    public ResponseEntity<List<ReviewResponse>> getReviewsByUser(
            @Parameter(description = "ID único del usuario autor de las reseñas.", required = true, example = "15")
            @PathVariable Long userId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Elimina una reseña específica por su ID.
     * <p>
     * Esta es una operación destructiva que elimina permanentemente un registro de reseña.
     * El acceso está restringido al autor original de la reseña o a un administrador
     * del sistema con fines de moderación.
     * </p>
     * <b>Desglose del Proceso de Negocio:</b>
     * <ul>
     * <li><b>Autorización:</b> Se verifica que el usuario autenticado sea el autor de la
     * reseña o tenga rol de 'ADMIN'.</li>
     * <li><b>Validación de Existencia:</b> El servicio confirma que la reseña con el ID
     * proporcionado existe antes de intentar eliminarla.</li>
     * <li><b>Eliminación:</b> Se elimina la entidad {@code Review} de la base de datos.</li>
     * <li><b>Efecto Secundario:</b> La eliminación de la reseña dispara un recálculo de la
     * calificación promedio (`averageRating`) del cuidador asociado.</li>
     * <li><b>Respuesta:</b> Se devuelve un mensaje de confirmación estructurado en un DTO.</li>
     * </ul>
     *
     * @param id El ID único de la reseña que se va a eliminar.
     * @return Un {@link ResponseEntity} que contiene un DTO {@link DeleteResponseDTO}
     * con un mensaje de confirmación de la operación.
     * @throws ReviewNotFoundException Si el ID no corresponde a ninguna reseña existente.
     * @throws AccessDeniedException Si un usuario intenta eliminar una reseña que no le
     * pertenece y no es administrador.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #Id == authentication.principal.id )")
    @Operation(
            summary = "Eliminar una reseña por ID",
            description = "Elimina permanentemente una reseña del sistema. Esta acción solo puede ser realizada por el autor original de la reseña o por un administrador.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reseña eliminada exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeleteResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "message": "La reseña con ID 5 ha sido eliminada exitosamente."
                }
                """))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. El token JWT no fue proporcionado o es inválido.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 401,
                  "message": "Autenticación requerida. Por favor, incluya un token válido.",
                  "timestamp": "2025-09-14T22:20:00Z",
                  "validationErrors": null
                }
                """))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Ocurre si un usuario intenta eliminar una reseña que no le pertenece y no es administrador.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 403,
                  "message": "Acceso Denegado: No tiene permiso para eliminar este recurso.",
                  "timestamp": "2025-09-14T22:21:00Z",
                  "validationErrors": null
                }
                """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reseña no encontrada. Ocurre si el 'id' no corresponde a ninguna reseña en el sistema.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 404,
                  "message": "No se encontró la reseña con ID: 999",
                  "timestamp": "2025-09-14T22:22:00Z",
                  "validationErrors": null
                }
                """))
            )
    })
    public ResponseEntity<DeleteResponseDTO> deleteReview(
            @Parameter(description = "ID único de la reseña a eliminar.", required = true, example = "5")
            @PathVariable Long id) {
        reviewService.deleteReview(id);
        String message = String.format("La reseña con ID %d ha sido eliminada exitosamente.", id);
        return ResponseEntity.ok(new DeleteResponseDTO(message));
    }
}
