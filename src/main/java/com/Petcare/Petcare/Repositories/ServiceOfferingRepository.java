package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.ServiceOffering.ServiceOffering;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la gestión de persistencia de ofertas de servicios.
 * 
 * <p>Esta interfaz extiende JpaRepository para proporcionar operaciones CRUD
 * básicas y define métodos de consulta personalizados para la entidad ServiceOffering.
 * Permite realizar búsquedas específicas por cuidador y validar duplicados.</p>
 * 
 * <p><strong>Métodos personalizados:</strong></p>
 * <ul>
 *   <li>findBySitterId - Busca servicios por ID del cuidador</li>
 *   <li>existsBySitterIdAndName - Valida duplicados por cuidador y nombre</li>
 * </ul>
 * 
 * @author Equipo Petcare
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface ServiceOfferingRepository extends JpaRepository< ServiceOffering, Long > {
    /**
     * Busca todos los servicios ofrecidos por un cuidador específico.
     * 
     * @param sitterId identificador del cuidador
     * @return Lista de servicios del cuidador
     */
    List<ServiceOffering> findBySitterId(Long sitterId);

    /**
     * Verifica si ya existe un servicio con el mismo nombre para un cuidador.
     * 
     * <p>Utilizado para prevenir servicios duplicados del mismo cuidador
     * con nombres idénticos.</p>
     * 
     * @param Id identificador del cuidador
     * @param name nombre del servicio a verificar
     * @return true si existe un servicio con ese nombre para el cuidador
     */
    boolean existsBySitterIdAndName ( @NotNull(message = "El ID del cuidador es obligatorio") @Positive(message = "El ID del cuidador debe ser positivo")
                                      Long Id, @NotBlank(message = "El nombre del servicio es obligatorio") @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
                                      String name );
    boolean existsByName( String name );
}
