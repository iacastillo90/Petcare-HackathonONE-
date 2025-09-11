package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de persistencia de la entidad Pet.
 *
 * <p>Proporciona métodos de consulta especializados para buscar, filtrar y contar
 * mascotas según diferentes criterios. Extiende JpaRepository para operaciones
 * CRUD básicas y añade consultas personalizadas optimizadas.</p>
 *
 * <p><strong>Categorías de consultas:</strong></p>
 * <ul>
 * <li>Búsquedas por cuenta: mascotas de cuentas específicas</li>
 * <li>Filtros por características: especie, raza, género</li>
 * <li>Consultas por estado: activas, inactivas, disponibles</li>
 * <li>Búsquedas de texto: nombre, características físicas</li>
 * <li>Estadísticas y conteos: por diferentes criterios</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see Pet
 */
@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    // ========== CONSULTAS POR CUENTA ==========

    /**
     * Busca todas las mascotas de una cuenta específica.
     *
     * @param accountId ID de la cuenta
     * @return Lista de mascotas de la cuenta
     */
    List<Pet> findByAccountId(Long accountId);

    /**
     * Busca mascotas activas de una cuenta específica.
     *
     * @param accountId ID de la cuenta
     * @return Lista de mascotas activas de la cuenta
     */
    List<Pet> findByAccountIdAndIsActiveTrue(Long accountId);

    /**
     * Cuenta el número de mascotas de una cuenta específica.
     *
     * @param accountId ID de la cuenta
     * @return Número de mascotas en la cuenta
     */
    long countByAccountId(Long accountId);

    /**
     * Cuenta las mascotas activas de una cuenta específica.
     *
     * @param accountId ID de la cuenta
     * @return Número de mascotas activas en la cuenta
     */
    long countByAccountIdAndIsActiveTrue(Long accountId);

    // ========== CONSULTAS POR CARACTERÍSTICAS ==========

    /**
     * Busca mascotas por especie.
     *
     * @param species Especie a buscar
     * @return Lista de mascotas de la especie especificada
     */
    List<Pet> findBySpeciesIgnoreCase(String species);

    /**
     * Busca mascotas por raza.
     *
     * @param breed Raza a buscar
     * @return Lista de mascotas de la raza especificada
     */
    List<Pet> findByBreedIgnoreCase(String breed);

    /**
     * Busca mascotas por género.
     *
     * @param gender Género a buscar
     * @return Lista de mascotas del género especificado
     */
    List<Pet> findByGenderIgnoreCase(String gender);

    /**
     * Busca mascotas por nombre (coincidencia exacta, ignorando mayúsculas).
     *
     * @param name Nombre a buscar
     * @return Lista de mascotas con el nombre especificado
     */
    List<Pet> findByNameIgnoreCase(String name);

    // ========== CONSULTAS POR ESTADO ==========

    /**
     * Busca todas las mascotas activas.
     *
     * @return Lista de mascotas activas
     */
    List<Pet> findByIsActiveTrue();

    /**
     * Busca todas las mascotas inactivas.
     *
     * @return Lista de mascotas inactivas
     */
    List<Pet> findByIsActiveFalse();

    /**
     * Verifica si existe una mascota con el nombre especificado en una cuenta.
     *
     * @param name Nombre de la mascota
     * @param accountId ID de la cuenta
     * @return true si existe una mascota con ese nombre en la cuenta
     */
    boolean existsByNameIgnoreCaseAndAccountId(String name, Long accountId);

    // ========== BÚSQUEDAS DE TEXTO ==========

    /**
     * Búsqueda de texto en campos relevantes de mascotas.
     *
     * @param searchTerm Término de búsqueda
     * @return Lista de mascotas que coinciden con el término
     */
    @Query("SELECT p FROM Pet p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.species) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.breed) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.color) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Pet> findBySearchTerm(@Param("searchTerm") String searchTerm);

    /**
     * Búsqueda de texto solo en mascotas activas.
     *
     * @param searchTerm Término de búsqueda
     * @return Lista de mascotas activas que coinciden con el término
     */
    @Query("SELECT p FROM Pet p WHERE p.isActive = true AND (" +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.species) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.breed) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.color) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Pet> findActivePetsBySearchTerm(@Param("searchTerm") String searchTerm);

    // ========== CONSULTAS DE ESTADÍSTICAS ==========

    /**
     * Cuenta mascotas por especie.
     *
     * @return Lista de resultados [especie, count]
     */
    @Query("SELECT p.species, COUNT(p) FROM Pet p GROUP BY p.species ORDER BY COUNT(p) DESC")
    List<Object[]> countBySpecies();

    /**
     * Cuenta mascotas por género.
     *
     * @return Lista de resultados [género, count]
     */
    @Query("SELECT p.gender, COUNT(p) FROM Pet p WHERE p.gender IS NOT NULL GROUP BY p.gender ORDER BY COUNT(p) DESC")
    List<Object[]> countByGender();

    /**
     * Cuenta mascotas activas por especie.
     *
     * @param species Especie a contar
     * @return Número de mascotas activas de la especie
     */
    long countBySpeciesIgnoreCaseAndIsActiveTrue(String species);

    /**
     * Cuenta el total de mascotas activas.
     *
     * @return Número total de mascotas activas
     */
    long countByIsActiveTrue();

    /**
     * Cuenta el total de mascotas inactivas.
     *
     * @return Número total de mascotas inactivas
     */
    long countByIsActiveFalse();

    /**
     * Cuenta mascotas registradas desde una fecha específica.
     *
     * @param since Fecha desde la cual contar
     * @return Número de mascotas registradas desde la fecha
     */
    long countByCreatedAtGreaterThanEqual(LocalDateTime since);

    /**
     * Cuenta cuentas que tienen al menos una mascota.
     *
     * @return Número de cuentas con mascotas
     */
    @Query("SELECT COUNT(DISTINCT p.account.id) FROM Pet p")
    long countDistinctAccounts();

    // ========== CONSULTAS DE RANGO DE EDAD ==========

    /**
     * Cuenta mascotas por rangos de edad para estadísticas.
     */
    @Query("SELECT " +
            "CASE " +
            "WHEN p.age IS NULL THEN 'No especificada' " +
            "WHEN p.age <= 1 THEN '0-1 años' " +
            "WHEN p.age <= 5 THEN '2-5 años' " +
            "WHEN p.age <= 10 THEN '6-10 años' " +
            "WHEN p.age <= 15 THEN '11-15 años' " +
            "ELSE '16+ años' " +
            "END, COUNT(p) " +
            "FROM Pet p " +
            "GROUP BY " +
            "CASE " +
            "WHEN p.age IS NULL THEN 'No especificada' " +
            "WHEN p.age <= 1 THEN '0-1 años' " +
            "WHEN p.age <= 5 THEN '2-5 años' " +
            "WHEN p.age <= 10 THEN '6-10 años' " +
            "WHEN p.age <= 15 THEN '11-15 años' " +
            "ELSE '16+ años' " +
            "END")
    List<Object[]> countByAgeRange();

    // ========== CONSULTAS ESPECIALIZADAS ==========

    /**
     * Encuentra mascotas que requieren atención especial (tienen medicamentos o alergias).
     *
     * @return Lista de mascotas con necesidades especiales
     */
    @Query("SELECT p FROM Pet p WHERE p.isActive = true AND " +
            "(p.medications IS NOT NULL AND TRIM(p.medications) != '' OR " +
            "p.allergies IS NOT NULL AND TRIM(p.allergies) != '')")
    List<Pet> findPetsWithSpecialNeeds();

    /**
     * Encuentra mascotas disponibles para servicios de una cuenta específica.
     *
     * @param accountId ID de la cuenta
     * @return Lista de mascotas disponibles para servicios
     */
    @Query("SELECT p FROM Pet p JOIN p.account a WHERE a.id = :accountId " +
            "AND p.isActive = true AND a.isActive = true")
    List<Pet> findAvailablePetsByAccountId(@Param("accountId") Long accountId);

    /**
     * Encuentra la mascota más reciente de una cuenta.
     *
     * @param accountId ID de la cuenta
     * @return Mascota más recientemente registrada de la cuenta
     */
    Optional<Pet> findTopByAccountIdOrderByCreatedAtDesc(Long accountId);

    // ========== CONSULTAS DE VALIDACIÓN ==========

    /**
     * Verifica si una cuenta tiene mascotas activas.
     *
     * @param accountId ID de la cuenta
     * @return true si la cuenta tiene mascotas activas
     */
    boolean existsByAccountIdAndIsActiveTrue(Long accountId);

    /**
     * Busca mascotas duplicadas por nombre y características en una cuenta.
     * Útil para prevenir registros duplicados.
     *
     * @param accountId ID de la cuenta
     * @param name Nombre de la mascota
     * @param species Especie de la mascota
     * @param breed Raza de la mascota (puede ser null)
     * @return Lista de mascotas similares
     */
    @Query("SELECT p FROM Pet p WHERE p.account.id = :accountId " +
            "AND LOWER(p.name) = LOWER(:name) " +
            "AND LOWER(p.species) = LOWER(:species) " +
            "AND ((:breed IS NULL AND p.breed IS NULL) OR LOWER(p.breed) = LOWER(:breed))")
    List<Pet> findSimilarPets(@Param("accountId") Long accountId,
                              @Param("name") String name,
                              @Param("species") String species,
                              @Param("breed") String breed);
}