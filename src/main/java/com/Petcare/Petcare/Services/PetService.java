package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.DTOs.Pet.*;
import com.Petcare.Petcare.Models.Pet;

import java.util.List;

/**
 * Interfaz para el servicio de gestión de mascotas (Pets).
 *
 * <p>Define el contrato completo para las operaciones de negocio relacionadas con
 * mascotas, incluyendo CRUD básico, consultas especializadas, validaciones de negocio
 * y generación de estadísticas. Esta interfaz sigue el patrón de capas del modelo
 * de Usuario para mantener consistencia arquitectónica.</p>
 *
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 * <li>Gestión CRUD completa con validaciones</li>
 * <li>Consultas por cuenta, especie y características</li>
 * <li>Control de estado activo/inactivo</li>
 * <li>Búsquedas de texto optimizadas</li>
 * <li>Estadísticas y métricas detalladas</li>
 * <li>Validaciones de negocio y duplicados</li>
 * </ul>
 *
 * <p><strong>Casos de uso cubiertos:</strong></p>
 * <ul>
 * <li>Registro y actualización de mascotas por clientes</li>
 * <li>Búsqueda y filtrado para servicios de cuidado</li>
 * <li>Gestión administrativa con controles de acceso</li>
 * <li>Reportes y dashboards ejecutivos</li>
 * <li>Validación de integridad de datos</li>
 * </ul>
 *
 * <p><strong>Patrones implementados:</strong></p>
 * <ul>
 * <li>Service Layer con separación de responsabilidades</li>
 * <li>DTO Pattern para transferencia de datos</li>
 * <li>Strategy Pattern para diferentes tipos de consulta</li>
 * <li>Command Pattern para operaciones complejas</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see Pet
 * @see PetResponse
 * @see CreatePetRequest
 * @see PetStatsResponse
 */
public interface PetService {

    // ========== OPERACIONES CRUD BÁSICAS ==========

    /**
     * Crea una nueva mascota en el sistema.
     *
     * <p>Valida que la cuenta existe y está activa, verifica duplicados si es necesario,
     * y establece valores por defecto apropiados. La mascota se crea en estado activo.</p>
     *
     * @param petRequest DTO con los datos de entrada para la nueva mascota
     * @return PetResponse DTO con los detalles de la mascota recién creada
     * @throws IllegalArgumentException si la cuenta asociada no existe o está inactiva
     * @throws IllegalStateException si se detecta una mascota duplicada (opcional)
     */
    PetResponse createPet(CreatePetRequest petRequest);

    /**
     * Obtiene una mascota específica por su ID.
     *
     * <p>Retorna información completa de la mascota incluyendo datos de la cuenta
     * propietaria. Valida permisos de acceso según el contexto de seguridad.</p>
     *
     * @param id El identificador único de la mascota
     * @return PetResponse DTO con la información completa de la mascota
     * @throws IllegalArgumentException si no se encuentra ninguna mascota con ese ID
     * @throws SecurityException si el usuario no tiene permisos para ver la mascota
     */
    PetResponse getPetById(Long id);

    /**
     * Obtiene una lista con todas las mascotas del sistema.
     *
     * <p>Para usuarios regulares, filtra solo sus mascotas. Para administradores,
     * retorna todas las mascotas del sistema. Incluye información básica de cuentas.</p>
     *
     * @return Lista de PetResponse DTOs con información completa
     */
    List<PetResponse> getAllPets();

    /**
     * Obtiene una lista resumida de todas las mascotas para listados optimizados.
     *
     * <p>Versión optimizada que retorna solo campos esenciales para mejorar
     * performance en listados grandes. Aplica filtros de seguridad apropiados.</p>
     *
     * @return Lista de PetSummaryResponse DTOs con información resumida
     */
    List<PetSummaryResponse> getAllPetsSummary();

    /**
     * Actualiza la información de una mascota existente.
     *
     * <p>Valida permisos de acceso, mantiene campos de auditoría, y preserva
     * relaciones existentes. Solo actualiza campos proporcionados en el DTO.</p>
     *
     * @param id El identificador único de la mascota a actualizar
     * @param petRequest DTO con los nuevos datos para la mascota
     * @return PetResponse DTO con la información actualizada
     * @throws IllegalArgumentException si no se encuentra la mascota
     * @throws SecurityException si el usuario no tiene permisos para modificar
     */
    PetResponse updatePet(Long id, CreatePetRequest petRequest);

    /**
     * Elimina una mascota del sistema por su ID.
     *
     * <p>Realiza eliminación lógica marcando la mascota como inactiva, o eliminación
     * física según configuración del sistema. Valida dependencias antes de eliminar.</p>
     *
     * @param id El identificador único de la mascota a eliminar
     * @throws IllegalArgumentException si no se encuentra la mascota
     * @throws SecurityException si el usuario no tiene permisos para eliminar
     * @throws IllegalStateException si existen dependencias que impiden la eliminación
     */
    void deletePet(Long id);

    // ========== CONSULTAS POR CUENTA ==========

    /**
     * Obtiene todas las mascotas de una cuenta específica.
     *
     * @param accountId ID de la cuenta propietaria
     * @return Lista de mascotas de la cuenta
     * @throws IllegalArgumentException si la cuenta no existe
     * @throws SecurityException si no tiene permisos para ver las mascotas de la cuenta
     */
    List<PetResponse> getPetsByAccountId(Long accountId);

