package com.Petcare.Petcare.DTOs.Pet;

import com.Petcare.Petcare.Models.Pet;

import java.util.Map;

/**
 * DTO para las respuestas de estadísticas de mascotas.
 *
 * <p>Proporciona información agregada y métricas sobre las mascotas registradas
 * en el sistema, útil para dashboards administrativos y reportes de gestión.
 * Incluye conteos generales, distribuciones por categorías y métricas de actividad.</p>
 *
 * <p><strong>Métricas incluidas:</strong></p>
 * <ul>
 * <li>Conteos totales: todas las mascotas, activas, inactivas</li>
 * <li>Distribuciones: por especie, por género, por rango de edad</li>
 * <li>Métricas temporales: registros por período</li>
 * <li>Estadísticas de cuentas: promedio de mascotas por cuenta</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see Pet
 */
public class PetStatsResponse {

    /**
     * Número total de mascotas registradas.
     */
    private long totalPets;

    /**
     * Número de mascotas activas.
     */
    private long activePets;

    /**
     * Número de mascotas inactivas.
     */
    private long inactivePets;

    /**
     * Distribución de mascotas por especie.
     * <p>Mapa donde la clave es el nombre de la especie y el valor es el conteo.</p>
     */
    private Map<String, Long> petsBySpecies;

    /**
     * Distribución de mascotas por género.
     * <p>Mapa donde la clave es el género y el valor es el conteo.</p>
     */
    private Map<String, Long> petsByGender;

    /**
     * Distribución de mascotas por rango de edad.
     * <p>Rangos: "0-1", "2-5", "6-10", "11-15", "16+"</p>
     */
    private Map<String, Long> petsByAgeRange;

    /**
     * Número total de cuentas con mascotas.
     */
    private long accountsWithPets;

    /**
     * Promedio de mascotas por cuenta.
     */
    private double averagePetsPerAccount;

    /**
     * Número de mascotas registradas en los últimos 30 días.
     */
    private long petsRegisteredLast30Days;

    /**
     * Número de mascotas registradas en los últimos 7 días.
     */
    private long petsRegisteredLast7Days;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor por defecto.
     */
    public PetStatsResponse() {}

    /**
     * Constructor con estadísticas básicas.
     *
     * @param totalPets Número total de mascotas
     * @param activePets Número de mascotas activas
     * @param inactivePets Número de mascotas inactivas
     */
    public PetStatsResponse(long totalPets, long activePets, long inactivePets) {
        this.totalPets = totalPets;
        this.activePets = activePets;
        this.inactivePets = inactivePets;
    }

    // ========== GETTERS Y SETTERS ==========

    public long getTotalPets() {
        return totalPets;
    }

    public void setTotalPets(long totalPets) {
        this.totalPets = totalPets;
    }

    public long getActivePets() {
        return activePets;
    }

    public void setActivePets(long activePets) {
        this.activePets = activePets;
    }

    public long getInactivePets() {
        return inactivePets;
    }

    public void setInactivePets(long inactivePets) {
        this.inactivePets = inactivePets;
    }

    public Map<String, Long> getPetsBySpecies() {
        return petsBySpecies;
    }

    public void setPetsBySpecies(Map<String, Long> petsBySpecies) {
        this.petsBySpecies = petsBySpecies;
    }

    public Map<String, Long> getPetsByGender() {
        return petsByGender;
    }

    public void setPetsByGender(Map<String, Long> petsByGender) {
        this.petsByGender = petsByGender;
    }

    public Map<String, Long> getPetsByAgeRange() {
        return petsByAgeRange;
    }

    public void setPetsByAgeRange(Map<String, Long> petsByAgeRange) {
        this.petsByAgeRange = petsByAgeRange;
    }

    public long getAccountsWithPets() {
        return accountsWithPets;
    }

    public void setAccountsWithPets(long accountsWithPets) {
        this.accountsWithPets = accountsWithPets;
    }

    public double getAveragePetsPerAccount() {
        return averagePetsPerAccount;
    }

    public void setAveragePetsPerAccount(double averagePetsPerAccount) {
        this.averagePetsPerAccount = averagePetsPerAccount;
    }

    public long getPetsRegisteredLast30Days() {
        return petsRegisteredLast30Days;
    }

    public void setPetsRegisteredLast30Days(long petsRegisteredLast30Days) {
        this.petsRegisteredLast30Days = petsRegisteredLast30Days;
    }

    public long getPetsRegisteredLast7Days() {
        return petsRegisteredLast7Days;
    }

    public void setPetsRegisteredLast7Days(long petsRegisteredLast7Days) {
        this.petsRegisteredLast7Days = petsRegisteredLast7Days;
    }
}