package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.SitterProfile;
import com.Petcare.Petcare.Models.SitterWorkExperience;
import com.Petcare.Petcare.Services.SitterWorkExperienceService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para las operaciones de acceso a datos de la entidad {@link SitterWorkExperience}.
 * <p>
 * Esta interfaz extiende {@link JpaRepository}, proporcionando las operaciones CRUD
 * estándar (Create, Read, Update, Delete) para el historial laboral de los cuidadores.
 * Además, define métodos de consulta personalizados para implementar la lógica de negocio
 * específica de la plataforma.
 *
 * @see SitterWorkExperience
 * @see SitterWorkExperienceService
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface SitterWorkExperienceRepository extends JpaRepository<SitterWorkExperience, Long> {

    /**
     * Busca todas las experiencias laborales asociadas a un ID de perfil de cuidador específico.
     * <p>
     * Spring Data JPA genera automáticamente la consulta a partir del nombre del método.
     * Esta es la consulta principal utilizada para construir la vista del historial profesional
     * en el perfil público de un cuidador.
     *
     * @param sitterProfileId El ID del {@link SitterProfile} cuyas experiencias se desean obtener.
     * @return Una {@link List} de entidades {@link SitterWorkExperience}. Si el perfil no tiene
     * experiencias registradas, la lista estará vacía, pero nunca será nula.
     */
    List<SitterWorkExperience> findBySitterProfileId(Long sitterProfileId);

    /**
     * Verifica si ya existe una experiencia laboral con los mismos datos clave para un perfil.
     * <p>
     * Este método es una herramienta de validación crucial para la capa de servicio. Permite
     * comprobar de manera eficiente si un cuidador está intentando añadir una experiencia
     * duplicada (misma empresa, mismo puesto y misma fecha de inicio) antes de intentar
     * la operación de guardado.
     *
     * @param sitterProfileId El ID del perfil del cuidador.
     * @param companyName El nombre de la empresa a verificar.
     * @param jobTitle El título del puesto a verificar.
     * @param startDate La fecha de inicio a verificar.
     * @return {@code true} si ya existe una entrada idéntica, {@code false} en caso contrario.
     */
    boolean existsBySitterProfileIdAndCompanyNameAndJobTitleAndStartDate(
            Long sitterProfileId, String companyName, String jobTitle, LocalDate startDate);
}