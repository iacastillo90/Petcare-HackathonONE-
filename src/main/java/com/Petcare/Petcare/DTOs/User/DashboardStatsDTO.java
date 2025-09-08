package com.Petcare.Petcare.DTOs.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDTO {
    private int activePets;
    private String activePetsChange;
    private int scheduledAppointments;
    private String scheduledAppointmentsChange;
    private String vaccinesUpToDate;
    private String vaccinesChange;
    private int pendingReminders;
    private String pendingRemindersChange;
}