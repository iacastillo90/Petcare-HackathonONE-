package com.Petcare.Petcare.Models;

import com.Petcare.Petcare.DTOs.Pet.CreatePetRequest;
import com.Petcare.Petcare.DTOs.Pet.PetResponse;
import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.Booking.Booking;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad que representa una mascota en la plataforma Petcare.
 *
 * <p>Las mascotas son los activos principales del sistema, asociadas a cuentas familiares
 * y disponibles para servicios de cuidado. Cada mascota pertenece a una cuenta específica
 * y puede ser gestionada por los usuarios asociados a dicha cuenta.</p>
 *
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 * <li>Gestión de información básica: nombre, especie, raza, edad</li>
 * <li>Información física: género, color, características especiales</li>
 * <li>Control de estado: activo/inactivo para disponibilidad de servicios</li>
 * <li>Notas especiales para cuidadores: medicamentos, alergias, comportamiento</li>
 * <li>Relación con reservas históricas y activas</li>
 * <li>Auditoría completa con timestamps de creación y actualización</li>
 * </ul>
 *
 * <p><strong>Casos de uso:</strong></p>
 * <ul>
 * <li>Registro de nuevas mascotas por parte de clientes</li>
 * <li>Búsqueda y filtrado de mascotas por características</li>
 * <li>Gestión de disponibilidad para servicios de cuidado</li>
 * <li>Consulta de información detallada para cuidadores</li>
 * <li>Análisis de historial de servicios y reservas</li>
 * </ul>
 *
 * <p><strong>Patrones implementados:</strong></p>
 * <ul>
 * <li>Entity Listener para auditoría automática</li>
 * <li>Validación Bean Validation con mensajes en español</li>
 * <li>Relación Many-to-One con Account mediante Foreign Key</li>
 * <li>Relación One-to-Many bidireccional con Booking</li>
 * <li>Soft delete mediante campo isActive</li>
 * <li>Métodos de utilidad para gestión de relaciones</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.1
 * @since 1.0
 * @see Account
 * @see Booking
 * @see PetResponse
 * @see CreatePetRequest
 */
@Entity
@Table(name = "pets",
        indexes = {
                @Index(name = "idx_pet_account_id", columnList = "account_id"),
                @Index(name = "idx_pet_name", columnList = "name"),
                @Index(name = "idx_pet_species", columnList = "species"),
                @Index(name = "idx_pet_active", columnList = "is_active"),
                @Index(name = "idx_pet_account_active", columnList = "account_id, is_active"),
                @Index(name = "idx_pet_breed", columnList = "breed"),
                @Index(name = "idx_pet_species_breed", columnList = "species, breed")
        })
@EntityListeners(AuditingEntityListener.class)
public class Pet {

    /**
     * Identificador único de la mascota.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * La cuenta a la que pertenece esta mascota.
     * <p>Esta es la relación propietaria que establece el vínculo entre
     * la mascota y la familia o usuario responsable.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pet_account"))
    @NotNull(message = "La cuenta es obligatoria")
    private Account account;

    /**
     * Reservas asociadas a esta mascota.
     * <p>Incluye tanto reservas históricas como futuras. La relación
     * es lazy para optimizar el rendimiento.</p>
     */
    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Booking> bookings = new HashSet<>();

    /**
     * Nombre de la mascota.
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String name;

    /**
     * Especie de la mascota (ej: "Perro", "Gato", "Ave").
     */
    @Column(length = 50)
    @Size(max = 50, message = "La especie no puede exceder los 50 caracteres")
    private String species;

    /**
     * Raza específica de la mascota.
     */
    @Column(length = 100)
    @Size(max = 100, message = "La raza no puede exceder los 100 caracteres")
    private String breed;

    /**
     * Edad de la mascota en años.
     */
    @PositiveOrZero(message = "La edad no puede ser negativa")
    private Integer age;

    /**
     * Peso de la mascota en kilogramos.
     */
    @PositiveOrZero(message = "El peso no puede ser negativo")
    @Column(precision = 5, scale = 2)
    private BigDecimal weight;

    /**
     * Género de la mascota.
     */
    @Column(length = 20)
    @Size(max = 20, message = "El género no puede exceder los 20 caracteres")
    private String gender;

