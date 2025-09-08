package com.Petcare.Petcare.DTOs.Sitter;

import java.math.BigDecimal;

public class SitterProfileDTO {

    private Long id;
    private Long userId;
    private String bio;
    private BigDecimal hourlyRate;
    private Integer servicingRadius;
    private String profileImageUrl;
    private boolean verified;
    private boolean availableForBookings;

    public SitterProfileDTO() {
    }

    public SitterProfileDTO(Long id, Long userId, String bio, BigDecimal hourlyRate,
                            Integer servicingRadius, String profileImageUrl,
                            boolean verified, boolean availableForBookings) {
        this.id = id;
        this.userId = userId;
        this.bio = bio;
        this.hourlyRate = hourlyRate;
        this.servicingRadius = servicingRadius;
        this.profileImageUrl = profileImageUrl;
        this.verified = verified;
        this.availableForBookings = availableForBookings;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Integer getServicingRadius() {
        return servicingRadius;
    }

    public void setServicingRadius(Integer servicingRadius) {
        this.servicingRadius = servicingRadius;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isAvailableForBookings() {
        return availableForBookings;
    }

    public void setAvailableForBookings(boolean availableForBookings) {
        this.availableForBookings = availableForBookings;
    }
}
