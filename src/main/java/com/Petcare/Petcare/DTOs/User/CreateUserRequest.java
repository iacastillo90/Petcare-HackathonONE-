package com.Petcare.Petcare.DTOs.User;

import jakarta.validation.constraints.*;

/**
 * DTO para la creación de nuevos usuarios en el sistema Petcare.
 *
 * <p>Este DTO contiene únicamente los campos que el cliente debe proporcionar
 * para registrar un nuevo usuario. Los campos del sistema como {@code id},
 * {@code isActive}, y timestamps se generan automáticamente en el servidor.</p>
 *
 * <p><strong>Campos generados automáticamente:</strong></p>
 * <ul>
 *   <li>{@code id}: Generado por la base de datos</li>
 *   <li>{@code role}: Se asigna basándose en el contexto de registro</li>
 *   <li>{@code isActive}: Se inicializa como {@code true}</li>
 *   <li>{@code createdAt} y {@code updatedAt}: Timestamps automáticos</li>
 *   <li>{@code emailVerifiedAt}: Se establece tras verificación</li>
 * </ul>
 *
 * <p><strong>Validaciones aplicadas:</strong></p>
 * <ul>
 *   <li>Todos los campos obligatorios están marcados con {@code @NotBlank}</li>
 *   <li>Email válido y único en el sistema</li>
 *   <li>Contraseña con longitud mínima de seguridad</li>
 *   <li>Límites de caracteres en todos los campos de texto</li>
 * </ul>
 *
 * <p><strong>Casos de uso principales:</strong></p>
 * <ul>
 *   <li>Registro de nuevos clientes propietarios de mascotas</li>
 *   <li>Registro de cuidadores que ofrecen servicios</li>
 *   <li>Creación de usuarios administrativos</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see com.Petcare.Petcare.Models.User.User
 */
public class CreateUserRequest {

    /**
     * Nombre del usuario.
     *
     * <p>Campo obligatorio usado para personalización de la experiencia
     * y comunicaciones del sistema. Se almacena sin procesar adicional.</p>
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 250, message = "El nombre no puede exceder 250 caracteres")
    private String firstName;

    /**
     * Apellido del usuario.
     *
     * <p>Campo obligatorio que complementa la identificación del usuario.
     * Usado para comunicaciones formales y verificación de identidad.</p>
     */
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 250, message = "El apellido no puede exceder 250 caracteres")
    private String lastName;

    /**
     * Dirección de correo electrónico del usuario.
     *
     * <p>Debe ser única en todo el sistema y funcionará como identificador
     * de autenticación. Se valida formato y unicidad antes del registro.</p>
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 250, message = "El email no puede exceder 250 caracteres")
    private String email;

    /**
     * Contraseña del usuario en texto plano.
     *
     * <p>Se cifrará usando bcrypt antes de almacenarse en la base de datos.
     * Debe cumplir con políticas mínimas de seguridad.</p>
     */
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    /**
     * Dirección física del usuario.
     *
     * <p>Campo obligatorio para servicios a domicilio y verificación
     * de ubicación para cuidadores. Se almacena como texto libre.</p>
     */
    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String address;

    /**
     * Número de teléfono del usuario.
     *
     * <p>Campo obligatorio para comunicaciones de emergencia y coordinación
     * de servicios. Se almacena como texto para soportar formatos internacionales.</p>
     */
    @NotBlank(message = "El número de teléfono es obligatorio")
    @Size(max = 250, message = "El número de teléfono no puede exceder 250 caracteres")
    private String phoneNumber;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío requerido para frameworks de serialización.
     */
    public CreateUserRequest() {
    }

    /**
     * Constructor completo para facilitar testing y creación programática.
     *
     * @param firstName nombre del usuario
     * @param lastName apellido del usuario
     * @param email dirección de correo electrónico única
     * @param password contraseña en texto plano
     * @param address dirección física
     * @param phoneNumber número de teléfono
     */
    public CreateUserRequest(String firstName, String lastName, String email,
                             String password, String address, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    // ========== GETTERS Y SETTERS ==========

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Retorna el nombre completo formateado.
     *
     * @return nombre y apellido concatenados
     */
    public String getFullName() {
        return (firstName + " " + lastName).trim();
    }

    /**
     * Verifica si todos los campos obligatorios están presentes.
     *
     * @return true si todos los campos requeridos tienen valor
     */
    public boolean hasAllRequiredFields() {
        return firstName != null && !firstName.trim().isEmpty() &&
                lastName != null && !lastName.trim().isEmpty() &&
                email != null && !email.trim().isEmpty() &&
                password != null && !password.trim().isEmpty() &&
                address != null && !address.trim().isEmpty() &&
                phoneNumber != null && !phoneNumber.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "CreateUserRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}