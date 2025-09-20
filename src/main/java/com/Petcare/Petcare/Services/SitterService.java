package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.DTOs.Sitter.SitterProfileDTO;
import com.Petcare.Petcare.DTOs.Sitter.SitterProfileSummary;
import com.Petcare.Petcare.Exception.Business.SitterProfileNotFoundException;
import com.Petcare.Petcare.Exception.Business.UserNotFoundException;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.util.List;

/**
 * Define el contrato para el servicio de gestión de perfiles de cuidadores (Sitters).
 *
 * <p>Este servicio encapsula toda la lógica de negocio relacionada con la creación,
 * consulta, actualización y eliminación de los perfiles profesionales de los cuidadores.
 * Actúa como una capa de abstracción entre los controladores y el acceso a datos,
 * asegurando que se apliquen las reglas de negocio, como la unicidad del perfil por usuario
 * y la validación de datos.</p>
 *
 * <p><strong>Casos de uso principales:</strong></p>
 * <ul>
 * <li>Permitir que un usuario con rol 'CLIENT' se convierta en 'SITTER' creando su perfil.</li>
 * <li>Gestionar la información profesional de un cuidador, como su biografía, tarifas y disponibilidad.</li>
 * <li>Proporcionar a los clientes endpoints para buscar y encontrar cuidadores disponibles.</li>
 * <li>Permitir a los administradores listar y gestionar todos los perfiles de cuidadores.</li>
 * </ul>
 *
 * @see com.Petcare.Petcare.Models.SitterProfile
 * @see SitterProfileDTO
 * @see SitterProfileSummary
 * @author Equipo Petcare
 * @version 1.0
 * @since 1.0
 */
public interface SitterService {

    /**
     * Crea un perfil de cuidador para un usuario existente en la plataforma.
     *
     * <p>Este método transforma a un usuario en un cuidador, creando un perfil profesional
     * asociado a él. Valida que el usuario no posea ya un perfil de cuidador para
     * mantener la integridad del sistema. La creación del perfil es un paso crucial
     * para que un usuario pueda ofrecer sus servicios.</p>
     *
     * @param userId El ID del usuario que desea crear el perfil de cuidador.
     * @param sitterProfileDTO DTO que contiene toda la información profesional del cuidador,
     * como su biografía, tarifa por hora, radio de servicio, etc.
     * @return El {@link SitterProfileDTO} del perfil recién creado, incluyendo el ID generado
     * por la base de datos.
     * @throws UserNotFoundException si no se encuentra un usuario con el ID proporcionado.
     * @throws IllegalArgumentException si los datos en el DTO son inválidos.
     */
    SitterProfileDTO createSitterProfile(Long userId, SitterProfileDTO sitterProfileDTO);

    /**
     * Obtiene el perfil de cuidador asociado a un ID de usuario específico.
     *
     * <p>Permite recuperar toda la información profesional de un cuidador a partir
     * del ID del usuario al que está vinculado. Es el método principal para visualizar
     * los detalles de un cuidador.</p>
     *
     * @param userId El ID del usuario cuyo perfil de cuidador se desea consultar.
     * @return Él {@link SitterProfileDTO} con los detalles del perfil.
     * @throws SitterProfileNotFoundException si el usuario con el ID especificado
     * no tiene un perfil de cuidador asociado.
     */
    SitterProfileDTO getSitterProfile(Long userId);

    /**
     * Actualiza la información de un perfil de cuidador existente.
     *
     * <p>Este método permite a un cuidador (o a un administrador) modificar los detalles
     * de su perfil profesional, como ajustar su tarifa, cambiar su biografía o
     * actualizar su foto de perfil.</p>
     *
     * @param userId El ID del usuario cuyo perfil de cuidador se va a actualizar.
     * @param sitterProfileDTO DTO que contiene los nuevos datos para el perfil.
     * Solo los campos no nulos en el DTO serán considerados
     * para la actualización.
     * @return Él {@link SitterProfileDTO} con la información del perfil ya actualizada.
     * @throws SitterProfileNotFoundException si el usuario no tiene un perfil para actualizar.
     */
    SitterProfileDTO updateSitterProfile(Long userId, SitterProfileDTO sitterProfileDTO);

    /**
     * Elimina el perfil de cuidador de un usuario.
     *
     * <p>Esta es una operación crítica que revoca la capacidad de un usuario para actuar
     * como cuidador en la plataforma. Puede implicar una eliminación lógica (desactivación)
     * o física, y potencialmente revertir el rol del usuario a 'CLIENT'.</p>
     *
     * @param userId El ID del usuario cuyo perfil de cuidador será eliminado.
     * @throws SitterProfileNotFoundException si no se encuentra un perfil para el usuario especificado.
     */
    void deleteSitterProfile(Long userId);

    /**
     * Obtiene una lista con todos los perfiles de cuidadores registrados en el sistema.
     *
     * <p>Esta operación está típicamente restringida a administradores y se utiliza para
     * la gestión general de la plataforma, auditorías o para generar reportes sobre
     * la comunidad de cuidadores.</p>
     *
     * @return Una lista de {@link SitterProfileDTO} con los perfiles completos de todos
     * los cuidadores.
     */
    List<SitterProfileDTO> getAllSitterProfiles();

    /**
     * Busca cuidadores disponibles que cumplan con ciertos criterios de búsqueda.
     *
     * <p>Este es el principal método utilizado por los clientes para encontrar cuidadores.
     * La búsqueda se realiza sobre perfiles que están verificados y disponibles para
     * recibir nuevas reservas. La respuesta es un DTO resumido (`SitterProfileSummary`)
     * para optimizar el rendimiento en listados largos.</p>
     *
     * @param city Filtro opcional por ciudad. Si se proporciona, la búsqueda se restringe
     * a cuidadores cuya dirección contenga el nombre de la ciudad. Si es nulo
     * o vacío, se buscan cuidadores en todas las ubicaciones.
     * @return Una lista de {@link SitterProfileSummary} con los perfiles resumidos de los
     * cuidadores que coinciden con los criterios de búsqueda.
     */
    List<SitterProfileSummary> findSitters(String city);
}