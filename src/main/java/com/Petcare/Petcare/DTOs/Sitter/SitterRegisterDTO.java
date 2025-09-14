package com.Petcare.Petcare.DTOs.Sitter;

import com.Petcare.Petcare.Controllers.UserController;
import com.Petcare.Petcare.DTOs.User.CreateUserRequest;
import com.Petcare.Petcare.Models.User.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para encapsular los datos de registro de un nuevo usuario Cuidador (Sitter).
 *
 * <p>Esta clase se utiliza como cuerpo de la solicitud (Request Body) en el endpoint de
 * registro de cuidadores. Contiene toda la información básica necesaria para crear una
 * nueva entidad {@link User} con el rol de 'SITTER', aplicando las validaciones
 * de negocio directamente a través de anotaciones para garantizar la integridad de los datos
 * desde la capa de controlador.</p>
 *
 * <p>Es similar a {@link CreateUserRequest}, pero está específicamente diseñada para el
 * flujo de registro de cuidadores, lo que permite diferenciar lógicas de negocio
 * o validaciones en el futuro si fuera necesario.</p>
 *
 * @see UserController#registerUserSitter(CreateUserRequest)
 * @see User
 * @author Equipo Petcare
 * @version 1.0
 * @since 1.0
 */
public class SitterRegisterDTO {

    /**
     * El nombre de pila del cuidador. Es un campo obligatorio.
     */
    @NotBlank(message = "First name is required")
    private String firstName;

    /**
     * El apellido del cuidador. Es un campo obligatorio.
     */
    @NotBlank(message = "Last name is required")
    private String lastName;

    /**
     * La dirección de correo electrónico del cuidador.
     * <p>Debe ser un correo válido y único en el sistema, ya que se utilizará como
     * identificador para el inicio de sesión.</p>
     */
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    /**
     * La contraseña para la nueva cuenta.
     * <p>Se recibe en texto plano y debe ser cifrada en la capa de servicio antes
     * de ser persistida. Debe cumplir con una longitud mínima por seguridad.</p>
     */
    @Size(min = 6, message = "Password must be at least 6 characters")
    @NotBlank(message = "Password is required")
    private String password;

    /**
     * La dirección física del cuidador.
     * <p>Este campo es opcional durante el registro inicial pero crucial para que
     * los clientes puedan encontrar cuidadores por ubicación.</p>
     */
    private String address;

    /**
     * El número de teléfono de contacto del cuidador.
     * <p>Es opcional en el registro pero importante para la comunicación
     * una vez que se confirma un servicio.</p>
     */
    private String phoneNumber;

    /**
     * Constructor por defecto.
     * <p>Requerido por frameworks como Jackson para la deserialización del JSON
     * de la petición a un objeto Java.</p>
     */
    public SitterRegisterDTO() {
    }

    /**
     * Constructor completo para crear una instancia del DTO con todos los datos.
     * <p>Útil para la creación de objetos en tests unitarios o en la lógica de negocio.</p>
     *
     * @param firstName El nombre del cuidador.
     * @param lastName El apellido del cuidador.
     * @param email La dirección de correo electrónico.
     * @param password La contraseña de la cuenta.
     * @param address La dirección física.
     * @param phoneNumber El número de teléfono.
     */
    public SitterRegisterDTO(String firstName, String lastName, String email,
                             String password, String address, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    // ========== Getters y Setters ==========

    /**
     * Obtiene el nombre del cuidador.
     * @return El nombre de pila del cuidador.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Establece el nombre del cuidador.
     * @param firstName El nuevo nombre de pila.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Obtiene el apellido del cuidador.
     * @return El apellido del cuidador.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Establece el apellido del cuidador.
     * @param lastName El nuevo apellido.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Obtiene el email del cuidador.
     * @return La dirección de correo electrónico.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece el email del cuidador.
     * @param email La nueva dirección de correo electrónico.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtiene la contraseña del cuidador.
     * @return La contraseña en texto plano.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Establece la contraseña del cuidador.
     * @param password La nueva contraseña.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Obtiene la dirección del cuidador.
     * @return La dirección física.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Establece la dirección del cuidador.
     * @param address La nueva dirección física.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Obtiene el número de teléfono del cuidador.
     * @return El número de teléfono.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Establece el número de teléfono del cuidador.
     * @param phoneNumber El nuevo número de teléfono.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}