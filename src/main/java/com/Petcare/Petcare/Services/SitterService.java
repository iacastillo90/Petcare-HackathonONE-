package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.DTOs.Sitter.SitterProfileDTO;
import java.util.List;

public interface SitterService {

    // Crear perfil de Sitter para un usuario
    SitterProfileDTO createSitterProfile(Long userId, SitterProfileDTO sitterProfileDTO);

    // Obtener perfil de Sitter por userId
    SitterProfileDTO getSitterProfile(Long userId);

    // Actualizar perfil de Sitter
    SitterProfileDTO updateSitterProfile(Long userId, SitterProfileDTO sitterProfileDTO);

    // Eliminar perfil de Sitter
    void deleteSitterProfile(Long userId);

    // Listar todos los perfiles de Sitter
    List<SitterProfileDTO> getAllSitterProfiles();
}
