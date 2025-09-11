package com.Petcare.Petcare.DTOs.User;

import java.util.List;

import com.Petcare.Petcare.DTOs.Booking.BookingSummaryResponse;
import com.Petcare.Petcare.DTOs.Pet.PetSummaryResponse;
import com.Petcare.Petcare.DTOs.Sitter.SitterProfileSummary;
import com.Petcare.Petcare.Models.SitterProfile;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MainDashboardDTO {
    private UserProfileDTO userProfile;
    private BookingSummaryResponse nextAppointment;
    private List<PetSummaryResponse> userPets;
    private List<SitterProfileSummary> recentSitters;
    private DashboardStatsDTO stats;
}