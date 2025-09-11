package com.Petcare.Petcare.DTOs.Sitter;

import com.Petcare.Petcare.Models.SitterProfile;
import com.Petcare.Petcare.Models.User.User;
import org.springframework.stereotype.Component;

@Component
public class SitterProfileMapper {

    public SitterProfileSummary toSummaryDto(SitterProfile profile) {
        if (profile == null || profile.getUser() == null) {
            // Es crucial manejar casos nulos para evitar NullPointerException.
            // Dependiendo de tu lógica, puedes lanzar una excepción o retornar null.
            throw new IllegalArgumentException("El perfil del cuidador o su usuario asociado es nulo.");
        }

        User sitterUser = profile.getUser();

        // Extraemos los datos del CUIDADOR para el DTO.
        return new SitterProfileSummary(
                profile.getId(),
                sitterUser.getFullName(),
                profile.getProfileImageUrl(),
                profile.getHourlyRate(),
                profile.getAverageRating(),
                profile.isVerified(),
                sitterUser.getAddress() // o un campo más específico como user.getCity() si lo tuvieras.
        );
    }
}