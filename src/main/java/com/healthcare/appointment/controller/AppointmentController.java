package com.healthcare.appointment.controller;

import com.healthcare.appointment.dto.AppointmentRequest;
import com.healthcare.appointment.dto.AppointmentResponse;
import com.healthcare.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST Controller exposing Healthcare Appointment Management APIs.
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Retrieve all appointments.
     * GET /api/appointments
     */
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        log.info("REST request to get all appointments");
        List<AppointmentResponse> list = appointmentService.getAllAppointments();
        return ResponseEntity.ok(list);
    }

    /**
     * Retrieve a specific appointment by ID.
     * GET /api/appointments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long id) {
        log.info("REST request to get appointment ID: {}", id);
        AppointmentResponse response = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Book/Create a new appointment.
     * POST /api/appointments
     */
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        log.info("REST request to create appointment for patient: {}", request.getPatientName());
        AppointmentResponse created = appointmentService.createAppointment(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    /**
     * Update an existing appointment by ID.
     * PUT /api/appointments/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest request) {
        log.info("REST request to update appointment ID: {}", id);
        AppointmentResponse updated = appointmentService.updateAppointment(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Cancel/Delete an appointment by ID.
     * DELETE /api/appointments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        log.info("REST request to delete appointment ID: {}", id);
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
