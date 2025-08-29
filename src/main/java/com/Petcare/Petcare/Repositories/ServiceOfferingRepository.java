package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.ServiceOffering.ServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceOfferingRepository extends JpaRepository< ServiceOffering, Long > {
}
