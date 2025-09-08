package com.Petcare.Petcare.DTOs.User;

import java.util.List;

import com.Petcare.Petcare.DTOs.Booking.BookingSummaryResponse;
import com.Petcare.Petcare.DTOs.Pet.PetSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MainDashboardDTO {
    private UserProfileDTO userProfile;
    private BookingSummaryResponse nextAppointment;
    private List<PetSummaryResponse> userPets;
//private List<SitterSummaryDTO> recentSitters;
    private DashboardStatsDTO stats;
}