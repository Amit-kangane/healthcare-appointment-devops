package com.healthcare.appointment.repository;

import com.healthcare.appointment.model.Appointment;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Appointment data persistence.
 */
public interface AppointmentRepository {

    List<Appointment> findAll();

    Optional<Appointment> findById(Long id);

    Appointment save(Appointment appointment);

    boolean deleteById(Long id);

    boolean existsById(Long id);

    long count();
}
