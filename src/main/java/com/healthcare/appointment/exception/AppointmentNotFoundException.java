package com.healthcare.appointment.exception;

/**
 * Exception thrown when an appointment with a given ID cannot be found.
 */
public class AppointmentNotFoundException extends RuntimeException {

    public AppointmentNotFoundException(Long id) {
        super("Appointment not found with ID: " + id);
    }

    public AppointmentNotFoundException(String message) {
        super(message);
    }
}
