package com.Petcare.Petcare.DTOs.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la solicitud de actualización de un usuario existente.
 * <p>
 * A diferencia de {@link CreateUserRequest}, todos los campos en este DTO son opcionales.
 * Esto permite a los clientes de la API enviar únicamente los datos que desean modificar,
 * soportando así actualizaciones parciales. Si un campo no se incluye en la solicitud (es nulo),
 * su valor correspondiente en la base de datos no será alterado.
 * </p>
 * <p>Las validaciones de formato y tamaño se aplican solo si se proporciona un valor para el campo.</p>
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see com.Petcare.Petcare.Models.User.User
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO con los campos opcionales para actualizar un usuario existente. Solo los campos proporcionados serán actualizados.")
public class UpdateUserRequest {

    @Schema(description = "El nuevo nombre del usuario. Opcional.", example = "Jane")
    @Size(max = 250, message = "El nombre no puede exceder 250 caracteres")
    private String firstName;

    @Schema(description = "El nuevo apellido del usuario. Opcional.", example = "Doe")
    @Size(max = 250, message = "El apellido no puede exceder 250 caracteres")
    private String lastName;

    @Schema(description = "La nueva dirección de correo electrónico del usuario. Opcional. Si se cambia, se requerirá una nueva verificación.", example = "jane.doe.new@example.com")
    @Email(message = "El formato del email no es válido")
    @Size(max = 250, message = "El email no puede exceder 250 caracteres")
    private String email;

    @Schema(description = "La nueva contraseña en texto plano del usuario. Opcional. Si se deja en blanco, la contraseña actual no se modificará.", example = "newSecurePassword123")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @Schema(description = "La nueva dirección física del usuario. Opcional.", example = "456 Oak Avenue, Springfield")
    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String address;

    @Schema(description = "El nuevo número de teléfono del usuario. Opcional.", example = "555-987-6543")
    @Size(max = 20, message = "El número de teléfono no puede exceder 20 caracteres")
    private String phoneNumber;


    // ========== GETTERS Y SETTERS ==========

    /**
     * Obtiene el nuevo nombre del usuario.
     * @return El nombre proporcionado, o null si no se especificó.
     */
    public String getFirstName() { return firstName; }

    /**
     * Establece el nuevo nombre del usuario.
     * @param firstName El nuevo nombre para la solicitud de actualización.
     */
    public void setFirstName(String firstName) { this.firstName = firstName; }

    /**
     * Obtiene el nuevo apellido del usuario.
     * @return El apellido proporcionado, o null si no se especificó.
     */
    public String getLastName() { return lastName; }

    /**
     * Establece el nuevo apellido del usuario.
     * @param lastName El nuevo apellido para la solicitud de actualización.
     */
    public void setLastName(String lastName) { this.lastName = lastName; }

    /**
     * Obtiene la nueva dirección de correo electrónico.
     * @return La dirección de email proporcionada, o null si no se especificó.
     */
    public String getEmail() { return email; }

    /**
     * Establece la nueva dirección de correo electrónico.
     * @param email La nueva dirección de email para la solicitud de actualización.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Obtiene la nueva contraseña en texto plano.
     * @return La contraseña proporcionada, o null si no se especificó un cambio.
     */
    public String getPassword() { return password; }

    /**
     * Establece la nueva contraseña en texto plano.
     * @param password La nueva contraseña para la solicitud de actualización.
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * Obtiene la nueva dirección física del usuario.
     * @return La dirección proporcionada, o null si no se especificó.
     */
    public String getAddress() { return address; }

    /**
     * Establece la nueva dirección física del usuario.
     * @param address La nueva dirección para la solicitud de actualización.
     */
    public void setAddress(String address) { this.address = address; }

    /**
     * Obtiene el nuevo número de teléfono del usuario.
     * @return El número de teléfono proporcionado, o null si no se especificó.
     */
    public String getPhoneNumber() { return phoneNumber; }

    /**
     * Establece el nuevo número de teléfono del usuario.
     * @param phoneNumber El nuevo número de teléfono para la solicitud de actualización.
     */
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}