    /**
     * Color principal de la mascota.
     */
    @Column(length = 50)
    @Size(max = 50, message = "El color no puede exceder los 50 caracteres")
    private String color;

    /**
     * Descripción física detallada de la mascota.
     */
    @Column(name = "physical_description", columnDefinition = "TEXT")
    @Size(max = 1000, message = "La descripción física no puede exceder los 1000 caracteres")
    private String physicalDescription;

    /**
     * Información sobre medicamentos actuales.
     */
    @Column(columnDefinition = "TEXT")
    @Size(max = 1000, message = "La información de medicamentos no puede exceder los 1000 caracteres")
    private String medications;

    /**
     * Información sobre alergias conocidas.
     */
    @Column(columnDefinition = "TEXT")
    @Size(max = 1000, message = "La información de alergias no puede exceder los 1000 caracteres")
    private String allergies;


    /**
     * Información sobre vacunas aplicadas a la mascota.
     */
    @Column(columnDefinition = "TEXT")
    @Size(max = 2000, message = "La información de vacunas no puede exceder los 2000 caracteres")
    private String vaccinations;




    /**
     * Notas especiales para cuidadores.
     */
    @Column(name = "special_notes", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Las notas especiales no pueden exceder los 2000 caracteres")
    private String specialNotes;

    /**
     * Indica si la mascota está activa en el sistema.
     * <p>Las mascotas inactivas no aparecen en búsquedas ni están disponibles
     * para servicios, pero mantienen su información para historial.</p>
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * Fecha y hora de creación del registro.
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última actualización del registro.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Pet() {}

    /**
     * Constructor para creación básica de mascota.
     *
     * @param account La cuenta propietaria
     * @param name Nombre de la mascota
     * @param species Especie de la mascota
     * @param breed Raza de la mascota
     * @param age Edad de la mascota
     */
    public Pet(Account account, String name, String species, String breed, Integer age) {
        this.account = account;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.age = age;
        this.bookings = new HashSet<>();
    }

    /**
     * Constructor completo con información detallada.
     */
    public Pet(Account account, String name, String species, String breed, Integer age,
               BigDecimal weight, String gender, String color, String physicalDescription,
               String medications, String allergies, String vaccinations, String specialNotes) {
        this.account = account;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.age = age;
        this.weight = weight;
        this.gender = gender;
        this.color = color;
        this.physicalDescription = physicalDescription;
        this.medications = medications;
        this.allergies = allergies;
        this.vaccinations = vaccinations;
        this.specialNotes = specialNotes;
        this.bookings = new HashSet<>();
    }

    // ========== GETTERS Y SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Set<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(Set<Booking> bookings) {
        this.bookings = bookings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getPhysicalDescription() {
        return physicalDescription;
    }

    public void setPhysicalDescription(String physicalDescription) {
        this.physicalDescription = physicalDescription;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getSpecialNotes() {
        return specialNotes;
    }

    public void setSpecialNotes(String specialNotes) {
        this.specialNotes = specialNotes;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }



    public String getVaccinations() {
        return vaccinations;
    }

    public void setVaccinations(String vaccinations) {
        this.vaccinations = vaccinations;
    }



    // ========== MÉTODOS DE UTILIDAD PARA RELACIONES BIDIRECCIONALES ==========

    /**
     * Agrega una reserva a esta mascota manteniendo la consistencia bidireccional.
     *
     * @param booking la reserva a agregar
     */
    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.setPet(this);
    }

    /**
     * Remueve una reserva de esta mascota manteniendo la consistencia bidireccional.
     *
     * @param booking la reserva a remover
     */
    public void removeBooking(Booking booking) {
        bookings.remove(booking);
        booking.setPet(null);
    }

    // ========== MÉTODOS DE UTILIDAD EXTENDIDOS ==========

    /**
     * Verifica si la mascota pertenece a la cuenta especificada.
     *
     * @param accountId ID de la cuenta a verificar
     * @return true si la mascota pertenece a la cuenta
     */
    public boolean belongsToAccount(Long accountId) {
        return account != null && Objects.equals(account.getId(), accountId);
    }

    /**
     * Verifica si la mascota está disponible para servicios.
     * <p>Una mascota está disponible si está activa y su cuenta también lo está.</p>
     *
     * @return true si está disponible para servicios
     */
    public boolean isAvailableForServices() {
        return isActive && account != null && account.isActive();
    }

    /**
     * Verifica si la mascota tiene reservas activas.
     * <p>Considera activas las reservas en estado PENDING, CONFIRMED o IN_PROGRESS.</p>
     *
     * @return true si tiene reservas activas
     */
    public boolean hasActiveBookings() {
        if (bookings == null || bookings.isEmpty()) {
            return false;
        }
        return bookings.stream()
                .anyMatch(booking -> !booking.isFinalState());
    }

    /**
     * Obtiene el número total de reservas completadas.
     *
     * @return cantidad de reservas completadas
     */
    public long getCompletedBookingsCount() {
        if (bookings == null) {
            return 0;
        }
        return bookings.stream()
                .filter(booking -> booking.getStatus() != null)
                .filter(booking -> booking.getStatus().name().equals("COMPLETED"))
                .count();
    }

    /**
     * Verifica si la mascota puede ser marcada como inactiva.
     * <p>Una mascota solo puede ser desactivada si no tiene reservas activas.</p>
     *
     * @return true si puede ser desactivada
     */
    public boolean canBeDeactivated() {
        return !hasActiveBookings();
    }

    /**
     * Obtiene información básica de la mascota para logging.
     *
     * @return cadena con información básica
     */
    public String getBasicInfo() {
        return String.format("%s (%s, %s)", name, species, breed);
    }

    /**
     * Verifica si la mascota tiene información médica registrada.
     *
     * @return true si tiene medicamentos o alergias registradas
     */
    public boolean hasMedicalInfo() {
        return (medications != null && !medications.trim().isEmpty()) ||
                (allergies != null && !allergies.trim().isEmpty());
    }

    /**
     * Obtiene todas las notas relevantes para cuidadores.
     * <p>Combina información médica y notas especiales en un solo texto.</p>
     *
     * @return notas consolidadas para cuidadores
     */
    public String getCareTakerNotes() {
        StringBuilder notes = new StringBuilder();

        if (medications != null && !medications.trim().isEmpty()) {
            notes.append("Medicamentos: ").append(medications).append("\n");
        }

        if (allergies != null && !allergies.trim().isEmpty()) {
            notes.append("Alergias: ").append(allergies).append("\n");
        }

        if (specialNotes != null && !specialNotes.trim().isEmpty()) {
            notes.append("Notas especiales: ").append(specialNotes);
        }

        return notes.toString().trim();
    }

    /**
     * Verifica si la mascota tiene información de vacunas registrada.
     *
     * @return true si tiene al menos una vacuna registrada
     */
    public boolean hasVaccinationInfo() {
        return vaccinations != null && !vaccinations.trim().isEmpty();
    }


    // ========== EQUALS, HASHCODE Y TOSTRING ==========

    /**
     * Implementación de equals basada en ID único.
     * <p>Para entidades sin ID persistido, se compara por campos de negocio únicos.</p>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pet)) return false;
        Pet pet = (Pet) o;

        // Si ambos tienen ID, comparar por ID
        if (this.id != null && pet.id != null) {
            return Objects.equals(this.id, pet.id);
        }

        // Si no tienen ID, comparar por campos de negocio únicos
        return Objects.equals(getName(), pet.getName()) &&
                Objects.equals(getAccount(), pet.getAccount()) &&
                Objects.equals(getSpecies(), pet.getSpecies()) &&
                Objects.equals(getBreed(), pet.getBreed());
    }

    /**
     * Implementación de hashCode consistente con equals.
     */
    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(getName(), getAccount(), getSpecies(), getBreed());
    }

    /**
     * Representación de cadena optimizada para logging.
     * <p>Evita lazy loading exceptions al no acceder a relaciones complejas.</p>
     */
    @Override
    public String toString() {
        return String.format("Pet{id=%d, name='%s', species='%s', breed='%s', " +
                        "accountId=%d, isActive=%s, bookingsCount=%d, createdAt=%s}",
                id, name, species, breed,
                (account != null ? account.getId() : null),
                isActive,
                (bookings != null ? bookings.size() : 0),
                createdAt);
    }
}