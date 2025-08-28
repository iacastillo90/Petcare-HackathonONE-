package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.DTOs.Pet.PetDTO;
import java.util.List;

public interface PetService {
    PetDTO createPet(PetDTO petDTO);
    PetDTO getPetById(Long id);
    List<PetDTO> getAllPets();
    PetDTO updatePet(Long id, PetDTO petDTO);
    void deletePet(Long id);
}
