package com.Petcare.Petcare.Configurations.Web;

import com.Petcare.Petcare.Repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Clase de configuración de Spring para definir beans relacionados con la autenticación web.
 * Configura el codificador de contraseñas, el servicio de detalles de usuario y el proveedor de autenticación.
 */
@Configuration
public class WebAuthentication {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(WebAuthentication.class);

    /**
     * Constructor que inyecta el repositorio de usuarios.
     *
     * @param userRepository El repositorio para acceder a los datos de los usuarios.
     */
    public WebAuthentication(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


}