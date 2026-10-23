package com.healthcare.appointment.dto;

import com.healthcare.appointment.model.Appointment;
import com.healthcare.appointment.model.AppointmentStatus;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for returning appointment details to clients.
 */
public class AppointmentResponse {

    private Long id;
    private String patientName;
    private String doctorName;
    private LocalDateTime appointmentDateTime;
    private AppointmentStatus status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AppointmentResponse() {
    }

    public AppointmentResponse(Long id, String patientName, String doctorName,
                               LocalDateTime appointmentDateTime, AppointmentStatus status,
                               String reason, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentDateTime = appointmentDateTime;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AppointmentResponse fromEntity(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatientName(),
                appointment.getDoctorName(),
                appointment.getAppointmentDateTime(),
                appointment.getStatus(),
                appointment.getReason(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
