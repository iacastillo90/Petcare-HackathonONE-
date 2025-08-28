package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.DTOs.Pet.PetDTO;
import com.Petcare.Petcare.Models.Pet;
import com.Petcare.Petcare.Repositories.PetRepository;
import com.Petcare.Petcare.Services.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PetServiceImplement implements PetService {

    private final PetRepository petRepository;

    @Autowired
    public PetServiceImplement(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Override
    public PetDTO createPet(PetDTO petDTO) {
        Pet pet = new Pet();
        pet.setName(petDTO.getName());
        pet.setType(petDTO.getType());
        pet.setAge(petDTO.getAge());
        pet.setOwner(petDTO.getOwner());

        Pet savedPet = petRepository.save(pet);
        return convertToDTO(savedPet);
    }

    @Override
    public PetDTO getPetById(Long id) {
        return petRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public List<PetDTO> getAllPets() {
        return petRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PetDTO updatePet(Long id, PetDTO petDTO) {
        return petRepository.findById(id).map(pet -> {
            pet.setName(petDTO.getName());
            pet.setType(petDTO.getType());
            pet.setAge(petDTO.getAge());
            pet.setOwner(petDTO.getOwner());
            return convertToDTO(petRepository.save(pet));
        }).orElse(null);
    }

    @Override
    public void deletePet(Long id) {
        petRepository.deleteById(id);
    }

    private PetDTO convertToDTO(Pet pet) {
        PetDTO dto = new PetDTO();
        dto.setId(pet.getId());
        dto.setName(pet.getName());
        dto.setType(pet.getType());
        dto.setAge(pet.getAge());
        dto.setOwner(pet.getOwner());
        return dto;
    }
}
