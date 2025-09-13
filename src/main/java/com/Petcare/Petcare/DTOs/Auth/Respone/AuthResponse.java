package com.Petcare.Petcare.DTOs.Auth.Respone;

import com.Petcare.Petcare.DTOs.User.UserProfileDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta de autenticación y registro exitoso.
 * <p>
 * Este objeto es devuelto por la API cada vez que un usuario inicia sesión o se registra
 * correctamente. Contiene toda la información necesaria para que la aplicación cliente
 * establezca y gestione una sesión de usuario autenticada.
 * </p>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see UserProfileDTO
 * @see com.Petcare.Petcare.Controllers.UserController
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta estándar para operaciones de login y registro exitosas, conteniendo el token de acceso y el perfil del usuario.")
public class AuthResponse {

    /**
     * El token de autenticación JWT (JSON Web Token).
     * <p>
     * Este token debe ser enviado por el cliente en la cabecera de autorización
     * de las solicitudes posteriores a endpoints protegidos, usando el esquema "Bearer".
     * </p>
     */
    @Schema(description = "Token JWT de portador (Bearer Token) para autorizar las siguientes peticiones.",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTcxNTY0NjQzNSwiZXhwIjoxNzE1NzMyODM1fQ.aBcDeFgHiJkLmNoPqRsTuVwXyZ...")
    private String token;

    /**
     * El rol principal del usuario autenticado.
     * <p>
     * Se proporciona para que la aplicación cliente pueda adaptar la interfaz de usuario
     * o habilitar/deshabilitar funcionalidades según los permisos del usuario (ej. mostrar un panel de administrador).
     * </p>
     */
    @Schema(description = "Rol del usuario autenticado, para control de acceso en el frontend.", example = "CLIENT")
    private String role;

    /**
     * Un perfil simplificado del usuario.
     * <p>
     * Contiene información básica y segura del usuario (nombre, email, iniciales) para ser
     * mostrada inmediatamente en la interfaz de usuario sin necesidad de una llamada adicional a la API.
     * </p>
     * @see UserProfileDTO
     */
    @Schema(description = "Perfil simplificado del usuario con información básica para mostrar en la UI.")
    private UserProfileDTO userProfile;
}