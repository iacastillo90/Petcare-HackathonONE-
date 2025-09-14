package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.SitterProfile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para las operaciones de acceso a datos de la entidad {@link SitterProfile}.
 *
 * <p>Este repositorio extiende {@link JpaRepository}, proporcionando las operaciones CRUD
 * estándar (Create, Read, Update, Delete) para los perfiles de cuidadores. Además, define
 * métodos de consulta personalizados para implementar la lógica de negocio específica de la
 * plataforma, como la búsqueda de cuidadores por estado, ubicación y actividad reciente.</p>
 *
 * <p>Las consultas se benefician de la capacidad de Spring Data JPA para generar queries
 * a partir de los nombres de los métodos y también utilizan la anotación {@code @Query} para
 * consultas más complejas y optimizadas.</p>
 *
 * @see SitterProfile
 * @author Equipo Petcare
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface SitterProfileRepository extends JpaRepository<SitterProfile, Long> {

    /**
     * Busca un perfil de cuidador utilizando el ID del usuario asociado.
     * <p>
     * Dado que la relación entre un {@code User} y un {@code SitterProfile} es uno a uno,
     * este es el método principal para recuperar un perfil específico a partir de un usuario.
     * </p>
     * @param userId El ID del usuario al que pertenece el perfil.
     * @return un {@link Optional} que contiene el {@link SitterProfile} si se encuentra,
     * o un {@code Optional} vacío si el usuario no tiene un perfil de cuidador.
     */
    Optional<SitterProfile> findByUserId(Long userId);

    /**
     * Busca todos los perfiles de cuidadores que están verificados y disponibles para nuevas reservas.
     * <p>
     * Esta consulta es fundamental para el motor de búsqueda del lado del cliente, ya que garantiza
     * que solo se muestren cuidadores calificados y activos que puedan aceptar trabajos.
     * </p>
     * @return Una lista de entidades {@link SitterProfile} que cumplen con ambos criterios.
     */
    List<SitterProfile> findByIsVerifiedTrueAndIsAvailableForBookingsTrue();

    /**
     * Busca perfiles verificados y disponibles cuya dirección de usuario contenga el texto de la ciudad.
     * <p>
     * Esta consulta permite a los clientes encontrar cuidadores locales. Spring Data JPA
     * crea automáticamente el JOIN con la entidad {@code User} para filtrar por el campo
     * {@code address}, realizando una búsqueda que no distingue entre mayúsculas y minúsculas.
     * </p>
     * @param city La cadena de texto (ciudad) a buscar dentro de la dirección del usuario.
     * @return Una lista de {@link SitterProfile} de cuidadores verificados, disponibles y
     * localizados en la ciudad especificada.
     */
    List<SitterProfile> findByIsVerifiedTrueAndIsAvailableForBookingsTrueAndUser_AddressContainingIgnoreCase(String city);

    /**
     * Obtiene una lista paginada de los perfiles de cuidadores actualizados más recientemente.
     * <p>
     * Esta consulta está optimizada para casos de uso como "cuidadores destacados" o "actividad reciente"
     * en un dashboard. Utiliza {@code JOIN FETCH} para cargar de forma anticipada la entidad {@code User}
     * asociada, evitando así el problema de N+1 consultas y mejorando el rendimiento.
     * </p>
     * @param pageable Objeto que contiene la información de paginación (número de página, tamaño).
     * @return Una lista de {@link SitterProfile} correspondiente a la página solicitada, ordenada
     * por fecha de actualización descendente.
     */
    @Query("SELECT sp FROM SitterProfile sp JOIN FETCH sp.user ORDER BY sp.updatedAt DESC")
    List<SitterProfile> findRecentWithUser(Pageable pageable);
}