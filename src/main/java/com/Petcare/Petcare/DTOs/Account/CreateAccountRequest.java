package com.Petcare.Petcare.DTOs.Account;

import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.User.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para solicitar la creación de una nueva cuenta de cliente en el sistema.
 * <p>
 * Este objeto encapsula los datos mínimos y esenciales que un cliente debe proporcionar
 * para registrar una nueva cuenta. Campos como el número de cuenta o el saldo inicial
 * son gestionados internamente por el sistema y no se incluyen en esta solicitud.
 * </p>
 *
 * <p><strong>Proceso de Negocio:</strong></p>
 * <ul>
 * <li>Un usuario ya registrado (propietario) utiliza este DTO para crear su cuenta principal.</li>
 * <li>El nombre de la cuenta permite al usuario identificarla fácilmente (ej. "Familia Pérez").</li>
 * <li>La moneda define la base para todas las transacciones futuras asociadas a esta cuenta.</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see Account
 */
@Schema(description = "DTO para solicitar la creación de una nueva cuenta de cliente, conteniendo los datos esenciales proporcionados por el usuario.")
public class CreateAccountRequest {

    /**
     * El identificador único del usuario que será el propietario principal de la cuenta.
     * <p>
     * El sistema validará que este ID corresponde a un usuario existente y activo.
     * </p>
     */
    @NotNull(message = "El ID del usuario propietario es obligatorio.")
    @Schema(description = "ID único del usuario que será el propietario de la cuenta.", example = "1", required = true)
    private Long ownerUserId;

    /**
     * El usuario propietario de la cuenta.
     * <p>
     * El sistema validará que este usuario corresponde a un usuario existente y activo.
     * </p>
     */
    @NotNull(message = "El usuario propietario es obligatorio")
    private User ownerUser;

    /**
     * Un nombre descriptivo y amigable para la cuenta, definido por el usuario.
     * <p>
     * Facilita la identificación de la cuenta en la interfaz, especialmente si un usuario
     * tiene acceso a múltiples cuentas.
     * </p>
     */
    @NotBlank(message = "El nombre de la cuenta es obligatorio.")
    @Size(min = 3, max = 100, message = "El nombre de la cuenta debe tener entre 3 y 100 caracteres.")
    @Schema(description = "Nombre descriptivo y amigable para la cuenta (ej. 'Familia Pérez').", example = "Cuenta Principal de los Castillo", required = true)
    private String accountName;

    /**
     * El número de la cuenta.
     * <p>
     * El sistema generará un número único para la cuenta.
     * </p>
     */
    @NotBlank(message = "El número de cuenta es obligatorio")
    @Size(max = 50, message = "El número de cuenta no puede exceder 50 caracteres")
    private String accountNumber;


    /**
     * Constructor vacío requerido para frameworks de serialización como Jackson.
     */
    public CreateAccountRequest() {
    }

    /**
     * Constructor que crea una instancia de CreateAccountRequest a partir de un objeto Account.
     *
     * @param account
     */
    public CreateAccountRequest(Account account){
        ownerUserId = account.getId();
        ownerUser = account.getOwnerUser();
        accountName = account.getAccountName();
        accountNumber = account.getAccountNumber();

    }

    // ========== GETTERS Y SETTERS ==========

    /**
     * Obtiene el ID del usuario propietario.
     * @return el identificador del usuario.
     */
    public Long getOwnerUserId() {
        return ownerUserId;
    }

    /**
     * Establece el ID del usuario propietario.
     * @param ownerUserId el nuevo identificador de usuario.
     */
    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    /**
     * Obtiene el usuario propietario.
     * @return el usuario propietario.
     */
    public User getOwnerUser() {
        return ownerUser;
    }

    /**
     * Establece el usuario propietario.
     * @param ownerUser el nuevo usuario propietario.
     */
    public void setOwnerUser(User ownerUser) {
        this.ownerUser = ownerUser;
    }

    /**
     * Obtiene el nombre de la cuenta.
     * @return el nombre descriptivo de la cuenta.
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Establece el nombre de la cuenta.
     * @param accountName el nuevo nombre descriptivo.
     */
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * Obtiene el número de la cuenta.
     * @return el número de la cuenta.
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * Establece el número de la cuenta.
     * @param accountNumber el nuevo número de la cuenta.
     */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}