package com.Petcare.Petcare.Controllers;

import com.Petcare.Petcare.DTOs.GlobalException.DeleteResponseDTO;
import com.Petcare.Petcare.DTOs.GlobalException.ErrorResponseDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.CreateServiceOfferingDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.ServiceOfferingDTO;
import com.Petcare.Petcare.DTOs.ServiceOffering.UpdateServiceOfferingDTO;
import com.Petcare.Petcare.Exception.Business.ServiceOfferingConflictException;
import com.Petcare.Petcare.Exception.Business.ServiceOfferingNotFoundException;
import com.Petcare.Petcare.Exception.Business.UserNotFoundException;
import com.Petcare.Petcare.Services.Implement.ServiceOfferingServiceImplement;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de ofertas de servicios de cuidado de mascotas.
 * 
 * <p>Este controlador maneja todas las operaciones CRUD relacionadas con los servicios
 * que ofrecen los cuidadores (sitters) en la plataforma Petcare. Incluye funcionalidades
 * para crear, consultar, actualizar y eliminar servicios como paseos, cuidado diurno,
 * y otros servicios especializados.</p>
 * 
 * <p><strong>Endpoints principales:</strong></p>
 * <ul>
 *   <li>GET /api/services - Lista todos los servicios disponibles</li>
 *   <li>POST /api/services/create/{id} - Crea un nuevo servicio para un sitter</li>
 *   <li>GET /api/services/{id} - Obtiene un servicio específico</li>
 *   <li>GET /api/services/all/{id} - Lista servicios de un sitter específico</li>
 *   <li>PATCH /api/services/{id} - Actualiza un servicio existente</li>
 *   <li>DELETE /api/services/{id} - Elimina un servicio</li>
 * </ul>
 * 
 * <p><strong>Seguridad:</strong></p>
 * <p>La mayoría de endpoints requieren autenticación y rol SITTER, excepto la consulta
 * general de servicios que está disponible públicamente para que los clientes puedan
 * buscar servicios disponibles.</p>
 * 
 * @author Equipo Petcare
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceOfferingController {
    private final ServiceOfferingServiceImplement serviceOfferingServiceImplement;

    /**
     * Verifica el estado operativo del controlador de ofertas de servicio.
     * <p>
     * Este endpoint de tipo "health check" tiene como único propósito confirmar que el controlador
     * {@code ServiceOfferingController} está activo y respondiendo correctamente a las solicitudes HTTP.
     * No interactúa con la base de datos ni con ninguna otra capa de servicio, por lo que su respuesta
     * garantiza únicamente la disponibilidad de la capa web del controlador.
     * </p>
     * Es ideal para ser utilizado en:
     * <ul>
     * <li>Pruebas de monitoreo automatizado (smoke tests).</li>
     * <li>Verificaciones de despliegue para confirmar que el servicio se ha iniciado.</li>
     * <li>Herramientas de desarrollo para una comprobación de conectividad inicial.</li>
     * </ul>
     *
     * @return Un {@link ResponseEntity} con un objeto JSON estructurado que confirma el estado
     * operativo del controlador, incluyendo un mensaje de estado y un timestamp.
     */
    @Operation(
            summary = "Verificar Estado del Controlador",
            description = "Endpoint de health check simple para confirmar que el ServiceOfferingController está activo y respondiendo a las solicitudes. No requiere autenticación y es ideal para usarse en pruebas de monitoreo, smoke tests o para que los desarrolladores verifiquen la conectividad inicial."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "El controlador está operativo y respondiendo correctamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Respuesta de Éxito",
                                    summary = "Ejemplo de respuesta cuando el controlador está activo",
                                    value = """
                        {
                          "status": "UP",
                          "message": "ServiceOfferingController está operativo.",
                          "timestamp": "2025-09-14T10:30:00.123Z"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno inesperado en el servidor. Ocurre si hay un problema de configuración fundamental que impide que el controlador procese la solicitud.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.Petcare.Petcare.DTOs.GlobalException.ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error Interno del Servidor",
                                    summary = "Ejemplo de respuesta para un error 500",
                                    value = """
                        {
                          "status": 500,
                          "message": "Ocurrió un error interno inesperado. Por favor, contacte a soporte.",
                          "timestamp": "2025-09-14T10:32:15.987Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            )
    })
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "ServiceOfferingController está operativo.");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene una lista con todas las ofertas de servicios activas en la plataforma.
     * <p>
     * Este endpoint está diseñado para ser consumido públicamente por los clientes que buscan
     * explorar los diferentes tipos de servicios que los cuidadores ofrecen en Petcare.
     * La respuesta es una lista de todos los servicios disponibles, permitiendo al frontend
     * construir un catálogo completo o un menú de búsqueda.
     * </p>
     * <b>Flujo del Proceso:</b>
     * <ul>
     * <li>El controlador invoca al servicio para recuperar todas las entidades {@code ServiceOffering}.</li>
     * <li>El servicio transforma cada entidad a su correspondiente {@link ServiceOfferingDTO}.</li>
     * <li>Si se encuentran servicios, se devuelve la lista con un estado HTTP 200 OK.</li>
     * <li>Si no hay ningún servicio registrado en la plataforma, se devuelve un estado HTTP 204 No Content para indicar explícitamente la ausencia de datos.</li>
     * </ul>
     *
     * @return Un {@link ResponseEntity} que contiene una lista de {@link ServiceOfferingDTO}.
     * Puede devolver un 200 OK con la lista, o un 204 No Content si no hay servicios.
     * @see ServiceOfferingServiceImplement#getAllServices()
     */
    @Operation(
            summary = "Listar Todas las Ofertas de Servicio",
            description = "Recupera una lista completa de todas las ofertas de servicio activas en la plataforma. Este endpoint es público y está destinado a que los clientes puedan explorar los tipos de cuidados disponibles (paseos, guardería, etc.). No requiere autenticación."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de servicios obtenida exitosamente.",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ServiceOfferingDTO.class)),
                            examples = @ExampleObject(
                                    name = "Respuesta Exitosa con Servicios",
                                    summary = "Ejemplo de una lista con varias ofertas de servicio",
                                    value = """
                        [
                          {
                            "id": 1,
                            "sitterId": 15,
                            "serviceType": "WALKING",
                            "name": "Paseo Energético de 60 Minutos",
                            "description": "Un paseo vigoroso de una hora por parques locales para perros con mucha energía.",
                            "price": 20.00,
                            "durationInMinutes": 60,
                            "isActive": true,
                            "createdAt": "2025-09-10T09:00:00Z"
                          },
                          {
                            "id": 2,
                            "sitterId": 22,
                            "serviceType": "DAYCARE",
                            "name": "Guardería de Día Completo (8 horas)",
                            "description": "Cuidado de día completo en un hogar seguro y divertido, con juegos y socialización.",
                            "price": 50.00,
                            "durationInMinutes": 480,
                            "isActive": true,
                            "createdAt": "2025-09-11T11:30:00Z"
                          }
                        ]
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No se encontraron ofertas de servicio. La búsqueda fue exitosa, pero no hay ningún servicio registrado en la plataforma en este momento.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor. Ocurrió un problema inesperado al intentar recuperar la lista de servicios, posiblemente debido a un problema de conexión con la base de datos.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error del Servidor",
                                    summary = "Ejemplo de respuesta para un error 500",
                                    value = """
                        {
                          "status": 500,
                          "message": "Ocurrió un error inesperado al procesar la solicitud.",
                          "timestamp": "2025-09-14T11:05:00.987Z",
                          "validationErrors": null
                        }
                        """
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<ServiceOfferingDTO>> getAllServices() {
        List<ServiceOfferingDTO> services = serviceOfferingServiceImplement.getAllServices();
        if (services.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(services);
    }

    /**
     * Crea una nueva oferta de servicio para un cuidador específico.
     * <p>
     * Este endpoint permite a un cuidador (Sitter) añadir un nuevo servicio a su catálogo profesional,
     * como "Paseo de 30 minutos" o "Cuidado diurno". La operación está restringida: un cuidador
     * solo puede crear servicios para sí mismo, mientras que un administrador puede crearlos para cualquier cuidador.
     * </p>
     * <b>Desglose del Proceso:</b>
     * <ul>
     * <li><b>Autorización:</b> Spring Security verifica que el solicitante sea un 'ADMIN' o que el ID del path coincida con el ID del cuidador autenticado.</li>
     * <li><b>Validación de Entrada:</b> Se validan las restricciones del DTO (ej. nombre no vacío, precio positivo, duración mínima).</li>
     * <li><b>Búsqueda de Usuario:</b> El servicio localiza al usuario (cuidador) a través del ID proporcionado en la URL.</li>
     * <li><b>Verificación de Duplicados:</b> Se asegura que el cuidador no tenga ya un servicio con el mismo nombre para evitar confusiones.</li>
     * <li><b>Persistencia:</b> Se crea y guarda la nueva entidad {@code ServiceOffering} en la base de datos.</li>
     * <li><b>Respuesta:</b> Se devuelve el DTO del servicio recién creado, incluyendo su ID único, con un estado HTTP 201.</li>
     * </ul>
     *
     * @param createServiceOfferingDTO DTO con los detalles del servicio a crear (tipo, nombre, precio, duración, etc.).
     * @param id El ID del <strong>usuario</strong> (User) con rol SITTER al cual se le asociará el nuevo servicio.
     * @return Un {@link ResponseEntity} con el {@link ServiceOfferingDTO} del servicio recién creado y un estado HTTP 201 (Created).
     * @throws UserNotFoundException Si el ID del cuidador proporcionado no corresponde a ningún usuario existente (resulta en HTTP 404).
     * @throws ServiceOfferingConflictException Si el cuidador ya tiene un servicio con el mismo nombre (resulta en HTTP 409).
     */
    @Operation(
            summary = "Crear una nueva oferta de servicio",
            description = "Permite a un cuidador ('SITTER') crear una nueva oferta de servicio para su propio perfil, o a un 'ADMIN' crearla para cualquier cuidador. El endpoint valida que no existan servicios con nombres duplicados para el mismo cuidador.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Servicio creado exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ServiceOfferingDTO.class),
                            examples = @ExampleObject(
                                    name = "Creación Exitosa",
                                    summary = "Ejemplo de un nuevo servicio de paseo creado",
                                    value = """
                        {
                          "id": 101,
                          "sitterId": 15,
                          "serviceType": "WALKING",
                          "name": "Paseo Matutino (30 min)",
                          "description": "Un paseo energizante de 30 minutos para empezar el día con buen pie. Ideal para perros de tamaño mediano.",
                          "price": 12.50,
                          "durationInMinutes": 30,
                          "isActive": true,
                          "createdAt": "2025-09-14T12:00:00Z"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos. El cuerpo de la solicitud no cumple con las validaciones (ej. precio nulo, nombre vacío, duración menor a 15 minutos).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Validación",
                                    value = """
                        {
                          "status": 400,
                          "message": "Validation failed",
                          "timestamp": "2025-09-14T12:05:00Z",
                          "validationErrors": {
                            "price": "El precio es obligatorio",
                            "durationInMinutes": "La duración mínima del servicio es 15 minutos"
                          }
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Ocurre si un cuidador autenticado intenta crear un servicio para otro cuidador.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Acceso Denegado",
                                    value = """
                        {
                          "status": 403,
                          "message": "Acceso denegado. No tiene permisos para realizar esta acción.",
                          "timestamp": "2025-09-14T12:10:00Z"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado. El ID de cuidador proporcionado en la URL no existe.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Usuario No Encontrado",
                                    value = """
                        {
                          "status": 404,
                          "message": "Usuario no encontrado con ID 999",
                          "timestamp": "2025-09-14T12:12:00Z"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto. El cuidador ya tiene una oferta de servicio con el mismo nombre.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Conflicto por Duplicado",
                                    value = """
                        {
                          "status": 409,
                          "message": "Conflicto: Ya existe una oferta de servicio con el nombre 'Paseo Matutino (30 min)' para este cuidador.",
                          "timestamp": "2025-09-14T12:15:00Z"
                        }
                        """
                            )
                    )
            )
    })
    @PostMapping("/create/{id}")
    @PreAuthorize("hasRole('SITTER') or hasRole('ADMIN')")
    public ResponseEntity<ServiceOfferingDTO> createServiceOffering(
            @Valid @RequestBody CreateServiceOfferingDTO createServiceOfferingDTO,
            @Parameter(description = "ID del usuario cuidador al que se le asignará el servicio.", required = true, example = "15")
            @PathVariable Long id) {
        ServiceOfferingDTO newService = serviceOfferingServiceImplement.createServiceOffering(createServiceOfferingDTO, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(newService);
    }

    /**
     * Obtiene los detalles completos de una oferta de servicio específica por su ID.
     * <p>
     * Este endpoint es de acceso público para cualquier usuario autenticado, permitiendo
     * a los clientes consultar la información detallada de un servicio (descripción completa,
     * precio, duración) antes de tomar una decisión de reserva.
     * </p>
     * <b>Flujo del Proceso:</b>
     * <ul>
     * <li>Se recibe una solicitud con el ID único de la oferta de servicio.</li>
     * <li>El servicio busca la entidad {@code ServiceOffering} correspondiente en la base de datos.</li>
     * <li>Si se encuentra, la entidad se mapea a un {@link ServiceOfferingDTO} y se devuelve.</li>
     * <li>Si no se encuentra, el servicio lanza una excepción que resulta en una respuesta HTTP 404.</li>
     * </ul>
     *
     * @param id El identificador único de la oferta de servicio que se desea consultar.
     * @return Un {@link ResponseEntity} que contiene el {@link ServiceOfferingDTO} con los detalles
     * del servicio y un estado HTTP 200 (OK).
     * @throws ServiceOfferingNotFoundException Si no se encuentra ninguna oferta de servicio con el ID proporcionado.
     */
    @Operation(
            summary = "Obtener detalles de una oferta de servicio por ID",
            description = "Recupera la información completa de una oferta de servicio específica utilizando su identificador único. Requiere que el usuario esté autenticado.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Detalles del servicio obtenidos exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ServiceOfferingDTO.class),
                            examples = @ExampleObject(
                                    name = "Respuesta Exitosa",
                                    summary = "Ejemplo de los detalles de un servicio de guardería",
                                    value = """
                        {
                          "id": 2,
                          "sitterId": 22,
                          "serviceType": "DAYCARE",
                          "name": "Guardería de Día Completo (8 horas)",
                          "description": "Cuidado de día completo en un hogar seguro y divertido, con juegos y socialización. Incluye dos paseos y snacks.",
                          "price": 50.00,
                          "durationInMinutes": 480,
                          "isActive": true,
                          "createdAt": "2025-09-11T11:30:00Z"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere un token JWT válido.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Autenticación",
                                    value = """
                        {
                          "status": 401,
                          "message": "Autenticación requerida. Por favor, incluya un token válido.",
                          "timestamp": "2025-09-14T12:20:00Z"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Servicio no encontrado. El ID proporcionado no corresponde a ninguna oferta de servicio existente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Servicio No Encontrado",
                                    value = """
                        {
                          "status": 404,
                          "message": "Oferta de servicio no encontrada con ID: 999",
                          "timestamp": "2025-09-14T12:22:00Z"
                        }
                        """
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServiceOfferingDTO> getServiceById(
            @Parameter(description = "ID único de la oferta de servicio a consultar.", required = true, example = "2")
            @PathVariable Long id) {
        ServiceOfferingDTO service = serviceOfferingServiceImplement.getServiceById(id);
        return ResponseEntity.ok(service);
    }

    /**
     * Recupera todas las ofertas de servicio de un cuidador específico.
     * <p>
     * Este endpoint público permite a cualquier usuario autenticado (especialmente clientes) ver el
     * catálogo completo de servicios que ofrece un cuidador en particular. Es fundamental para que
     * los clientes puedan explorar las opciones disponibles antes de realizar una reserva.
     * </p>
     * <b>Desglose del Proceso:</b>
     * <ul>
     * <li><b>Validación de Usuario:</b> El servicio primero verifica que el {@code id} corresponda a un usuario existente en el sistema.</li>
     * <li><b>Recuperación de Datos:</b> Se obtienen todas las entidades {@code ServiceOffering} asociadas a ese ID de usuario (cuidador).</li>
     * <li><b>Mapeo a DTO:</b> Cada entidad se convierte a su DTO correspondiente ({@link ServiceOfferingDTO}) para la respuesta.</li>
     * <li><b>Respuesta:</b> Se devuelve la lista de servicios. Si el cuidador existe pero no tiene servicios registrados, se devuelve una respuesta HTTP 204 No Content.</li>
     * </ul>
     *
     * @param id El ID del <strong>usuario</strong> (User) con rol SITTER cuyo catálogo de servicios se desea obtener.
     * @return Un {@link ResponseEntity} que contiene una lista de {@link ServiceOfferingDTO}. Devuelve 200 OK con la lista si se encuentran servicios, o 204 No Content si el cuidador no tiene servicios.
     * @throws UserNotFoundException Si el {@code id} no corresponde a ningún usuario en el sistema (resulta en HTTP 404).
     */
    @Operation(
            summary = "Listar servicios de un cuidador específico",
            description = "Obtiene el catálogo completo de servicios ofrecidos por un único cuidador, identificado por su ID de usuario. Es un endpoint público para usuarios autenticados, útil para que los clientes exploren las opciones de un cuidador antes de reservar.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Catálogo de servicios del cuidador obtenido exitosamente.",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ServiceOfferingDTO.class)),
                            examples = @ExampleObject(
                                    name = "Catálogo de un Cuidador",
                                    summary = "Ejemplo de respuesta con los servicios de un cuidador",
                                    value = """
                        [
                          {
                            "id": 1,
                            "sitterId": 15,
                            "serviceType": "WALKING",
                            "name": "Paseo Energético de 60 Minutos",
                            "description": "Un paseo vigoroso de una hora por parques locales para perros con mucha energía.",
                            "price": 20.00,
                            "durationInMinutes": 60,
                            "isActive": true,
                            "createdAt": "2025-09-10T09:00:00Z"
                          },
                          {
                            "id": 3,
                            "sitterId": 15,
                            "serviceType": "SITTING",
                            "name": "Cuidado a Domicilio (3 horas)",
                            "description": "Cuidado y compañía para tu mascota en la comodidad de tu hogar por 3 horas.",
                            "price": 45.00,
                            "durationInMinutes": 180,
                            "isActive": true,
                            "createdAt": "2025-09-12T14:00:00Z"
                          }
                        ]
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "El cuidador existe pero no tiene ninguna oferta de servicio registrada.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere un token JWT válido.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Autenticación",
                                    value = """
                        {
                          "status": 401,
                          "message": "Autenticación requerida. Por favor, incluya un token válido.",
                          "timestamp": "2025-09-14T12:25:00Z"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cuidador no encontrado. El ID proporcionado en la URL no corresponde a un usuario existente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Cuidador No Encontrado",
                                    value = """
                        {
                          "status": 404,
                          "message": "Usuario no encontrado con ID: 999",
                          "timestamp": "2025-09-14T12:28:00Z"
                        }
                        """
                            )
                    )
            )
    })
    @GetMapping("/all/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServiceOfferingDTO>> getAllServicesByUserId(
            @Parameter(description = "ID del usuario cuidador cuyo catálogo de servicios se desea obtener.", required = true, example = "15")
            @PathVariable Long id) {

        List<ServiceOfferingDTO> services = serviceOfferingServiceImplement.getAllServicesByUserId(id);

        if (services.isEmpty()) {

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(services);
    }

    /**
     * Actualiza una oferta de servicio existente.
     * <p>
     * Permite a un cuidador ('SITTER') modificar los detalles de uno de sus propios servicios,
     * o a un administrador ('ADMIN') modificar cualquier servicio del sistema. La autorización
     * se valida en la capa de servicio para asegurar que un cuidador no pueda editar servicios ajenos.
     * </p>
     * <b>Desglose del Proceso:</b>
     * <ul>
     * <li><b>Validación de Entrada:</b> Se valida que el cuerpo de la solicitud (DTO) cumpla con las restricciones definidas.</li>
     * <li><b>Búsqueda:</b> Se localiza la oferta de servicio existente por su ID único.</li>
     * <li><b>Autorización (en Servicio):</b> Se confirma que el usuario autenticado es el propietario del servicio o un administrador.</li>
     * <li><b>Aplicación de Cambios:</b> Los campos de la entidad se actualizan con los datos del DTO.</li>
     * <li><b>Persistencia:</b> Se guardan los cambios en la base de datos.</li>
     * <li><b>Respuesta:</b> Se devuelve la oferta de servicio con la información actualizada.</li>
     * </ul>
     *
     * @param id El ID único de la oferta de servicio que se va a actualizar.
     * @param updateDTO El DTO que contiene los nuevos datos para el servicio.
     * @return Un {@link ResponseEntity} que contiene el {@link ServiceOfferingDTO} con los datos actualizados, y un estado HTTP 200 (OK).
     * @throws ServiceOfferingNotFoundException Si no se encuentra ninguna oferta de servicio con el ID proporcionado.
     * @throws org.springframework.security.access.AccessDeniedException Si el usuario no es el propietario del servicio ni un administrador.
     */
    @Operation(
            summary = "Actualizar una oferta de servicio existente",
            description = "Permite a un cuidador modificar los detalles de una de sus ofertas de servicio. Requiere que el solicitante sea el propietario del servicio o un administrador.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Servicio actualizado exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ServiceOfferingDTO.class),
                            examples = @ExampleObject(
                                    name = "Actualización Exitosa",
                                    summary = "Ejemplo de un servicio actualizado",
                                    value = """
                        {
                          "id": 1,
                          "sitterId": 15,
                          "serviceType": "WALKING",
                          "name": "Paseo Premium de 60 Minutos con Juegos",
                          "description": "Un paseo vigoroso de una hora por parques locales, incluyendo 15 minutos de juegos interactivos.",
                          "price": 25.00,
                          "durationInMinutes": 60,
                          "isActive": true,
                          "createdAt": "2025-09-10T09:00:00Z"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos. El cuerpo de la solicitud no cumple con las validaciones.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Validación",
                                    value = """
                        {
                          "status": 400,
                          "message": "Validation failed",
                          "timestamp": "2025-09-14T13:05:00Z",
                          "validationErrors": { "price": "El precio debe ser mayor a 0" }
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. El usuario no es el propietario del servicio ni un administrador.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de Acceso Denegado",
                                    value = """
                        {
                          "status": 403,
                          "message": "Acceso denegado. No tiene permisos para modificar este recurso.",
                          "timestamp": "2025-09-14T13:10:00Z"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Servicio no encontrado. El ID proporcionado no corresponde a ninguna oferta de servicio.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Servicio No Encontrado",
                                    value = """
                        {
                          "status": 404,
                          "message": "Oferta de servicio no encontrada con ID: 999",
                          "timestamp": "2025-09-14T13:12:00Z"
                        }
                        """
                            )
                    )
            )
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SITTER') or hasRole('ADMIN')")
    public ResponseEntity<ServiceOfferingDTO> updateService(
            @Parameter(description = "ID único de la oferta de servicio a actualizar.", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceOfferingDTO updateDTO) {
        ServiceOfferingDTO updatedService = serviceOfferingServiceImplement.updateServiceOffering(id, updateDTO);
        return ResponseEntity.ok(updatedService);
    }

    /**
     * Elimina (desactiva) una oferta de servicio.
     * <p>
     * Realiza una eliminación lógica de la oferta de servicio marcándola como inactiva.
     * Esto previene que aparezca en nuevas búsquedas pero mantiene el registro para
     * la integridad de las reservas históricas.
     * </p>
     * <b>Desglose del Proceso:</b>
     * <ul>
     * <li><b>Búsqueda:</b> Se localiza la oferta de servicio por su ID.</li>
     * <li><b>Autorización (en Servicio):</b> Se valida que el usuario sea el propietario o un administrador.</li>
     * <li><b>Actualización:</b> Se establece el campo {@code isActive} de la entidad a {@code false}.</li>
     * <li><b>Persistencia:</b> Se guardan los cambios en la base de datos.</li>
     * <li><b>Respuesta:</b> Se devuelve un mensaje explícito de éxito y un estado HTTP 200 (OK).</li>
     * </ul>
     *
     * @param id El ID único de la oferta de servicio a eliminar (desactivar).
     * @return Un {@link ResponseEntity} con un {@link DeleteResponseDTO} confirmando la operación.
     * @throws ServiceOfferingNotFoundException Si no se encuentra el servicio con el ID proporcionado.
     * @throws org.springframework.security.access.AccessDeniedException Si el usuario no tiene permisos para eliminar el servicio.
     */
    @Operation(
            summary = "Eliminar (desactivar) una oferta de servicio",
            description = "Realiza una eliminación lógica de una oferta de servicio marcándola como inactiva. Requiere que el solicitante sea el propietario del servicio o un administrador.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Servicio desactivado exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeleteResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Desactivación Exitosa",
                                    value = """
                        {
                          "message": "La oferta de servicio con ID 1 ha sido eliminada (desactivada) exitosamente."
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. El usuario no es el propietario del servicio ni un administrador.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Servicio no encontrado. El ID proporcionado no existe.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Servicio No Encontrado",
                                    value = """
                        {
                          "status": 404,
                          "message": "Oferta de servicio no encontrada con ID: 999",
                          "timestamp": "2025-09-14T13:20:00Z"
                        }
                        """
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SITTER') or hasRole('ADMIN')")
    public ResponseEntity<DeleteResponseDTO> deleteService(
            @Parameter(description = "ID único de la oferta de servicio a eliminar (desactivar).", required = true, example = "1")
            @PathVariable Long id) {
        serviceOfferingServiceImplement.deleteServiceOffering(id);
        String message = String.format("La oferta de servicio con ID %d ha sido eliminada (desactivada) exitosamente.", id);
        return ResponseEntity.ok(new DeleteResponseDTO(message));
    }
}