    /**
     * Obtiene solo las mascotas activas de una cuenta específica.
     *
     * @param accountId ID de la cuenta propietaria
     * @return Lista de mascotas activas de la cuenta
     * @throws IllegalArgumentException si la cuenta no existe
     */
    List<PetResponse> getActivePetsByAccountId(Long accountId);

    /**
     * Obtiene resumen de mascotas de una cuenta para listados optimizados.
     *
     * @param accountId ID de la cuenta propietaria
     * @return Lista resumida de mascotas de la cuenta
     */
    List<PetSummaryResponse> getPetsSummaryByAccountId(Long accountId);

    // ========== CONSULTAS POR CARACTERÍSTICAS ==========

    /**
     * Obtiene mascotas filtradas por especie.
     *
     * @param species Especie a buscar (ignorando mayúsculas/minúsculas)
     * @return Lista de mascotas de la especie especificada
     */
    List<PetResponse> getPetsBySpecies(String species);

    /**
     * Obtiene mascotas filtradas por raza.
     *
     * @param breed Raza a buscar (ignorando mayúsculas/minúsculas)
     * @return Lista de mascotas de la raza especificada
     */
    List<PetResponse> getPetsByBreed(String breed);

    /**
     * Obtiene mascotas filtradas por género.
     *
     * @param gender Género a buscar
     * @return Lista de mascotas del género especificado
     */
    List<PetResponse> getPetsByGender(String gender);

    // ========== CONSULTAS POR ESTADO ==========

    /**
     * Obtiene todas las mascotas activas del sistema.
     *
     * @return Lista de mascotas activas (filtrada por permisos del usuario)
     */
    List<PetResponse> getActivePets();

    /**
     * Obtiene todas las mascotas inactivas del sistema.
     *
     * @return Lista de mascotas inactivas (solo para administradores)
     * @throws SecurityException si no tiene permisos administrativos
     */
    List<PetResponse> getInactivePets();

    /**
     * Obtiene mascotas disponibles para servicios de una cuenta.
     *
     * <p>Retorna mascotas activas de cuentas activas, listas para servicios de cuidado.</p>
     *
     * @param accountId ID de la cuenta
     * @return Lista de mascotas disponibles para servicios
     */
    List<PetResponse> getAvailablePetsByAccountId(Long accountId);

    // ========== OPERACIONES DE ESTADO ==========

    /**
     * Activa o desactiva una mascota.
     *
     * <p>Cambia el estado de actividad de la mascota. Mascotas inactivas no aparecen
     * en búsquedas regulares ni están disponibles para servicios.</p>
     *
     * @param id ID de la mascota
     * @return PetResponse DTO con el nuevo estado
     * @throws IllegalArgumentException si la mascota no existe
     * @throws SecurityException si no tiene permisos para cambiar el estado
     */
    PetResponse togglePetActive(Long id);

    // ========== BÚSQUEDAS DE TEXTO ==========

    /**
     * Realiza búsqueda de texto en campos relevantes de mascotas.
     *
     * <p>Busca en nombre, especie, raza y color de las mascotas. Aplica filtros
     * de seguridad según el contexto del usuario.</p>
     *
     * @param searchTerm Término de búsqueda
     * @return Lista de mascotas que coinciden con el término
     */
    List<PetResponse> searchPets(String searchTerm);

    /**
     * Realiza búsqueda de texto solo en mascotas activas.
     *
     * @param searchTerm Término de búsqueda
     * @return Lista de mascotas activas que coinciden con el término
     */
    List<PetResponse> searchActivePets(String searchTerm);

    // ========== ESTADÍSTICAS Y MÉTRICAS ==========

    /**
     * Obtiene estadísticas completas de mascotas del sistema.
     *
     * <p>Incluye conteos totales, distribuciones por características, métricas
     * temporales y estadísticas de cuentas. Solo disponible para administradores.</p>
     *
     * @return PetStatsResponse con estadísticas completas
     * @throws SecurityException si no tiene permisos administrativos
     */
    PetStatsResponse getPetStats();

    // ========== OPERACIONES DE VALIDACIÓN Y UTILIDAD ==========

    /**
     * Verifica si existe una mascota con el nombre especificado en una cuenta.
     *
     * @param name Nombre a verificar
     * @param accountId ID de la cuenta
     * @return true si existe una mascota con ese nombre en la cuenta
     */
    boolean isPetNameAvailable(String name, Long accountId);

    /**
     * Encuentra mascotas que requieren atención especial.
     *
     * <p>Retorna mascotas que tienen medicamentos o alergias registradas,
     * útil para servicios de cuidado especializados.</p>
     *
     * @return Lista de mascotas con necesidades especiales
     */
    List<PetResponse> getPetsWithSpecialNeeds();

    /**
     * Obtiene la mascota más reciente registrada en una cuenta.
     *
     * @param accountId ID de la cuenta
     * @return PetResponse de la mascota más reciente, o null si no hay mascotas
     */
    PetResponse getNewestPetByAccountId(Long accountId);

    // ========== ENDPOINT DE SALUD ==========

    /**
     * Endpoint de verificación de salud del servicio.
     *
     * @return Mensaje de confirmación de que el servicio está operativo
     */
    String healthCheck();
}