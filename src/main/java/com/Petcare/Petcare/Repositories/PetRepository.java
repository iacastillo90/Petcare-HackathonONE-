package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

}
