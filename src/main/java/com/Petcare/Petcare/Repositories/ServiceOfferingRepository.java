package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.ServiceOffering.ServiceOffering;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceOfferingRepository extends JpaRepository< ServiceOffering, Long > {
    Long findBySitterId( Long sitterId );

    boolean existsBySitterIdAndName ( @NotNull(message = "El ID del cuidador es obligatorio") @Positive(message = "El ID del cuidador debe ser positivo")
                                      Long Id, @NotBlank(message = "El nombre del servicio es obligatorio") @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
                                      String name );
}
