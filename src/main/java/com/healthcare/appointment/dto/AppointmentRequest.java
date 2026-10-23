package com.healthcare.appointment.dto;

import com.healthcare.appointment.model.AppointmentStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for creating or updating an appointment.
 */
public class AppointmentRequest {

    @NotBlank(message = "Patient name is required and cannot be blank")
    @Size(min = 2, max = 100, message = "Patient name must be between 2 and 100 characters")
    private String patientName;

    @NotBlank(message = "Doctor name is required and cannot be blank")
    @Size(min = 2, max = 100, message = "Doctor name must be between 2 and 100 characters")
    private String doctorName;

    @NotNull(message = "Appointment date and time is required")
    @Future(message = "Appointment date and time must be in the future")
    private LocalDateTime appointmentDateTime;

    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Size(max = 255, message = "Reason cannot exceed 255 characters")
    private String reason;

    public AppointmentRequest() {
    }

    public AppointmentRequest(String patientName, String doctorName,
                              LocalDateTime appointmentDateTime,
                              AppointmentStatus status, String reason) {
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentDateTime = appointmentDateTime;
        this.status = status != null ? status : AppointmentStatus.SCHEDULED;
        this.reason = reason;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
