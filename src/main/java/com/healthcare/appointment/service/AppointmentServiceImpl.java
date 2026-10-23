package com.healthcare.appointment.service;

import com.healthcare.appointment.dto.AppointmentRequest;
import com.healthcare.appointment.dto.AppointmentResponse;
import com.healthcare.appointment.exception.AppointmentNotFoundException;
import com.healthcare.appointment.exception.InvalidAppointmentException;
import com.healthcare.appointment.model.Appointment;
import com.healthcare.appointment.model.AppointmentStatus;
import com.healthcare.appointment.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Production implementation of AppointmentService.
 */
@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private final AppointmentRepository repository;
    private final AppointmentMetricsService metricsService;

    public AppointmentServiceImpl(AppointmentRepository repository, AppointmentMetricsService metricsService) {
        this.repository = repository;
        this.metricsService = metricsService;
    }

    @Override
    public List<AppointmentResponse> getAllAppointments() {
        log.debug("Fetching all appointments from repository");
        return repository.findAll().stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentResponse getAppointmentById(Long id) {
        log.debug("Fetching appointment with ID: {}", id);
        Appointment appointment = repository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
        return AppointmentResponse.fromEntity(appointment);
    }

    @Override
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        log.info("Creating new appointment for fictional patient: {}", request.getPatientName());
        validateAppointmentDate(request.getAppointmentDateTime());

        Appointment appointment = new Appointment();
        appointment.setPatientName(request.getPatientName().trim());
        appointment.setDoctorName(request.getDoctorName().trim());
        appointment.setAppointmentDateTime(request.getAppointmentDateTime());
        appointment.setStatus(request.getStatus() != null ? request.getStatus() : AppointmentStatus.SCHEDULED);
        appointment.setReason(request.getReason() != null ? request.getReason().trim() : "");

        Appointment saved = repository.save(appointment);
        metricsService.incrementCreated();
        log.info("Successfully created appointment with ID: {}", saved.getId());
        return AppointmentResponse.fromEntity(saved);
    }

    @Override
    public AppointmentResponse updateAppointment(Long id, AppointmentRequest request) {
        log.info("Updating appointment with ID: {}", id);
        Appointment existing = repository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));

        validateAppointmentDate(request.getAppointmentDateTime());

        existing.setPatientName(request.getPatientName().trim());
        existing.setDoctorName(request.getDoctorName().trim());
        existing.setAppointmentDateTime(request.getAppointmentDateTime());
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        if (request.getReason() != null) {
            existing.setReason(request.getReason().trim());
        }

        Appointment updated = repository.save(existing);
        metricsService.incrementUpdated();
        log.info("Successfully updated appointment with ID: {}", id);
        return AppointmentResponse.fromEntity(updated);
    }

    @Override
    public void deleteAppointment(Long id) {
        log.info("Deleting appointment with ID: {}", id);
        if (!repository.existsById(id)) {
            throw new AppointmentNotFoundException(id);
        }

        repository.deleteById(id);
        metricsService.incrementCancelled();
        log.info("Successfully deleted appointment with ID: {}", id);
    }

    private void validateAppointmentDate(LocalDateTime appointmentDateTime) {
        if (appointmentDateTime == null) {
            throw new InvalidAppointmentException("Appointment date and time cannot be null");
        }
        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidAppointmentException("Appointment date and time must be in the future");
        }
    }
}
