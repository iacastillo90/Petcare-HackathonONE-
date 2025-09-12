package com.Petcare.Petcare.DTOs.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO para la actualización de usuarios existentes.
 * <p>
 * A diferencia de {@link CreateUserRequest}, los campos en este DTO son opcionales,
 * permitiendo al cliente enviar solo los datos que desea modificar.
 * Las validaciones de tamaño y formato se mantienen.
 * </p>
 */
public class UpdateUserRequest {

    @Size(max = 250, message = "El nombre no puede exceder 250 caracteres")
    private String firstName;

    @Size(max = 250, message = "El apellido no puede exceder 250 caracteres")
    private String lastName;

    @Email(message = "El formato del email no es válido")
    @Size(max = 250, message = "El email no puede exceder 250 caracteres")
    private String email;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String address;

    @Size(max = 20, message = "El número de teléfono no puede exceder 20 caracteres")
    private String phoneNumber;

    public UpdateUserRequest(String firstName, String lastName, String email, String password, String address, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
